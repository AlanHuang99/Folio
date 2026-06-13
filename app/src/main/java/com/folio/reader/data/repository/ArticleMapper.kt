package com.folio.reader.data.repository

import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.data.api.models.StreamItem

data class Article(
    val id: String,
    val title: String,
    val url: String?,
    val feedTitle: String?,
    val feedStreamId: String?,
    val author: String?,
    val publishedSec: Long,
    val contentHtml: String?,
    val excerpt: String,
    val thumbnailUrl: String?,
    val isRead: Boolean,
    val isStarred: Boolean,
)

/** Pure transforms from a raw stream item into the UI Article model. */
object ArticleMapper {

    private val imgRegex = Regex("""<img[^>]+src\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val tagRegex = Regex("<[^>]+>")
    private val whitespaceRegex = Regex("\\s+")

    fun toArticle(item: StreamItem): Article {
        val html = item.content?.content ?: item.summary?.content
        val url = item.alternate.orEmpty().firstOrNull()?.href
            ?: item.canonical.orEmpty().firstOrNull()?.href
        val thumbnail = item.enclosure.orEmpty().firstOrNull { it.type?.startsWith("image") == true }?.href
            ?: firstImageUrl(html)
        return Article(
            id = item.id,
            title = item.title?.trim()?.ifBlank { null } ?: "(untitled)",
            url = url,
            feedTitle = item.origin?.title,
            feedStreamId = item.origin?.streamId,
            author = item.author,
            publishedSec = item.published,
            contentHtml = html,
            excerpt = htmlToExcerpt(html),
            thumbnailUrl = thumbnail,
            isRead = GReaderEndpoints.TAG_READ in item.categories.orEmpty(),
            isStarred = GReaderEndpoints.TAG_STARRED in item.categories.orEmpty(),
        )
    }

    /** Strip HTML tags + entities, collapse whitespace, truncate with an ellipsis. */
    fun htmlToExcerpt(html: String?, maxLength: Int = 200): String {
        if (html.isNullOrBlank()) return ""
        val text = whitespaceRegex.replace(decodeEntities(tagRegex.replace(html, " ")), " ").trim()
        return if (text.length > maxLength) text.take(maxLength).trimEnd() + "…" else text
    }

    /** The first <img src> URL in the HTML, or null. */
    fun firstImageUrl(html: String?): String? {
        if (html.isNullOrBlank()) return null
        return imgRegex.find(html)?.groupValues?.get(1)
    }

    // Decode &amp; last so already-escaped entities aren't double-decoded.
    private fun decodeEntities(s: String): String =
        s.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
}
