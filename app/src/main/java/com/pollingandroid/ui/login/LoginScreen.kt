package com.pollingandroid.ui.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.PollingOrder
import com.pollingandroid.ui.theme.PrimaryColor
import com.pollingandroid.ui.theme.SecondaryColor
import com.pollingandroid.ui.theme.TertiaryColor
import com.pollingandroid.ui.theme.Black
import com.pollingandroid.ui.theme.LinkBlue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.PopupProperties
import com.pollingandroid.util.PollingOrderUtils
import com.pollingandroid.util.UserUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    loginHandler: LoginHandler,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val email by loginViewModel.email.observeAsState("")
    val password by loginViewModel.password.observeAsState("")
    val pollingOrders by loginViewModel.pollingOrders.observeAsState(emptyList())
    val selectedPollingOrder by loginViewModel.selectedPollingOrder.observeAsState(null)
    val isLoading by loginViewModel.isLoading.observeAsState(false)
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PollingOrderUtils.fetchPollingOrders { orders ->
            loginViewModel.setPollingOrders(orders)
        }
    }

    Column(modifier = modifier
        .fillMaxSize()
        .background(color = PrimaryColor)) {
        TopAppBar(
            title = { Text("Welcome to ÆPolling") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = SecondaryColor
            )
        )
        Column(
            modifier = modifier
                .weight(1f)
                .padding(horizontal = 30.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { loginViewModel.setEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { loginViewModel.setPassword(it) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TertiaryColor,
                        contentColor = Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedPollingOrder?.toString() ?: "Select Polling Order"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(MaterialTheme.colorScheme.surface),
                    properties = PopupProperties(focusable = true)
                ) {
                    pollingOrders.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.toString()) },
                            onClick = {
                                loginViewModel.setSelectedPollingOrder(order)
                                expanded = false
                                val encryptedOrderName = UserUtils.encryptData(order.toString())
                                SecureStorage.store("PollingOrderName", encryptedOrderName)

                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    loginViewModel.setLoading(true)
                    loginHandler.handleLogin(
                        email,
                        password,
                        selectedPollingOrder?.polling_order_id ?: 0
                    ) { logSuccess ->
                        loginViewModel.setLoading(false)
                        if (logSuccess) {
                            navController.navigate("home")
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LinkBlue),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = { navController.navigate("register") },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text("Register")
                }
                TextButton(
                    onClick = { navController.navigate("requestresetpassword") },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text("Forgot Password")
                }
            }
        }
    }
}
