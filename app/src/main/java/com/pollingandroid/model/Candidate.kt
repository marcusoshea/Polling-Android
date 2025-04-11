package com.pollingandroid.model

data class Candidate(
    val candidateId: Int,
    val name: String,
    val pollingOrderId: Int,
    val authToken: String,
    val watchList: Boolean? = null
)