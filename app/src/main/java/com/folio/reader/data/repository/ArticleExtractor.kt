package com.folio.reader.data.repository

import net.dankito.readability4j.Readability4J

/**
 * Extracts the main readable article HTML from a full web page (reader mode).
 * Pure (no IO) so it is unit-testable; the network fetch lives in ReaderModeRepository.
 */
object ArticleExtractor {
    fun extract(url: String, html: String): String? = try {
        Readability4J(url, html).parse().content?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }
}
