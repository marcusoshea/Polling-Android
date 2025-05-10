package com.pollingandroid.ui.polling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.Polling
import com.pollingandroid.model.PollingSummary
import com.pollingandroid.model.PollingNoteRequest
import com.pollingandroid.repository.PollingOrderRepository
import com.pollingandroid.ui.candidates.models.Candidate
import com.pollingandroid.ui.login.SecureStorage
import com.pollingandroid.ui.polling.models.PollingState
import com.pollingandroid.ui.polling.models.PollingMember
import com.pollingandroid.ui.polling.models.CandidateVote
import com.pollingandroid.util.UserUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PollingViewModel : ViewModel() {

    val pollingOrderName: LiveData<String> = PollingOrderRepository.pollingOrderName

    // Current state of the polling screen
    private val _state = MutableLiveData<PollingState>(PollingState.LOADING)
    val state: LiveData<PollingState> = _state
    
    // Current polling data
    private val _currentPolling = MutableLiveData<Polling?>()
    val currentPolling: LiveData<Polling?> = _currentPolling
    
    // Available candidates for voting
    private val _candidates = MutableLiveData<List<Candidate>>()
    val candidates: LiveData<List<Candidate>> = _candidates
    
    // List of all order members for proxy voting
    private val _orderMembers = MutableLiveData<List<PollingMember>>()
    val orderMembers: LiveData<List<PollingMember>> = _orderMembers
    
    // Selected member for voting (proxy or self)
    private val _selectedMember = MutableLiveData<PollingMember?>()
    val selectedMember: LiveData<PollingMember?> = _selectedMember
    
    // Candidate votes and notes
    private val _candidateVotes = MutableLiveData<List<CandidateVote>>()
    val candidateVotes: LiveData<List<CandidateVote>> = _candidateVotes
    
    // Error message
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the order of the ${pollingOrderName.value} PollingScreen"
    }

    val text: LiveData<String> = _text
    
    // Load the current polling and related data
    fun loadCurrentPollingData(pollingOrderId: Int, authToken: String) {
        _state.value = PollingState.LOADING

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")

                // Use enqueue instead of execute to perform the network call asynchronously
                RetrofitInstance.api.getCurrentPolling(pollingOrderId, headers).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string() ?: "{}"
                                val polling = parseCurrentPolling(responseBody)
                                _currentPolling.postValue(polling)

                                if (polling != null) {
                                    // If we have an active polling, load members and candidates
                                    loadOrderMembers(pollingOrderId, authToken)
                                    loadCandidates(pollingOrderId, authToken)

                                    // Also load the current member's info
                                    val memberId = SecureStorage.retrieve("memberId") ?: "0"
                                    loadPollingSummary(polling.pollingId, memberId, authToken)

                                    // Default to "Vote as self" by setting selectedMember to null
                                    _selectedMember.postValue(null)
                                }

                                _state.postValue(PollingState.LOADED)
                            } else {
                                _errorMessage.postValue("Failed to load current polling: ${response.code()}")
                                _state.postValue(PollingState.ERROR)
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            _errorMessage.postValue("Network error: ${t.message}")
                            _state.postValue(PollingState.ERROR)
                        }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.postValue("Error: ${e.message}")
                _state.postValue(PollingState.ERROR)
            }
        }
    }
    
    // Load all members of the polling order
    private fun loadOrderMembers(orderId: Int, authToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                RetrofitInstance.api.getAllMembers(orderId, headers).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string() ?: "[]"
                                val members = parseOrderMembers(responseBody)
                                _orderMembers.postValue(members)
                            } else {
                                _errorMessage.postValue("Failed to load order members: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            _errorMessage.postValue("Error loading order members: ${t.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.postValue("Error loading order members: ${e.message}")
            }
        }
    }
    
    // Set a member as the selected member for proxy voting
    fun selectMember(memberId: Int) {
        // Special case: -1 means "Vote as self"
        if (memberId == -1) {
            // Clear selected member to indicate voting as self
            _selectedMember.postValue(null)

            // Reload polling summary for logged in user
            val authToken = UserUtils.decryptData(SecureStorage.retrieve("accessToken") ?: "") ?: ""
            val pollingId = _currentPolling.value?.pollingId ?: 0
            val currentUserId = SecureStorage.retrieve("memberId") ?: "0"
            loadPollingSummary(pollingId, currentUserId, authToken)
            return
        }

        // Normal case: select another member
        val members = _orderMembers.value ?: return
        val member = members.find { it.id == memberId }
        _selectedMember.postValue(member)
        
        // Reload polling summary for the selected member
        val authToken = UserUtils.decryptData(SecureStorage.retrieve("accessToken") ?: "") ?: ""
        val pollingId = _currentPolling.value?.pollingId ?: 0
        loadPollingSummary(pollingId, memberId.toString(), authToken)
    }
    
    // Load candidates for the polling order
    private fun loadCandidates(orderId: Int, authToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                RetrofitInstance.api.getAllCandidates(orderId, headers).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string() ?: "[]"
                                _candidates.postValue(parseCandidates(responseBody))
                            } else {
                                _errorMessage.postValue("Failed to load candidates: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            _errorMessage.postValue("Error loading candidates: ${t.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.postValue("Error loading candidates: ${e.message}")
            }
        }
    }
    
    // Load polling summary (votes and notes) for a specific member
    fun loadPollingSummary(pollingId: Int, memberId: String, authToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                android.util.Log.d(
                    "PollingViewModel",
                    "Loading polling summary - Polling ID: $pollingId, Member ID: $memberId"
                )

                RetrofitInstance.api.getPollingSummary(pollingId, memberId, headers).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string() ?: "[]"
                                android.util.Log.d(
                                    "PollingViewModel",
                                    "Polling summary response: $responseBody"
                                )

                                val summaries = parsePollingSummaries(responseBody)
                                android.util.Log.d(
                                    "PollingViewModel",
                                    "Parsed ${summaries.size} polling summaries"
                                )

                                // Convert to candidate votes for UI
                                val votes = summaries.map { summary ->
                                    android.util.Log.d(
                                        "PollingViewModel",
                                        "Summary for candidate ${summary.name}: " +
                                                "vote=${summary.vote}, note=${summary.note}, pollingNotesId=${summary.pollingNotesId}" +
                                                ", completed=${summary.completed}"
                                    )

                                    CandidateVote(
                                        candidateId = summary.candidateId,
                                        candidateName = summary.name,
                                        note = summary.note ?: "",
                                        vote = summary.vote,
                                        isPrivate = summary.isPrivate ?: false,
                                        pollingNotesId = summary.pollingNotesId,
                                        completed = summary.completed
                                    )
                                }

                                _candidateVotes.postValue(votes)
                            } else {
                                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                                android.util.Log.e(
                                    "PollingViewModel",
                                    "Failed to load polling summary: ${response.code()} - $errorBody"
                                )
                                _errorMessage.postValue("Failed to load polling summary: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            android.util.Log.e(
                                "PollingViewModel",
                                "Error loading polling summary",
                                t
                            )
                            _errorMessage.postValue("Error loading polling summary: ${t.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("PollingViewModel", "Exception loading polling summary", e)
                _errorMessage.postValue("Error loading polling summary: ${e.message}")
            }
        }
    }
    
    // Update votes for all candidates at once
    fun updateVotes(
        votes: List<CandidateVote>,
        isCompleted: Boolean,
        authToken: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Validate auth token
                if (authToken.isBlank()) {
                    android.util.Log.e(
                        "PollingViewModel",
                        "Auth token is blank, cannot update votes"
                    )
                    _errorMessage.postValue("Authentication error: Please log in again")
                    return@launch
                }

                val headers = mapOf("Authorization" to "Bearer $authToken")
                
                // Get the member ID and selected member ID
                val currentMemberId = SecureStorage.retrieve("memberId") ?: "0"

                // If _selectedMember.value is null, it means "Vote as self"
                // Otherwise, use the selected member's ID
                val selectedMemberId = _selectedMember.value?.id?.toString() ?: currentMemberId

                val pollingId = _currentPolling.value?.pollingId ?: 0
                val polling = _currentPolling.value

                if (polling == null) {
                    _errorMessage.postValue("Error: No active polling found")
                    return@launch
                }

                android.util.Log.d(
                    "PollingViewModel",
                    "Preparing to update votes for polling ID: $pollingId"
                )
                android.util.Log.d(
                    "PollingViewModel",
                    "Selected member ID: $selectedMemberId (null means voting as self)"
                )
                android.util.Log.d(
                    "PollingViewModel",
                    "Current logged-in member ID: $currentMemberId"
                )
                android.util.Log.d("PollingViewModel", "Number of votes to update: ${votes.size}")
                android.util.Log.d("PollingViewModel", "Is completed submission: $isCompleted")

                // Get the current date in the format YYYY-MM-DD
                val currentDate = java.time.LocalDate.now().toString() + "T00:00:00.000Z"

                val votesList = votes.map { vote ->
                    // Find candidate details from the candidates list 
                    val candidateDetails = _candidates.value?.find {
                        it.candidate_id == vote.candidateId
                    }

                    // Get existing vote to check for polling_notes_id
                    val existingVote =
                        _candidateVotes.value?.find { it.candidateId == vote.candidateId }
                    val existingPollingNotesId = existingVote?.pollingNotesId ?: 0

                    // Check if we're preserving an existing ID or setting to null for new records
                    val finalPollingNotesId = if (vote.pollingNotesId > 0) {
                        vote.pollingNotesId
                    } else if (existingPollingNotesId > 0) {
                        existingPollingNotesId  // Use existing ID if available
                    } else {
                        null  // Explicitly set to null for new records
                    }

                    android.util.Log.d(
                        "PollingViewModel",
                        "Processing vote for candidate: ${vote.candidateName}, ID: ${vote.candidateId}, " +
                                "existing pollingNotesId: $existingPollingNotesId, final pollingNotesId: $finalPollingNotesId"
                    )

                    // Create a PollingNoteRequest object for each vote
                    PollingNoteRequest(
                        pollingId = pollingId,
                        candidateId = vote.candidateId,
                        pollingCandidateId = candidateDetails?.let {
                            // Try to calculate polling_candidate_id if possible
                            pollingId * 1000 + vote.candidateId
                        },
                        name = vote.candidateName,
                        pollingOrderId = polling.pollingOrderId,
                        link = candidateDetails?.link ?: "",
                        watchList = candidateDetails?.watch_list ?: false,
                        pollingNotesId = finalPollingNotesId,
                        note = if (vote.note.isBlank()) null else vote.note,
                        vote = vote.vote,
                        pnCreatedAt = currentDate,
                        pollingOrderMemberId = selectedMemberId.toInt(),
                        completed = isCompleted,
                        isPrivate = vote.isPrivate,
                        authToken = authToken
                    )
                }

                android.util.Log.d(
                    "PollingViewModel",
                    "Making API call to create/update polling notes"
                )

                // For debugging, log the first vote's data to inspect its structure
                if (votesList.isNotEmpty()) {
                    android.util.Log.d("PollingViewModel", "First vote data: ${votesList.first()}")

                    // Log JSON representation
                    try {
                        val gson =
                            com.google.gson.GsonBuilder().serializeNulls().setPrettyPrinting()
                                .create()
                        val jsonString = gson.toJson(votesList)
                        android.util.Log.d("PollingViewModel", "JSON to be sent:")
                        android.util.Log.d("PollingViewModel", jsonString)
                    } catch (e: Exception) {
                        android.util.Log.e("PollingViewModel", "Error serializing to JSON", e)
                    }

                    // Additional debug for all votes to check for invalid/empty values
                    votesList.forEachIndexed { index, request ->
                        android.util.Log.d("PollingViewModel", "Vote $index data:")
                        android.util.Log.d("PollingViewModel", "- pollingId: ${request.pollingId}")
                        android.util.Log.d(
                            "PollingViewModel",
                            "- candidateId: ${request.candidateId}"
                        )
                        android.util.Log.d(
                            "PollingViewModel",
                            "- pollingCandidateId: ${request.pollingCandidateId}"
                        )
                        android.util.Log.d("PollingViewModel", "- name: ${request.name}")
                        android.util.Log.d(
                            "PollingViewModel",
                            "- pollingOrderId: ${request.pollingOrderId}"
                        )
                        android.util.Log.d(
                            "PollingViewModel",
                            "- pollingNotesId: ${request.pollingNotesId}"
                        )
                        android.util.Log.d("PollingViewModel", "- note: ${request.note}")
                        android.util.Log.d("PollingViewModel", "- vote: ${request.vote}")
                        android.util.Log.d(
                            "PollingViewModel",
                            "- pnCreatedAt: ${request.pnCreatedAt}"
                        )
                        android.util.Log.d(
                            "PollingViewModel",
                            "- pollingOrderMemberId: ${request.pollingOrderMemberId}"
                        )
                        android.util.Log.d("PollingViewModel", "- completed: ${request.completed}")
                        android.util.Log.d("PollingViewModel", "- isPrivate: ${request.isPrivate}")
                    }
                }

                RetrofitInstance.api.createPollingNotes(votesList, headers).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            android.util.Log.d(
                                "PollingViewModel",
                                "API response received: ${response.code()}"
                            )
                            android.util.Log.d(
                                "PollingViewModel",
                                "Request URL: ${call.request().url}"
                            )
                            android.util.Log.d(
                                "PollingViewModel",
                                "Request Headers: ${call.request().headers}"
                            )

                            // Try to log the request body
                            try {
                                val copy = call.request().newBuilder().build()
                                val buffer = okio.Buffer()
                                copy.body?.writeTo(buffer)
                                val requestBody = buffer.readUtf8()
                                android.util.Log.d(
                                    "PollingViewModel",
                                    "Raw request body: $requestBody"
                                )
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "PollingViewModel",
                                    "Error logging request body",
                                    e
                                )
                            }
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string() ?: "{}"
                                android.util.Log.d(
                                    "PollingViewModel",
                                    "Votes updated successfully: $responseBody"
                                )
                                // Try to parse the response to get updated polling_notes_id values
                                try {
                                    val json = org.json.JSONObject(responseBody)
                                    if (json.has("data")) {
                                        val dataArray = json.getJSONArray("data")
                                        val updatedVotes = mutableListOf<CandidateVote>()

                                        // Process each vote in the response
                                        for (i in 0 until dataArray.length()) {
                                            val voteJson = dataArray.getJSONObject(i)
                                            val candidateId = voteJson.optInt("candidate_id", 0)
                                            val pollingNotesId =
                                                voteJson.optInt("polling_notes_id", 0)

                                            // Find the corresponding vote in our current list
                                            val existingVote =
                                                votes.find { it.candidateId == candidateId }
                                            if (existingVote != null) {
                                                if (pollingNotesId > 0) {
                                                    // Update the polling_notes_id with the new value from API
                                                    android.util.Log.d(
                                                        "PollingViewModel",
                                                        "Updated pollingNotesId for candidate $candidateId: $pollingNotesId (old: ${existingVote.pollingNotesId})"
                                                    )

                                                    // Create an updated copy with the new pollingNotesId
                                                    updatedVotes.add(
                                                        existingVote.copy(
                                                            pollingNotesId = pollingNotesId
                                                        )
                                                    )
                                                } else if (existingVote.pollingNotesId > 0) {
                                                    // Keep the existing pollingNotesId if API didn't return one
                                                    android.util.Log.d(
                                                        "PollingViewModel",
                                                        "Preserving existing pollingNotesId: ${existingVote.pollingNotesId} for candidate $candidateId"
                                                    )
                                                    updatedVotes.add(existingVote)
                                                } else {
                                                    // No existing or new ID
                                                    updatedVotes.add(existingVote)
                                                }
                                            } else {
                                                android.util.Log.e(
                                                    "PollingViewModel",
                                                    "Could not find vote for candidate $candidateId"
                                                )
                                            }
                                        }

                                        if (updatedVotes.isNotEmpty()) {
                                            // Update the UI with the new votes
                                            _candidateVotes.postValue(updatedVotes)
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e(
                                        "PollingViewModel",
                                        "Error parsing response for polling_notes_id values", e
                                    )
                                }

                                // Reload the summary to refresh the UI
                                loadPollingSummary(pollingId, selectedMemberId, authToken)
                                onSuccess()
                            } else {
                                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                                android.util.Log.e(
                                    "PollingViewModel",
                                    "Failed to update votes: ${response.code()} - $errorBody"
                                )
                                _errorMessage.postValue("Failed to update votes: ${response.code()} - $errorBody")
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            android.util.Log.e(
                                "PollingViewModel",
                                "Error updating votes: ${t.message}",
                                t
                            )
                            _errorMessage.postValue("Error updating votes: ${t.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("PollingViewModel", "Exception updating votes", e)
                _errorMessage.postValue("Error updating votes: ${e.message}")
            }
        }
    }
    
    // Submit a single vote for one candidate
    fun submitPollingNote(
        note: String,
        candidateId: Int,
        pollingId: Int,
        vote: Int?,
        isPrivate: Boolean,
        authToken: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                
                // Get the current member ID (or selected member ID for proxy voting)
                val selectedMemberId = _selectedMember.value?.id?.toString()
                    ?: SecureStorage.retrieve("memberId") ?: "0"

                // Get current polling order id
                val currentPollingOrderId = _currentPolling.value?.pollingOrderId?.toString() ?: "0"

                // Get the current date in the format YYYY-MM-DD
                val currentDate = java.time.LocalDate.now().toString() + "T00:00:00.000Z"

                // Get candidate name
                val candidateName =
                    _candidates.value?.find { it.candidate_id == candidateId }?.name ?: ""

                // Check if there's an existing note we're updating
                val existingNote = _candidateVotes.value?.find { it.candidateId == candidateId }
                val existingPollingNotesId = existingNote?.pollingNotesId ?: 0

                // Use existing ID if available, otherwise null for new records
                val finalPollingNotesId =
                    if (existingPollingNotesId > 0) existingPollingNotesId else null

                // Log debug information
                android.util.Log.d(
                    "PollingViewModel",
                    "submitPollingNote - candidateId: $candidateId, pollingId: $pollingId"
                )
                android.util.Log.d(
                    "PollingViewModel",
                    "submitPollingNote - Existing pollingNotesId: $existingPollingNotesId, using: $finalPollingNotesId"
                )

                // Create request object
                val pollingNote = PollingNoteRequest(
                    pollingId = pollingId,
                    candidateId = candidateId,
                    pollingCandidateId = _currentPolling.value?.pollingId?.let { it * 1000 + candidateId },
                    name = candidateName,
                    pollingOrderId = currentPollingOrderId.toInt(),
                    link = _candidates.value?.find { it.candidate_id == candidateId }?.link ?: "",
                    watchList = _candidates.value?.find { it.candidate_id == candidateId }?.watch_list
                        ?: false,
                    pollingNotesId = finalPollingNotesId,
                    note = if (note.isEmpty()) null else note,
                    vote = vote,
                    pnCreatedAt = currentDate,
                    pollingOrderMemberId = selectedMemberId.toInt(),
                    completed = true,
                    isPrivate = isPrivate,
                    authToken = authToken
                )

                val body = listOf(pollingNote)

                // Log JSON representation of the request
                try {
                    val gson =
                        com.google.gson.GsonBuilder().serializeNulls().setPrettyPrinting().create()
                    val jsonString = gson.toJson(body)
                    android.util.Log.d("PollingViewModel", "submitPollingNote - JSON to be sent:")
                    android.util.Log.d("PollingViewModel", jsonString)
                } catch (e: Exception) {
                    android.util.Log.e("PollingViewModel", "Error serializing to JSON", e)
                }

                RetrofitInstance.api.createPollingNotes(body, headers).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            android.util.Log.d(
                                "PollingViewModel",
                                "Request Headers: ${call.request().headers}"
                            )
                            android.util.Log.d(
                                "PollingViewModel",
                                "Request Body: ${call.request().body?.toString()}"
                            )

                            // Try to log the raw request body
                            try {
                                val copy = call.request().newBuilder().build()
                                val buffer = okio.Buffer()
                                copy.body?.writeTo(buffer)
                                val requestBody = buffer.readUtf8()
                                android.util.Log.d(
                                    "PollingViewModel",
                                    "Raw request body: $requestBody"
                                )
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "PollingViewModel",
                                    "Error logging request body",
                                    e
                                )
                            }
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string() ?: "{}"
                                android.util.Log.d(
                                    "PollingViewModel",
                                    "Polling note submitted successfully: $responseBody"
                                )

                                // Try to parse the response to get updated polling_notes_id
                                try {
                                    val json = org.json.JSONObject(responseBody)
                                    if (json.has("data")) {
                                        val dataArray = json.getJSONArray("data")
                                        if (dataArray.length() > 0) {
                                            val voteJson = dataArray.getJSONObject(0)
                                            val returnedPollingNotesId =
                                                voteJson.optInt("polling_notes_id", 0)

                                            if (returnedPollingNotesId > 0) {
                                                android.util.Log.d(
                                                    "PollingViewModel",
                                                    "Received new pollingNotesId: $returnedPollingNotesId for candidate $candidateId"
                                                )

                                                // Find and update the existing vote
                                                _candidateVotes.value?.let { currentVotes ->
                                                    val updatedVotes = currentVotes.map { vote ->
                                                        if (vote.candidateId == candidateId) {
                                                            vote.copy(pollingNotesId = returnedPollingNotesId)
                                                        } else {
                                                            vote
                                                        }
                                                    }
                                                    _candidateVotes.postValue(updatedVotes)
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e(
                                        "PollingViewModel",
                                        "Error parsing response for polling_notes_id", e
                                    )
                                }

                                // Reload polling summary to refresh the UI
                                loadPollingSummary(pollingId, selectedMemberId, authToken)
                                onSuccess()
                            } else {
                                _errorMessage.postValue("Failed to submit polling note: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            _errorMessage.postValue("Error submitting polling note: ${t.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.postValue("Error submitting polling note: ${e.message}")
            }
        }
    }
    
    // Parse current polling from JSON response
    private fun parseCurrentPolling(jsonString: String): Polling? {
        return try {
            val json = JSONObject(jsonString)
            
            if (json.has("polling")) {
                val pollingJson = json.getJSONObject("polling")
                
                Polling(
                    pollingId = pollingJson.optInt("polling_id", 0),
                    name = pollingJson.optString("polling_name", ""),
                    pollingOrderId = pollingJson.optInt("polling_order_id", 0),
                    startDate = pollingJson.optString("start_date", ""),
                    endDate = pollingJson.optString("end_date", ""),
                    accessToken = ""  // This field may need to be updated if used
                )
            } else if (json.has("polling_id")) {
                // Direct polling object
                Polling(
                    pollingId = json.optInt("polling_id", 0),
                    name = json.optString("polling_name", ""),
                    pollingOrderId = json.optInt("polling_order_id", 0),
                    startDate = json.optString("start_date", ""),
                    endDate = json.optString("end_date", ""),
                    accessToken = ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Parse order members from JSON response
    private fun parseOrderMembers(jsonString: String): List<PollingMember> {
        val members = mutableListOf<PollingMember>()
        
        try {
            val jsonArray = JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val memberJson = jsonArray.getJSONObject(i)
                
                // Only include active and approved members
                val isActive = memberJson.optBoolean("active", false)
                val isApproved = memberJson.optBoolean("approved", false)
                val isRemoved = memberJson.optBoolean("removed", false)
                
                if (isActive && isApproved && !isRemoved) {
                    val member = PollingMember(
                        id = memberJson.optInt("polling_order_member_id", 0),
                        name = memberJson.optString("name", ""),
                        email = memberJson.optString("email", "")
                    )
                    members.add(member)
                }
            }
        } catch (e: Exception) {
            // Handle parsing error
        }
        
        return members
    }
    
    // Parse candidates from JSON response
    private fun parseCandidates(jsonString: String): List<Candidate> {
        val candidates = mutableListOf<Candidate>()
        
        try {
            val jsonArray = JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val candidateJson = jsonArray.getJSONObject(i)
                
                val candidate = Candidate(
                    candidate_id = candidateJson.optInt("candidate_id", 0),
                    name = candidateJson.optString("name", ""),
                    link = candidateJson.optString("link", ""),
                    polling_order_id = candidateJson.optInt("polling_order_id", 0),
                    watch_list = candidateJson.optBoolean("watch_list", false)
                )
                
                candidates.add(candidate)
            }
        } catch (e: Exception) {
            // Handle parsing error
        }
        
        return candidates
    }
    
    // Parse polling summaries from JSON response
    private fun parsePollingSummaries(jsonString: String): List<PollingSummary> {
        val summaries = mutableListOf<PollingSummary>()
        
        try {
            val jsonArray = JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val summaryJson = jsonArray.getJSONObject(i)
                
                // Handle null vote values
                val voteValue = if (summaryJson.isNull("vote")) null 
                    else summaryJson.optInt("vote", -1)
                
                val summary = PollingSummary(
                    pollingId = summaryJson.optInt("polling_id", 0),
                    pollingName = summaryJson.optString("polling_name", ""),
                    startDate = summaryJson.optString("start_date", ""),
                    endDate = summaryJson.optString("end_date", ""),
                    pollingOrderId = summaryJson.optInt("polling_order_id", 0),
                    candidateId = summaryJson.optInt("candidate_id", 0),
                    pollingCandidateId = summaryJson.optInt("polling_candidate_id", 0),
                    name = summaryJson.optString("name", ""),
                    pollingNotesId = summaryJson.optInt("polling_notes_id", 0),
                    note = if (summaryJson.isNull("note")) null else summaryJson.optString("note", ""),
                    vote = voteValue,
                    pnCreatedAt = summaryJson.optString("pn_created_at", ""),
                    pollingOrderMemberId = summaryJson.optInt("polling_order_member_id", 0),
                    completed = summaryJson.optBoolean("completed", false),
                    isPrivate = summaryJson.optBoolean("private", false)
                )
                
                summaries.add(summary)
            }
        } catch (e: Exception) {
            // Handle parsing error
        }
        
        return summaries
    }
}