package com.folio.reader.data.api

/**
 * Pure helpers and constants for the Google Reader API. Kept free of Android/
 * Retrofit dependencies so they are unit-testable on the JVM.
 */
object GReaderEndpoints {
    const val STREAM_READING_LIST = "user/-/state/com.google/reading-list"
    const val STREAM_STARRED = "user/-/state/com.google/starred"
    const val TAG_READ = "user/-/state/com.google/read"
    const val TAG_STARRED = "user/-/state/com.google/starred"
    const val LABEL_PREFIX = "user/-/label/"

    /** Build the greader.php base URL (with trailing slash) from a user-entered server URL. */
    fun greaderBaseUrl(serverUrl: String): String {
        var s = serverUrl.trim().trimEnd('/')
        if (!s.endsWith("/api/greader.php")) s += "/api/greader.php"
        return "$s/"
    }

    /** Extract the token from a ClientLogin response body, or null if absent. */
    fun parseAuthToken(body: String): String? =
        body.lineSequence()
            .firstOrNull { it.startsWith("Auth=") }
            ?.removePrefix("Auth=")
            ?.trim()
            ?.ifEmpty { null }

    /** Percent-encode a stream id for use as a URL path, keeping '/' separators. */
    fun encodeStreamId(streamId: String): String {
        val sb = StringBuilder()
        for (byte in streamId.toByteArray(Charsets.UTF_8)) {
            val value = byte.toInt() and 0xFF
            val c = value.toChar()
            if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c in "-._~/") {
                sb.append(c)
            } else {
                sb.append('%').append(value.toString(16).uppercase().padStart(2, '0'))
            }
        }
        return sb.toString()
    }
}
