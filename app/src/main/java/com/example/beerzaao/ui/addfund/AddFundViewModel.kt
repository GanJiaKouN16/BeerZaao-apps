package com.example.beerzaao.ui.addfund

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beerzaao.BeerZaaoApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddFundUiState(
    val fundCode: String = "",
    val fundName: String? = null,
    val isValidating: Boolean = false,
    val isAdding: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isDuplicate: Boolean = false,
    val hasValidated: Boolean = false
)

class AddFundViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BeerZaaoApp
    private val _uiState = MutableStateFlow(AddFundUiState())
    val uiState: StateFlow<AddFundUiState> = _uiState.asStateFlow()

    fun updateFundCode(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                fundCode = code,
                fundName = null,
                error = null,
                success = false,
                isDuplicate = false,
                hasValidated = false
            )
        }
    }

    fun validateFundCode() {
        val code = _uiState.value.fundCode
        if (code.length != 6) {
            _uiState.value = _uiState.value.copy(error = "请输入6位基金代码")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isValidating = true, error = null)

            val exists = app.localFundRepository.isFundExists(code)
            if (exists) {
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    isDuplicate = true,
                    error = "基金已在自选列表中"
                )
                return@launch
            }

            val estimateResult = app.fundRepository.getFundEstimate(code)
            var name = estimateResult.getOrNull()?.name
            if (name == null) {
                val nameResult = app.fundRepository.getFundName(code)
                name = nameResult.getOrNull()
            }
            _uiState.value = _uiState.value.copy(
                isValidating = false,
                fundName = name,
                error = null,
                hasValidated = true
            )
        }
    }

    fun addFund() {
        val code = _uiState.value.fundCode
        val name = _uiState.value.fundName ?: code

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAdding = true, error = null)
            val result = app.localFundRepository.addFund(code, name)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isAdding = false,
                    success = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isAdding = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AddFundUiState()
    }
}
