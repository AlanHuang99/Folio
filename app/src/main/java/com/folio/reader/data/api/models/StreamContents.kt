package com.folio.reader.data.api.models

// Collection fields are nullable: Gson instantiates via Unsafe and ignores Kotlin
// default values, so a JSON object that omits an array leaves the field null.
data class StreamContentsResponse(
    val items: List<StreamItem>? = null,
    val continuation: String? = null,
)

data class StreamItem(
    val id: String,
    val title: String? = null,
    val published: Long = 0,
    val author: String? = null,
    val canonical: List<Link>? = null,
    val alternate: List<Link>? = null,
    val categories: List<String>? = null,
    val origin: Origin? = null,
    val summary: Content? = null,
    val content: Content? = null,
    val enclosure: List<Enclosure>? = null,
)

data class Link(val href: String? = null)

data class Origin(
    val streamId: String? = null,
    val title: String? = null,
    val htmlUrl: String? = null,
)

data class Content(val content: String? = null)

data class Enclosure(val href: String? = null, val type: String? = null)
