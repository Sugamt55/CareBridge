package com.example.carebridge.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carebridge.data.repository.SignUpRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.launch

class SignUpViewModel(private val repository: SignUpRepository = SignUpRepository()) : ViewModel() {

    private val _signUpState = MutableLiveData<SignUpResult>()
    val signUpState: LiveData<SignUpResult> = _signUpState

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        address: String
    ) {
        if (email.isBlank() || password.isBlank() || firstName.isBlank()) {
            _signUpState.value = SignUpResult.Error("Please fill in all required fields.")
            return
        }

        _signUpState.value = SignUpResult.Loading
        
        viewModelScope.launch {
            val result = repository.registerUser(
                email, password, firstName, lastName, phone, address
            )
            result.fold(
                onSuccess = {
                    _signUpState.value = SignUpResult.Success
                },
                onFailure = { exception ->
                    val errorMessage = when (exception) {
                        is FirebaseAuthUserCollisionException -> "This email is already registered."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                        is FirebaseNetworkException -> "Network error. Please check your connection."
                        else -> exception.localizedMessage ?: "Registration failed. Check Firebase console configuration."
                    }
                    _signUpState.value = SignUpResult.Error(errorMessage)
                }
            )
        }
    }

    fun resetState() {
        _signUpState.value = SignUpResult.Idle
    }
}

sealed class SignUpResult {
    object Idle : SignUpResult()
    object Loading : SignUpResult()
    object Success : SignUpResult()
    data class Error(val message: String) : SignUpResult()
}
