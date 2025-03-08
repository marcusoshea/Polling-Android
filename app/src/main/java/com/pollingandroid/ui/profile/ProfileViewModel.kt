package com.pollingandroid.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pollingandroid.model.PollingOrderMember
import com.pollingandroid.repository.PollingOrderMemberRepository
import com.pollingandroid.util.UserUtils
import com.pollingandroid.repository.PollingOrderRepository
import com.pollingandroid.ui.login.SecureStorage

class ProfileViewModel : ViewModel() {

    val pollingOrderName: LiveData<String> = PollingOrderRepository.pollingOrderName
    val memberInfo: LiveData<PollingOrderMember> = PollingOrderMemberRepository.pollingOrderMember

    fun updateProfile(context: Context, name: String, email: String, active: Boolean) {
        val member = memberInfo.value ?: return
        val accessToken = UserUtils.decryptData(SecureStorage.retrieve("accessToken").toString()) ?: return
        ProfileHandler(context).updateProfile(
            member.memberId,
            name,
            email,
            member.pollingOrder,
            active,
            accessToken
        ) { success ->
            if (success) {
                PollingOrderMemberRepository.setPollingOrderMember(
                    member.copy(name = name, email = email, active = active)
                )
            }
        }
    }

    fun updatePassword(context: Context, currentPassword: String, newPassword: String) {
        val member = memberInfo.value ?: return
        val accessToken = UserUtils.decryptData(SecureStorage.retrieve("accessToken").toString()) ?: return
        ProfileHandler(context).updatePassword(
            member.email,
            currentPassword,
            newPassword,
            member.pollingOrder.toString(),
            accessToken
        ) {}
    }
}