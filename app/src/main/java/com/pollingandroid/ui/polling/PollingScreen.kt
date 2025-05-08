package com.pollingandroid.ui.polling

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
import com.pollingandroid.ui.theme.PrimaryColor
import com.pollingandroid.ui.theme.TertiaryColor
import androidx.compose.ui.text.font.FontStyle

@Composable
fun PollingScreen(
    navController: NavController = rememberNavController(),
    pollingViewModel: PollingViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val pollingOrderName = pollingViewModel.pollingOrderName.observeAsState("").value

    if (!isUserLoggedIn(context)) {
        LaunchedEffect(Unit) {
            navController.navigate("login")
        }
    } else {
        val text by pollingViewModel.text.observeAsState("")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = "$pollingOrderName Polling",
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
                            .background(color = TertiaryColor)
                            .padding(20.dp)
                            .fillMaxWidth(.95f)
                    ) {
                        Text(
                            text = text,
                            style = LocalTextStyle.current.copy(fontStyle = FontStyle.Italic)
                        )
                    }
                }
            }
        )
    }
}