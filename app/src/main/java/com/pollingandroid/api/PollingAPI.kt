package com.pollingandroid.api

import com.pollingandroid.model.LoginRequest
import com.pollingandroid.model.PollingOrder
import com.pollingandroid.model.RegistrationRequest

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.ResponseBody

interface PollingApi {
    @GET("pollingorder")
    fun getPollingOrders(): Call<List<PollingOrder>>

    @POST("/member/login")
    fun login(@Body loginRequest: LoginRequest): Call<ResponseBody>

    @POST("/member/create")
    fun register(@Body registrationRequest: RegistrationRequest): Call<ResponseBody>


}


/*
const created = today.toISOString().split('T')[0];

return this.http.post(
API_URL + '/member/create',
{ "name": memberName,
    "email": email,
    "password": password,
    "polling_order_id": polling_order_id,
    "pom_created_at": created,
},
httpOptions
);
*/