package com.example.carebridge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carebridge.data.FoodPredictionResponse
import com.example.carebridge.data.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class ScanUiState {
    object Idle : ScanUiState()
    object Loading : ScanUiState()
    data class Success(val response: FoodPredictionResponse) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun analyzeFood(imageFile: File) {
        _uiState.value = ScanUiState.Loading

        viewModelScope.launch {
            try {
                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val response = RetrofitInstance.api.predictFood(body)
                _uiState.value = ScanUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error(e.message ?: "Failed to analyze food")
            }
        }
    }

    fun resetState() {
        _uiState.value = ScanUiState.Idle
    }
}
