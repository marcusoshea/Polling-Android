package com.pollingandroid.ui.polling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import com.pollingandroid.ui.theme.BeigeLightBackground
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pollingandroid.model.Polling
import com.pollingandroid.ui.candidates.models.Candidate
import com.pollingandroid.ui.polling.models.PollingState
import com.pollingandroid.ui.polling.models.PollingMember
import com.pollingandroid.ui.polling.models.CandidateVote
import com.pollingandroid.util.UserUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pollingandroid.ui.login.SecureStorage
import com.pollingandroid.ui.theme.Black
import com.pollingandroid.ui.theme.LinkBlue
import com.pollingandroid.ui.theme.Red

@Composable
fun PollingScreen(
    navController: NavController = rememberNavController(),
    pollingViewModel: PollingViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    // Use direct values from LiveData
    val pollingOrderName = pollingViewModel.pollingOrderName.observeAsState("").value
    val pollingState = pollingViewModel.state.observeAsState(initial = PollingState.LOADING).value
    val currentPolling = pollingViewModel.currentPolling.observeAsState(null).value
    val candidates = pollingViewModel.candidates.observeAsState(initial = emptyList()).value
    val errorMessage = pollingViewModel.errorMessage.observeAsState(null).value
    val orderMembers = pollingViewModel.orderMembers.observeAsState(initial = emptyList()).value
    val selectedMember = pollingViewModel.selectedMember.observeAsState(null).value
    val candidateVotes = pollingViewModel.candidateVotes.observeAsState(initial = emptyList()).value

    if (!isUserLoggedIn(context)) {
        LaunchedEffect(Unit) {
            navController.navigate("login")
        }
    } else {
        // Load data when screen launches
        LaunchedEffect(Unit) {
            // Use the correct key "accessToken" instead of "token"
            val encryptedToken = SecureStorage.retrieve("accessToken")

            val authToken = UserUtils.decryptData(encryptedToken ?: "") ?: ""
            val pollingOrderId = SecureStorage.retrieve("pollingOrder")?.toIntOrNull() ?: 0

            if (authToken.isBlank()) {
            }

            if (pollingOrderId == 0) {
            }

            pollingViewModel.loadCurrentPollingData(pollingOrderId, authToken)
        }

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

                    when (pollingState) {
                        PollingState.LOADING -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Gold)
                            }
                        }

                        PollingState.ERROR -> {
                            ErrorDisplay(errorMessage ?: "Unknown error occurred")
                        }

                        PollingState.LOADED -> {
                            val pollingData = currentPolling
                            if (pollingData == null) {
                                NoActivePolling()
                            } else {
                                ActivePollingContent(
                                    polling = pollingData,
                                    candidates = candidates,
                                    candidateVotes = candidateVotes,
                                    orderMembers = orderMembers,
                                    selectedMember = selectedMember,
                                    onMemberSelected = { memberId ->
                                        pollingViewModel.selectMember(memberId)
                                    },
                                    onUpdateVotes = { updatedVotes, isCompleted, callback ->
                                        val encryptedToken = SecureStorage.retrieve("accessToken")
                                        val authToken =
                                            UserUtils.decryptData(encryptedToken ?: "") ?: ""

                                        pollingViewModel.updateVotes(
                                            updatedVotes,
                                            isCompleted,
                                            authToken
                                        ) {
                                            callback()
                                        }
                                    },
                                    pollingViewModel = pollingViewModel
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ErrorDisplay(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = TertiaryColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                color = Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = Black
            )
        }
    }
}

@Composable
private fun NoActivePolling() {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = TertiaryColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Active Polling",
                style = MaterialTheme.typography.headlineSmall,
                color = Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "There is currently no active polling session for your order.",
                textAlign = TextAlign.Center,
                color = Black
            )
        }
    }
}

@Composable
private fun ActivePollingContent(
    polling: Polling,
    candidates: List<Candidate>,
    candidateVotes: List<CandidateVote>,
    orderMembers: List<PollingMember>,
    selectedMember: PollingMember?,
    onMemberSelected: (Int) -> Unit,
    onUpdateVotes: (List<CandidateVote>, Boolean, () -> Unit) -> Unit,
    pollingViewModel: PollingViewModel
) {
    val context = LocalContext.current

    // Create mutable state for votes
    val votes = remember(candidateVotes) {
        candidateVotes.associateBy { it.candidateId }.toMutableMap()
    }

    // Track button state and success message
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var isUpdate by remember { mutableStateOf(false) }
    var isDraft by remember { mutableStateOf(false) }

    // Handle success message display
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            // Auto-hide success message after 3 seconds
            kotlinx.coroutines.delay(3000L)  // Explicit long notation for clarity
            showSuccessMessage = false
        }
    }

    // Convert map back to list for submission
    fun getVotesList(): List<CandidateVote> {
        return votes.values.toList()
    }

    // Check if votes have already been submitted (at least one vote has pn_created_at set)
    val hasSubmittedVotes = candidateVotes.any {
        it.pollingNotesId > 0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Polling Title and Date
        Text(
            text = polling.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BeigeLightBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Polling Dates: ${formatDate(polling.startDate)} thru ${formatDate(polling.endDate)}",
            style = MaterialTheme.typography.bodyMedium,
            color = BeigeLightBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Proxy Voting Dropdown
        if (orderMembers.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }

            Text(
                text = "Order Clerk Proxy Vote As:",
                style = MaterialTheme.typography.bodyMedium,
                color = BeigeLightBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    colors = CardDefaults.cardColors(containerColor = TertiaryColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMember?.name ?: "Select Member",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Black
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Member",
                            tint = Black
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(MaterialTheme.colorScheme.surface),
                    properties = PopupProperties(focusable = true)
                ) {
                    // Add a message when there are no members
                    if (orderMembers.isEmpty()) {
                        DropdownMenuItem(
                            onClick = { expanded = false },
                            text = { Text("No members available", color = BeigeLightBackground) }
                        )
                    }

                    orderMembers.forEach { member ->
                        DropdownMenuItem(
                            onClick = {
                                onMemberSelected(member.id)
                                expanded = false
                            },
                            text = {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BeigeLightBackground
                                )
                            }
                        )
                    }
                }
            }
        }

        // Voting Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TertiaryColor)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Add a title above the table headers
                Text(
                    text = "Vote on Candidates",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    color = Black
                )

                // Remove the old header row and divider since headers are now in each card
                Divider(color = Color.Gray)

                // Candidate rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(candidates) { candidate ->
                        val candidateVote = votes[candidate.candidate_id] ?: CandidateVote(
                            candidateId = candidate.candidate_id,
                            candidateName = candidate.name
                        ).also { votes[candidate.candidate_id] = it }

                        CandidateVoteRow(
                            candidate = candidate,
                            candidateVote = candidateVote,
                            onVoteChange = { vote ->
                                candidateVote.vote = vote
                                votes[candidate.candidate_id] = candidateVote
                            },
                            onNoteChange = { note ->
                                candidateVote.note = note
                                votes[candidate.candidate_id] = candidateVote
                            },
                            onPrivateChange = { isPrivate ->
                                candidateVote.isPrivate = isPrivate
                                votes[candidate.candidate_id] = candidateVote
                            }
                        )
                    }
                }

                // Success message
                AnimatedVisibility(
                    visible = showSuccessMessage,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50) // Green color for success
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = when {
                                isDraft -> "Draft saved successfully"
                                isUpdate && hasSubmittedVotes -> "Submitted votes updated successfully"
                                else -> "Votes submitted successfully"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Submit button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    // Save Draft button
                    if (!hasSubmittedVotes) {
                        Button(
                            onClick = {
                                isSubmitting = true
                                isUpdate = false
                                isDraft = true
                                onUpdateVotes(getVotesList(), false) {
                                    isSubmitting = false
                                    showSuccessMessage = true
                                }
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubmitting) Color.Gray else LinkBlue
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = BeigeLightBackground
                                )
                            } else {
                                Text("Save Draft", color = BeigeLightBackground)
                            }
                        }
                    }

                    // Submit Polling Vote button
                    Button(
                        onClick = {
                            isSubmitting = true
                            isUpdate = true
                            isDraft = false
                            onUpdateVotes(getVotesList(), true) {
                                isSubmitting = false
                                showSuccessMessage = true
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubmitting) Color.Gray else if (hasSubmittedVotes) Gold else Gold
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = BeigeLightBackground
                            )
                        } else {
                            Text(
                                if (hasSubmittedVotes) "Update Your Submitted Polling Vote" else "Submit Polling Vote",
                                color = if (hasSubmittedVotes) BeigeLightBackground else PrimaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CandidateVoteRow(
    candidate: Candidate,
    candidateVote: CandidateVote,
    onVoteChange: (Int?) -> Unit,
    onNoteChange: (String) -> Unit,
    onPrivateChange: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var note by remember { mutableStateOf(candidateVote.note) }

    // Update the note in the candidateVote when it changes locally
    LaunchedEffect(note) {
        if (note != candidateVote.note) {
            onNoteChange(note)
        }
    }

    // Update local state when candidateVote changes
    LaunchedEffect(candidateVote.note) {
        if (candidateVote.note != note) {
            note = candidateVote.note
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Line 1: Candidate name
            Text(
                text = candidate.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TertiaryColor.copy(alpha = 0.3f))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Line 2: Note field
            Text(
                text = "Your Note:",
                style = MaterialTheme.typography.bodyMedium,
                color = Black.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 5,
                enabled = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                textStyle = TextStyle(color = Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Black,
                    unfocusedTextColor = Black,
                    disabledTextColor = Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Black,
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Line 3: Vote dropdown and privacy checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vote dropdown
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Your Vote:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = when (candidateVote.vote) {
                            1 -> "Yes"
                            0, 3 -> "No"
                            4 -> "Abstain"
                            2 -> "Wait"
                            else -> "Select"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Vote",
                                modifier = Modifier.clickable { expanded = true },
                                tint = Black
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        textStyle = TextStyle(color = Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Black,
                            unfocusedTextColor = Black,
                            disabledTextColor = Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Black
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.surface),
                        properties = PopupProperties(focusable = true)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onVoteChange(null)
                                expanded = false
                            },
                            text = {
                                Text(
                                    "Select Your Vote",
                                    color = BeigeLightBackground
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onVoteChange(1)
                                expanded = false
                            },
                            text = { Text("Yes", color = BeigeLightBackground) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onVoteChange(3)
                                expanded = false
                            },
                            text = { Text("No", color = BeigeLightBackground) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onVoteChange(4)
                                expanded = false
                            },
                            text = { Text("Abstain", color = BeigeLightBackground) }
                        )

                        // Get current polling order ID
                        val pollingOrderId =
                            SecureStorage.retrieve("pollingOrder")?.toIntOrNull() ?: 0

                        // Only show this option if polling order is not 1 or 8
                        if (pollingOrderId != 1 && pollingOrderId != 8) {
                            DropdownMenuItem(
                                onClick = {
                                    onVoteChange(2)
                                    expanded = false
                                },
                                text = { Text("Wait", color = BeigeLightBackground) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Privacy checkbox
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Private:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Use a remembered state for checkbox
                    var isPrivate by remember { mutableStateOf(candidateVote.isPrivate) }

                    // Update the candidateVote when local state changes
                    LaunchedEffect(isPrivate) {
                        if (isPrivate != candidateVote.isPrivate) {
                            onPrivateChange(isPrivate)
                        }
                    }

                    // Update local state when candidateVote changes
                    LaunchedEffect(candidateVote.isPrivate) {
                        if (candidateVote.isPrivate != isPrivate) {
                            isPrivate = candidateVote.isPrivate
                        }
                    }

                    Checkbox(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

// Helper function to format date strings
private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("T")[0].split("-")
        if (parts.size >= 3) {
            "${parts[1]}/${parts[2]}/${parts[0]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}