package com.pollingandroid.ui.profile

import android.content.Context
import android.widget.Toast
import com.pollingandroid.api.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileHandler(private val context: Context) {

    fun updateProfile(memberId: Int, memberName: String, memberEmail: String, memberOrderId: Int, active: Boolean, accessToken: String, callback: (Boolean) -> Unit) {
        val reqHeader = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${accessToken.replace("\n", "").trim()}"
        )
        val body = mapOf(
            "name" to memberName,
            "email" to memberEmail,
            "polling_order_member_id" to memberId.toString(),
            "polling_order_id" to memberOrderId.toString(),
            "active" to active.toString(),
            "authToken" to accessToken.replace("\n", "").trim(),
            "approved" to "1",
            "removed" to "0"
        )
        RetrofitInstance.api.updateProfile(memberId, body, reqHeader).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    callback(true)
                } else {
                    Toast.makeText(context, "Profile update failed", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Profile update failed: ${t.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }
}