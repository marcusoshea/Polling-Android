package com.pollingandroid.ui.profile

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
import com.pollingandroid.ui.components.TopAppBar
import com.pollingandroid.ui.theme.Gold
import com.pollingandroid.ui.theme.TextBoxBackground
import com.pollingandroid.ui.theme.PrimaryColor
import androidx.compose.ui.text.font.FontStyle

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
                            .background(color = Gold)
                            .padding(20.dp)
                            .fillMaxWidth(.95f)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = TextBoxBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = TextBoxBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = active,
                                onCheckedChange = { active = it }
                            )
                            Text("Active")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            profileViewModel.updateProfile(context, name, email, active)
                        }) {
                            Text("Update Profile")
                        }
                    }
                }
            }
        )
    }
}