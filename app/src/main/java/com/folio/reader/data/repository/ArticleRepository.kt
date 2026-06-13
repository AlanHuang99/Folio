package com.folio.reader.data.repository

import com.folio.reader.data.api.GReaderApi
import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.data.api.ServerSession
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val api: GReaderApi,
    private val session: ServerSession,
) {
    data class Page(val articles: List<Article>, val continuation: String?)

    private val cache = ConcurrentHashMap<String, Article>()

    @Volatile
    private var streamOrder: List<String> = emptyList()

    suspend fun loadStream(
        streamId: String,
        excludeRead: Boolean,
        continuation: String?,
        count: Int = 40,
    ): Page {
        val response = api.streamContents(
            streamId = GReaderEndpoints.encodeStreamId(streamId),
            count = count,
            continuation = continuation,
            excludeTarget = if (excludeRead) GReaderEndpoints.TAG_READ else null,
        )
        val articles = response.items.orEmpty().map(ArticleMapper::toArticle)
        articles.forEach { cache[it.id] = it }
        return Page(articles, response.continuation)
    }

    /** Record the visible ordered list so the reader can page through siblings. */
    fun setReaderContext(articles: List<Article>) {
        articles.forEach { cache[it.id] = it }
        streamOrder = articles.map { it.id }
    }

    fun readerOrder(): List<String> = streamOrder

    fun cached(id: String): Article? = cache[id]

    fun updateCache(article: Article) {
        cache[article.id] = article
    }

    /** Fetch a single article by id (used when the cache is cold, e.g. after process death). */
    suspend fun fetchArticle(id: String): Article? {
        val article = runCatching {
            api.itemContents(id).items.orEmpty().firstOrNull()?.let(ArticleMapper::toArticle)
        }.getOrNull()
        if (article != null) cache[id] = article
        return article
    }

    suspend fun setRead(itemId: String, read: Boolean): Boolean = editTag(
        itemId,
        add = if (read) GReaderEndpoints.TAG_READ else null,
        remove = if (read) null else GReaderEndpoints.TAG_READ,
    )

    suspend fun setStarred(itemId: String, starred: Boolean): Boolean = editTag(
        itemId,
        add = if (starred) GReaderEndpoints.TAG_STARRED else null,
        remove = if (starred) null else GReaderEndpoints.TAG_STARRED,
    )

    private suspend fun editTag(itemId: String, add: String?, remove: String?): Boolean = try {
        api.editTag(itemId, add, remove, writeToken())
        true
    } catch (e: Exception) {
        // The write token may have expired — refetch once and retry.
        session.writeToken = null
        runCatching { api.editTag(itemId, add, remove, writeToken()) }.isSuccess
    }

    private suspend fun writeToken(): String =
        session.writeToken ?: api.writeToken().string().trim().also { session.writeToken = it }
}
