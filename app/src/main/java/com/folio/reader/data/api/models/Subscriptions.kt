package com.folio.reader.data.api.models

import com.google.gson.annotations.SerializedName

// Collection fields are nullable: Gson ignores Kotlin defaults, so an omitted
// array deserializes to null rather than an empty list.
data class SubscriptionListResponse(val subscriptions: List<Subscription>? = null)

data class Subscription(
    val id: String,
    val title: String? = null,
    val url: String? = null,
    val htmlUrl: String? = null,
    val iconUrl: String? = null,
    val categories: List<SubscriptionCategory>? = null,
)

data class SubscriptionCategory(val id: String, val label: String? = null)

data class TagListResponse(val tags: List<Tag>? = null)

data class Tag(val id: String, val type: String? = null)

data class UnreadCountResponse(
    val max: Int = 0,
    val unreadcounts: List<UnreadCount>? = null,
)

data class UnreadCount(
    val id: String,
    val count: Int = 0,
    @SerializedName("newestItemTimestampUsec") val newestItemTimestampUsec: String? = null,
)
