package com.pollingandroidv2.ui.login

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Toast
import com.google.gson.Gson
import com.polling_android.api.RetrofitInstance
import com.polling_android.model.LoginRequest
import com.polling_android.model.PollingOrderMember
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class LoginHandler(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    //TODO: Prior to releasing to production replace the keyAlias with your own key alias!!
    private val keyAlias = "MyKeyAlias"

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    private fun encryptData(data: String): String {
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(iv + encryptedData, Base64.DEFAULT)
    }

    private fun decryptData(data: String): String {
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val decodedData = Base64.decode(data, Base64.DEFAULT)
        val iv = decodedData.copyOfRange(0, 12)
        val encryptedData = decodedData.copyOfRange(12, decodedData.size)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encryptedData))
    }

    fun handleLogin(email: String, password: String, pollingOrder: Int, callback: (logSuccess: Boolean) -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty() && pollingOrder != 0) {
            val loginRequest = LoginRequest(email, password, pollingOrder)
            RetrofitInstance.api.login(loginRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        val pollingOrderMember = Gson().fromJson(responseBody, PollingOrderMember::class.java)
                        // Store the response body in SharedPreferences with encryption
                        sharedPreferences.edit().putString("memberName", encryptData(pollingOrderMember.name)).apply()
                        sharedPreferences.edit().putString("accessToken", encryptData(pollingOrderMember.access_token)).apply()
                        sharedPreferences.edit().putBoolean("isOrderAdmin", pollingOrderMember.isOrderAdmin).apply()
                        sharedPreferences.edit().putInt("pollingOrder", pollingOrderMember.pollingOrder).apply()
                        sharedPreferences.edit().putInt("memberId", pollingOrderMember.memberId).apply()
                        sharedPreferences.edit().putString("email", encryptData(pollingOrderMember.email)).apply()
                        sharedPreferences.edit().putBoolean("active", pollingOrderMember.active).apply()

                        Toast.makeText(context, "Login successful: ${decryptData(sharedPreferences.getString("memberName", "")!!)}", Toast.LENGTH_SHORT).show()
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
        sharedPreferences.edit().clear().apply()
        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
    }
}