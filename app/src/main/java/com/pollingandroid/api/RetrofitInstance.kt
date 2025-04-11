package com.pollingandroid.api

import com.pollingandroid.api.PollingApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api-polling.aethelmearc.org"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().also { client ->
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                client.addInterceptor(loggingInterceptor)
            }.build())
            .build()
    }

    val api: PollingApi by lazy {
        retrofit.create(PollingApi::class.java)
    }
}