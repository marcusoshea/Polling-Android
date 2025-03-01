package com.pollingandroid.ui.registration

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.PollingOrder
import com.pollingandroid.ui.theme.PrimaryColor
import com.pollingandroid.ui.theme.SecondaryColor
import com.pollingandroid.ui.theme.TertiaryColor
import com.pollingandroid.ui.theme.Black
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.pollingandroid.util.UserUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    registrationHandler: RegistrationHandler,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    registrationViewModel: RegistrationViewModel = viewModel()
) {
    val context = LocalContext.current
    val name by registrationViewModel.name.observeAsState("")
    val email by registrationViewModel.email.observeAsState("")
    val password by registrationViewModel.password.observeAsState("")
    val pollingOrders by registrationViewModel.pollingOrders.observeAsState(emptyList())
    val selectedPollingOrder by registrationViewModel.selectedPollingOrder.observeAsState(null)
    val isLoading by registrationViewModel.isLoading.observeAsState(false)
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchPollingOrders { orders ->
            registrationViewModel.setPollingOrders(orders)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(color = PrimaryColor)) {
        TopAppBar(
            title = { Text("Welcome to ÆPolling") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = SecondaryColor
            )
        )
        Column(modifier = modifier.padding(30.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = name,
                onValueChange = { registrationViewModel.setName(it) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { registrationViewModel.setEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { registrationViewModel.setPassword(it) },
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
                                registrationViewModel.setSelectedPollingOrder(order)
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
                    registrationViewModel.setLoading(true)
                    registrationHandler.handleRegistration(
                        name,
                        email,
                        password,
                        selectedPollingOrder?.polling_order_id ?: 0
                    ) { logSuccess ->
                        registrationViewModel.setLoading(false)
                        if (logSuccess) {
                            navController.navigate("home")
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Login")
                }
            }
        }
    }
}

private fun fetchPollingOrders(callback: (List<PollingOrder>) -> Unit) {
    RetrofitInstance.api.getPollingOrders().enqueue(object : Callback<List<PollingOrder>> {
        override fun onResponse(call: Call<List<PollingOrder>>, response: Response<List<PollingOrder>>) {
            if (response.isSuccessful) {
                val orders = response.body()?.sortedBy { it.polling_order_name }
                if (orders.isNullOrEmpty()) {
                    Log.w("RegistrationScreen", "Received empty or null polling orders")
                }
                callback(orders ?: emptyList())
            } else {
                Log.e("RegistrationScreen", "Error fetching polling orders: ${response.code()} - ${response.message()}")
                callback(emptyList())
            }
        }

        override fun onFailure(call: Call<List<PollingOrder>>, t: Throwable) {
            Log.e("RegistrationScreen", "Failed to fetch polling orders", t)
            callback(emptyList())
        }
    })
}