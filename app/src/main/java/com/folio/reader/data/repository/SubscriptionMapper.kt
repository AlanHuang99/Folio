package com.folio.reader.data.repository

import com.folio.reader.data.api.models.Subscription
import com.folio.reader.data.api.models.UnreadCount

data class FeedNode(
    val streamId: String,
    val title: String,
    val iconUrl: String?,
    val unreadCount: Int,
)

data class CategoryNode(
    val streamId: String,
    val label: String,
    val unreadCount: Int,
    val feeds: List<FeedNode>,
)

data class SubscriptionTree(
    val totalUnread: Int,
    val categories: List<CategoryNode>,
    val uncategorized: List<FeedNode>,
)

/** Pure transform from raw API lists into a category→feed tree with unread counts. */
object SubscriptionMapper {
    fun build(subscriptions: List<Subscription>, counts: List<UnreadCount>): SubscriptionTree {
        val countById = counts.associate { it.id to it.count }
        val feedsByCategory = LinkedHashMap<String, MutableList<FeedNode>>()
        val categoryStreamId = HashMap<String, String>()
        val uncategorized = mutableListOf<FeedNode>()
        var totalUnread = 0

        for (sub in subscriptions) {
            val unread = countById[sub.id] ?: 0
            totalUnread += unread
            val node = FeedNode(sub.id, sub.title ?: sub.id, sub.iconUrl, unread)
            val category = sub.categories.firstOrNull()
            if (category?.label != null) {
                feedsByCategory.getOrPut(category.label) { mutableListOf() }.add(node)
                categoryStreamId[category.label] = category.id
            } else {
                uncategorized.add(node)
            }
        }

        val categories = feedsByCategory.entries
            .map { (label, feeds) ->
                val sortedFeeds = feeds.sortedBy { it.title.lowercase() }
                val streamId = categoryStreamId[label] ?: "user/-/label/$label"
                val unread = countById[streamId] ?: sortedFeeds.sumOf { it.unreadCount }
                CategoryNode(streamId, label, unread, sortedFeeds)
            }
            .sortedBy { it.label.lowercase() }

        return SubscriptionTree(totalUnread, categories, uncategorized.sortedBy { it.title.lowercase() })
    }
}
