package com.polling_android.api

import com.polling_android.model.LoginRequest
import com.polling_android.model.PollingOrder
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.ResponseBody

interface PollingApi {
    @GET("pollingorder")
    fun getPollingOrders(): Call<List<PollingOrder>>

    @POST("/member/login")
    fun login(@Body loginRequest: LoginRequest): Call<ResponseBody>



}
