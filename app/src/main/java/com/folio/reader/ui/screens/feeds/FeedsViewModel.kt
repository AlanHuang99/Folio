package com.folio.reader.ui.screens.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.repository.SubscriptionRepository
import com.folio.reader.data.repository.SubscriptionTree
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FeedsUiState {
    data object Loading : FeedsUiState
    data class Error(val message: String) : FeedsUiState
    data class Content(val tree: SubscriptionTree) : FeedsUiState
}

@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FeedsUiState>(FeedsUiState.Loading)
    val state: StateFlow<FeedsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = FeedsUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                FeedsUiState.Content(repository.getSubscriptionTree())
            } catch (e: Exception) {
                FeedsUiState.Error(e.message ?: "Could not load subscriptions.")
            }
        }
    }
}
