package com.pollingandroidv2.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.polling_android.api.RetrofitInstance
import com.polling_android.model.PollingOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(navController: NavController, loginHandler: LoginHandler, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pollingOrders by remember { mutableStateOf(emptyList<PollingOrder>()) }
    var selectedPollingOrder by remember { mutableStateOf<PollingOrder?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchPollingOrders { orders ->
            pollingOrders = orders
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { expanded = true }) {
            Text(text = selectedPollingOrder?.toString() ?: "Select Polling Order")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            pollingOrders.forEach { order ->
                DropdownMenuItem(onClick = {
                    selectedPollingOrder = order
                    expanded = false
                }, text = { Text(order.toString()) })
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                isLoading = true
                loginHandler.handleLogin(email, password, selectedPollingOrder?.polling_order_id ?: 0) { logSuccess ->
                    isLoading = false
                    if (logSuccess) {
                        navController.navigate("home")
                    }
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Login")
            }
        }
    }
}

private fun fetchPollingOrders(callback: (List<PollingOrder>) -> Unit) {
    RetrofitInstance.api.getPollingOrders().enqueue(object : Callback<List<PollingOrder>> {
        override fun onResponse(call: Call<List<PollingOrder>>, response: Response<List<PollingOrder>>) {
            if (response.isSuccessful) {
                callback(response.body() ?: emptyList())
            }
        }

        override fun onFailure(call: Call<List<PollingOrder>>, t: Throwable) {
            callback(emptyList())
        }
    })
}