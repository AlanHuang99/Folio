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
}
