package com.folio.reader.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.repository.Article
import com.folio.reader.data.repository.ArticleRepository
import com.folio.reader.data.repository.ArticleSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    data object Prompt : SearchUiState
    data object Loading : SearchUiState
    data class Results(val articles: List<Article>) : SearchUiState
    data object Error : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private var pool: List<Article> = emptyList()
    private var loaded = false

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Prompt)
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                pool = repository.loadRecent()
                loaded = true
                applyFilter()
            } catch (e: Exception) {
                if (_query.value.isNotBlank()) _state.value = SearchUiState.Error
            }
        }
    }

    fun onQuery(query: String) {
        _query.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val q = _query.value
        _state.value = when {
            q.isBlank() -> SearchUiState.Prompt
            !loaded -> SearchUiState.Loading
            else -> SearchUiState.Results(ArticleSearch.filter(pool, q))
        }
    }

    fun openArticle(article: Article) {
        val results = (_state.value as? SearchUiState.Results)?.articles ?: return
        repository.setReaderContext(results)
        if (!article.isRead) viewModelScope.launch { repository.setRead(article.id, true) }
    }
}
