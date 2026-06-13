package com.folio.reader.data.repository

/**
 * Client-side article search (FreshRSS's Google Reader API has no text search).
 * Matches when every whitespace-separated token appears, case-insensitively, in the
 * article's title, excerpt, feed name, or author. Pure — unit-testable.
 */
object ArticleSearch {
    private val whitespace = Regex("\\s+")

    fun matches(article: Article, query: String): Boolean {
        val tokens = query.trim().lowercase().split(whitespace).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return false
        val haystack = listOf(article.title, article.excerpt, article.feedTitle, article.author)
            .joinToString(" ") { it.orEmpty() }
            .lowercase()
        return tokens.all { haystack.contains(it) }
    }

    fun filter(articles: List<Article>, query: String): List<Article> =
        articles.filter { matches(it, query) }
}
