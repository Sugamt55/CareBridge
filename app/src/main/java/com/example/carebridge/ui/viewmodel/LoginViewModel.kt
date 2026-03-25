package com.example.carebridge.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carebridge.data.model.UserModel
import com.example.carebridge.data.repository.LoginRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login screen.
 * Orchestrates the data flow between the UI and Repository.
 */
class LoginViewModel(private val repository: LoginRepository = LoginRepository()) : ViewModel() {

    // Internal mutable state for login status
    private val _loginState = MutableLiveData<LoginResult>()
    // Public immutable LiveData for the View to observe
    val loginState: LiveData<LoginResult> = _loginState

    /**
     * Attempts to log in the user with provided credentials.
     */
    fun login(username: String, password: String) {
        _loginState.value = LoginResult.Loading

        viewModelScope.launch {
            val result = repository.login(username, password)
            result.fold(
                onSuccess = { user ->
                    _loginState.value = LoginResult.Success(user)
                },
                onFailure = { exception ->
                    _loginState.value = LoginResult.Error(exception.message ?: "Unknown error occurred")
                }
            )
        }
    }

    /**
     * Resets the login state (e.g., after showing an error message).
     */
    fun resetState() {
        _loginState.value = LoginResult.Idle
    }
}

/**
 * Sealed class representing various states of the login process.
 */
sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class Success(val user: UserModel) : LoginResult()
    data class Error(val message: String) : LoginResult()
}
