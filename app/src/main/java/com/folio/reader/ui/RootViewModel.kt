package com.folio.reader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthState { Loading, LoggedOut, LoggedIn }

@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    private val restored = MutableStateFlow(false)

    init {
        // Restore the in-memory session (base URL + token) before reporting
        // LoggedIn, so the first authenticated screen can make API calls.
        viewModelScope.launch {
            authRepository.restoreSession()
            restored.value = true
        }
    }

    val authState: StateFlow<AuthState> =
        combine(restored, authRepository.isLoggedIn) { isRestored, loggedIn ->
            when {
                !isRestored -> AuthState.Loading
                loggedIn -> AuthState.LoggedIn
                else -> AuthState.LoggedOut
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)
}
