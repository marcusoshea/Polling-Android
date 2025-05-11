package com.pollingandroid.ui.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pollingandroid.util.UserUtils.isUserLoggedIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.pollingandroid.ui.components.TopAppBar
import com.pollingandroid.ui.theme.Gold
import com.pollingandroid.ui.theme.TextBoxBackground
import com.pollingandroid.ui.theme.PrimaryColor
import com.pollingandroid.ui.theme.SecondaryColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.pollingandroid.ui.theme.LinkBlue
import com.pollingandroid.ui.theme.TertiaryColor
import com.pollingandroid.ui.login.SecureStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController = rememberNavController(),
    profileViewModel: ProfileViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val pollingOrderName = profileViewModel.pollingOrderName.observeAsState("").value
    val memberInfo = profileViewModel.memberInfo.observeAsState()

    if (!isUserLoggedIn(context)) {
        LaunchedEffect(Unit) {
            navController.navigate("login")
        }
    } else {
        var name by remember { mutableStateOf(memberInfo.value?.name ?: "") }
        var email by remember { mutableStateOf(memberInfo.value?.email ?: "") }
        var active by remember { mutableStateOf(memberInfo.value?.active ?: false) }
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }

        // Track submission state
        var isSubmitting by remember { mutableStateOf(false) }

        // Helper function to sign out the user
        fun signOut() {
            // Clear SharedPreferences
            val sharedPreferences =
                context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            // Clear SecureStorage
            SecureStorage.clear()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = "$pollingOrderName Profile",
                    onMenuClick = onMenuClick
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(color = PrimaryColor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = PrimaryColor.copy(alpha = 0.5f))
                            .padding(20.dp)
                            .fillMaxWidth(.95f)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Name",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Enter your name", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                cursorColor = Color.White,
                                focusedContainerColor = TextBoxBackground,
                                unfocusedContainerColor = TextBoxBackground,
                                focusedIndicatorColor = PrimaryColor,
                                unfocusedIndicatorColor = PrimaryColor,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Email",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("Enter your email", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                cursorColor = Color.White,
                                focusedContainerColor = TextBoxBackground,
                                unfocusedContainerColor = TextBoxBackground,
                                focusedIndicatorColor = PrimaryColor,
                                unfocusedIndicatorColor = PrimaryColor,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = active,
                                onCheckedChange = { active = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LinkBlue,
                                    uncheckedColor = Color.Gray,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text("Active", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                profileViewModel.updateProfile(context, name, email, active)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LinkBlue)
                        ) {
                            Text("Update Profile", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = PrimaryColor.copy(alpha = 0.5f))
                            .padding(20.dp)
                            .fillMaxWidth(.95f)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Current Password",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        TextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            placeholder = { Text("Enter current password", color = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                cursorColor = Color.White,
                                focusedContainerColor = TextBoxBackground,
                                unfocusedContainerColor = TextBoxBackground,
                                focusedIndicatorColor = PrimaryColor,
                                unfocusedIndicatorColor = PrimaryColor,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "New Password",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        TextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            placeholder = { Text("Enter new password", color = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                cursorColor = Color.White,
                                focusedContainerColor = TextBoxBackground,
                                unfocusedContainerColor = TextBoxBackground,
                                focusedIndicatorColor = PrimaryColor,
                                unfocusedIndicatorColor = PrimaryColor,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // Prevent double submission
                                if (!isSubmitting) {
                                    isSubmitting = true
                                    try {
                                        profileViewModel.updatePassword(
                                            context,
                                            currentPassword,
                                            newPassword
                                        ) { success ->
                                            isSubmitting = false
                                            if (success) {
                                                // Navigate safely on the main thread
                                                android.os.Handler(android.os.Looper.getMainLooper())
                                                    .post {
                                                        // Clear password fields
                                                        currentPassword = ""
                                                        newPassword = ""
                                                        // Sign out and navigate to login
                                                        signOut()
                                                        navController.navigate("login") {
                                                            // Clear back stack so user can't go back
                                                            popUpTo("home") { inclusive = true }
                                                        }
                                                    }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isSubmitting = false
                                    }
                                }
                            },
                            enabled = !isSubmitting && currentPassword.length >= 6 && newPassword.length >= 6,
                            colors = ButtonDefaults.buttonColors(containerColor = LinkBlue)
                        ) {
                            if (isSubmitting) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Changing...", color = Color.White)
                                }
                            } else {
                                Text("Change Password", color = Color.White)
                            }
                        }
                    }
                }
            }
        )
    }
}