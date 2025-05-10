package com.pollingandroid.model

import com.google.gson.annotations.SerializedName

data class PollingNoteRequest(
    @SerializedName("polling_id") val pollingId: Int,
    @SerializedName("candidate_id") val candidateId: Int,
    @SerializedName("polling_candidate_id") val pollingCandidateId: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("polling_order_id") val pollingOrderId: Int,
    @SerializedName("link") val link: String = "",
    @SerializedName("watch_list") val watchList: Boolean = false,
    @SerializedName("polling_notes_id") val pollingNotesId: Int? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("vote") val vote: Int? = null,
    @SerializedName("pn_created_at") val pnCreatedAt: String,
    @SerializedName("polling_order_member_id") val pollingOrderMemberId: Int,
    @SerializedName("completed") val completed: Boolean = true,
    @SerializedName("private") val isPrivate: Boolean,
    @SerializedName("authToken") val authToken: String? = null
)