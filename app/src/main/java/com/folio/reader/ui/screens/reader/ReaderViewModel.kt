package com.folio.reader.ui.screens.reader

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.repository.Article
import com.folio.reader.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ArticleRepository,
) : ViewModel() {

    private val openedId: String = savedStateHandle.get<String>("id").orEmpty()

    /** The ordered ids the reader can swipe through (the list the user opened from). */
    val ids: List<String> = repository.readerOrder().ifEmpty { listOf(openedId) }
    val initialIndex: Int = ids.indexOf(openedId).coerceAtLeast(0)

    private val overrides = mutableStateMapOf<String, Article>()
    private val fetched = mutableStateMapOf<String, Article>()

    fun article(id: String): Article? = overrides[id] ?: fetched[id] ?: repository.cached(id)

    fun ensureLoaded(id: String) {
        if (article(id) != null) return
        viewModelScope.launch { repository.fetchArticle(id)?.let { fetched[id] = it } }
    }

    fun markReadOnSettle(id: String) {
        val current = article(id) ?: return
        if (current.isRead) return
        applyLocal(current.copy(isRead = true))
        viewModelScope.launch { repository.setRead(id, true) }
    }

    fun toggleStar(id: String) {
        val current = article(id) ?: return
        val target = !current.isStarred
        applyLocal(current.copy(isStarred = target))
        viewModelScope.launch {
            if (!repository.setStarred(id, target)) applyLocal(current.copy(isStarred = !target))
        }
    }

    fun toggleRead(id: String) {
        val current = article(id) ?: return
        val target = !current.isRead
        applyLocal(current.copy(isRead = target))
        viewModelScope.launch {
            if (!repository.setRead(id, target)) applyLocal(current.copy(isRead = !target))
        }
    }

    private fun applyLocal(article: Article) {
        overrides[article.id] = article
        repository.updateCache(article)
    }
}
