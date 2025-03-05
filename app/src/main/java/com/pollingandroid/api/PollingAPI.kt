package com.pollingandroid.api

import com.pollingandroid.model.LoginRequest
import com.pollingandroid.model.PollingOrder
import com.pollingandroid.model.RegistrationRequest
import com.pollingandroid.model.ResetPassword
import com.pollingandroid.model.ResetPasswordRequest

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

    @POST("/member/passwordToken")
    fun requestResetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Call<ResponseBody>

    @POST("verify/:token")
    fun resetPassword(@Body resetPassword: ResetPassword): Call<ResponseBody>

}
