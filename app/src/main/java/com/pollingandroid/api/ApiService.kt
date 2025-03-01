package com.pollingandroid.api

import com.pollingandroid.model.PollingOrder
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("pollingOrders")
    fun getPollingOrders(): Call<List<PollingOrder>>
}