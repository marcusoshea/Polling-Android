package com.pollingandroidv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pollingandroidv2.ui.home.HomeScreen
import com.pollingandroidv2.ui.login.LoginHandler
import com.pollingandroidv2.ui.login.LoginScreen
import com.pollingandroidv2.ui.theme.PollingAndroidV2Theme
import com.pollingandroidv2.ui.theme.PrimaryColor
import com.pollingandroidv2.ui.theme.TertiaryColor
import kotlinx.coroutines.launch
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.clickable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PollingAndroidV2Theme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val loginHandler = LoginHandler(this@MainActivity)
                val modifier = Modifier
                Surface(color = MaterialTheme.colorScheme.background) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            Column(modifier = modifier.background(color = PrimaryColor)) {
                                Text(text = "Home")
                                Text(text = "Profile")
                                Text(text = "Pollings")
                                Text(text = "Candidates")
                                Text(text = "Report")
                                val annotatedText = buildAnnotatedString {
                                    pushStringAnnotation(tag = "SignOut", annotation = "signOut")
                                    withStyle(style = SpanStyle(color = TertiaryColor)) {
                                        append("Sign Out")
                                    }
                                    pop()
                                }
                                Text(
                                    text = annotatedText,
                                    modifier = Modifier.clickable {
                                        loginHandler.signOut()
                                        scope.launch { drawerState.close() }
                                        navController.navigate("login") {
                                        }
                                    }
                                )
                            }
                        }
                    ) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(
                                    navController = navController,
                                    loginHandler = loginHandler,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("home") {
                                HomeScreen(
                                    navController = navController,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}