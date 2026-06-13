package com.folio.reader.data.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Rewrites every request onto the user-configured greader.php base URL. Retrofit
 * is built with a placeholder base URL because the real host is only known at
 * runtime (after the user enters their server). Requests issued before a server
 * is configured pass through unchanged (and will fail fast).
 */
class DynamicBaseUrlInterceptor @Inject constructor(
    private val session: ServerSession
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val base = session.baseUrl ?: return chain.proceed(request)
        val relative = request.url.encodedPath.removePrefix("/") +
            (request.url.encodedQuery?.let { "?$it" } ?: "")
        val full = (base.trimEnd('/') + "/" + relative).toHttpUrlOrNull()
            ?: return chain.proceed(request)
        return chain.proceed(request.newBuilder().url(full).build())
    }
}
