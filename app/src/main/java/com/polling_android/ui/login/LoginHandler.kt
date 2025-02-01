package com.polling_android.ui.login

import android.content.Context
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.polling_android.api.RetrofitInstance
import com.polling_android.model.LoginRequest
import com.polling_android.model.PollingOrderMember
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

class LoginHandler(private val context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun handleLogin(email: String, password: String, pollingOrder: Int, callback: (logSuccess: Boolean) -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            val loginRequest = LoginRequest(email, password, pollingOrder)
            RetrofitInstance.api.login(loginRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        val pollingOrderMember = Gson().fromJson(responseBody, PollingOrderMember::class.java)
                        // Store the response body in EncryptedSharedPreferences
                        sharedPreferences.edit().putString("memberName", pollingOrderMember.name).apply()
                        sharedPreferences.edit().putString("accessToken", pollingOrderMember.access_token).apply()
                        sharedPreferences.edit().putBoolean("isOrderAdmin", pollingOrderMember.isOrderAdmin).apply()
                        sharedPreferences.edit().putInt("pollingOrder", pollingOrderMember.pollingOrder).apply()
                        sharedPreferences.edit().putInt("memberId", pollingOrderMember.memberId).apply()
                        sharedPreferences.edit().putString("email", pollingOrderMember.email).apply()
                        sharedPreferences.edit().putBoolean("active", pollingOrderMember.active).apply()

                        Toast.makeText(context, "Login successful: ${sharedPreferences.getString("memberName", null)}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            callback(false)
        }
    }
}
