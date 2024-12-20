package com.polling_android.api

import com.polling_android.model.PollingOrder
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface PollingApi {
    @GET("pollingorder")
    fun getPollingOrders(): Call<List<PollingOrder>>
}
