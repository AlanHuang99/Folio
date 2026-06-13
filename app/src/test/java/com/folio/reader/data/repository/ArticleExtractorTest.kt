package com.folio.reader.data.repository

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleExtractorTest {

    @Test
    fun `extracts the main article body from a cluttered page`() {
        val html = """
            <html><head><title>X</title></head><body>
            <nav>Home Sections Subscribe Log in</nav>
            <header>Site banner and menus</header>
            <article>
              <h1>The Headline</h1>
              <p>This is the first substantial paragraph of the article body, long enough that the readability algorithm treats it as the primary content of the page rather than chrome.</p>
              <p>Here is a second substantial paragraph continuing the article with more meaningful prose so that the scoring clearly favours this block over the surrounding navigation and ads.</p>
              <p>And a third paragraph to be safe, ensuring the article content dominates the page by word count and by a low link density compared with the menus.</p>
            </article>
            <aside>Related links and advertisements that should be dropped.</aside>
            <footer>Copyright and footer navigation junk.</footer>
            </body></html>
        """.trimIndent()

        val content = ArticleExtractor.extract("https://example.com/post", html)
        assertNotNull(content)
        assertTrue(content!!.contains("first substantial paragraph"))
        assertTrue(content.contains("third paragraph"))
    }
}
