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

    // Utility method for calculating pollingCandidateId safely
    private fun calculatePollingCandidateIdSafely(pollingId: Int, candidateId: Int): Int? {
        return try {
            // Use Long for calculation to avoid integer overflow
            val result = pollingId.toLong() * 1000L + candidateId.toLong()

            // Check if the result fits within a normal Int range
            if (result <= Int.MAX_VALUE && result >= Int.MIN_VALUE) {
                result.toInt()
            } else {
                // Fall back to null if overflow would occur
                null
            }
        } catch (e: Exception) {
            // Handle any exceptions and return null
            null
        }
    }

    // Helper function for safely parsing string to int
    private fun safeParseInt(value: String?, default: Int = 0): Int {
        return try {
            value?.toInt() ?: default
        } catch (e: NumberFormatException) {
            default
        }
    }

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
                                    loadPollingSummary(
                                        polling.pollingId,
                                        memberId,
                                        authToken
                                    )

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

                RetrofitInstance.api.getPollingSummary(pollingId, memberId, headers).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string() ?: "[]"
                                val summaries = parsePollingSummaries(responseBody)
                                val votes = summaries.map { summary ->
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
                                _errorMessage.postValue("Failed to load polling summary: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            _errorMessage.postValue("Error loading polling summary: ${t.message}")
                        }
                    }
                )
            } catch (e: Exception) {
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
                    _errorMessage.postValue("Authentication error: Please log in again")
                    return@launch
                }

                val headers = mapOf("Authorization" to "Bearer $authToken")
                
                // Get the member ID and selected member ID
                val currentMemberId = SecureStorage.retrieve("memberId") ?: "0"

                // If _selectedMember.value is null, it means "Vote as self"
                // Otherwise, use the selected member's ID
                val selectedMemberId =
                    _selectedMember.value?.id?.toString() ?: currentMemberId

                val pollingId = _currentPolling.value?.pollingId ?: 0
                val polling = _currentPolling.value

                if (polling == null) {
                    _errorMessage.postValue("Error: No active polling found")
                    return@launch
                }

                // Removed debug logs

                try {
                    // Get the current date in the format YYYY-MM-DD
                    val currentDate = java.time.LocalDate.now().toString() + "T00:00:00.000Z"

                    // Create a safe list to collect valid vote requests
                    val votesList = mutableListOf<PollingNoteRequest>()

                    // Process each vote individually with error handling
                    for (vote in votes) {
                        try {
                            // Removed debug log

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

                            // Calculate pollingCandidateId safely
                            val safePollingCandidateId =
                                calculatePollingCandidateIdSafely(pollingId, vote.candidateId)

                            // Sanitize note - ensure null if blank
                            val sanitizedNote = if (vote.note.isBlank()) null else vote.note

                            val pollingRequest = PollingNoteRequest(
                                pollingId = pollingId,
                                candidateId = vote.candidateId,
                                pollingCandidateId = safePollingCandidateId,
                                name = vote.candidateName,
                                pollingOrderId = polling.pollingOrderId,
                                link = candidateDetails?.link ?: "",
                                watchList = candidateDetails?.watch_list ?: false,
                                pollingNotesId = finalPollingNotesId,
                                note = sanitizedNote,
                                vote = vote.vote,
                                pnCreatedAt = currentDate,
                                pollingOrderMemberId = try {
                                    selectedMemberId.toInt()
                                } catch (e: Exception) {
                                    0 // Default value if conversion fails
                                },
                                completed = isCompleted,
                                isPrivate = vote.isPrivate,
                                authToken = authToken
                            )

                            // Add the request to our list
                            votesList.add(pollingRequest)

                            // Removed debug log
                        } catch (e: Exception) {
                            // Silent error handling
                        }
                    }

                    // If we couldn't create any valid vote requests, abort
                    if (votesList.isEmpty()) {
                        _errorMessage.postValue("Could not create any valid vote requests")
                        return@launch
                    }

                    // Removed debug log

                    // Make the API request
                    RetrofitInstance.api.createPollingNotes(votesList, headers).enqueue(
                        object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.isSuccessful) {
                                    try {
                                        // Call onSuccess on main thread
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            onSuccess()
                                        }

                                        // Reload data in the background without blocking
                                        viewModelScope.launch(Dispatchers.IO) {
                                            try {
                                                loadPollingSummary(
                                                    pollingId,
                                                    selectedMemberId,
                                                    authToken
                                                )
                                            } catch (e: Exception) {
                                                // Silent error handling
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Silent error handling
                                    }
                                } else {
                                    val errorBody =
                                        response.errorBody()?.string() ?: "Unknown error"
                                    _errorMessage.postValue("Failed to update votes: ${response.code()} - $errorBody")
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                _errorMessage.postValue("Error updating votes: ${t.message}")
                            }
                        }
                    )
                } catch (e: Exception) {
                    _errorMessage.postValue("Error updating votes: ${e.message}")
                }
            } catch (e: Exception) {
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
                // Removed debug log

                // Validate parameters
                if (authToken.isBlank()) {
                    _errorMessage.postValue("Authentication error: Please log in again")
                    return@launch
                }

                if (pollingId <= 0) {
                    _errorMessage.postValue("Invalid polling ID")
                    return@launch
                }

                val headers = mapOf("Authorization" to "Bearer $authToken")

                try {
                    // Get the current member ID (or selected member ID for proxy voting)
                    val selectedMemberId = _selectedMember.value?.id?.toString()
                        ?: SecureStorage.retrieve("memberId") ?: "0"

                    // Get current polling order id
                    val currentPollingOrderId =
                        _currentPolling.value?.pollingOrderId ?: 0

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

                    // Calculate pollingCandidateId safely
                    val safePollingCandidateId = _currentPolling.value?.pollingId?.let {
                        calculatePollingCandidateIdSafely(it, candidateId)
                    }

                    // Sanitize the note
                    val sanitizedNote = if (note.isEmpty()) null else note

                    // Removed debug logging

                    // Create request object
                    val pollingNote = PollingNoteRequest(
                        pollingId = pollingId,
                        candidateId = candidateId,
                        pollingCandidateId = safePollingCandidateId,
                        name = candidateName,
                        pollingOrderId = currentPollingOrderId,
                        link = _candidates.value?.find { it.candidate_id == candidateId }?.link
                            ?: "",
                        watchList = _candidates.value?.find { it.candidate_id == candidateId }?.watch_list
                            ?: false,
                        pollingNotesId = finalPollingNotesId,
                        note = sanitizedNote,
                        vote = vote,
                        pnCreatedAt = currentDate,
                        pollingOrderMemberId = try {
                            selectedMemberId.toInt()
                        } catch (e: Exception) {
                            0 // Default value if conversion fails
                        },
                        completed = true,
                        isPrivate = isPrivate,
                        authToken = authToken
                    )

                    val body = listOf(pollingNote)

                    // Removed debug logging

                    RetrofitInstance.api.createPollingNotes(body, headers).enqueue(
                        object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.isSuccessful) {
                                    try {
                                        // Call onSuccess from the main thread to avoid any threading issues
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            onSuccess()
                                        }

                                        // Reload data in the background without blocking
                                        viewModelScope.launch(Dispatchers.IO) {
                                            try {
                                                loadPollingSummary(
                                                    pollingId,
                                                    selectedMemberId,
                                                    authToken
                                                )
                                            } catch (e: Exception) {
                                                // Silent error handling
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Silent error handling
                                    }
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

                // Safely get polling_candidate_id with overflow protection
                val pollingCandidateId = try {
                    if (summaryJson.has("polling_candidate_id")) {
                        summaryJson.getInt("polling_candidate_id")
                    } else {
                        0
                    }
                } catch (e: Exception) {
                    // Handle overflow or parsing issues
                    0
                }

                val summary = PollingSummary(
                    pollingId = summaryJson.optInt("polling_id", 0),
                    pollingName = summaryJson.optString("polling_name", ""),
                    startDate = summaryJson.optString("start_date", ""),
                    endDate = summaryJson.optString("end_date", ""),
                    pollingOrderId = summaryJson.optInt("polling_order_id", 0),
                    candidateId = summaryJson.optInt("candidate_id", 0),
                    pollingCandidateId = pollingCandidateId,
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