package com.pollingandroid.ui.candidates.models

data class PollingGroup(
    val pollingId: Int,
    val pollingName: String,
    val startDate: String,
    val endDate: String,
    val notes: List<PollingNote> = emptyList()
)