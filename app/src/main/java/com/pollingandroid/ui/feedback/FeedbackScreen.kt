package com.pollingandroid.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pollingandroid.ui.components.TopAppBar
import com.pollingandroid.ui.login.SecureStorage
import com.pollingandroid.ui.theme.PrimaryColor
import com.pollingandroid.ui.theme.TertiaryColor
import com.pollingandroid.util.UserUtils
import kotlinx.coroutines.launch

@Composable
fun FeedbackScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    feedbackHandler: FeedbackHandler
) {
    var message by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Get user info from storage
    val userName = remember {
        SecureStorage.retrieve("memberName")?.let { encryptedName ->
            try {
                UserUtils.decryptData(encryptedName)
            } catch (e: Exception) {
                "Unknown User"
            }
        } ?: "Unknown User"
    }

    val userEmail = remember {
        SecureStorage.retrieve("email")?.let { encryptedEmail ->
            try {
                UserUtils.decryptData(encryptedEmail)
            } catch (e: Exception) {
                "unknown@email.com"
            }
        } ?: "unknown@email.com"
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(PrimaryColor)
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
                text = buildAnnotatedString {
                    append("Thank you for providing feedback about the polling application.\n")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, fontSize = 16.sp)) {
                        append("Note: this form is NOT for candidate feedback.")
                    }
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Display user info (non-editable)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = TertiaryColor
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                        .background(TertiaryColor)
                ) {
                    Text(
                        text = "Feedback from:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                    errorMessage = null // Clear error when user types
                },
                label = { Text("Your Message") },
                placeholder = { Text("Tell us what you think...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.White)
                ,
                minLines = 6,
                enabled = !isLoading && !isSubmitted,
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
                    // Validate message field
                    if (message.text.isBlank()) {
                        errorMessage = "Please enter your feedback message"
                        return@Button
                    }

                    scope.launch {
                        try {
                            isLoading = true
                            errorMessage = null

                            feedbackHandler.submitFeedback(
                                name = userName,
                                email = userEmail,
                                message = message.text.trim(),
                                onSuccess = {
                                    isSubmitted = true
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
                enabled = !isLoading && !isSubmitted && message.text.isNotBlank()
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
