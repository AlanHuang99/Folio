package com.folio.reader.data.repository

import com.folio.reader.data.api.GReaderApi
import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.data.api.ServerSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val api: GReaderApi,
    private val session: ServerSession,
) {
    /** Outcome of a subscription/category management operation. */
    sealed interface OpResult {
        data object Success : OpResult
        data class Error(val message: String) : OpResult
    }

    /** Fetch subscriptions + unread counts and fold them into a category→feed tree. */
    suspend fun getSubscriptionTree(): SubscriptionTree {
        val subscriptions = api.subscriptions().subscriptions.orEmpty()
        val counts = api.unreadCounts().unreadcounts.orEmpty()
        return SubscriptionMapper.build(subscriptions, counts)
    }

    /** Total unread across all feeds (for the navigation badge); 0 on failure. */
    suspend fun getTotalUnread(): Int = runCatching { getSubscriptionTree().totalUnread }.getOrDefault(0)

    /** Subscribe to a feed by URL, optionally placing it in [category]. */
    suspend fun addFeed(input: String, category: String?): OpResult {
        val url = GReaderEndpoints.normalizeFeedInput(input)
        if (url.isEmpty()) return OpResult.Error("Enter a feed URL.")
        return try {
            val resp = withToken { api.quickAddSubscription(url, it) }
            if (resp.numResults < 1 || resp.streamId.isNullOrBlank()) {
                OpResult.Error(resp.error?.ifBlank { null } ?: "No feed found at that URL.")
            } else {
                if (!category.isNullOrBlank()) {
                    // Best-effort categorization; the feed is already subscribed.
                    runCatching {
                        withToken {
                            api.editSubscription(
                                "edit", resp.streamId, null,
                                GReaderEndpoints.labelStreamId(category), null, it,
                            )
                        }
                    }
                }
                OpResult.Success
            }
        } catch (e: Exception) {
            OpResult.Error(e.message ?: "Couldn't add the feed.")
        }
    }

    /** Rename a feed. */
    suspend fun renameFeed(streamId: String, newTitle: String): OpResult {
        if (newTitle.isBlank()) return OpResult.Error("Enter a name.")
        return runWrite { api.editSubscription("edit", streamId, newTitle.trim(), null, null, it) }
    }

    /** Move a feed between categories. Pass null to add to / remove from none. */
    suspend fun moveFeed(streamId: String, fromCategory: String?, toCategory: String?): OpResult =
        runWrite {
            api.editSubscription(
                "edit", streamId, null,
                toCategory?.takeIf { c -> c.isNotBlank() }?.let(GReaderEndpoints::labelStreamId),
                fromCategory?.takeIf { c -> c.isNotBlank() }?.let(GReaderEndpoints::labelStreamId),
                it,
            )
        }

    /** Unsubscribe (delete) a feed. */
    suspend fun unsubscribeFeed(streamId: String): OpResult =
        runWrite { api.editSubscription("unsubscribe", streamId, null, null, null, it) }

    /** Rename a category (folder). */
    suspend fun renameCategory(oldName: String, newName: String): OpResult {
        if (newName.isBlank()) return OpResult.Error("Enter a name.")
        return runWrite {
            api.renameTag(
                GReaderEndpoints.labelStreamId(oldName),
                GReaderEndpoints.labelStreamId(newName.trim()),
                it,
            )
        }
    }

    /** Delete a category (folder); its feeds become uncategorized. */
    suspend fun deleteCategory(name: String): OpResult =
        runWrite { api.disableTag(GReaderEndpoints.labelStreamId(name), it) }

    /** Run a write API call, mapping success/failure to an [OpResult]. */
    private suspend fun runWrite(block: suspend (token: String) -> Unit): OpResult = try {
        withToken(block)
        OpResult.Success
    } catch (e: Exception) {
        OpResult.Error(e.message ?: "Operation failed.")
    }

    /** Run [block] with the write token, refetching once if the first attempt fails. */
    private suspend fun <T> withToken(block: suspend (token: String) -> T): T = try {
        block(writeToken())
    } catch (e: Exception) {
        session.writeToken = null
        block(writeToken())
    }

    private suspend fun writeToken(): String =
        session.writeToken ?: api.writeToken().string().trim().also { session.writeToken = it }
}
