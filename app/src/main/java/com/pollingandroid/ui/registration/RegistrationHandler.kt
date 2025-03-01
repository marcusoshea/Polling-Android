package com.pollingandroid.ui.registration

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.RegistrationRequest
import com.pollingandroid.util.UserUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import android.app.AlertDialog
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class RegistrationHandler(private val context: Context) {

    @SuppressLint("SimpleDateFormat")
    fun handleRegistration(name: String, email: String, password: String, pollingOrder: Int, callback: (logSuccess: Boolean) -> Unit) {
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && pollingOrder != 0) {
            val today = java.util.Date()
            val created = SimpleDateFormat("yyyy-MM-dd").format(today)
            val registrationRequest = RegistrationRequest(name, email, password, pollingOrder, created.toString())
            RetrofitInstance.api.register(registrationRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val builder = AlertDialog.Builder(context)
                        val message = TextView(context).apply {
                            text = "Registration successful: $name\n\nYou must be approved by an admin prior to being granted access"
                            setPadding(50, 30, 50, 30)
                            gravity = Gravity.CENTER
                            textSize = 16f
                        }
                        builder.setView(LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL
                            addView(message)
                            setPadding(20, 20, 20, 20)
                        })
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
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