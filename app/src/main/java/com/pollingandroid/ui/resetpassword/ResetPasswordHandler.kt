package com.pollingandroid.ui.resetpassword

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.ResetPassword
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import android.app.AlertDialog
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class ResetPasswordHandler(private val context: Context) {

    @SuppressLint("SimpleDateFormat")
    fun handleResetPassword(email: String, password: String, pollingOrder: Int, callback: (logSuccess: Boolean) -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty() && pollingOrder != 0) {
            val resetPassword = ResetPassword(email, password, pollingOrder)
            RetrofitInstance.api.resetPassword(resetPassword).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val builder = AlertDialog.Builder(context)
                        val message = TextView(context).apply {
                            text = "Reset Password successful"
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
                        Toast.makeText(context, "Reset Password failed", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(context, "Reset Password failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            })
        } else {
            Toast.makeText(context, "Please enter all Reset Password information", Toast.LENGTH_SHORT).show()
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