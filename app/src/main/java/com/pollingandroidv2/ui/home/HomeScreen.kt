package com.pollingandroidv2.ui.home

import androidx.compose.foundation.layout.*
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
import com.polling_android.util.UserUtils.isUserLoggedIn
import com.pollingandroidv2.ui.home.HomeViewModel

@Composable
fun HomeScreen(navController: NavController = rememberNavController(), homeViewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current

    if (!isUserLoggedIn(context)) {
        LaunchedEffect(Unit) {
            navController.navigate("LoginScreen")
        }
    } else {
        val text by homeViewModel.text.observeAsState("")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = text)
        }
    }
}