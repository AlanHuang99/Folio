package com.folio.reader.data.repository

import com.folio.reader.data.api.models.Content
import com.folio.reader.data.api.models.Enclosure
import com.folio.reader.data.api.models.Link
import com.folio.reader.data.api.models.Origin
import com.folio.reader.data.api.models.StreamContentsResponse
import com.folio.reader.data.api.models.StreamItem
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleMapperTest {

    @Test
    fun `htmlToExcerpt strips tags and collapses whitespace`() {
        assertEquals("Hello world", ArticleMapper.htmlToExcerpt("<p>Hello <b>world</b></p>"))
        assertEquals("a b", ArticleMapper.htmlToExcerpt("a\n\n   b"))
    }

    @Test
    fun `htmlToExcerpt decodes basic entities`() {
        assertEquals("A & B", ArticleMapper.htmlToExcerpt("A &amp; B"))
        assertEquals("\"q\"", ArticleMapper.htmlToExcerpt("&quot;q&quot;"))
    }

    @Test
    fun `htmlToExcerpt returns empty for null or blank`() {
        assertEquals("", ArticleMapper.htmlToExcerpt(null))
        assertEquals("", ArticleMapper.htmlToExcerpt("   "))
    }

    @Test
    fun `htmlToExcerpt truncates with an ellipsis`() {
        assertEquals("xxxxxxxxxx…", ArticleMapper.htmlToExcerpt("<p>${"x".repeat(50)}</p>", 10))
    }

    @Test
    fun `firstImageUrl finds the first img src in either quote style`() {
        assertEquals("http://e/i.png", ArticleMapper.firstImageUrl("<p>x</p><img src=\"http://e/i.png\"/>"))
        assertEquals("http://e/j.png", ArticleMapper.firstImageUrl("<img src='http://e/j.png'>"))
        assertNull(ArticleMapper.firstImageUrl("<p>no image</p>"))
        assertNull(ArticleMapper.firstImageUrl(null))
    }

    @Test
    fun `toArticle derives read and starred from categories`() {
        val item = StreamItem(
            id = "item/1",
            categories = listOf(
                "user/-/state/com.google/reading-list",
                "user/-/state/com.google/read",
                "user/-/state/com.google/starred",
            ),
        )
        val a = ArticleMapper.toArticle(item)
        assertTrue(a.isRead)
        assertTrue(a.isStarred)
    }

    @Test
    fun `toArticle treats a missing read tag as unread`() {
        val a = ArticleMapper.toArticle(StreamItem(id = "item/2", categories = listOf("user/-/label/Tech")))
        assertFalse(a.isRead)
        assertFalse(a.isStarred)
    }

    @Test
    fun `toArticle prefers an image enclosure for the thumbnail`() {
        val item = StreamItem(
            id = "item/3",
            summary = Content("<p>Body <img src=\"http://img/in.png\"></p>"),
            enclosure = listOf(Enclosure("http://enc/x.png", "image/jpeg")),
        )
        assertEquals("http://enc/x.png", ArticleMapper.toArticle(item).thumbnailUrl)
    }

    @Test
    fun `toArticle falls back to the first inline image for the thumbnail`() {
        val item = StreamItem(id = "item/4", summary = Content("<p>Body <img src=\"http://img/in.png\"></p>"))
        assertEquals("http://img/in.png", ArticleMapper.toArticle(item).thumbnailUrl)
    }

    @Test
    fun `toArticle maps title url feed author and content`() {
        val item = StreamItem(
            id = "item/5",
            title = "Headline",
            published = 1781344811,
            author = "Parth",
            alternate = listOf(Link("http://alt")),
            canonical = listOf(Link("http://can")),
            origin = Origin("feed/6", "Android Police", "http://ap"),
            content = Content("<p>Full content</p>"),
            summary = Content("<p>Summary</p>"),
        )
        val a = ArticleMapper.toArticle(item)
        assertEquals("Headline", a.title)
        assertEquals("http://alt", a.url) // alternate preferred over canonical
        assertEquals("Android Police", a.feedTitle)
        assertEquals("feed/6", a.feedStreamId)
        assertEquals("Parth", a.author)
        assertEquals(1781344811L, a.publishedSec)
        assertEquals("<p>Full content</p>", a.contentHtml) // content preferred over summary
        assertEquals("Full content", a.excerpt)
    }

    @Test
    fun `toArticle uses a placeholder for a missing title`() {
        assertEquals("(untitled)", ArticleMapper.toArticle(StreamItem(id = "item/6")).title)
    }

    // Gson instantiates via Unsafe and ignores Kotlin defaults, so JSON that omits
    // optional arrays yields null lists. The mapper must not crash on those.
    @Test
    fun `toArticle survives a Gson-parsed item missing optional lists`() {
        val json = """{"items":[{"id":"item/1","title":"No media","published":100,""" +
            """"origin":{"streamId":"feed/1","title":"F"},"summary":{"content":"<p>Hi</p>"}}]}"""
        val response = Gson().fromJson(json, StreamContentsResponse::class.java)
        val article = ArticleMapper.toArticle(response.items!!.first())
        assertEquals("No media", article.title)
        assertFalse(article.isRead)
        assertNull(article.thumbnailUrl)
        assertEquals("Hi", article.excerpt)
    }
}
