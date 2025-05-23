package com.pollingandroid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.pollingandroid.ui.candidates.CandidatesScreen
import com.pollingandroid.ui.polling.PollingScreen
import com.pollingandroid.ui.report.ReportScreen
import com.pollingandroid.ui.requestresetpassword.RequestResetPasswordHandler
import com.pollingandroid.ui.requestresetpassword.RequestResetPasswordScreen
import com.pollingandroid.ui.resetpassword.ResetPasswordScreen
import com.pollingandroid.ui.theme.Black
import com.pollingandroid.ui.feedback.FeedbackScreen
import com.pollingandroid.ui.feedback.FeedbackHandler
import com.pollingandroid.util.Constants

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
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    drawerState.close()
                }
                val loginHandler = LoginHandler(this@MainActivity)
                val registrationHandler = RegistrationHandler(this@MainActivity)
                val feedbackHandler = FeedbackHandler(this@MainActivity)

                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                val showDrawer = currentRoute !in listOf(
                    "login",
                    "register",
                    "requestresetpassword",
                    "resetpassword"
                )

                val isDrawerOpen =
                    remember { derivedStateOf { drawerState.currentValue == DrawerValue.Open } }.value

                Surface(color = MaterialTheme.colorScheme.background) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        scrimColor = Color.Transparent,
                        drawerContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(if (isDrawerOpen) 0.40f else 0f)

                                    .alpha(if (isDrawerOpen) 1f else 0f)
                            ) {
                                if (showDrawer && isDrawerOpen) {
                                    Column(
                                        modifier = Modifier
                                            .padding(top = 80.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(
                                                    RoundedCornerShape(
                                                        topEnd = 16.dp,
                                                        bottomStart = 16.dp,
                                                        bottomEnd = 16.dp
                                                    )
                                                )
                                                .background(color = TertiaryColor)
                                                .padding(20.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    navController.navigate("home")
                                                    scope.launch { drawerState.close() }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                contentPadding = PaddingValues(vertical = 8.dp),
                                                border = null,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 0.dp,
                                                    pressedElevation = 0.dp
                                                )
                                            ) {
                                                Text(
                                                    text = "Home",
                                                    color = Black
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    navController.navigate("profile")
                                                    scope.launch { drawerState.close() }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                contentPadding = PaddingValues(vertical = 8.dp),
                                                border = null,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 0.dp,
                                                    pressedElevation = 0.dp
                                                )
                                            ) {
                                                Text(
                                                    text = "Profile",
                                                    color = Black
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    navController.navigate("polling")
                                                    scope.launch { drawerState.close() }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                contentPadding = PaddingValues(vertical = 8.dp),
                                                border = null,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 0.dp,
                                                    pressedElevation = 0.dp
                                                )
                                            ) {
                                                Text(
                                                    text = "Polling",
                                                    color = Black
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    navController.navigate("candidates")
                                                    scope.launch { drawerState.close() }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                contentPadding = PaddingValues(vertical = 8.dp),
                                                border = null,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 0.dp,
                                                    pressedElevation = 0.dp
                                                )
                                            ) {
                                                Text(
                                                    text = "Candidates",
                                                    color = Black
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    navController.navigate("report")
                                                    scope.launch { drawerState.close() }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                contentPadding = PaddingValues(vertical = 8.dp),
                                                border = null,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 0.dp,
                                                    pressedElevation = 0.dp
                                                )
                                            ) {
                                                Text(
                                                    text = "Polling Report",
                                                    color = Black
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    navController.navigate("feedback")
                                                    scope.launch { drawerState.close() }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                contentPadding = PaddingValues(vertical = 8.dp),
                                                border = null,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 0.dp,
                                                    pressedElevation = 0.dp
                                                )
                                            ) {
                                                Text(
                                                    text = "Feedback",
                                                    color = Black
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    loginHandler.signOut()
                                                    scope.launch { drawerState.close() }
                                                    navController.navigate("login") {
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                contentPadding = PaddingValues(vertical = 8.dp),
                                                border = null,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 0.dp,
                                                    pressedElevation = 0.dp
                                                )
                                            ) {
                                                Text(
                                                    text = "Sign Out",
                                                    color = Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        gesturesEnabled = showDrawer
                    ) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(
                                    navController = navController,
                                    loginHandler = loginHandler,
                                    onMenuClick = { }
                                )
                            }
                            composable("home") {
                                HomeScreen(
                                    navController = navController,
                                    onMenuClick = { if (showDrawer) scope.launch { drawerState.open() } }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    navController = navController,
                                    onMenuClick = { if (showDrawer) scope.launch { drawerState.open() } }
                                )
                            }
                            composable("polling") {
                                PollingScreen(
                                    navController = navController,
                                    onMenuClick = { if (showDrawer) scope.launch { drawerState.open() } }
                                )
                            }
                            composable("candidates") {
                                CandidatesScreen(
                                    navController = navController,
                                    onMenuClick = { if (showDrawer) scope.launch { drawerState.open() } }
                                )
                            }
                            composable("report") {
                                ReportScreen(
                                    navController = navController,
                                    onMenuClick = { if (showDrawer) scope.launch { drawerState.open() } }
                                )
                            }
                            composable("feedback") {
                                FeedbackScreen(
                                    navController = navController,
                                    onMenuClick = { if (showDrawer) scope.launch { drawerState.open() } },
                                    feedbackHandler = feedbackHandler
                                )
                            }
                            composable("register") {
                                RegistrationScreen(
                                    navController = navController,
                                    registrationHandler = registrationHandler,
                                    onMenuClick = { }
                                )
                            }
                            composable("requestresetpassword") {
                                RequestResetPasswordScreen(
                                    navController = navController,
                                    requestResetPasswordHandler = RequestResetPasswordHandler(this@MainActivity),
                                    onMenuClick = { }
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
                                    onMenuClick = { }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
