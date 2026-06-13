package com.folio.reader.data.repository

import com.folio.reader.data.api.GReaderApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val api: GReaderApi
) {
    /** Fetch subscriptions + unread counts and fold them into a category→feed tree. */
    suspend fun getSubscriptionTree(): SubscriptionTree {
        val subscriptions = api.subscriptions().subscriptions
        val counts = api.unreadCounts().unreadcounts
        return SubscriptionMapper.build(subscriptions, counts)
    }
}
