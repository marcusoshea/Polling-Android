package com.polling_android.ui.login

import android.content.Context
import android.widget.Toast

class LoginHandler(private val context: Context) {

    fun handleLogin(email: String, password: String) {
        // Add your login logic here
        if (email.isNotEmpty() && password.isNotEmpty()) {
            // Perform login operation
            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }
}