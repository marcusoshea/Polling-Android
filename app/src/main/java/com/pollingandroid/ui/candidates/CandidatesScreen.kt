package com.pollingandroid.ui.candidates

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pollingandroid.ui.candidates.models.Candidate
import com.pollingandroid.ui.candidates.models.CandidateImage
import com.pollingandroid.ui.candidates.models.ExternalNote
import com.pollingandroid.ui.candidates.models.PollingGroup
import com.pollingandroid.ui.candidates.models.PollingNote
import com.pollingandroid.ui.components.TopAppBar
import com.pollingandroid.ui.theme.*
import com.pollingandroid.util.Constants
import com.pollingandroid.util.UserUtils.isUserLoggedIn
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidatesScreen(
    navController: NavController = rememberNavController(),
    candidatesViewModel: CandidatesViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current

    if (!isUserLoggedIn(context)) {
        LaunchedEffect(Unit) {
            navController.navigate("login")
        }
    } else {
        val pollingOrderName by candidatesViewModel.pollingOrderName.observeAsState("")
        val candidates by candidatesViewModel.candidates.observeAsState(emptyList())
        val selectedCandidate by candidatesViewModel.selectedCandidate.observeAsState()
        val pollingNotes by candidatesViewModel.pollingNotes.observeAsState(emptyList())
        val externalNotes by candidatesViewModel.externalNotes.observeAsState(emptyList())
        val pollingGroups by candidatesViewModel.pollingGroups.observeAsState(emptyList())
        val candidateImages by candidatesViewModel.candidateImages.observeAsState(emptyList())
        val isLoading by candidatesViewModel.isLoading.observeAsState(false)
        val errorMessage by candidatesViewModel.errorMessage.observeAsState(null)
        val newNoteText by candidatesViewModel.newNoteText.observeAsState("")
        val isPrivateNote by candidatesViewModel.isPrivateNote.observeAsState(false)
        val showPollingNotes by candidatesViewModel.showPollingNotes.observeAsState(true)
        val showExternalNotes by candidatesViewModel.showExternalNotes.observeAsState(true)
        val noteAddedSuccess by candidatesViewModel.noteAddedSuccess.observeAsState()

        // Clear the note added success message once observed
        LaunchedEffect(noteAddedSuccess) {
            if (noteAddedSuccess != null) {
                candidatesViewModel.resetNoteAddedStatus()
            }
        }

        Scaffold(
            topBar = {
                if (selectedCandidate == null) {
                    TopAppBar(
                        title = "$pollingOrderName Candidates",
                        onMenuClick = onMenuClick
                    )
                } else {
                    CenterAlignedTopAppBar(
                        title = { Text("Candidate Details - ${selectedCandidate!!.name}") },
                        navigationIcon = {
                            IconButton(onClick = { candidatesViewModel.clearSelectedCandidate() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = SecondaryColor
                        )
                    )
                }
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(color = PrimaryColor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Gold)
                        }
                    } else if (!errorMessage.isNullOrEmpty()) {
                        ErrorMessage(errorMessage!!) {
                            candidatesViewModel.loadCandidates()
                        }
                    } else if (selectedCandidate == null) {
                        CandidateListScreen(
                            candidates = candidates,
                            onCandidateClick = { candidate ->
                                candidatesViewModel.selectCandidate(candidate)
                            },
                            onToggleWatchlist = { candidate ->
                                candidatesViewModel.toggleCandidateWatchlist(candidate)
                            }
                        )
                    } else {
                        CandidateDetail(
                            candidate = selectedCandidate!!,
                            pollingNotes = pollingNotes,
                            externalNotes = externalNotes,
                            pollingGroups = pollingGroups,
                            candidateImages = candidateImages,
                            newNoteText = newNoteText,
                            isPrivateNote = isPrivateNote,
                            showPollingNotes = showPollingNotes,
                            showExternalNotes = showExternalNotes,
                            onNewNoteTextChange = { candidatesViewModel.updateNewNoteText(it) },
                            onPrivateNoteToggle = { candidatesViewModel.toggleIsPrivateNote() },
                            onAddNote = { candidatesViewModel.addExternalNote() },
                            onDeleteNote = { candidatesViewModel.deleteExternalNote(it) },
                            onTogglePollingNotes = { candidatesViewModel.toggleShowPollingNotes() },
                            onToggleExternalNotes = { candidatesViewModel.toggleShowExternalNotes() },
                            onToggleWatchlist = { candidatesViewModel.toggleCandidateWatchlist(it) }
                        )
                    }

                    // Show snackbar for success/failure messages when adding notes
                    noteAddedSuccess?.let { success ->
                        val message =
                            if (success) "Note added successfully" else "Failed to add note"
                        val snackbarHostState = remember { SnackbarHostState() }

                        LaunchedEffect(success) {
                            snackbarHostState.showSnackbar(message)
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun CandidateListScreen(
    candidates: List<Candidate>,
    onCandidateClick: (Candidate) -> Unit,
    onToggleWatchlist: (Candidate) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SandCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Candidate List",
                    style = MaterialTheme.typography.titleLarge,
                    color = Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Watch List",
                    style = MaterialTheme.typography.titleLarge,
                    color = Black
                )
            }

            Divider(
                color = PrimaryColor,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Candidate rows
            if (candidates.isEmpty()) {
                Text(
                    text = "No candidates available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            } else {
                LazyColumn {
                    items(candidates) { candidate ->
                        CandidateListItem(
                            candidate = candidate,
                            onClick = { onCandidateClick(candidate) },
                            onWatchlistToggle = { onToggleWatchlist(candidate) }
                        )
                        Divider(color = PrimaryColor.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateListItem(
    candidate: Candidate,
    onClick: () -> Unit,
    onWatchlistToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = candidate.name,
            style = MaterialTheme.typography.bodyLarge,
            color = LinkBlue,
            modifier = Modifier.weight(1f)
        )

        if (candidate.watch_list) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "On Watch List",
                tint = CheckmarkGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CandidateDetail(
    candidate: Candidate,
    pollingNotes: List<PollingNote>,
    externalNotes: List<ExternalNote>,
    pollingGroups: List<PollingGroup>,
    candidateImages: List<CandidateImage>,
    newNoteText: String,
    isPrivateNote: Boolean,
    showPollingNotes: Boolean,
    showExternalNotes: Boolean,
    onNewNoteTextChange: (String) -> Unit,
    onPrivateNoteToggle: () -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (Int) -> Unit,
    onTogglePollingNotes: () -> Unit,
    onToggleExternalNotes: () -> Unit,
    onToggleWatchlist: (Candidate) -> Unit
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Create New Non-Polling Note card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = SandCardBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Line 1: Title
                Text(
                    text = "Create New Non-Polling Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Line 2: Textbox with 50 rows
                OutlinedTextField(
                    value = newNoteText,
                    onValueChange = onNewNoteTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp), // Makes room for approximately 50 rows of text
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BeigeLightBackground,
                        focusedContainerColor = BeigeLightBackground,
                        unfocusedTextColor = Black,
                        focusedTextColor = Black
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    maxLines = 50
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Line 3: Create button
                Button(
                    onClick = onAddNote,
                    enabled = newNoteText.isNotBlank(),
                    modifier = Modifier
                        .align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D4B) // Green button color
                    )
                ) {
                    Text("Create")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Polling Notes sections
        pollingGroups.forEach { pollingGroup ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SandCardBackground
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
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = SandCardBackground
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

                if (externalNotes.isEmpty()) {
                    Text(
                        text = "No external notes available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
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

                            // Delete button
                            IconButton(
                                onClick = { onDeleteNote(note.externalNoteId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Note",
                                    tint = Color.Red
                                )
                            }
                        }
                        Divider(color = PrimaryColor.copy(alpha = 0.3f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Images Section
        if (candidateImages.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SandCardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    candidateImages.forEach { image ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            // Image displayed using WebView
                            val imageUrl = "${Constants.S3_IMAGE_PREFIX}${image.imageUrl}"
                            val context = LocalContext.current
                            var showFullScreenImage by remember { mutableStateOf(false) }

                            if (showFullScreenImage) {
                                Dialog(
                                    onDismissRequest = { showFullScreenImage = false },
                                    properties = DialogProperties(
                                        dismissOnBackPress = true,
                                        dismissOnClickOutside = true,
                                        usePlatformDefaultWidth = false
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                            .background(Color.Black.copy(alpha = 0.9f))
                                    ) {
                                        // WebView to display the full image
                                        AndroidView(
                                            factory = { context ->
                                                WebView(context).apply {
                                                    webViewClient = WebViewClient()
                                                    settings.apply {
                                                        loadWithOverviewMode = true
                                                        useWideViewPort = true
                                                        builtInZoomControls = true
                                                        displayZoomControls = false
                                                    }
                                                    loadUrl(imageUrl)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp)
                                        )

                                        // Close button in top-right corner
                                        IconButton(
                                            onClick = { showFullScreenImage = false },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(48.dp)
                                                .background(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }

                                        // Image filename at the bottom
                                        Text(
                                            text = image.imageUrl,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .background(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                                .padding(bottom = 24.dp)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(width = 180.dp, height = 180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = PrimaryColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { showFullScreenImage = true }
                            ) {
                                AndroidView(
                                    factory = { context ->
                                        WebView(context).apply {
                                            webViewClient = object : WebViewClient() {
                                                override fun onPageFinished(
                                                    view: WebView?,
                                                    url: String?
                                                ) {
                                                    super.onPageFinished(view, url)
                                                    // Hide loading indicator when page is loaded
                                                }
                                            }
                                            settings.apply {
                                                loadWithOverviewMode = true
                                                useWideViewPort = true
                                                builtInZoomControls = false
                                                displayZoomControls = false
                                            }
                                            loadUrl(imageUrl)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Image description
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                if (image.description.isNotEmpty()) {
                                    if (image.description.contains("<html") ||
                                        image.description.contains("<body") ||
                                        image.description.contains("<div") ||
                                        image.description.contains("<p>") ||
                                        image.description.contains("<font") ||
                                        image.description.contains("<") && image.description.contains(
                                            ">"
                                        )
                                    ) {
                                        var webViewHeight by remember { mutableStateOf(250.dp) }
                                        AndroidView(
                                            factory = { context ->
                                                WebView(context).apply {
                                                    webViewClient = object : WebViewClient() {
                                                        override fun onPageFinished(
                                                            view: WebView?,
                                                            url: String?
                                                        ) {
                                                            super.onPageFinished(view, url)
                                                            view?.evaluateJavascript(
                                                                "(function() { return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight); })();",
                                                            ) { height ->
                                                                try {
                                                                    val contentHeight =
                                                                        height.toFloat().toInt()
                                                                    val calculatedHeight =
                                                                        contentHeight / context.resources.displayMetrics.density
                                                                    webViewHeight = maxOf(
                                                                        250.dp,
                                                                        (calculatedHeight + 100).dp
                                                                    )
                                                                } catch (e: Exception) {
                                                                    webViewHeight = 250.dp
                                                                }
                                                            }
                                                        }
                                                    }
                                                    settings.apply {
                                                        javaScriptEnabled = true
                                                        useWideViewPort = true
                                                        loadWithOverviewMode = true
                                                    }
                                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                                    loadDataWithBaseURL(
                                                        null,
                                                        buildString {
                                                            append("<html><head>")
                                                            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                                                            append("<style>body{background-color:#ECD9A1; margin:8px; padding:8px;}</style>")
                                                            append("</head><body>")
                                                            // If the description already contains complete HTML structure, use as is
                                                            // Otherwise wrap the content
                                                            if (image.description.trim()
                                                                    .startsWith("<html") &&
                                                                image.description.trim()
                                                                    .endsWith("</html>")
                                                            ) {
                                                                // Extract the body content if it's a complete HTML document
                                                                val bodyStart =
                                                                    image.description.indexOf("<body")
                                                                val bodyEnd =
                                                                    image.description.indexOf("</body>")
                                                                if (bodyStart >= 0 && bodyEnd >= 0) {
                                                                    val bodyTagEnd =
                                                                        image.description.indexOf(
                                                                            ">",
                                                                            bodyStart
                                                                        )
                                                                    append(
                                                                        image.description.substring(
                                                                            bodyTagEnd + 1,
                                                                            bodyEnd
                                                                        )
                                                                    )
                                                                } else {
                                                                    append(image.description)
                                                                }
                                                            } else {
                                                                append(image.description)
                                                            }
                                                            append("</body></html>")
                                                        },
                                                        "text/html",
                                                        "UTF-8",
                                                        null
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(webViewHeight)
                                                .background(TertiaryColor)
                                                .padding(bottom = 8.dp),
                                            update = { webView ->
                                                // Update the WebView when needed
                                            }
                                        )
                                    } else {
                                        Text(
                                            text = image.description,
                                            style = MaterialTheme.typography.bodyMedium,
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

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Red,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = Black
            )
        ) {
            Text("Retry")
        }
    }
}