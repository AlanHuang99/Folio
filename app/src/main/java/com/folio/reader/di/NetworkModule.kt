package com.folio.reader.di

import com.folio.reader.BuildConfig
import com.folio.reader.data.api.AuthInterceptor
import com.folio.reader.data.api.DynamicBaseUrlInterceptor
import com.folio.reader.data.api.GReaderApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        dynamicBaseUrl: DynamicBaseUrlInterceptor,
        auth: AuthInterceptor,
    ): OkHttpClient {
        // Logging is gated to debug builds — the ClientLogin URL carries the
        // password as a query parameter, so release builds log nothing.
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrl)
            .addInterceptor(auth)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://localhost/") // placeholder; rewritten per request
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()

    @Provides
    @Singleton
    fun provideGReaderApi(retrofit: Retrofit): GReaderApi =
        retrofit.create(GReaderApi::class.java)

    // A plain client for fetching arbitrary article web pages (reader mode). It
    // deliberately omits the dynamic-base-URL and auth interceptors, which would
    // otherwise rewrite the request onto the FreshRSS server.
    @Provides
    @Singleton
    @Named("web")
    fun provideWebClient(): OkHttpClient =
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
}
