package com.folio.reader.data.repository

import com.folio.reader.data.api.models.Subscription
import com.folio.reader.data.api.models.SubscriptionCategory
import com.folio.reader.data.api.models.UnreadCount
import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionMapperTest {

    private fun sub(id: String, title: String, cat: Pair<String, String>?) = Subscription(
        id = id,
        title = title,
        categories = cat?.let { listOf(SubscriptionCategory(it.first, it.second)) } ?: emptyList(),
    )

    @Test
    fun `groups feeds by category and sums total unread`() {
        val subs = listOf(
            sub("feed/8", "xkcd", "user/-/label/Fun" to "Fun"),
            sub("feed/4", "Ars Technica", "user/-/label/News" to "News"),
            sub("feed/2", "The Verge", "user/-/label/News" to "News"),
        )
        val counts = listOf(
            UnreadCount("feed/8", 4), UnreadCount("feed/4", 20), UnreadCount("feed/2", 38),
            UnreadCount("user/-/label/News", 58), UnreadCount("user/-/label/Fun", 4),
        )
        val tree = SubscriptionMapper.build(subs, counts)

        assertEquals(62, tree.totalUnread)
        assertEquals(listOf("Fun", "News"), tree.categories.map { it.label })
        val news = tree.categories.first { it.label == "News" }
        assertEquals(58, news.unreadCount)
        assertEquals(listOf("Ars Technica", "The Verge"), news.feeds.map { it.title })
        assertEquals(listOf(20, 38), news.feeds.map { it.unreadCount })
    }

    @Test
    fun `uncategorized feeds go in their own bucket`() {
        val tree = SubscriptionMapper.build(listOf(sub("feed/9", "Loose", null)), emptyList())
        assertEquals(0, tree.categories.size)
        assertEquals(listOf("Loose"), tree.uncategorized.map { it.title })
        assertEquals(0, tree.uncategorized[0].unreadCount)
    }

    @Test
    fun `category unread falls back to sum of feeds when no label count exists`() {
        val subs = listOf(
            sub("feed/1", "A", "user/-/label/Tech" to "Tech"),
            sub("feed/2", "B", "user/-/label/Tech" to "Tech"),
        )
        val counts = listOf(UnreadCount("feed/1", 3), UnreadCount("feed/2", 5))
        val tree = SubscriptionMapper.build(subs, counts)
        assertEquals(8, tree.categories.first().unreadCount)
    }
}
