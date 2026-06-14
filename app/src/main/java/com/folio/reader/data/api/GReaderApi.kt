package com.folio.reader.data.api

import com.folio.reader.data.api.models.QuickAddResponse
import com.folio.reader.data.api.models.StreamContentsResponse
import com.folio.reader.data.api.models.SubscriptionListResponse
import com.folio.reader.data.api.models.TagListResponse
import com.folio.reader.data.api.models.UnreadCountResponse
import com.folio.reader.data.api.models.UserInfo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

    // streamId is pre-encoded (slashes kept) by GReaderEndpoints.encodeStreamId.
    @GET("reader/api/0/stream/contents/{streamId}")
    suspend fun streamContents(
        @Path("streamId", encoded = true) streamId: String,
        @Query("n") count: Int,
        @Query("c") continuation: String?,
        @Query("xt") excludeTarget: String?,
        @Query("output") output: String = "json",
    ): StreamContentsResponse

    @GET("reader/api/0/token")
    suspend fun writeToken(): ResponseBody

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    suspend fun editTag(
        @Field("i") itemId: String,
        @Field("a") add: String?,
        @Field("r") remove: String?,
        @Field("T") token: String,
    ): ResponseBody

    @FormUrlEncoded
    @POST("reader/api/0/stream/items/contents?output=json")
    suspend fun itemContents(@Field("i") itemId: String): StreamContentsResponse

    // --- Subscription management ---

    /** Subscribe to a feed by URL. FreshRSS fetches and parses it server-side. */
    @FormUrlEncoded
    @POST("reader/api/0/subscription/quickadd?output=json")
    suspend fun quickAddSubscription(
        @Field("quickadd") url: String,
        @Field("T") token: String,
    ): QuickAddResponse

    /**
     * Edit a subscription. `action` is "edit" (rename/move) or "unsubscribe".
     * `title` renames; `addLabel`/`removeLabel` are full label stream ids
     * (user/-/label/Name) to add/remove the feed's category.
     */
    @FormUrlEncoded
    @POST("reader/api/0/subscription/edit")
    suspend fun editSubscription(
        @Field("ac") action: String,
        @Field("s") streamId: String,
        @Field("t") title: String?,
        @Field("a") addLabel: String?,
        @Field("r") removeLabel: String?,
        @Field("T") token: String,
    ): ResponseBody

    /** Rename a category (folder). `source`/`dest` are label stream ids. */
    @FormUrlEncoded
    @POST("reader/api/0/rename-tag")
    suspend fun renameTag(
        @Field("s") source: String,
        @Field("dest") dest: String,
        @Field("T") token: String,
    ): ResponseBody

    /** Delete a category (folder); its feeds become uncategorized. */
    @FormUrlEncoded
    @POST("reader/api/0/disable-tag")
    suspend fun disableTag(
        @Field("s") streamId: String,
        @Field("T") token: String,
    ): ResponseBody
}
