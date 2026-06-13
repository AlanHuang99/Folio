package com.folio.reader.data.repository

import com.folio.reader.data.api.GReaderApi
import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.data.api.ServerSession
import com.folio.reader.data.db.PendingAction
import com.folio.reader.data.db.PendingActionDao
import com.folio.reader.data.sync.SyncScheduler
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val api: GReaderApi,
    private val session: ServerSession,
    private val pendingDao: PendingActionDao,
    private val syncScheduler: SyncScheduler,
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

    suspend fun fetchArticle(id: String): Article? {
        val article = runCatching {
            api.itemContents(id).items.orEmpty().firstOrNull()?.let(ArticleMapper::toArticle)
        }.getOrNull()
        if (article != null) cache[id] = article
        return article
    }

    // --- Read / star: queued so changes survive offline, flushed by SyncWorker ---

    suspend fun setRead(itemId: String, read: Boolean): Boolean =
        enqueue(itemId, GReaderEndpoints.TAG_READ, read)

    suspend fun setStarred(itemId: String, starred: Boolean): Boolean =
        enqueue(itemId, GReaderEndpoints.TAG_STARRED, starred)

    private suspend fun enqueue(itemId: String, tag: String, add: Boolean): Boolean = try {
        pendingDao.deleteByItemAndTag(itemId, tag)
        pendingDao.insert(
            PendingAction(itemId = itemId, tag = tag, add = add, createdAt = System.currentTimeMillis()),
        )
        syncScheduler.requestSync()
        true
    } catch (e: Exception) {
        false
    }

    /** Replay all queued edit-tag actions; returns true if the queue is now empty. */
    suspend fun flush(): Boolean {
        val pending = pendingDao.getAll()
        var allSucceeded = true
        for (action in pending) {
            val ok = sendEditTag(
                action.itemId,
                add = if (action.add) action.tag else null,
                remove = if (action.add) null else action.tag,
            )
            if (ok) pendingDao.deleteById(action.id) else allSucceeded = false
        }
        return allSucceeded
    }

    private suspend fun sendEditTag(itemId: String, add: String?, remove: String?): Boolean = try {
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
