package com.folio.reader.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Fetches an article's original web page and extracts the clean full text (reader mode). */
@Singleton
class ReaderModeRepository @Inject constructor(
    @Named("web") private val client: OkHttpClient,
) {
    private val cache = ConcurrentHashMap<String, String>()

    /** The readable article HTML for [url], or null if it couldn't be fetched/extracted. */
    suspend fun readable(url: String): String? = withContext(Dispatchers.IO) {
        cache[url]?.let { return@withContext it }
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android) Folio")
                .build()
            val html = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                response.body?.string()
            } ?: return@withContext null
            ArticleExtractor.extract(url, html)?.also { cache[url] = it }
        } catch (e: Exception) {
            null
        }
    }
}
