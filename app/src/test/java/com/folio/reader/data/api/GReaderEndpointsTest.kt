package com.folio.reader.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GReaderEndpointsTest {

    @Test
    fun `greaderBaseUrl appends api path to a bare host`() {
        assertEquals(
            "http://alan-mint:8280/api/greader.php/",
            GReaderEndpoints.greaderBaseUrl("http://alan-mint:8280")
        )
    }

    @Test
    fun `greaderBaseUrl strips a trailing slash before appending`() {
        assertEquals(
            "http://alan-mint:8280/api/greader.php/",
            GReaderEndpoints.greaderBaseUrl("http://alan-mint:8280/")
        )
    }

    @Test
    fun `greaderBaseUrl keeps an existing api greader path`() {
        assertEquals(
            "https://reader.example.com/api/greader.php/",
            GReaderEndpoints.greaderBaseUrl("https://reader.example.com/api/greader.php")
        )
    }

    @Test
    fun `greaderBaseUrl preserves a subpath`() {
        assertEquals(
            "https://example.com/freshrss/api/greader.php/",
            GReaderEndpoints.greaderBaseUrl("https://example.com/freshrss")
        )
    }

    @Test
    fun `greaderBaseUrl trims surrounding whitespace`() {
        assertEquals(
            "https://h/api/greader.php/",
            GReaderEndpoints.greaderBaseUrl("  https://h  ")
        )
    }

    @Test
    fun `parseAuthToken extracts the Auth line`() {
        val body = "SID=test/abc\nLSID=null\nAuth=test/deadbeef\n"
        assertEquals("test/deadbeef", GReaderEndpoints.parseAuthToken(body))
    }

    @Test
    fun `parseAuthToken returns null when absent`() {
        assertNull(GReaderEndpoints.parseAuthToken("Error=BadAuthentication"))
    }

    @Test
    fun `parseAuthToken trims trailing carriage return`() {
        assertEquals("tok", GReaderEndpoints.parseAuthToken("Auth=tok\r\n"))
    }

    @Test
    fun `encodeStreamId keeps slashes and simple ids`() {
        assertEquals("feed/8", GReaderEndpoints.encodeStreamId("feed/8"))
        assertEquals(
            "user/-/state/com.google/reading-list",
            GReaderEndpoints.encodeStreamId("user/-/state/com.google/reading-list"),
        )
    }

    @Test
    fun `encodeStreamId percent-encodes spaces and unsafe chars`() {
        assertEquals(
            "user/-/label/My%20News",
            GReaderEndpoints.encodeStreamId("user/-/label/My News"),
        )
    }

    @Test
    fun `labelStreamId prefixes a category name`() {
        assertEquals("user/-/label/Tech", GReaderEndpoints.labelStreamId("Tech"))
        assertEquals("user/-/label/AI", GReaderEndpoints.labelStreamId("AI"))
    }

    @Test
    fun `labelStreamId trims whitespace`() {
        assertEquals("user/-/label/Weather", GReaderEndpoints.labelStreamId("  Weather "))
    }

    @Test
    fun `categoryNameFromStreamId returns the trailing label`() {
        assertEquals("Tech", GReaderEndpoints.categoryNameFromStreamId("user/-/label/Tech"))
    }

    @Test
    fun `categoryNameFromStreamId returns null for non-label streams`() {
        assertNull(GReaderEndpoints.categoryNameFromStreamId("feed/8"))
    }

    @Test
    fun `normalizeFeedInput prepends https when scheme is missing`() {
        assertEquals("https://example.com/feed", GReaderEndpoints.normalizeFeedInput("example.com/feed"))
    }

    @Test
    fun `normalizeFeedInput keeps an existing scheme`() {
        assertEquals("http://h/rss", GReaderEndpoints.normalizeFeedInput("  http://h/rss  "))
        assertEquals("https://h/rss", GReaderEndpoints.normalizeFeedInput("https://h/rss"))
    }

    @Test
    fun `normalizeFeedInput returns empty for blank input`() {
        assertEquals("", GReaderEndpoints.normalizeFeedInput("   "))
    }
}
