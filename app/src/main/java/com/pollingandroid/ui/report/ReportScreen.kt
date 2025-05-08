package com.pollingandroid.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.pollingandroid.ui.theme.PrimaryColor
import com.pollingandroid.ui.theme.SecondaryColor
import com.pollingandroid.ui.theme.TertiaryColor
import com.pollingandroid.ui.theme.Black
import com.pollingandroid.ui.theme.Gold
import com.pollingandroid.ui.theme.TextBoxBackground
import com.pollingandroid.ui.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    viewModel: ReportViewModel = viewModel()
) {
    val pollingTitle by viewModel.pollingTitle.collectAsState()
    val pollingOrderName by viewModel.pollingOrderName.collectAsState()
    val closedPollingAvailable by viewModel.closedPollingAvailable.collectAsState()
    val inProcessPollingAvailable by viewModel.inProcessPollingAvailable.collectAsState()
    val candidateList by viewModel.candidateList.collectAsState()
    val pollingSummary by viewModel.pollingSummary.collectAsState()
    val pollingTotals by viewModel.pollingTotals.collectAsState()
    val showNotes by viewModel.showNotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCandidates()
    }

    Scaffold(
        topBar = {
            com.pollingandroid.ui.components.TopAppBar(
                title = pollingTitle,
                onMenuClick = onMenuClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(color = PrimaryColor)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            if (inProcessPollingAvailable) {
                Button(
                    onClick = { viewModel.toggleReport() },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (closedPollingAvailable) "Show In-Process Polling" else "Show Closed Polling",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = TertiaryColor
                    )
                }
            } else if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Gold
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Report summary section
                        if (closedPollingAvailable && pollingSummary != null) {
                            // Closed polling content
                            Text(
                                text = buildAnnotatedString {
                                    append("The most recent closed ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(pollingOrderName)
                                    }
                                    append(" polling was the ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(pollingSummary?.pollingName ?: "")
                                    }
                                    append(" which ran from ")
                                    append(pollingSummary?.startDate ?: "")
                                    append(" to ")
                                    append(pollingSummary?.endDate ?: "")
                                    append(".")
                                },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = Black
                            )

                            if (pollingSummary?.pollingOrderPollingType == 1) {
                                if (pollingSummary?.pollingOrderParticipation ?: 0 > 0) {
                                    Text(
                                        text = buildAnnotatedString {
                                            append("The ")
                                            append(pollingOrderName)
                                            append(" order requires an active member participation rate of ")
                                            append(
                                                (pollingSummary?.pollingOrderParticipation
                                                    ?: 0).toString()
                                            )
                                            append("% to certify a polling.\n\n")
                                            append("The ")
                                            append(pollingSummary?.pollingName ?: "")
                                            append(" had participation of ")
                                            append(
                                                (pollingSummary?.participatingMembers
                                                    ?: 0).toString()
                                            )
                                            append(" of ")
                                            append((pollingSummary?.activeMembers ?: 0).toString())
                                            append(" active order members, resulting in a participation rate of ")
                                            append(pollingSummary?.participationRate ?: "0")
                                            append("%. The polling is thus ")
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Black
                                    )

                                    Text(
                                        text = pollingSummary?.certified ?: "not certified",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        color = Black
                                    )

                                    Text(
                                        text = "A candidate must attain a rate of ${pollingSummary?.pollingOrderScore}% to be recommended to join the order.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        color = Black
                                    )
                                }
                            } else if (pollingSummary?.pollingOrderPollingType == 2) {
                                Text(
                                    text = "The $pollingOrderName order recommends the top candidates within a polling exceeding a specific rating.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    color = Black
                                )
                            }
                        } else if (inProcessPollingAvailable && pollingSummary != null) {
                            // In-process polling content
                            Text(
                                text = buildAnnotatedString {
                                    append("In-Process Polling Candidate List (")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("NOT FINALIZED")
                                    }
                                    append("):")
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = Black
                            )

                            if (pollingSummary?.pollingOrderPollingType == 1) {
                                if (pollingSummary?.pollingOrderParticipation ?: 0 > 0) {
                                    Text(
                                        text = buildAnnotatedString {
                                            append("The ")
                                            append(pollingOrderName)
                                            append(" order requires an active member participation rate of ")
                                            append(
                                                (pollingSummary?.pollingOrderParticipation
                                                    ?: 0).toString()
                                            )
                                            append("% to certify a polling.\n\n")
                                            append("The ")
                                            append(pollingSummary?.pollingName ?: "")
                                            append(" has a participation of ")
                                            append(
                                                (pollingSummary?.participatingMembers
                                                    ?: 0).toString()
                                            )
                                            append(" of ")
                                            append((pollingSummary?.activeMembers ?: 0).toString())
                                            append(" active order members, resulting in a participation rate of ")
                                            append(pollingSummary?.participationRate ?: "0")
                                            append("%. The polling is currently ")
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Black
                                    )

                                    Text(
                                        text = pollingSummary?.certified ?: "not certified",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        color = Black
                                    )

                                    Text(
                                        text = "A candidate must attain a rate of ${pollingSummary?.pollingOrderScore}% to be recommended to join the order.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        color = Black
                                    )
                                }
                            } else if (pollingSummary?.pollingOrderPollingType == 2) {
                                Text(
                                    text = "The $pollingOrderName order recommends the top candidates within a polling exceeding a specific rating.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    color = Black
                                )
                            }
                        } else if (!closedPollingAvailable && !inProcessPollingAvailable) {
                            Text(
                                text = "No Polling Report Data Available",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Black
                            )
                        }

                        // Show Notes toggle
                        if (candidateList.isNotEmpty()) {
                            Divider(
                                color = Black
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = showNotes,
                                    onCheckedChange = { viewModel.toggleNotesVisibility() },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = TertiaryColor,
                                        uncheckedColor = Black,
                                        checkmarkColor = Black
                                    )
                                )
                                Text(
                                    text = "Show Notes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = Black
                                )
                            }
                        }

                        // Display candidate list
                        if (candidateList.isNotEmpty()) {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Black
                            )

                            Text(
                                text = "Polling Candidate List:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Black
                            )
                            candidateList.forEach { candidate ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(0.999f)
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Gold
                                    )
                                ) {
                                    var expanded by remember { mutableStateOf(false) }
                                    val hasNotes = candidate.notes.isNotEmpty()

                                    Column(
                                        modifier = Modifier
                                            .border(width = 2.dp, color = PrimaryColor,
                                                shape = RoundedCornerShape(8.dp))

                                    ) {
                                        // Candidate name and recommendation
                                        Text(
                                            text = candidate.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp),
                                            color = Black
                                        )

                                        Text(
                                            text = candidate.recommended,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(horizontal = 6.dp),
                                            color = Black
                                        )

                                        // Vote counts section
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically

                                        ) {
                                            Text(
                                                text = "(",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .padding(horizontal = 6.dp)
                                                    .padding(bottom = 8.dp),
                                                color = Black
                                            )

                                            // Get vote counts for this candidate
                                            val yesCount = candidate.voteCounts["Yes"] ?: 0
                                            val noCount = candidate.voteCounts["No"] ?: 0
                                            val waitCount = candidate.voteCounts["Wait"] ?: 0
                                            val abstainCount = candidate.voteCounts["Abstain"] ?: 0

                                            Text(
                                                text = "Yes: $yesCount, No: $noCount" +
                                                        (if (waitCount > 0) ", Wait: $waitCount" else "") +
                                                        (if (abstainCount > 0) ", Abstain: $abstainCount" else ""),
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(bottom = 8.dp),
                                                color = Black
                                            )

                                            Text(
                                                text = ") = ${candidate.inProcessRating}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(bottom = 8.dp),
                                                color = Black

                                            )

                                        }

                                        // Notes section
                                        if (showNotes && hasNotes) {
                                            Divider(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = Black
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { expanded = !expanded }
                                                    .padding(bottom = 8.dp)
                                                    .padding(horizontal = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Notes",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Black
                                                )

                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDropDown,
                                                    contentDescription = if (expanded) "Collapse" else "Expand",
                                                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                                                    tint = Black
                                                )
                                            }

                                            if (expanded) {
                                                Column {
                                                    candidate.notes.forEach { note ->
                                                        Row(
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        ) {
                                                            Text(
                                                                text = "• ",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Black
                                                            )

                                                            Column {
                                                                if (note.private) {
                                                                    Text(
                                                                        text = "--PRIVATE RESPONSE--",
                                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                                            fontWeight = FontWeight.Bold
                                                                        ),
                                                                        color = Black
                                                                    )
                                                                }

                                                                // Only show note text if not empty
                                                                if (note.note.isNotBlank()) {
                                                                    Text(
                                                                        text = "\"${note.note}\"",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = Black
                                                                    )
                                                                }

                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.padding(end = 8.dp)
                                                                ) {
                                                                    Text(
                                                                        text = "- ${note.memberName}",
                                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                                            fontStyle = FontStyle.Italic
                                                                        ),
                                                                        modifier = Modifier.padding(
                                                                            start = 8.dp
                                                                        ),
                                                                        color = Black
                                                                    )

                                                                    // Always show vote if present
                                                                    if (note.vote.isNotEmpty()) {
                                                                        Spacer(
                                                                            modifier = Modifier.width(
                                                                                8.dp
                                                                            )
                                                                        )
                                                                        Text(
                                                                            text = "(${note.vote})",
                                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                                fontStyle = FontStyle.Italic,
                                                                                fontWeight = FontWeight.Bold
                                                                            ),
                                                                            color = Black
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if ((closedPollingAvailable || inProcessPollingAvailable) && !isLoading) {
                            Text(
                                text = "No candidates found in the polling report",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Black
                            )
                        }
                    }
                }
            }
        }
    }
}