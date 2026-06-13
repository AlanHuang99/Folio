package com.folio.reader.data.api

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Adds the GoogleLogin auth header to every request once a token is present. */
class AuthInterceptor @Inject constructor(
    private val session: ServerSession
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = session.token ?: return chain.proceed(chain.request())
        val authed = chain.request().newBuilder()
            .header("Authorization", "GoogleLogin auth=$token")
            .build()
        return chain.proceed(authed)
    }
}
