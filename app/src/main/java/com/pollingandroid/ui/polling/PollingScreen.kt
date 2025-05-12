package com.pollingandroid.ui.polling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.pollingandroid.ui.candidates.CandidatesViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import com.pollingandroid.ui.candidates.models.ExternalNote
import com.pollingandroid.ui.candidates.models.PollingGroup
import com.pollingandroid.ui.candidates.models.PollingNote
import com.pollingandroid.ui.candidates.models.CandidateImage
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import com.pollingandroid.ui.theme.SandCardBackground
import kotlinx.coroutines.delay

@Composable
fun PollingScreen(
    navController: NavController = rememberNavController(),
    pollingViewModel: PollingViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val candidatesViewModel: CandidatesViewModel = viewModel()
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
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = 5.dp
                        )
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
                                    pollingViewModel = pollingViewModel,
                                    candidatesViewModel = candidatesViewModel
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
    pollingViewModel: PollingViewModel,
    candidatesViewModel: CandidatesViewModel
) {
    val context = LocalContext.current

    // Create mutable state for votes
    val votes = remember(candidateVotes) {
        // Create a map of candidate votes indexed by candidate ID
        val voteMap = candidateVotes.associateBy { it.candidateId }.toMutableMap()
        voteMap
    }

    // Track if all votes are completed (to trigger recomposition)
    var voteUpdateCounter by remember { mutableStateOf(0) }

    // Track which candidates have had their votes explicitly selected
    val selectedVotes = remember { mutableStateMapOf<Int, Boolean>() }

    // Initialize the selectedVotes map with all candidates set to false (not explicitly selected)
    // Exception: if a vote already exists for a candidate, mark it as explicitly selected
    LaunchedEffect(candidates, candidateVotes) {
        candidates.forEach { candidate ->
            // Check if there's an existing vote for this candidate
            val existingVote = candidateVotes.find { it.candidateId == candidate.candidate_id }
            val hasRealVote = existingVote?.vote != null

            // If the vote is already set, mark it as explicitly selected
            if (hasRealVote) {
                selectedVotes[candidate.candidate_id] = true

            } else if (!selectedVotes.containsKey(candidate.candidate_id)) {
                // Otherwise, initialize as not selected
                selectedVotes[candidate.candidate_id] = false
            }
        }

        // Force voteUpdateCounter to update to trigger button enablement check
        voteUpdateCounter++
    }

    // Check if votes have already been submitted (has pollingNotesId and completed == true)
    val hasSubmittedVotes = candidateVotes.any { vote ->
        // Check if this vote has a pollingNotesId and is marked as completed
        vote.pollingNotesId > 0 && vote.completed
    }

    // Special case for submitted votes - mark them all as selected without requiring UI interaction
    LaunchedEffect(hasSubmittedVotes) {
        if (hasSubmittedVotes) {
            // Check if all votes are complete and mark them as selected
            var allVotesHaveValues = true
            candidates.forEach { candidate ->
                val vote = votes[candidate.candidate_id]
                if (vote?.vote != null) {
                    selectedVotes[candidate.candidate_id] = true
                } else {
                    allVotesHaveValues = false
                }
            }

            // Trigger UI update if needed
            if (allVotesHaveValues) {
                voteUpdateCounter++
            }
        }
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

    var showDialog by remember { mutableStateOf(false) }
    var selectedCandidate by remember { mutableStateOf<Candidate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        // Proxy Voting Dropdown - only show for order admins
        val isOrderAdmin = SecureStorage.retrieve("isOrderAdmin")?.toBoolean() == true

        if (orderMembers.isNotEmpty() && isOrderAdmin) {
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
                            text = if (selectedMember == null) "Vote as self" else selectedMember.name,
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
                        .background(PrimaryColor),
                    properties = PopupProperties(focusable = true)
                ) {
                    // Add "Vote as self" option first
                    DropdownMenuItem(
                        onClick = {
                            onMemberSelected(-1) // -1 indicates voting as self
                            expanded = false
                        },
                        text = {
                            Text(
                                text = "Vote as self",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BeigeLightBackground
                            )
                        }
                    )

                    // Add divider
                    Divider(color = BeigeLightBackground.copy(alpha = 0.5f))

                    // Add members
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 0.dp)
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = TertiaryColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight()
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
                val lazyListState = rememberLazyListState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 5.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(candidates) { candidate ->
                            val candidateVote = votes[candidate.candidate_id] ?: CandidateVote(
                                candidateId = candidate.candidate_id,
                                candidateName = candidate.name,
                                // Ensure vote is initially null
                                vote = null
                            ).also { votes[candidate.candidate_id] = it }



                            CandidateVoteRow(
                                candidate = candidate,
                                candidateVote = candidateVote,
                                onVoteChange = { vote ->
                                    candidateVote.vote = vote
                                    votes[candidate.candidate_id] = candidateVote
                                    // Mark this vote as explicitly selected by the user
                                    selectedVotes[candidate.candidate_id] = true

                                    // Increment counter to trigger recomposition
                                    voteUpdateCounter++
                                },
                                onNoteChange = { note ->
                                    candidateVote.note = note
                                    votes[candidate.candidate_id] = candidateVote
                                },
                                onPrivateChange = { isPrivate ->
                                    candidateVote.isPrivate = isPrivate
                                    votes[candidate.candidate_id] = candidateVote
                                },
                                onCandidateNameClick = {
                                    selectedCandidate = candidate
                                    showDialog = true
                                },
                                selectedVotes = selectedVotes
                            )
                        }
                    }

                    // Add scrollbar indicator at bottom
                    // Add up/down scroll indicators when not at top/bottom
                    if (lazyListState.canScrollBackward) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            TertiaryColor.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    if (lazyListState.canScrollForward) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            TertiaryColor.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                        )

                        // Dots to indicate scrollability
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Gold.copy(alpha = 0.7f))
                                        .padding(horizontal = 2.dp)
                                )
                            }
                        }
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
                    // Check if all candidates have votes - depends on voteUpdateCounter to trigger recomposition
                    val allCandidatesHaveVotes by remember(voteUpdateCounter, hasSubmittedVotes) {
                        derivedStateOf {
                            // If these are already submitted votes, check that all votes have actual values
                            if (hasSubmittedVotes) {
                                // Check only that every vote has a non-null value
                                val result = candidates.isNotEmpty() &&
                                        votes.size == candidates.size &&
                                        votes.values.none { it.vote == null }

                                return@derivedStateOf result
                            }

                            // For new votes or normal updates, run the full check
                            // Check that:
                            // 1. There are candidates to vote on
                            // 2. All candidates have entries in the selectedVotes map
                            // 3. All votes have been explicitly selected (selectedVotes value is true)
                            // 4. No votes are null
                            candidates.isNotEmpty() &&
                                    selectedVotes.size == candidates.size &&
                                    selectedVotes.values.all { it } &&
                                    votes.values.none { it.vote == null }
                        }
                    }

                    // Save Draft button
                    if (!hasSubmittedVotes) {
                        Button(
                            onClick = {
                                try {
                                    isSubmitting = true
                                    isUpdate = false
                                    isDraft = true

                                    // Use the regular onUpdateVotes function but with isCompleted=false
                                    onUpdateVotes(getVotesList(), false) {
                                        isSubmitting = false
                                        showSuccessMessage = true
                                    }
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    // Show user-friendly error message
                                    android.widget.Toast.makeText(
                                        context,
                                        "Unable to save draft. Please try again.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
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

                    // Submit Polling Vote button - exact name and action will vary based on state
                    val buttonText =
                        if (hasSubmittedVotes) "Update Your Submitted Polling Vote" else "Submit Polling Vote"

                    // Button is only enabled if user has explicitly selected a vote for each candidate
                    val buttonEnabled = !isSubmitting && allCandidatesHaveVotes

                    Button(
                        onClick = {
                            isSubmitting = true
                            isUpdate = true
                            isDraft = false

                            // Add try-catch around onUpdateVotes call
                            try {
                                onUpdateVotes(getVotesList(), true) {
                                    try {
                                        isSubmitting = false
                                        showSuccessMessage = true
                                    } catch (e: Exception) {
                                        // Silent error handling for cleaner release
                                    }
                                }
                            } catch (e: Exception) {
                                isSubmitting = false
                                // Show user-friendly error message
                                android.widget.Toast.makeText(
                                    context,
                                    "Unable to submit votes. Please try again.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        enabled = buttonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!buttonEnabled) Color.Gray else LinkBlue
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = BeigeLightBackground
                            )
                        } else {
                            Text(
                                buttonText,
                                color = BeigeLightBackground
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedCandidate != null) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            CandidateDetailOverlay(
                candidate = selectedCandidate!!,
                candidatesViewModel = candidatesViewModel,
                onClose = { showDialog = false }
            )
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
    onPrivateChange: (Boolean) -> Unit,
    onCandidateNameClick: () -> Unit,
    selectedVotes: MutableMap<Int, Boolean>
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TertiaryColor.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = LinkBlue)
                    ) {
                        onCandidateNameClick()
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = candidate.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View Candidate Details",
                        tint = LinkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

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
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clickable { expanded = true },
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
                            .background(PrimaryColor),
                        properties = PopupProperties(focusable = true)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onVoteChange(null)
                                // When selecting the default option, mark as not explicitly selected
                                selectedVotes[candidate.candidate_id] = false
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
                                // Mark this candidate as having an explicitly selected vote
                                selectedVotes[candidate.candidate_id] = true
                                expanded = false
                            },
                            text = { Text("Yes", color = BeigeLightBackground) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onVoteChange(3)
                                // Mark this candidate as having an explicitly selected vote
                                selectedVotes[candidate.candidate_id] = true
                                expanded = false
                            },
                            text = { Text("No", color = BeigeLightBackground) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onVoteChange(4)
                                // Mark this candidate as having an explicitly selected vote
                                selectedVotes[candidate.candidate_id] = true
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
                                    // Mark this candidate as having an explicitly selected vote
                                    selectedVotes[candidate.candidate_id] = true
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

@Composable
fun CandidateDetailOverlay(
    candidate: Candidate,
    candidatesViewModel: CandidatesViewModel,
    onClose: () -> Unit
) {
    // Load candidate details when overlay opens
    LaunchedEffect(candidate.candidate_id) {
        candidatesViewModel.selectCandidate(candidate)
    }

    // Observe state from the ViewModel
    val selectedCandidate by candidatesViewModel.selectedCandidate.observeAsState()
    val pollingNotes by candidatesViewModel.pollingNotes.observeAsState(emptyList())
    val externalNotes by candidatesViewModel.externalNotes.observeAsState(emptyList())
    val pollingGroups by candidatesViewModel.pollingGroups.observeAsState(emptyList())
    val candidateImages by candidatesViewModel.candidateImages.observeAsState(emptyList())
    val isLoading by candidatesViewModel.isLoading.observeAsState(false)
    val errorMessage by candidatesViewModel.errorMessage.observeAsState(null)
    val showPollingNotes by candidatesViewModel.showPollingNotes.observeAsState(true)
    val showExternalNotes by candidatesViewModel.showExternalNotes.observeAsState(true)

    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .heightIn(max = 600.dp),
        colors = CardDefaults.cardColors(containerColor = SandCardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold)
                }
            } else {
                // Candidate header
                Text(
                    text = candidate.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Black,
                    fontWeight = FontWeight.Bold
                )

                Divider(
                    color = PrimaryColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Polling Notes sections
                pollingGroups.forEach { pollingGroup ->
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TertiaryColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                        ) {
                            // Polling group header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = pollingGroup.pollingName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Black
                                )

                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (expanded) "Collapse" else "Expand",
                                    tint = Black
                                )
                            }

                            // Expanded content
                            if (expanded) {
                                pollingGroup.notes.forEach { note ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Show "PRIVATE RESPONSE" text if the note is marked as private
                                            if (note.isPrivate) {
                                                Text(
                                                    text = "--PRIVATE RESPONSE--",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Red,
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                            }

                                            // Note text if available
                                            if (!note.note.isNullOrBlank()) {
                                                Text(
                                                    text = "Note: \"${note.note}\"",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Black,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    // Member name and vote information
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),

                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "~ ${note.memberName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Black,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = "Vote: ${note.getVoteText()}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Black
                                        )
                                    }
                                    Divider(
                                        color = PrimaryColor.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Non-Polling Notes section
                if (externalNotes.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TertiaryColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Non-Polling Notes",
                                style = MaterialTheme.typography.titleLarge,
                                color = Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Divider(color = PrimaryColor.copy(alpha = 0.3f))

                            externalNotes.forEach { note ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Show "PRIVATE RESPONSE" text if the note is marked as private
                                        if (note.isPrivate) {
                                            Text(
                                                text = "--PRIVATE RESPONSE--",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Red,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }

                                        Text(
                                            text = "\"${note.note}\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Black
                                        )

                                        Text(
                                            text = "- ${note.memberName} on ${note.createdAt}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontStyle = FontStyle.Italic,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Black
                                        )
                                    }
                                }
                                Divider(color = PrimaryColor.copy(alpha = 0.3f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Close button
                Button(
                    onClick = {
                        candidatesViewModel.clearSelectedCandidate()
                        onClose()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = LinkBlue)
                ) {
                    Text("Close", color = BeigeLightBackground)
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