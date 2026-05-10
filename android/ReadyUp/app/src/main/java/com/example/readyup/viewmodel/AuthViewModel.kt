package com.example.readyup.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readyup.SessionManager
import com.example.readyup.data.model.LoginRequest
import com.example.readyup.data.model.RegisterRequest
import com.example.readyup.data.remote.Api
import com.example.readyup.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean = false,
    val userId: Int = -1,
    val username: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val session = SessionManager(app)
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        if (session.isLoggedIn()) {
            ApiClient.accessToken = session.accessToken
            _state.value = AuthState(
                isLoggedIn = true,
                userId = session.userId,
                username = session.username ?: "",
                email = session.email ?: ""
            )
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val tokens = Api.service.login(LoginRequest(username, password))
                ApiClient.accessToken = tokens.access
                session.accessToken = tokens.access
                session.refreshToken = tokens.refresh
                val me = Api.service.me()
                session.userId = me.id
                session.username = me.username
                session.email = me.email
                _state.value = AuthState(isLoggedIn = true, userId = me.id, username = me.username, email = me.email)
            } catch (e: Exception) {
                val msg = if (e.message?.contains("401") == true || e.message?.contains("credentials") == true)
                    "Credenciales incorrectas" else "Error de conexión"
                _state.value = _state.value.copy(isLoading = false, error = msg)
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                Api.service.register(RegisterRequest(username, email, password))
                login(username, password)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("409") == true -> "Usuario o email ya existe"
                    e.message?.contains("400") == true -> "Completa todos los campos"
                    else -> "Error de conexión"
                }
                _state.value = _state.value.copy(isLoading = false, error = msg)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun logout() {
        session.clear()
        ApiClient.accessToken = null
        _state.value = AuthState()
    }
}
