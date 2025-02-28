package com.polling_android.util

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.pollingandroidv2.ui.login.SecureStorage
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import com.pollingandroidv2.util.Constants

object UserUtils {

    private const val KEY_ALIAS = Constants.KEY_ALIAS
    private const val PREF_NAME = "secure_prefs"
    private const val MEMBER_NAME_KEY = "memberName"
    private const val ANDROID_KEY_STORE = Constants.ANDROID_KEY_STORE

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    init {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    fun getStoredPollingOrderName(): String? {
        return SecureStorage.retrieve("PollingOrderName")?.let { decryptData(it) }
    }

    fun encryptData(data: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(iv + encryptedData, Base64.DEFAULT)
    }

    fun decryptData(data: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val decodedData = Base64.decode(data, Base64.DEFAULT)
        val iv = decodedData.copyOfRange(0, 12)
        val encryptedData = decodedData.copyOfRange(12, decodedData.size)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encryptedData))
    }

    fun isUserLoggedIn(context: Context): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val encryptedMemberName = sharedPreferences.getString(MEMBER_NAME_KEY, null)
        return encryptedMemberName?.let { decryptData(it) } != null
    }
}