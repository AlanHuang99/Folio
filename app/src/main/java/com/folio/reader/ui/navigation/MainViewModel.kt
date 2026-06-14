package com.folio.reader.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.repository.AuthRepository
import com.folio.reader.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    val userName: StateFlow<String?> =
        authRepository.activeAccountFlow
            .map { it?.userName }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _unreadTotal = MutableStateFlow(0)
    val unreadTotal: StateFlow<Int> = _unreadTotal.asStateFlow()

    init {
        refreshUnread()
    }

    fun refreshUnread() {
        viewModelScope.launch { _unreadTotal.value = subscriptionRepository.getTotalUnread() }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.logout() }
    }
}
