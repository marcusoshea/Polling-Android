package com.pollingandroid.ui.candidates.models

data class PollingNote(
    val pollingNotesId: Int,
    val note: String?,
    val vote: Int,
    val pollingId: Int,
    val candidateId: Int,
    val pollingOrderId: Int,
    val createdAt: String,
    val pollingOrderMemberId: Int,
    val completed: Boolean,
    val isPrivate: Boolean,
    val memberName: String,
    val pollingName: String = "",
    val startDate: String = "",
    val endDate: String = ""
) {
    fun getVoteText(): String {
        return when (vote) {
            1 -> "Yes"
            2 -> "Wait"
            3 -> "No"
            4 -> "Abstain"
            else -> "Unknown"
        }
    }

    fun hasDisplayableContent(): Boolean {
        return !note.isNullOrBlank() || vote in 1..4
    }
}