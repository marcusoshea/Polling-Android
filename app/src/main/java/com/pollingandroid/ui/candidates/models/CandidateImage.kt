package com.pollingandroid.ui.candidates.models

data class CandidateImage(
    val imageId: Int,
    val candidateId: Int,
    val imageUrl: String,
    val description: String,
    val uploadedBy: String,
    val uploadDate: String
)