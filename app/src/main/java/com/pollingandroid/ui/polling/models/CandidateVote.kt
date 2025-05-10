package com.pollingandroid.ui.polling.models

data class CandidateVote(
    var candidateId: Int,
    var candidateName: String,
    var note: String = "",
    var vote: Int? = null,
    var isPrivate: Boolean = false,
    var pollingNotesId: Int = 0,
    var completed: Boolean = false
)