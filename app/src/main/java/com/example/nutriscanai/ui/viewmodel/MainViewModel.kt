package com.example.nutriscanai.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscanai.data.FoodPredictionResponse
import com.example.nutriscanai.data.RetrofitInstance
import com.example.nutriscanai.data.local.TFLiteClassifier
import com.example.nutriscanai.data.model.FoodDatabaseItem
import com.example.nutriscanai.data.repository.FoodDatabaseRepository
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

    fun setErrorMessage(message: String) {
        _uiState.value = ScanUiState.Error(message)
    }

    fun analyzeFood(imageFile: File) {
        _uiState.value = ScanUiState.Loading
        Log.d("MainViewModel", "analyzeFood started for: ${imageFile.name}")

        viewModelScope.launch {
            try {
                // Try Local Analysis First
                val bitmap = withContext(Dispatchers.IO) {
                    if (imageFile.exists()) {
                        BitmapFactory.decodeFile(imageFile.absolutePath)
                    } else {
                        Log.e("MainViewModel", "File does not exist: ${imageFile.absolutePath}")
                        null
                    }
                }
                
                val localResult = if (bitmap != null) classifier.classify(bitmap) else null
                
                if (localResult != null) {
                    Log.d("MainViewModel", "Local Result: ${localResult.label} with confidence ${localResult.confidence}")
                }

                if (localResult != null && localResult.confidence > 0.6f) {
                    Log.d("MainViewModel", "Local confidence high (>0.6). Emitting Success.")
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
                    } else {
                        Log.w("MainViewModel", "Local label '${localResult.label}' not found in database. Falling back to API.")
                    }
                }
                
                // Fallback to API
                Log.d("MainViewModel", "Confidence low or metadata missing. Attempting API call...")
                
                if (!imageFile.exists()) {
                    _uiState.value = ScanUiState.Error("Image file missing.")
                    return@launch
                }

                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                Log.d("MainViewModel", "Executing Retrofit call to Backend...")
                val response = RetrofitInstance.api.predictFood(body)
                Log.d("MainViewModel", "API SUCCESS: Server returned '${response.foodName}'")
                
                val detailedDataFromResponse = localRepository.getFoodByName(response.foodName)
                _uiState.value = ScanUiState.Success(response, detailedDataFromResponse)

            } catch (e: java.net.ConnectException) {
                Log.e("MainViewModel", "CONNECTION FAILED: ${e.message}")
                _uiState.value = ScanUiState.Error("Could not reach server. Verify IP and Wi-Fi.")
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("MainViewModel", "TIMEOUT: ${e.message}")
                _uiState.value = ScanUiState.Error("Server timed out. Check connection.")
            } catch (e: Exception) {
                Log.e("MainViewModel", "GENERAL ERROR: ${e.message}", e)
                _uiState.value = ScanUiState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun simulateSuccess() {
        _uiState.value = ScanUiState.Loading
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Simulating connection test...")
                val health = RetrofitInstance.api.healthCheck()
                Log.d("MainViewModel", "Health check successful: $health")
                
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
                val mockDetailedData = localRepository.getFoodByName("Apple")
                _uiState.value = ScanUiState.Success(mockResponse, mockDetailedData)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Simulated connection failed", e)
                _uiState.value = ScanUiState.Error("Connection test failed. See Logcat.")
            }
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
