package com.pollingandroidv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.pollingandroidv2.ui.login.LoginHandler
import com.pollingandroidv2.ui.login.LoginScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pollingandroidv2.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            Surface(color = MaterialTheme.colorScheme.background) {
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(navController = navController, loginHandler = LoginHandler(this@MainActivity))
                    }
                    composable("home") {
                        HomeScreen()
                    }
                }
            }
        }
    }
}