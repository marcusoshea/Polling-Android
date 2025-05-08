package com.pollingandroid.ui.requestresetpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import com.pollingandroid.ui.theme.PrimaryColor
import com.pollingandroid.ui.theme.SecondaryColor
import com.pollingandroid.ui.theme.TertiaryColor
import com.pollingandroid.ui.theme.Black
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.pollingandroid.ui.theme.LinkBlue
import com.pollingandroid.util.UserUtils
import com.pollingandroid.util.PollingOrderUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestResetPasswordScreen(
    navController: NavController,
    requestResetPasswordHandler: RequestResetPasswordHandler,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    requestResetPasswordViewModel: RequestResetPasswordViewModel = viewModel()
) {
    val context = LocalContext.current
    val email by requestResetPasswordViewModel.email.observeAsState("")
    val pollingOrders by requestResetPasswordViewModel.pollingOrders.observeAsState(emptyList())
    val selectedPollingOrder by requestResetPasswordViewModel.selectedPollingOrder.observeAsState(null)
    val isLoading by requestResetPasswordViewModel.isLoading.observeAsState(false)
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PollingOrderUtils.fetchPollingOrders { orders ->
            requestResetPasswordViewModel.setPollingOrders(orders)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(color = PrimaryColor)) {
        TopAppBar(
            title = { Text("Request Reset Password") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = SecondaryColor
            )
        )
        Column(modifier = modifier.padding(30.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Login",
                tint = Color.White,
                modifier = Modifier
                    .background(color = TertiaryColor, shape = CircleShape)
                    .padding(8.dp)
                    .clickable { navController.navigate("login") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { requestResetPasswordViewModel.setEmail(it) },
                label = { Text("Email") },
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
                                requestResetPasswordViewModel.setSelectedPollingOrder(order)
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
                    requestResetPasswordViewModel.setLoading(true)
                    requestResetPasswordHandler.handleRequestResetPassword(
                        email,
                        selectedPollingOrder?.polling_order_id ?: 0
                    ) { logSuccess ->
                        requestResetPasswordViewModel.setLoading(false)
                        if (logSuccess) {
                            navController.navigate("login")
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
                    Text("Request Reset Password")
                }
            }
        }
    }
}