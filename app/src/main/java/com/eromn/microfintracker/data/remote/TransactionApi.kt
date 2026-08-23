package com.eromn.microfintracker.data.remote

import com.eromn.microfintracker.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit contract for the transactions API. The shared [Json] instance handles any
 * extra fields returned by the server and omits null fields from the request payload.
 */
interface TransactionApi {

    /**
     * Adds a new transaction to the server.
     */
    @POST("transactions")
    suspend fun createTransaction(@Body transaction: TransactionDto): TransactionDto

    companion object {
        const val BASE_URL = "https://iemsur.com.mx/api/"

        /**
         * Single shared serializer instance. Unknown server fields are ignored and null
         * request fields are omitted.
         */
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

        /**
         * Builds a [TransactionApi] wired with [json] and an optional [OkHttpClient].
         */
        fun create(
            baseUrl: String = BASE_URL,
            okHttpClient: OkHttpClient = defaultClient()
        ): TransactionApi {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(TransactionApi::class.java)
        }

        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder().apply {
                // Never log request/response bodies outside of debug builds.
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                }
            }.build()
        }
    }
}
