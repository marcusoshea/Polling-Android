package com.pollingandroid

import android.content.Intent
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
import com.pollingandroid.ui.home.HomeScreen
import com.pollingandroid.ui.login.LoginHandler
import com.pollingandroid.ui.login.LoginScreen
import com.pollingandroid.ui.profile.ProfileScreen
import com.pollingandroid.ui.registration.RegistrationScreen
import com.pollingandroid.ui.registration.RegistrationHandler
import com.pollingandroid.ui.theme.PollingAndroidTheme
import com.pollingandroid.ui.theme.PrimaryColor
import com.pollingandroid.ui.theme.TertiaryColor
import com.pollingandroid.ui.theme.SecondaryColor
import kotlinx.coroutines.launch
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.navigation.navArgument
import com.pollingandroid.ui.candidates.CandidatesScreen
import com.pollingandroid.ui.polling.PollingScreen
import com.pollingandroid.ui.report.ReportScreen
import com.pollingandroid.ui.requestresetpassword.RequestResetPasswordHandler
import com.pollingandroid.ui.requestresetpassword.RequestResetPasswordScreen
import com.pollingandroid.ui.resetpassword.ResetPasswordScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val intent = intent
            val action = intent.action
            val data = intent.data

            if (Intent.ACTION_VIEW == action && data != null) {
                val token = data.getQueryParameter("token")
                if (token != null) {
                    val navController = rememberNavController()
                    navController.navigate("resetpassword?token=$token")
                }
            }
            PollingAndroidTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val loginHandler = LoginHandler(this@MainActivity)
                val registrationHandler = RegistrationHandler(this@MainActivity)
                val modifier = Modifier
                Surface(color = MaterialTheme.colorScheme.background) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            Column(modifier = modifier
                                .padding( top = 80.dp)
                            ) {
                                Column(
                                    modifier = modifier
                                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                        .background(color = TertiaryColor)
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = "Home",
                                        modifier = Modifier.clickable {
                                            navController.navigate("home")
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                    HorizontalDivider(
                                        color = PrimaryColor,
                                        thickness = 1.dp,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth(.25f)
                                    )
                                    Text(
                                        text = "Profile",
                                        modifier = Modifier.clickable {
                                            navController.navigate("profile")
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                    HorizontalDivider(
                                        color = PrimaryColor,
                                        thickness = 1.dp,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth(.25f)
                                    )
                                    Text(
                                        text = "Polling",
                                        modifier = Modifier.clickable {
                                            navController.navigate("polling")
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                    HorizontalDivider(
                                        color = PrimaryColor,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(.25f)
                                    )
                                    Text(
                                        text = "Candidates",
                                        modifier = Modifier.clickable {
                                            navController.navigate("candidates")
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                    HorizontalDivider(
                                        color = PrimaryColor,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(.25f)
                                    )
                                    Text(
                                        text = "Report",
                                        modifier = Modifier.clickable {
                                            navController.navigate("report")
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                    HorizontalDivider(
                                        color = PrimaryColor,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(.25f)
                                    )
                                    val annotatedText = buildAnnotatedString {
                                        pushStringAnnotation(
                                            tag = "SignOut",
                                            annotation = "signOut"
                                        )
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
                            composable("profile") {
                                ProfileScreen(
                                    navController = navController,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("polling") {
                                PollingScreen(
                                    navController = navController,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("candidates") {
                                CandidatesScreen(
                                    navController = navController,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("report") {
                                ReportScreen(
                                    navController = navController,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("register") {
                                RegistrationScreen(
                                    navController = navController,
                                    registrationHandler = registrationHandler,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("requestresetpassword") {
                                RequestResetPasswordScreen(
                                    navController = navController,
                                    requestResetPasswordHandler = RequestResetPasswordHandler(this@MainActivity),
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable(
                                "resetpassword?token={token}",
                                arguments = listOf(navArgument("token") { defaultValue = "" })
                            ) {
                                val token = it.arguments?.getString("token") ?: ""
                                ResetPasswordScreen(
                                    navController = navController,
                                    token = token,
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