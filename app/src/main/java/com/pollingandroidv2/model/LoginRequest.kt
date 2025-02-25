package com.polling_android.model
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("polling_order_id") val pollingOrderId: Int
)