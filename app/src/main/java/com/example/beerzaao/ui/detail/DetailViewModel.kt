package com.example.beerzaao.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beerzaao.BeerZaaoApp
import com.example.beerzaao.data.remote.dto.FundEstimateDto
import com.example.beerzaao.data.remote.dto.FundGradeDto
import com.example.beerzaao.data.remote.dto.StockQuoteItem
import com.example.beerzaao.util.FundPerformance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HoldingItem(
    val stockCode: String,
    val stockName: String,
    val holdingRate: String,
    val changeRate: String,
    val quote: StockQuoteItem? = null,
    val type: String = "stock"
)

data class DetailUiState(
    val fundCode: String = "",
    val fundName: String = "",
    val estimate: FundEstimateDto? = null,
    val isEstimateLoading: Boolean = false,
    val estimateError: String? = null,
    val weightedEstimateRate: Double? = null,
    val holdings: List<HoldingItem> = emptyList(),
    val isHoldingsLoading: Boolean = false,
    val holdingsError: String? = null,
    val performance: FundPerformance? = null,
    val isPerformanceLoading: Boolean = false,
    val grade: FundGradeDto? = null,
    val isGradeLoading: Boolean = false
)

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BeerZaaoApp
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadFundDetail(fundCode: String) {
        _uiState.value = _uiState.value.copy(fundCode = fundCode)

        viewModelScope.launch {
            val fund = app.localFundRepository.getFundByCode(fundCode)
            _uiState.value = _uiState.value.copy(fundName = fund?.fundName ?: "")
        }

        refreshEstimate()
        loadHoldings()
        loadPerformance()
        loadGrade()
    }

    fun refreshEstimate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEstimateLoading = true, estimateError = null)
            val result = app.fundRepository.getFundEstimate(_uiState.value.fundCode)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    estimate = result.getOrNull(),
                    isEstimateLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isEstimateLoading = false,
                    estimateError = result.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun loadHoldings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHoldingsLoading = true, holdingsError = null)
            val result = app.fundRepository.getFundHoldings(_uiState.value.fundCode)
            if (result.isSuccess) {
                val holdingsData = result.getOrNull() ?: emptyList()
                val holdings = holdingsData.map { map ->
                    HoldingItem(
                        stockCode = map["stockCode"] ?: "",
                        stockName = map["stockName"] ?: "",
                        holdingRate = map["holdingRate"] ?: "",
                        changeRate = map["changeRate"] ?: "",
                        type = map["type"] ?: "stock"
                    )
                }
                _uiState.value = _uiState.value.copy(
                    holdings = holdings,
                    isHoldingsLoading = false
                )

                loadStockQuotes(holdings)
            } else {
                _uiState.value = _uiState.value.copy(
                    isHoldingsLoading = false,
                    holdingsError = result.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun loadPerformance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPerformanceLoading = true)
            val result = app.fundRepository.getFundPerformance(_uiState.value.fundCode)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    performance = result.getOrNull(),
                    isPerformanceLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isPerformanceLoading = false)
            }
        }
    }

    private fun loadStockQuotes(holdings: List<HoldingItem>) {
        viewModelScope.launch {
            val stocks = holdings.filter { it.type == "stock" }
            if (stocks.isEmpty()) return@launch

            val stockCodes = stocks.map { it.stockCode }

            val result = app.fundRepository.getStockQuotes(stockCodes)
            if (result.isSuccess) {
                val quotes = result.getOrNull()?.data?.diff ?: emptyList()
                val quoteMap = quotes.associateBy { it.stockCode }

                var weightedSum = 0.0
                var totalRate = 0.0
                val updatedHoldings = holdings.map { holding ->
                    if (holding.type == "stock") {
                        val quote = quoteMap[holding.stockCode]
                        val rate = holding.holdingRate.toDoubleOrNull() ?: 0.0
                        val change = quote?.changeRate ?: 0.0
                        weightedSum += rate * change
                        totalRate += rate
                        holding.copy(quote = quote)
                    } else {
                        holding
                    }
                }
                val weightedRate = if (totalRate > 0) weightedSum / totalRate else 0.0

                _uiState.value = _uiState.value.copy(
                    holdings = updatedHoldings,
                    weightedEstimateRate = weightedRate
                )
            }
        }
    }

    private fun loadGrade() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGradeLoading = true)
            val result = app.fundRepository.getFundGrade(_uiState.value.fundCode)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    grade = result.getOrNull(),
                    isGradeLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isGradeLoading = false)
            }
        }
    }
}
