package com.folio.reader.ui.screens.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.SettingsStore
import com.folio.reader.data.repository.SubscriptionRepository
import com.folio.reader.data.repository.SubscriptionRepository.OpResult
import com.folio.reader.data.repository.SubscriptionTree
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FeedsUiState {
    data object Loading : FeedsUiState
    data class Error(val message: String) : FeedsUiState
    data class Content(val tree: SubscriptionTree) : FeedsUiState
}

@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    private val settings: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow<FeedsUiState>(FeedsUiState.Loading)
    val state: StateFlow<FeedsUiState> = _state.asStateFlow()

    /** Names of categories the user has folded in the Feeds screen (persisted). */
    val collapsedCategories: StateFlow<Set<String>> =
        settings.collapsedCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleCategoryCollapsed(name: String) {
        viewModelScope.launch { settings.toggleCategoryCollapsed(name) }
    }

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    // Transient one-shot feedback for management actions (shown as a snackbar).
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init { load() }

    /** Existing category names, for move/add pickers; empty unless content is loaded. */
    fun categoryNames(): List<String> =
        (_state.value as? FeedsUiState.Content)?.tree?.categories?.map { it.label } ?: emptyList()

    /** Full (re)load with a loading spinner. Used on first open and from the error retry. */
    fun load() {
        _state.value = FeedsUiState.Loading
        viewModelScope.launch { fetch(silent = false) }
    }

    /** Silent refresh that keeps the current content visible (pull-to-refresh, resume). */
    fun refresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            fetch(silent = true)
            _refreshing.value = false
        }
    }

    private suspend fun fetch(silent: Boolean) {
        try {
            _state.value = FeedsUiState.Content(repository.getSubscriptionTree())
        } catch (e: Exception) {
            // On a silent refresh, keep whatever content we already have.
            if (!silent || _state.value !is FeedsUiState.Content) {
                _state.value = FeedsUiState.Error(e.message ?: "Could not load subscriptions.")
            }
        }
    }

    fun consumeMessage() { _message.value = null }

    fun addFeed(url: String, category: String?) =
        runAction("Feed added") { repository.addFeed(url, category) }

    fun renameFeed(streamId: String, newTitle: String) =
        runAction("Feed renamed") { repository.renameFeed(streamId, newTitle) }

    fun moveFeed(streamId: String, fromCategory: String?, toCategory: String?) =
        runAction("Feed moved") { repository.moveFeed(streamId, fromCategory, toCategory) }

    fun unsubscribe(streamId: String, title: String) =
        runAction("Unsubscribed from $title") { repository.unsubscribeFeed(streamId) }

    fun renameCategory(oldName: String, newName: String) =
        runAction("Folder renamed") { repository.renameCategory(oldName, newName) }

    fun deleteCategory(name: String) =
        runAction("Folder deleted") { repository.deleteCategory(name) }

    private fun runAction(successMessage: String, block: suspend () -> OpResult) {
        viewModelScope.launch {
            when (val result = block()) {
                is OpResult.Success -> {
                    _message.value = successMessage
                    refresh()
                }
                is OpResult.Error -> _message.value = result.message
            }
        }
    }
}
