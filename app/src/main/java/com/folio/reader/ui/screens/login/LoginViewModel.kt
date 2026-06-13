package com.folio.reader.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.repository.AuthRepository
import com.folio.reader.data.repository.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onServerUrl(v: String) = _state.update { it.copy(serverUrl = v, error = null) }
    fun onUsername(v: String) = _state.update { it.copy(username = v, error = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, error = null) }

    fun login() {
        val s = _state.value
        if (s.loading) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(s.serverUrl, s.username, s.password)) {
                is LoginResult.Error -> _state.update { it.copy(loading = false, error = result.message) }
                LoginResult.Success -> Unit // isLoggedIn flips; RootViewModel swaps the screen.
            }
        }
    }
}
