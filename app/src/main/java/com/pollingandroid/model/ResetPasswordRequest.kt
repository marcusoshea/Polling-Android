package com.pollingandroid.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("polling_order_id") val pollingOrderId: Int
)