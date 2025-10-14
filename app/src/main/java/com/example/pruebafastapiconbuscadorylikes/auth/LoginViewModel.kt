package com.example.pruebafastapiconbuscadorylikes.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _usuario = MutableStateFlow("")
    val usuario: StateFlow<String> = _usuario

    private val _contrasena = MutableStateFlow("")
    val contrasena: StateFlow<String> = _contrasena

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    fun onUsuarioChange(value: String) {
        _usuario.value = value
    }

    fun onContrasenaChange(value: String) {
        _contrasena.value = value
    }

    fun iniciarSesion(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_usuario.value.isBlank() || _contrasena.value.isBlank()) {
            _error.value = "Todos los campos son obligatorios"
            onError(_error.value)
        } else {
            AuthManager.loginUser(
                _usuario.value,
                _contrasena.value,
                onSuccess = { uid -> onSuccess(uid) },
                onError = { msg ->
                    _error.value = msg
                    onError(msg)
                }
            )
        }
    }
}
