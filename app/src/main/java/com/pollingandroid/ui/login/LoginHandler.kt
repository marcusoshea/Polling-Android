package com.pollingandroid.ui.login

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.LoginRequest
import com.pollingandroid.model.PollingOrderMember
import com.pollingandroid.repository.PollingOrderRepository
import com.pollingandroid.repository.PollingOrderMemberRepository
import com.pollingandroid.util.UserUtils
import com.pollingandroid.util.LiveDataCleaner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

class LoginHandler(private val context: Context) {

    fun handleLogin(email: String, password: String, pollingOrder: Int, callback: (logSuccess: Boolean) -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty() && pollingOrder != 0) {
            val loginRequest = LoginRequest(email, password, pollingOrder)
            RetrofitInstance.api.login(loginRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        val pollingOrderMember = Gson().fromJson(responseBody, PollingOrderMember::class.java)
                        val encryptedName = UserUtils.encryptData(pollingOrderMember.name)
                        val encryptedAccessToken = UserUtils.encryptData(pollingOrderMember.access_token)
                        val encryptedEmail = UserUtils.encryptData(pollingOrderMember.email)
                        SecureStorage.store("memberName", encryptedName)
                        SecureStorage.store("email", encryptedEmail)
                        SecureStorage.store("memberId", pollingOrderMember.memberId.toString())
                        SecureStorage.store("pollingOrder", pollingOrderMember.pollingOrder.toString())
                        SecureStorage.store("active", pollingOrderMember.active.toString())
                        SecureStorage.store("accessToken", encryptedAccessToken)
                        SecureStorage.store("isOrderAdmin", pollingOrderMember.isOrderAdmin.toString())

                        // Update the PollingOrderMemberRepository with the new member data
                        PollingOrderMemberRepository.setPollingOrderMember(pollingOrderMember)

                        // Update the PollingOrderRepository with the stored polling order name
                        val storedPollingOrderName = UserUtils.getStoredPollingOrderName()
                        if (!storedPollingOrderName.isNullOrEmpty()) {
                            PollingOrderRepository.updatePollingOrderName(storedPollingOrderName)
                        }

                        Toast.makeText(context, "Login successful: ${UserUtils.decryptData(encryptedName)}", Toast.LENGTH_SHORT).show()
                        callback(true)
                    } else {
                        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(context, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            })
        } else {
            Toast.makeText(context, "Please enter all authentication information", Toast.LENGTH_SHORT).show()
            callback(false)
        }
    }

    fun signOut() {
        // First, clear all LiveData in repositories to prevent data leakage between sessions
        LiveDataCleaner.clearAllLiveData()

        // Then clear all stored credentials and user data
        SecureStorage.clear()

        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
    }
}

object SecureStorage {
    private val storage = mutableMapOf<String, String>()

    fun store(key: String, value: String) {
        storage[key] = value
    }

    fun retrieve(key: String): String? {
        return storage[key]
    }

    fun clear() {
        storage.clear()
    }
}
