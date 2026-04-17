package com.coachantiarnaque.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client Retrofit centralisé pour les appels API.
 */
object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val safeBrowsingService: SafeBrowsingService by lazy {
        Retrofit.Builder()
            .baseUrl("https://safebrowsing.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SafeBrowsingService::class.java)
    }

    val virusTotalService: VirusTotalService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.virustotal.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VirusTotalService::class.java)
    }

    val whoisApiService: WhoisApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.whoisxmlapi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WhoisApiService::class.java)
    }
}
