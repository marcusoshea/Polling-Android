package com.pollingandroid.ui.candidates.models

data class ExternalNote(
    val externalNoteId: Int,
    val candidateId: Int,
    val note: String,
    val createdAt: String,
    val updatedAt: String,
    val memberName: String,
    val memberId: Int,
    val isPrivate: Boolean
)