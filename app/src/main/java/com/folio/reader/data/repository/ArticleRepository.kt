package com.folio.reader.data.repository

import com.folio.reader.data.api.GReaderApi
import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.data.api.ServerSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val api: GReaderApi,
    private val session: ServerSession,
) {
    data class Page(val articles: List<Article>, val continuation: String?)

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
        return Page(response.items.orEmpty().map(ArticleMapper::toArticle), response.continuation)
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
