package com.folio.reader.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleSearchTest {

    private fun art(title: String, excerpt: String = "", feed: String? = null, author: String? = null) =
        Article(
            id = title, title = title, url = null, feedTitle = feed, feedStreamId = null,
            author = author, publishedSec = 0, contentHtml = null, excerpt = excerpt,
            thumbnailUrl = null, isRead = false, isStarred = false,
        )

    @Test
    fun `matches title case-insensitively`() {
        assertTrue(ArticleSearch.matches(art("Android tabs"), "android"))
        assertTrue(ArticleSearch.matches(art("Android tabs"), "ANDROID"))
    }

    @Test
    fun `matches in excerpt feed and author`() {
        assertTrue(ArticleSearch.matches(art("X", excerpt = "all about Gemini"), "gemini"))
        assertTrue(ArticleSearch.matches(art("X", feed = "BBC News"), "bbc"))
        assertTrue(ArticleSearch.matches(art("X", author = "Dhruv Bhutani"), "dhruv"))
    }

    @Test
    fun `requires all tokens to match`() {
        assertTrue(ArticleSearch.matches(art("Android Chrome tabs"), "android tabs"))
        assertFalse(ArticleSearch.matches(art("Android tabs"), "android iphone"))
    }

    @Test
    fun `blank query does not match`() {
        assertFalse(ArticleSearch.matches(art("Android"), "   "))
    }

    @Test
    fun `filter returns the matching subset preserving order`() {
        val list = listOf(art("Kotlin"), art("Java"), art("Rust and Java"))
        assertEquals(listOf("Java", "Rust and Java"), ArticleSearch.filter(list, "java").map { it.title })
    }
}
