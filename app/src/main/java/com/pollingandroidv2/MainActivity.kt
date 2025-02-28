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
import com.pollingandroidv2.ui.profile.ProfileScreen
import com.pollingandroidv2.ui.theme.PollingAndroidV2Theme
import com.pollingandroidv2.ui.theme.PrimaryColor
import com.pollingandroidv2.ui.theme.TertiaryColor
import com.pollingandroidv2.ui.theme.SecondaryColor
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
import com.pollingandroidv2.ui.candidates.CandidatesScreen
import com.pollingandroidv2.ui.polling.PollingScreen
import com.pollingandroidv2.ui.report.ReportScreen

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
                            Column(modifier = modifier
                                .padding( top = 80.dp)
                            ) {
                                Column(
                                    modifier = modifier
                                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                        .background(color = SecondaryColor)
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
                        }
                    }
                }
            }
        }
    }
}