package com.polling_android.ui.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pollingandroidv2.ui.home.HomeScreen
import com.pollingandroidv2.ui.theme.PollingAndroidV2Theme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PollingAndroidV2Theme {
                HomeScreen(
                    onMenuClick = { /* Empty lambda or add your callback implementation here */ }
                )
            }
        }
    }
}