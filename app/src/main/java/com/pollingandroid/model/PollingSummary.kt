package com.pollingandroid.model

data class PollingSummary(
    val pollingId: Int,
    val pollingName: String,
    val startDate: String,
    val endDate: String,
    val pollingOrderId: Int,
    val candidateId: Int,
    val pollingCandidateId: Int,
    val name: String,
    val pollingNotesId: Int,
    val note: String,
    val vote: Int,
    val pnCreatedAt: String,
    val pollingOrderMemberId: Int,
    val completed: Boolean
)