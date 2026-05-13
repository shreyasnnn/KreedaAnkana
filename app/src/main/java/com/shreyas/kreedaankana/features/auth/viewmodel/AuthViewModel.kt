package com.shreyas.kreedaankana.features.auth.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun handleGoogleSignIn(data: Intent?) {
        _authState.value = AuthState(isLoading = true)

        AuthManager.handleSignInResult(
            data = data,
            onSuccess = {
                _authState.value = AuthState(isSuccess = true)
            },
            onError = { errorMessage ->
                _authState.value = AuthState(
                    isLoading = false,
                    error = errorMessage
                )
            }
        )
    }

    fun resetState() {
        _authState.value = AuthState()
    }
}