package com.example.carebridge.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carebridge.data.FoodPredictionResponse
import com.example.carebridge.data.RetrofitInstance
import com.example.carebridge.data.local.TFLiteClassifier
import com.example.carebridge.data.model.FoodDatabaseItem
import com.example.carebridge.data.model.Macronutrients
import com.example.carebridge.data.model.Micronutrients
import com.example.carebridge.data.repository.FoodDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class ScanUiState {
    object Idle : ScanUiState()
    object Loading : ScanUiState()
    data class Success(val response: FoodPredictionResponse, val detailedData: FoodDatabaseItem? = null) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val localRepository = FoodDatabaseRepository(application)
    private val classifier = TFLiteClassifier(application)

    fun isModelLoaded(): Boolean = classifier.isModelLoaded()

    fun analyzeFood(imageFile: File) {
        _uiState.value = ScanUiState.Loading

        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(imageFile.absolutePath)
                }
                
                val localResult = if (bitmap != null) classifier.classify(bitmap) else null
                
                if (localResult != null && localResult.confidence > 0.5f) {
                    val detailedData = localRepository.getFoodByName(localResult.label)
                    
                    if (detailedData != null) {
                        _uiState.value = ScanUiState.Success(
                            FoodPredictionResponse(
                                foodName = detailedData.foodName,
                                servingSize = detailedData.servingSize,
                                calories = detailedData.calories,
                                protein = "${detailedData.macronutrients.proteinG}g",
                                carbs = "${detailedData.macronutrients.carbsG}g",
                                fat = "${detailedData.macronutrients.fatG}g",
                                phLevel = 7.0,
                                isAlkaline = detailedData.phClassification.lowercase().contains("alkaline")
                            ),
                            detailedData
                        )
                        return@launch
                    }
                }
                
                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val response = RetrofitInstance.api.predictFood(body)
                val detailedDataFromResponse = localRepository.getFoodByName(response.foodName)
                
                _uiState.value = ScanUiState.Success(response, detailedDataFromResponse)

            } catch (e: Exception) {
                Log.e("MainViewModel", "Analysis error: ${e.message}", e)
                _uiState.value = ScanUiState.Error(e.message ?: "Failed to analyze food")
            }
        }
    }

    /**
     * Helper for manual integration testing. 
     * Simulates a successful scan result integrated with local database metadata.
     */
    fun simulateSuccess() {
        _uiState.value = ScanUiState.Loading
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Simulate a bit of processing time
            val mockResponse = FoodPredictionResponse(
                foodName = "Apple",
                servingSize = "100g",
                calories = 52,
                protein = "0.3g",
                carbs = "14g",
                fat = "0.2g",
                phLevel = 3.5,
                isAlkaline = false
            )
            val mockDetailedData = localRepository.getFoodByName("Apple") ?: FoodDatabaseItem(
                foodName = "Apple",
                servingSize = "1 medium (182g)",
                calories = 95,
                macronutrients = Macronutrients(0.5, 25.0, 0.3, 4.4, 19.0),
                micronutrients = Micronutrients(0.1, 8.4, 1.0, 0.1, 195.0),
                phClassification = "Acidic",
                phReason = "High in malic acid."
            )
            _uiState.value = ScanUiState.Success(mockResponse, mockDetailedData)
        }
    }

    fun resetState() {
        _uiState.value = ScanUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}
