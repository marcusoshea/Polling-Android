package com.pollingandroid.ui.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pollingandroid.ui.components.TopAppBar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    feedbackHandler: FeedbackHandler
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var message by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = "Feedback",
            onMenuClick = onMenuClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Send us your feedback",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null // Clear error when user types
                },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = errorMessage != null && name.text.isBlank()
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null // Clear error when user types
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = errorMessage != null && email.text.isBlank()
            )

            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                    errorMessage = null // Clear error when user types
                },
                label = { Text("Message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 4,
                enabled = !isLoading,
                isError = errorMessage != null && message.text.isBlank()
            )

            // Show error message if validation fails
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Button(
                onClick = {
                    // Validate fields
                    if (name.text.isBlank() || email.text.isBlank() || message.text.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
                        errorMessage = "Please enter a valid email address"
                        return@Button
                    }

                    scope.launch {
                        try {
                            isLoading = true
                            errorMessage = null

                            feedbackHandler.submitFeedback(
                                name = name.text.trim(),
                                email = email.text.trim(),
                                message = message.text.trim(),
                                onSuccess = {
                                    isSubmitted = true
                                    // Keep isLoading = true to maintain disabled state
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Feedback submitted successfully!")
                                        navController.popBackStack()
                                    }
                                },
                                onError = { error ->
                                    isLoading = false
                                    errorMessage = error
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Failed to submit feedback: $error")
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "An unexpected error occurred: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && !isSubmitted
            ) {
                if (isLoading || isSubmitted) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(if (isSubmitted) "Submitted!" else "Submitting...")
                    }
                } else {
                    Text("Submit Feedback")
                }
            }
        }
    }

    // Snackbar host
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(hostState = snackbarHostState)
    }
}
