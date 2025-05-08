package com.pollingandroid.ui.candidates.models

data class Candidate(
    val id: Int = 0,
    val candidate_id: Int = 0,
    val name: String = "",
    val link: String? = null,
    val polling_order_id: Int = 0,
    val watch_list: Boolean = false,

    val membershipId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val emailAddress: String = "",
    val address: String = "",
    val city: String = "",
    val province: String = "",
    val postalCode: String = "",
    val telephone: String = "",
    val dateOfBirth: String = "",
    val occupation: String = "",
    val society: String = "",
    val kingdom: String = "",
    val yearsActive: Int = 0,
    val branchName: String = "",
    val branchCity: String = "",
    val branchProvince: String = "",
    val awards: String = "",
    val offices: String = "",
    val whyJoin: String = "",
    val recommendation: String = "",
    val order: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val memberName: String = "",
    val submittingMemberId: Int = 0,
    val pollingId: Int = 0,
    val pollingComplete: Boolean = false
) {
    fun getFullName(): String = if (name.isNotBlank()) {
        name
    } else {
        "$firstName $lastName"
    }

    fun getLocationString(): String = if (city.isNotBlank() && province.isNotBlank()) {
        "$city, $province"
    } else {
        ""
    }

    val isOnWatchlist: Boolean
        get() = watch_list
}