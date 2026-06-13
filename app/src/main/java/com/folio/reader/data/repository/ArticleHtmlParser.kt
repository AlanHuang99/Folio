package com.folio.reader.data.repository

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

/** A renderable piece of an article: a run of HTML text, or an image. */
sealed interface ArticleBlock {
    data class Text(val html: String) : ArticleBlock
    data class Image(val url: String) : ArticleBlock
}

/**
 * Splits article HTML into a sequence of text runs and standalone images, so the
 * reader can render text with Compose and images with Coil. Image-only blocks
 * (e.g. <figure><img></figure>) become Image blocks; everything else accumulates
 * into Text blocks. Pure (Jsoup only) so it is unit-testable on the JVM.
 */
object ArticleHtmlParser {
    fun parse(html: String?): List<ArticleBlock> {
        if (html.isNullOrBlank()) return emptyList()
        val body = Jsoup.parseBodyFragment(html).body()
        val blocks = mutableListOf<ArticleBlock>()
        val buffer = StringBuilder()

        fun flush() {
            val text = buffer.toString().trim()
            buffer.setLength(0)
            if (text.isNotEmpty()) blocks += ArticleBlock.Text(text)
        }

        for (node in body.childNodes()) {
            when (node) {
                is TextNode -> if (!node.isBlank) buffer.append(node.outerHtml())

                is Element -> {
                    if (node.tagName() == "img") {
                        flush()
                        node.attr("src").takeIf { it.isNotBlank() }?.let { blocks += ArticleBlock.Image(it) }
                    } else {
                        val images = node.select("img")
                        if (images.isNotEmpty() && node.text().isBlank()) {
                            flush()
                            images.forEach { img ->
                                img.attr("src").takeIf { it.isNotBlank() }?.let { blocks += ArticleBlock.Image(it) }
                            }
                        } else {
                            buffer.append(node.outerHtml()).append('\n')
                        }
                    }
                }
            }
        }
        flush()

        // Bare-text fragment with no element children.
        if (blocks.isEmpty() && body.text().isNotBlank()) blocks += ArticleBlock.Text(html.trim())
        return blocks
    }
}
