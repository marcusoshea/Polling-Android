package com.pollingandroid.ui.requestresetpassword

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.ResetPasswordRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import android.app.AlertDialog
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class RequestResetPasswordHandler(private val context: Context) {

    @SuppressLint("SimpleDateFormat")
    fun handleRequestResetPassword(email: String, pollingOrder: Int, callback: (logSuccess: Boolean) -> Unit) {
        if (email.isNotEmpty() && pollingOrder != 0) {
            val today = java.util.Date()
            val created = SimpleDateFormat("yyyy-MM-dd").format(today)
            val resetPasswordRequest = ResetPasswordRequest(email, pollingOrder)
            RetrofitInstance.api.requestResetPassword(resetPasswordRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val builder = AlertDialog.Builder(context)
                        val message = TextView(context).apply {
                            text = "Request Reset Password successful: \n\nYou will recieve email instructions if your account exists"
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
                        Toast.makeText(context, "Request Reset Password failed", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(context, "Request Reset Password failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            })
        } else {
            Toast.makeText(context, "Please enter all Request Reset Password information", Toast.LENGTH_SHORT).show()
            callback(false)
        }
    }

}
