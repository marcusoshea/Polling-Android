package com.pollingandroid.model

data class Polling(
    val pollingId: Int,
    val name: String,
    val pollingOrderId: Int,
    val startDate: String,
    val endDate: String,
    val accessToken: String
)