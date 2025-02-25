package com.pollingandroidv2.ui.signout

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.pollingandroidv2.ui.theme.PollingAndroidV2Theme

class SignOutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PollingAndroidV2Theme {
                SignOutScreen()
            }
        }
    }
}

@Composable
fun SignOutScreen() {
    val context = LocalContext.current
    val navController = rememberNavController()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            signOut(context)
            navController.navigate("LoginScreen")
        }) {
            Text("Sign Out")
        }
    }
}

private fun signOut(context: Context) {
    val sharedPreferences = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().apply()
}