package com.pollingandroid.model

import com.google.gson.annotations.SerializedName

data class FeedbackRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("message")
    val message: String
)
