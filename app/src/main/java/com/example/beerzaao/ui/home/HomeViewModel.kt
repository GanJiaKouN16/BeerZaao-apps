package com.example.beerzaao.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beerzaao.BeerZaaoApp
import com.example.beerzaao.data.local.FundEntity
import com.example.beerzaao.data.remote.dto.FundEstimateDto
import com.example.beerzaao.ui.update.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FundWithEstimate(
    val fund: FundEntity,
    val estimate: FundEstimateDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class HomeUiState(
    val funds: List<FundWithEstimate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val updateState: UpdateState = UpdateState.Idle
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BeerZaaoApp
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadFunds()
    }

    fun loadFunds() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            app.localFundRepository.getAllFunds().collect { funds ->
                val fundsWithEstimate = funds.map { fund ->
                    FundWithEstimate(fund = fund)
                }
                _uiState.value = _uiState.value.copy(
                    funds = fundsWithEstimate,
                    isLoading = false
                )
                // 加载每个基金的估值
                refreshEstimates()
            }
        }
    }

    fun refreshEstimates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val updatedFunds = _uiState.value.funds.map { fundWithEstimate ->
                fundWithEstimate.copy(isLoading = true)
            }
            _uiState.value = _uiState.value.copy(funds = updatedFunds)

            val refreshedFunds = _uiState.value.funds.map { fundWithEstimate ->
                try {
                    val result = app.fundRepository.getFundEstimate(fundWithEstimate.fund.fundCode)
                    if (result.isSuccess) {
                        fundWithEstimate.copy(
                            estimate = result.getOrNull(),
                            isLoading = false,
                            error = null
                        )
                    } else {
                        fundWithEstimate.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                } catch (e: Exception) {
                    fundWithEstimate.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
            _uiState.value = _uiState.value.copy(
                funds = refreshedFunds,
                isRefreshing = false
            )
        }
    }

    fun deleteFund(code: String) {
        viewModelScope.launch {
            val result = app.localFundRepository.deleteFund(code)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun moveFund(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val funds = _uiState.value.funds.toMutableList()
            val item = funds.removeAt(fromIndex)
            funds.add(toIndex, item)

            _uiState.value = _uiState.value.copy(funds = funds)

            val entities = funds.map { it.fund }
            app.localFundRepository.updateSortOrder(entities)
        }
    }

    fun checkUpdate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(updateState = UpdateState.Checking)
            val state = app.updateManager.checkForUpdate()
            _uiState.value = _uiState.value.copy(updateState = state)
        }
    }

    fun downloadAndInstall() {
        viewModelScope.launch {
            val info = when (val s = _uiState.value.updateState) {
                is UpdateState.Available -> s.info
                else -> return@launch
            }

            _uiState.value = _uiState.value.copy(
                updateState = UpdateState.Downloading(0f)
            )

            try {
                val apkFile = downloadFull(info.apkUrl)

                _uiState.value = _uiState.value.copy(
                    updateState = UpdateState.ReadyToInstall(apkFile)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    updateState = UpdateState.Error("下载失败: ${e.message}")
                )
            }
        }
    }

    private suspend fun downloadFull(apkUrl: String): java.io.File {
        return app.updateManager.downloadFullApk(apkUrl) { progress ->
            _uiState.value = _uiState.value.copy(
                updateState = UpdateState.Downloading(progress)
            )
        }.getOrThrow().also { file ->
            app.updateManager.saveAsBase(file)
        }
    }

    fun dismissUpdateDialog() {
        _uiState.value = _uiState.value.copy(updateState = UpdateState.Idle)
    }

    fun installApk() {
        val state = _uiState.value.updateState
        if (state is UpdateState.ReadyToInstall) {
            app.updateManager.installApk(state.apkFile)
        }
    }
}