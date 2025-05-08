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

    @GET("/polling/pollingreport/{orderId}")
    fun getPollingReport(
        @Path("orderId") orderId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/polling/inprocesspollingreport/{orderId}")
    fun getInProcessPollingReport(
        @Path("orderId") orderId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @POST("/pollingnote/create")
    fun createPollingNotes(
        @Body body: List<Map<String, Any>>,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/polling/currentpolling/{orderId}")
    fun getCurrentPolling(
        @Path("orderId") orderId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/polling/pollingsummary/{pollingId}/{pollingOrderMemberId}")
    fun getPollingSummary(
        @Path("pollingId") pollingId: Int,
        @Path("pollingOrderMemberId") pollingOrderMemberId: String,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/externalnote/candidate/{id}")
    fun getExternalNoteByCandidateId(
        @Path("id") id: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/polling/allpn/{id}")
    fun getPollingNoteByCandidateId(
        @Path("id") id: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    /**
     * Get all polling notes for a specific polling ID.
     * The polling_notes_id must be passed in the request body,
     * not as a path parameter. Required fields are:
     * - polling_notes_id (String): The ID of the polling notes
     * - authToken (String): The authentication token
     * Note: authToken is included in both the request body and headers for consistency.
     */
    @POST("/pollingnote/all")
    fun getAllPollingNotesById(
        @Body body: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/candidate/{candidateId}")
    fun getCandidate(
        @Path("candidateId") candidateId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @POST("/externalnote/create")
    fun createExternalNote(
        @Body body: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @POST("/externalnote/delete")
    fun removeExternalNote(
        @Body body: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/pollingnote/totals/{pollingId}")
    fun getPollingReportTotals(
        @Path("pollingId") pollingId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/candidate/all/{orderID}")
    fun getAllCandidates(
        @Path("orderID") orderID: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/candidate/candidateImages/{candidate_id}")
    fun getAllCandidateImages(
        @Path("candidate_id") candidateId: String,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/polling/pollingreport/{orderId}/summary")
    fun getPollingReportSummary(
        @Path("orderId") orderId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/polling/pollingreport/{orderId}/details")
    fun getPollingReportDetails(
        @Path("orderId") orderId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/polling/pollingreport/{orderId}/notes")
    fun getPollingReportNotes(
        @Path("orderId") orderId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @GET("/polling/pollingreport/{orderId}/candidates")
    fun getPollingReportCandidates(
        @Path("orderId") orderId: Int,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

}