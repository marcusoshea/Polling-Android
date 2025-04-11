package com.pollingandroid.model

data class Note(
    val externalNotesId: Int,
    val candidateId: Int,
    val pollingOrderMemberId: Int,
    val externalNote: String,
    val enCreatedAt: String
)