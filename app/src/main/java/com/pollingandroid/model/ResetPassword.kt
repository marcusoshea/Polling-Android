package com.pollingandroid.model

import com.google.gson.annotations.SerializedName

data class ResetPassword(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("polling_order_id") val pollingOrderId: Int
)