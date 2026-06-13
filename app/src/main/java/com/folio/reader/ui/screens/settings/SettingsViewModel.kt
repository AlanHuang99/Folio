package com.folio.reader.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.SettingsStore
import com.folio.reader.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settings: SettingsStore,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val serverUrl: StateFlow<String?> =
        settings.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val userName: StateFlow<String?> =
        settings.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun signOut() {
        viewModelScope.launch { authRepository.logout() }
    }
}
