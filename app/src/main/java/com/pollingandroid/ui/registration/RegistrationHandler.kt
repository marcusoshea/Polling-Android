package com.pollingandroid.ui.registration

import android.content.Context
import android.widget.Toast
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.RegistrationRequest
import com.pollingandroid.util.UserUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

class RegistrationHandler(private val context: Context) {

    fun handleRegistration(name: String, email: String, password: String, pollingOrder: Int, callback: (logSuccess: Boolean) -> Unit) {
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && pollingOrder != 0) {
            val registrationRequest = RegistrationRequest(name, email, password, pollingOrder)
            RetrofitInstance.api.register(registrationRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()

                        Toast.makeText(context, "Registration successful: ${UserUtils.decryptData(name)}", Toast.LENGTH_SHORT).show()
                        callback(true)
                    } else {
                        Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(context, "Registration failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            })
        } else {
            Toast.makeText(context, "Please enter all Registration information", Toast.LENGTH_SHORT).show()
            callback(false)
        }
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