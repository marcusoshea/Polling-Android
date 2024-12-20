package com.polling_android.api

import com.polling_android.model.PollingOrder
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("pollingOrders")
    fun getPollingOrders(): Call<List<PollingOrder>>
}