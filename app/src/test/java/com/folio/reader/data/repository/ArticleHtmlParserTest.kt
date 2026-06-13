package com.folio.reader.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleHtmlParserTest {

    @Test
    fun `splits text and standalone image into separate blocks`() {
        val blocks = ArticleHtmlParser.parse("<p>Intro</p><figure><img src=\"u1\"></figure><p>Body</p>")
        assertEquals(3, blocks.size)
        assertTrue((blocks[0] as ArticleBlock.Text).html.contains("Intro"))
        assertEquals("u1", (blocks[1] as ArticleBlock.Image).url)
        assertTrue((blocks[2] as ArticleBlock.Text).html.contains("Body"))
    }

    @Test
    fun `a bare image becomes an image block`() {
        val blocks = ArticleHtmlParser.parse("<img src=\"only.png\">")
        assertEquals(listOf(ArticleBlock.Image("only.png")), blocks)
    }

    @Test
    fun `consecutive text elements accumulate into one text block`() {
        val blocks = ArticleHtmlParser.parse("<p>A</p><p>B</p>")
        assertEquals(1, blocks.size)
        val text = (blocks[0] as ArticleBlock.Text).html
        assertTrue(text.contains("A") && text.contains("B"))
    }

    @Test
    fun `an image inside a text paragraph stays in the text block`() {
        val blocks = ArticleHtmlParser.parse("<p>see <img src=\"x\"> this</p>")
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is ArticleBlock.Text)
    }

    @Test
    fun `a bare text node before an image is kept`() {
        val blocks = ArticleHtmlParser.parse("Lead sentence.<figure><img src=\"u\"></figure>")
        assertEquals(2, blocks.size)
        assertTrue((blocks[0] as ArticleBlock.Text).html.contains("Lead sentence"))
        assertEquals("u", (blocks[1] as ArticleBlock.Image).url)
    }

    @Test
    fun `blank or null html yields no blocks`() {
        assertEquals(emptyList<ArticleBlock>(), ArticleHtmlParser.parse(""))
        assertEquals(emptyList<ArticleBlock>(), ArticleHtmlParser.parse(null))
    }
}
