package com.pollingandroid.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pollingandroid.model.PollingOrderMember
import com.pollingandroid.ui.login.SecureStorage
import com.pollingandroid.util.UserUtils

object PollingOrderMemberRepository {
    private val _pollingOrderMember = MutableLiveData<PollingOrderMember>().apply {
        value = fetchPollingOrderMemberFromStorage()
    }

    val pollingOrderMember: LiveData<PollingOrderMember> = _pollingOrderMember

    fun setPollingOrderMember(member: PollingOrderMember) {
        _pollingOrderMember.value = member
        storePollingOrderMemberToStorage(member)
    }

    private fun fetchPollingOrderMemberFromStorage(): PollingOrderMember {
        val name = SecureStorage.retrieve("memberName")?.let { UserUtils.decryptData(it) } ?: ""
        val email = SecureStorage.retrieve("email")?.let { UserUtils.decryptData(it) } ?: ""
        val memberId = SecureStorage.retrieve("memberId")?.toIntOrNull() ?: 0
        val pollingOrder = SecureStorage.retrieve("pollingOrder")?.toIntOrNull() ?: 0
        val active = SecureStorage.retrieve("active")?.toBoolean() ?: false
        val accessToken = SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) } ?: ""
        val isOrderAdmin = SecureStorage.retrieve("isOrderAdmin")?.toBoolean() ?: false

        return PollingOrderMember(
            name = name,
            email = email,
            memberId = memberId,
            pollingOrder = pollingOrder,
            active = active,
            access_token = accessToken,
            isOrderAdmin = isOrderAdmin
        )
    }

    private fun storePollingOrderMemberToStorage(member: PollingOrderMember) {
        SecureStorage.store("memberName", UserUtils.encryptData(member.name))
        SecureStorage.store("email", UserUtils.encryptData(member.email))
        SecureStorage.store("memberId", member.memberId.toString())
        SecureStorage.store("pollingOrder", member.pollingOrder.toString())
        SecureStorage.store("active", member.active.toString())
        SecureStorage.store("accessToken", UserUtils.encryptData(member.access_token))
        SecureStorage.store("isOrderAdmin", member.isOrderAdmin.toString())
    }
}