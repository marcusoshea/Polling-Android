package com.pollingandroid.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

private val Context.secureDataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_prefs")

/**
 * Single, persistent credential/session store.
 *
 * Values survive process death (backed by Jetpack DataStore on disk), replacing the four
 * in-memory `mutableMapOf` copies that used to live in the login/registration/reset handlers.
 * Sensitive values (name, email, access token) are already Android Keystore AES/GCM encrypted by
 * [UserUtils] before they reach here, so ciphertext — not plaintext — is what lands on disk.
 *
 * The public API stays synchronous so existing callers are unchanged: an in-memory cache is
 * hydrated once from disk in [init], reads come from the cache, and writes are cached immediately
 * then flushed to DataStore in call order on a single-threaded IO context.
 */
object SecureStorage {
    private val cache = ConcurrentHashMap<String, String>()

    // limitedParallelism(1) serializes disk writes so store()/clear() persist in the order called.
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private var dataStore: DataStore<Preferences>? = null

    /** Hydrate the in-memory cache from disk. Call once, before any read, from Application.onCreate. */
    fun init(context: Context) {
        if (dataStore != null) return
        val ds = context.applicationContext.secureDataStore
        dataStore = ds
        runBlocking {
            ds.data.first().asMap().forEach { (key, value) ->
                cache[key.name] = value.toString()
            }
        }
    }

    fun store(key: String, value: String) {
        cache[key] = value
        val ds = dataStore ?: return
        scope.launch { ds.edit { it[stringPreferencesKey(key)] = value } }
    }

    fun retrieve(key: String): String? = cache[key]

    fun clear() {
        cache.clear()
        val ds = dataStore ?: return
        scope.launch { ds.edit { it.clear() } }
    }
}
