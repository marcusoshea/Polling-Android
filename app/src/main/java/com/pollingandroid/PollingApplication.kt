package com.pollingandroid

import android.app.Application
import com.pollingandroid.util.SecureStorage

class PollingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load persisted credentials/session into memory before any Activity or ViewModel reads them.
        SecureStorage.init(this)
    }
}
