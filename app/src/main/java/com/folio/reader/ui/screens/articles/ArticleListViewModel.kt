package com.folio.reader.ui.screens.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.repository.Article
import com.folio.reader.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleListUiState(
    val articles: List<Article> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
)

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private var streamId: String = ""
    private var excludeRead: Boolean = false
    private var continuation: String? = null
    private var started = false

    private val _state = MutableStateFlow(ArticleListUiState())
    val state: StateFlow<ArticleListUiState> = _state.asStateFlow()

    fun start(streamId: String, excludeRead: Boolean) {
        if (started && this.streamId == streamId && this.excludeRead == excludeRead) return
        started = true
        this.streamId = streamId
        this.excludeRead = excludeRead
        loadFirstPage(refresh = false)
    }

    fun refresh() = loadFirstPage(refresh = true)

    private fun loadFirstPage(refresh: Boolean) {
        _state.update { it.copy(loading = !refresh, refreshing = refresh, error = null) }
        viewModelScope.launch {
            try {
                val page = repository.loadStream(streamId, excludeRead, continuation = null)
                continuation = page.continuation
                _state.update {
                    it.copy(
                        articles = page.articles,
                        loading = false,
                        refreshing = false,
                        endReached = page.continuation == null,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, refreshing = false, error = e.message ?: "Could not load articles.")
                }
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || s.endReached || continuation == null) return
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            try {
                val page = repository.loadStream(streamId, excludeRead, continuation = continuation)
                continuation = page.continuation
                _state.update {
                    it.copy(
                        articles = it.articles + page.articles,
                        loadingMore = false,
                        endReached = page.continuation == null,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }

    fun toggleRead(article: Article) {
        val target = !article.isRead
        update(article.id) { it.copy(isRead = target) }
        viewModelScope.launch {
            if (!repository.setRead(article.id, target)) update(article.id) { it.copy(isRead = !target) }
        }
    }

    fun toggleStar(article: Article) {
        val target = !article.isStarred
        update(article.id) { it.copy(isStarred = target) }
        viewModelScope.launch {
            if (!repository.setStarred(article.id, target)) update(article.id) { it.copy(isStarred = !target) }
        }
    }

    /**
     * Called when an article is opened: hand the visible ordered list to the
     * reader (so it can swipe between siblings) and mark this one read.
     */
    fun openArticle(article: Article) {
        repository.setReaderContext(_state.value.articles)
        if (article.isRead) return
        update(article.id) { it.copy(isRead = true) }
        viewModelScope.launch { repository.setRead(article.id, true) }
    }

    private fun update(id: String, transform: (Article) -> Article) {
        _state.update { s -> s.copy(articles = s.articles.map { if (it.id == id) transform(it) else it }) }
    }
}
