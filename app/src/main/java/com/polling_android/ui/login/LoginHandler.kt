package com.polling_android.ui.login

import android.content.Context
import android.widget.Toast
import com.polling_android.api.RetrofitInstance
import com.polling_android.model.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginHandler(private val context: Context) {

    fun handleLogin(email: String, password: String, pollingOrder: Int) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            val loginRequest = LoginRequest(email, password, pollingOrder)
            RetrofitInstance.api.login(loginRequest).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }
}