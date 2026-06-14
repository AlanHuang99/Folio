package com.folio.reader.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.Account
import com.folio.reader.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val accounts: StateFlow<List<Account>> =
        authRepository.accountsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeAccount: StateFlow<Account?> =
        authRepository.activeAccountFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun switchAccount(id: String) {
        viewModelScope.launch { authRepository.switchAccount(id) }
    }

    fun removeAccount(id: String) {
        viewModelScope.launch { authRepository.removeAccount(id) }
    }

    /** Sign out of the active account (falls back to another account if one exists). */
    fun signOut() {
        viewModelScope.launch { authRepository.logout() }
    }
}
