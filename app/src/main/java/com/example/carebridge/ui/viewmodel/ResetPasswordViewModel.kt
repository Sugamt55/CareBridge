package com.example.carebridge.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carebridge.data.repository.ResetPasswordRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Reset Password screen using Firebase.
 */
class ResetPasswordViewModel(private val repository: ResetPasswordRepository = ResetPasswordRepository()) : ViewModel() {

    private val _resetState = MutableLiveData<ResetResult>()
    val resetState: LiveData<ResetResult> = _resetState

    /**
     * Sends a password reset email via Firebase.
     */
    fun sendResetEmail(email: String) {
        val trimmedEmail = email.trim()
        
        if (trimmedEmail.isEmpty()) {
            _resetState.value = ResetResult.Error("Please enter an email address.")
            return
        }

        _resetState.value = ResetResult.Loading
        
        viewModelScope.launch {
            val result = repository.resetPassword(trimmedEmail)
            result.fold(
                onSuccess = {
                    _resetState.value = ResetResult.Success
                },
                onFailure = { exception ->
                    _resetState.value = ResetResult.Error(exception.message ?: "Reset failed")
                }
            )
        }
    }

    fun resetState() {
        _resetState.value = ResetResult.Idle
    }
}

sealed class ResetResult {
    object Idle : ResetResult()
    object Loading : ResetResult()
    object Success : ResetResult()
    data class Error(val message: String) : ResetResult()
}
