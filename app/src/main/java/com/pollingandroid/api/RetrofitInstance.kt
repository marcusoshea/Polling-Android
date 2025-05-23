package com.pollingandroid.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.pollingandroid.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val TAG = "RetrofitInstance"
    // private const val BASE_URL = "https://api-polling.aethelmearc.org"
    private val BASE_URL = Constants.BASE_API_URL

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .serializeNulls()
                        .create()
                )
            )
            .client(createOkHttpClient())
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: PollingApi by lazy {
        retrofit.create(PollingApi::class.java)
    }
}
