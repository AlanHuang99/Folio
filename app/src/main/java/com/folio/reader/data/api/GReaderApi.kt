package com.folio.reader.data.api

import com.folio.reader.data.api.models.SubscriptionListResponse
import com.folio.reader.data.api.models.TagListResponse
import com.folio.reader.data.api.models.UnreadCountResponse
import com.folio.reader.data.api.models.UserInfo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * The FreshRSS Google Reader API. Endpoint paths are relative to the greader.php
 * base; DynamicBaseUrlInterceptor rewrites them onto the configured server.
 */
interface GReaderApi {

    @GET("accounts/ClientLogin")
    suspend fun clientLogin(
        @Query("Email") email: String,
        @Query("Passwd") password: String,
    ): Response<ResponseBody>

    @GET("reader/api/0/user-info?output=json")
    suspend fun userInfo(): UserInfo

    @GET("reader/api/0/subscription/list?output=json")
    suspend fun subscriptions(): SubscriptionListResponse

    @GET("reader/api/0/tag/list?output=json")
    suspend fun tags(): TagListResponse

    @GET("reader/api/0/unread-count?output=json")
    suspend fun unreadCounts(): UnreadCountResponse
}
