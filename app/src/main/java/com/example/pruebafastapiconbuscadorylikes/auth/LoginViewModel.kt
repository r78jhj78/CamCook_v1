package com.example.pruebafastapiconbuscadorylikes.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun login(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val email = _uiState.value.email
        val password = _uiState.value.password

        AuthManager.loginUser(
            email,
            password,
            onSuccess = { uid -> onSuccess(uid) },
            onError = { msg -> _uiState.update { it.copy(error = msg) }; onError(msg) }
        )
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val error: String = ""
)
