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
import retrofit2.http.HeaderMap
import retrofit2.http.PUT
import retrofit2.http.Path

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

    @PUT("/member/edit/{memberId}")
    fun updateProfile(
        @Path("memberId") memberId: Int,
        @Body body: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @PUT("/member/changePassword")
    fun updatePassword(
        @Body body: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

}
