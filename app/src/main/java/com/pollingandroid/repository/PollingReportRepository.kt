package com.pollingandroid.repository

import android.util.Log
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.ui.report.Candidate
import com.pollingandroid.ui.report.Note
import com.pollingandroid.ui.report.PollingSummary
import com.pollingandroid.ui.report.VoteTotal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.await
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale

class PollingReportRepository {

    data class ReportData(
        val summary: PollingSummary? = null,
        val candidates: List<Candidate> = emptyList(),
        val voteTotals: List<com.pollingandroid.ui.report.VoteTotal> = emptyList()
    )

    suspend fun getPollingReport(orderId: Int, authToken: String): ReportData {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                Log.d("PollingReportRepository", "Fetching polling report for order ID: $orderId")
                val response = RetrofitInstance.api.getPollingReport(orderId, headers).await()
                Log.d("PollingReportRepository", "Received polling report response")

                val jsonString = response.string()
                val pollingSummary = extractPollingSummary(jsonString)

                // After getting the polling summary, fetch notes and vote totals if we have a valid polling ID
                pollingSummary?.let { summary ->
                    val pollingId = extractPollingId(jsonString)
                    if (pollingId > 0) {
                        // Create a map with polling ID and auth token
                        val pollingIdMap = mapOf(
                            "polling_notes_id" to pollingId.toString(),
                            "authToken" to authToken
                        )

                        // Get polling notes
                        Log.d(
                            "PollingReportRepository",
                            "Sending request to getAllPollingNotesById with polling_notes_id: $pollingId"
                        )
                        Log.d(
                            "PollingReportRepository",
                            "Request body: $pollingIdMap, headers: $headers"
                        )
                        val notesResponse =
                            RetrofitInstance.api.getAllPollingNotesById(pollingIdMap, headers)
                                .await()
                        val allNotes = parseNotes(notesResponse.string())

                        // Get vote totals
                        val totalsResponse =
                            RetrofitInstance.api.getPollingReportTotals(pollingId, headers).await()
                        val voteTotals = parseVoteTotals(totalsResponse.string())

                        // Parse full candidate info with notes and votes
                        val candidates =
                            parsePollingCandidates(allNotes, voteTotals, summary.pollingOrderScore)

                        return@withContext ReportData(
                            summary = summary,
                            candidates = candidates,
                            voteTotals = voteTotals
                        )
                    }
                }

                // Fallback - basic parsing if we couldn't get more detailed data
                val candidates = parseBasicCandidateInfo(jsonString)
                return@withContext ReportData(
                    summary = pollingSummary,
                    candidates = candidates,
                    voteTotals = emptyList()
                )
            } catch (e: UnknownHostException) {
                // Network error
                Log.e("PollingReportRepository", "Network error getting polling report", e)
                ReportData()
            } catch (e: Exception) {
                // Other errors
                Log.e("PollingReportRepository", "Error getting polling report", e)
                ReportData()
            }
        }
    }

    suspend fun getInProcessPollingReport(orderId: Int, authToken: String): ReportData {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                Log.d(
                    "PollingReportRepository",
                    "Fetching in-process polling report for order ID: $orderId"
                )
                val response =
                    RetrofitInstance.api.getInProcessPollingReport(orderId, headers).await()
                Log.d("PollingReportRepository", "Received in-process polling report response")

                val jsonString = response.string()
                val pollingSummary = extractPollingSummary(jsonString)

                // After getting the polling summary, fetch notes and vote totals if we have a valid polling ID
                pollingSummary?.let { summary ->
                    val pollingId = extractPollingId(jsonString)
                    if (pollingId > 0) {
                        // Create a map with polling ID and auth token
                        val pollingIdMap = mapOf(
                            "polling_notes_id" to pollingId.toString(),
                            "authToken" to authToken
                        )

                        // Get polling notes
                        Log.d(
                            "PollingReportRepository",
                            "Sending request to getAllPollingNotesById with polling_notes_id: $pollingId"
                        )
                        Log.d(
                            "PollingReportRepository",
                            "Request body: $pollingIdMap, headers: $headers"
                        )
                        val notesResponse =
                            RetrofitInstance.api.getAllPollingNotesById(pollingIdMap, headers)
                                .await()
                        val allNotes = parseNotes(notesResponse.string())

                        // Get vote totals
                        val totalsResponse =
                            RetrofitInstance.api.getPollingReportTotals(pollingId, headers).await()
                        val voteTotals = parseVoteTotals(totalsResponse.string())

                        // Parse full candidate info with notes and votes
                        val candidates =
                            parsePollingCandidates(allNotes, voteTotals, summary.pollingOrderScore)

                        return@withContext ReportData(
                            summary = summary,
                            candidates = candidates,
                            voteTotals = voteTotals
                        )
                    }
                }

                // Fallback - basic parsing
                val candidates = parseBasicCandidateInfo(jsonString)
                return@withContext ReportData(
                    summary = pollingSummary,
                    candidates = candidates,
                    voteTotals = emptyList()
                )
            } catch (e: UnknownHostException) {
                // Network error
                Log.e(
                    "PollingReportRepository",
                    "Network error getting in-process polling report",
                    e
                )
                ReportData()
            } catch (e: Exception) {
                // Other errors
                Log.e("PollingReportRepository", "Error getting in-process polling report", e)
                ReportData()
            }
        }
    }

    private fun parseFullPollingReportResponse(
        pollingResponse: ResponseBody,
        notesResponse: ResponseBody,
        totalsResponse: ResponseBody
    ): ReportData {
        val pollingSummary = extractPollingSummary(pollingResponse.string())
        val allNotes = parseNotes(notesResponse.string())
        val voteTotals = parseVoteTotals(totalsResponse.string())
        val candidates =
            parsePollingCandidates(allNotes, voteTotals, pollingSummary?.pollingOrderScore ?: 70)

        return ReportData(
            summary = pollingSummary,
            candidates = candidates,
            voteTotals = voteTotals
        )
    }

    private fun parsePollingReportResponse(response: ResponseBody): ReportData {
        val candidates = mutableListOf<Candidate>()
        val voteTotals = mutableListOf<com.pollingandroid.ui.report.VoteTotal>()
        var pollingSummary: PollingSummary? = null

        try {
            val jsonString = response.string()
            Log.d("PollingReportRepository", "Response JSON: ${jsonString.take(200)}...")
            val jsonArray = JSONArray(jsonString)

            pollingSummary = extractPollingSummary(jsonString)

            // Fallback parsing for candidates
            for (i in 0 until jsonArray.length()) {
                val candidateJson = jsonArray.getJSONObject(i)
                if (!candidateJson.has("name")) continue

                val name = candidateJson.getString("name")
                val recommendedPercentage = candidateJson.getString("recommendedPercentage")
                val recommendedText = ""

                val notesList = mutableListOf<Note>()
                if (candidateJson.has("notes")) {
                    val notesArray = candidateJson.getJSONArray("notes")
                    for (j in 0 until notesArray.length()) {
                        val noteJson = notesArray.getJSONObject(j)
                        val noteText = noteJson.getString("note")
                        val isPrivate = noteJson.getBoolean("private")
                        val memberName = noteJson.getString("memberName")

                        notesList.add(
                            Note(
                                note = noteText,
                                private = isPrivate,
                                memberName = memberName
                            )
                        )
                    }
                }

                // Extract vote counts if available
                val voteCountsMap = mutableMapOf<String, Int>()
                if (candidateJson.has("votes")) {
                    try {
                        val votesObj = candidateJson.getJSONObject("votes")
                        val voteTypes = listOf("Yes", "No", "Wait", "Abstain")
                        for (voteType in voteTypes) {
                            if (votesObj.has(voteType)) {
                                voteCountsMap[voteType] = votesObj.getInt(voteType)
                            } else {
                                // Default to 0 if not found
                                voteCountsMap[voteType] = 0
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PollingReportRepository", "Error parsing votes for $name", e)
                        // Ensure defaults are set
                        voteCountsMap["Yes"] = 0
                        voteCountsMap["No"] = 0
                        voteCountsMap["Wait"] = 0
                        voteCountsMap["Abstain"] = 0
                    }
                } else {
                    // Default values if no votes section
                    voteCountsMap["Yes"] = 0
                    voteCountsMap["No"] = 0
                    voteCountsMap["Wait"] = 0
                    voteCountsMap["Abstain"] = 0
                }

                candidates.add(
                    Candidate(
                        name = name,
                        recommended = recommendedText,
                        inProcessRating = recommendedPercentage,
                        notes = notesList,
                        voteCounts = voteCountsMap
                    )
                )
            }
        } catch (e: Exception) {
            // Handle JSON parsing errors
            Log.e("PollingReportRepository", "Error parsing JSON response", e)
        }

        return ReportData(
            summary = pollingSummary,
            candidates = candidates,
            voteTotals = voteTotals
        )
    }

    private fun parseNotes(jsonString: String): List<Map<String, Any>> {
        val notesList = mutableListOf<Map<String, Any>>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val noteJson = jsonArray.getJSONObject(i)

                // Skip notes where the note field is null
                if (noteJson.isNull("note")) continue

                val noteMap = mutableMapOf<String, Any>()

                // Extract note data
                noteMap["polling_notes_id"] = noteJson.optInt("polling_notes_id", 0)
                noteMap["candidate_id"] = noteJson.optInt("candidate_id", 0)
                noteMap["vote"] = noteJson.optInt("vote", 4)
                noteMap["note"] = noteJson.optString("note", "")
                noteMap["private"] = noteJson.optBoolean("private", false)
                noteMap["member_name"] = noteJson.optString("member_name", "")

                notesList.add(noteMap)
            }
        } catch (e: Exception) {
            Log.e("PollingReportRepository", "Error parsing notes", e)
        }
        return notesList
    }

    private fun parseVoteTotals(jsonString: String): List<com.pollingandroid.ui.report.VoteTotal> {
        val voteTotals = mutableListOf<com.pollingandroid.ui.report.VoteTotal>()
        try {
            val jsonArray = JSONArray(jsonString)
            Log.d("PollingReportRepository", "Parsing ${jsonArray.length()} vote totals")
            Log.d("PollingReportRepository", "Vote totals raw JSON: ${jsonString.take(200)}...")

            for (i in 0 until jsonArray.length()) {
                val voteJson = jsonArray.getJSONObject(i)

                // Extract raw vote text
                val rawVoteText = voteJson.optString("vote", "")
                Log.d("PollingReportRepository", "Raw vote text: $rawVoteText")

                // Convert vote to standardized format
                val voteValue = when {
                    rawVoteText.equals("Yes", ignoreCase = true) -> "Yes"
                    rawVoteText.equals("No", ignoreCase = true) -> "No"
                    rawVoteText.equals("Wait", ignoreCase = true) -> "Wait"
                    rawVoteText.equals("Abstain", ignoreCase = true) -> "Abstain"
                    rawVoteText.equals("1", ignoreCase = true) -> "Yes"
                    rawVoteText.equals("2", ignoreCase = true) -> "No"
                    rawVoteText.equals("3", ignoreCase = true) -> "Wait"
                    rawVoteText.equals("4", ignoreCase = true) -> "Abstain"
                    else -> "Null"
                }

                // Convert total to integer - handling both string and int formats
                val totalStr = voteJson.optString("total", "0")
                val total = totalStr.toIntOrNull() ?: voteJson.optInt("total", 0)

                val name = voteJson.optString("name", "")

                // Extract candidate_id and polling_order_id
                val candidateId = voteJson.optInt("candidate_id", 0)
                val pollingOrderId = voteJson.optInt("polling_order_id", 0)

                Log.d(
                    "PollingReportRepository",
                    "Vote parsed: name=$name, vote=$voteValue, total=$total, candidate_id=$candidateId, polling_order_id=$pollingOrderId"
                )

                voteTotals.add(
                    com.pollingandroid.ui.report.VoteTotal(
                        name = name,
                        vote = voteValue,
                        total = total,
                        candidateId = candidateId,
                        pollingOrderId = pollingOrderId
                    )
                )
            }

            // Log the unique candidate names and their vote counts
            val candidateNames = voteTotals.map { it.name }.distinct()
            Log.d(
                "PollingReportRepository",
                "Found ${candidateNames.size} unique candidates in votes"
            )
            candidateNames.forEach { name ->
                val candidateVotes = voteTotals.filter { it.name == name }
                Log.d(
                    "PollingReportRepository",
                    "Votes for $name: ${candidateVotes.joinToString { "${it.vote}=${it.total}" }}"
                )
            }

            // Final debug output showing full vote data
            Log.d("PollingReportRepository", "Complete vote data: $voteTotals")
        } catch (e: Exception) {
            Log.e("PollingReportRepository", "Error parsing vote totals", e)
        }
        return voteTotals
    }

    private fun extractPollingSummary(jsonString: String): PollingSummary? {
        // The polling summary values need to come from the following fields in the JSON response:
        // polling_name, start_date, end_date, polling_order_polling_score, polling_order_polling_type, 
        // polling_order_polling_participation, active_members, member_participation. 
        // This function is responsible for extracting these values and creating a PollingSummary object.
        try {
            val jsonArray = JSONArray(jsonString)

            // Check if we have polling data
            if (jsonArray.length() >= 3) {
                val pollingData = jsonArray.getJSONObject(0)
                val activeMembers = if (jsonArray.length() > 1) jsonArray.getJSONObject(1)
                    .optInt("active_members", 0) else 0
                val participatingMembers = if (jsonArray.length() > 2) jsonArray.getJSONObject(2)
                    .optInt("member_participation", 0) else 0

                // Log raw polling data to verify field names
                Log.d(
                    "PollingReportRepository",
                    "Raw polling data: ${pollingData.toString().take(500)}"
                )

                // Log whether the expected fields exist
                Log.d("PollingReportRepository", "Field existence check:")
                Log.d(
                    "PollingReportRepository",
                    "polling_name exists: ${pollingData.has("polling_name")}"
                )
                Log.d(
                    "PollingReportRepository",
                    "polling_order_polling_score exists: ${pollingData.has("polling_order_polling_score")}"
                )
                Log.d(
                    "PollingReportRepository",
                    "polling_order_polling_type exists: ${pollingData.has("polling_order_polling_type")}"
                )
                Log.d(
                    "PollingReportRepository",
                    "polling_order_polling_participation exists: ${pollingData.has("polling_order_polling_participation")}"
                )

                val pollingName = pollingData.optString("polling_name", "Polling Report")
                val startDate = formatDate(pollingData.optString("start_date", ""))
                val endDate = formatDate(pollingData.optString("end_date", ""))
                val pollingOrderScore = pollingData.optInt("polling_order_polling_score", 0)
                val pollingOrderPollingType = pollingData.optInt("polling_order_polling_type", 0)
                val pollingOrderParticipation =
                    pollingData.optInt("polling_order_polling_participation", 0)

                // Log the extracted values
                Log.d("PollingReportRepository", "Extracted polling settings:")
                Log.d("PollingReportRepository", "polling_order_polling_score: $pollingOrderScore")
                Log.d(
                    "PollingReportRepository",
                    "polling_order_polling_type: $pollingOrderPollingType"
                )
                Log.d(
                    "PollingReportRepository",
                    "polling_order_polling_participation: $pollingOrderParticipation"
                )

                // Calculate participation rate and certified status
                val participationRate = if (activeMembers > 0) {
                    String.format(
                        "%.2f",
                        (participatingMembers.toFloat() / activeMembers.toFloat()) * 100
                    )
                } else "0.00"

                val certified =
                    if (participatingMembers.toFloat() / activeMembers.toFloat() * 100 >= pollingOrderParticipation) {
                        "certified"
                    } else "not certified"

                return PollingSummary(
                    pollingName = pollingName,
                    startDate = startDate,
                    endDate = endDate,
                    pollingOrderPollingType = pollingOrderPollingType,
                    pollingOrderParticipation = pollingOrderParticipation,
                    pollingOrderScore = pollingOrderScore,
                    activeMembers = activeMembers,
                    participatingMembers = participatingMembers,
                    participationRate = participationRate,
                    certified = certified
                )
            }
        } catch (e: Exception) {
            Log.e("PollingReportRepository", "Error extracting polling summary", e)
        }
        return null
    }

    private fun parsePollingCandidates(
        notes: List<Map<String, Any>>,
        voteTotals: List<com.pollingandroid.ui.report.VoteTotal>,
        requiredScore: Int
    ): List<Candidate> {
        // Process vote totals by candidate name
        val votesByCandidate = voteTotals.groupBy { it.name }
        val candidates = mutableListOf<Candidate>()

        votesByCandidate.forEach { (name, votes) ->
            // Calculate vote counts
            val voteCountMap = mutableMapOf<String, Int>().apply {
                // Initialize all vote types to 0
                listOf("Yes", "No", "Wait", "Abstain").forEach { voteType ->
                    put(voteType, 0)
                }
                // Update with actual vote counts
                votes.forEach { voteTotal ->
                    put(voteTotal.vote, voteTotal.total)
                }
            }

            val yesCount = voteCountMap["Yes"] ?: 0
            val noCount = voteCountMap["No"] ?: 0
            val waitCount = voteCountMap["Wait"] ?: 0

            val totalVotesMinusAbstain = yesCount + noCount + waitCount
            val recommendedPercent = if (totalVotesMinusAbstain > 0) {
                (yesCount.toFloat() / totalVotesMinusAbstain.toFloat()) * 100
            } else 0f

            Log.d(
                "PollingReportRepository",
                "Candidate $name vote counts: Yes=$yesCount, No=$noCount, Wait=$waitCount"
            )
            Log.d(
                "PollingReportRepository",
                "Recommendation calculation: $recommendedPercent% (required: $requiredScore%)"
            )

            val recommendedPercentage = String.format("%.2f%%", recommendedPercent)
            val recommendedText = if (requiredScore > 0) { if (recommendedPercent >= requiredScore) {
                "has been recommended to join the order with a rating of: "
            } else {
                "has NOT been recommended to join the order with a rating of: "
            } } else { "" }

            // Get notes for this candidate
            val candidateNotes = mutableListOf<Note>()
            // First, try to find the candidateId for this name from the vote totals
            val candidateId = votes.firstOrNull()?.candidateId ?: 0
            // Then filter notes by candidate_id instead of candidate_name
            notes.filter { note ->
                (note["candidate_id"] as? Int) == candidateId
            }.forEach { note ->
                val noteText = note["note"] as? String
                if (!noteText.isNullOrBlank()) {
                    candidateNotes.add(
                        Note(
                            note = noteText,
                            private = note["private"] as? Boolean ?: false,
                            memberName = note["member_name"] as? String ?: "Unknown"
                        )
                    )
                }
            }

            candidates.add(
                Candidate(
                    name = name,
                    recommended = recommendedText,
                    inProcessRating = recommendedPercentage,
                    voteCounts = voteCountMap,
                    notes = candidateNotes
                )
            )
        }

        return candidates.sortedByDescending {
            it.inProcessRating.removeSuffix("%").toFloatOrNull() ?: 0f
        }
    }

    private fun formatDate(dateString: String): String {
        if (dateString.isEmpty()) return ""

        try {
            val parts = dateString.split("T")
            if (parts.isNotEmpty()) {
                val datePart = parts[0]
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

                val date = inputFormat.parse(datePart)
                date?.let {
                    return outputFormat.format(it)
                }
            }
        } catch (e: Exception) {
            Log.e("PollingReportRepository", "Error formatting date", e)
        }

        return dateString.split("T")[0]
    }

    private fun extractPollingId(jsonString: String): Int {
        try {
            val jsonArray = JSONArray(jsonString)
            if (jsonArray.length() > 0) {
                val pollingData = jsonArray.getJSONObject(0)
                return pollingData.optInt("polling_id", 0)
            }
        } catch (e: Exception) {
            Log.e("PollingReportRepository", "Error extracting polling ID", e)
        }
        return 0
    }

    private fun parseBasicCandidateInfo(jsonString: String): List<Candidate> {
        val candidates = mutableListOf<Candidate>()
        try {
            Log.d(
                "PollingReportRepository",
                "Parsing candidate info from: ${jsonString.take(100)}..."
            )

            // Parse the response JSON
            val jsonArray = JSONArray(jsonString)
            val requiredScore = 75  // Default value for scoring

            // The main response should have at least 3 objects:
            // 1. Polling summary data
            // 2. Active members count
            // 3. Participating members count

            // If there's more data after that, it might be candidate information
            var candidateInfo: JSONArray? = null

            // First, check if any item in the array is a JSONArray - this could be our candidate list
            for (i in 0 until jsonArray.length()) {
                try {
                    val item = jsonArray.get(i)
                    if (item is JSONArray) {
                        candidateInfo = item
                        break
                    }
                } catch (e: Exception) {
                    // Not a JSONArray, continue
                    continue
                }
            }

            // If we found a candidate array, parse it
            if (candidateInfo != null) {
                for (i in 0 until candidateInfo.length()) {
                    try {
                        val candidateObj = candidateInfo.getJSONObject(i)
                        parseCandidateObject(candidateObj, candidates, requiredScore)
                    } catch (e: Exception) {
                        Log.e("PollingReportRepository", "Error parsing candidate item", e)
                    }
                }
            } else {
                // If no array was found, try parsing individual objects after index 3
                for (i in 3 until jsonArray.length()) {
                    try {
                        val obj = jsonArray.getJSONObject(i)
                        parseCandidateObject(obj, candidates, requiredScore)
                    } catch (e: Exception) {
                        // Not a candidate object, continue
                        continue
                    }
                }
            }

            // If we still didn't find candidates, look through more complex structures
            if (candidates.isEmpty()) {
                try {
                    // The response might have a complicated structure
                    // Look for any object with candidate-like properties
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.opt(i)
                        if (item is JSONObject) {
                            // Check if this might be a container for candidates
                            if (item.has("candidates")) {
                                val candidatesArray = item.getJSONArray("candidates")
                                for (j in 0 until candidatesArray.length()) {
                                    parseCandidateObject(
                                        candidatesArray.getJSONObject(j),
                                        candidates,
                                        requiredScore
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        "PollingReportRepository",
                        "Error looking for candidates in complex structure",
                        e
                    )
                }
            }

            // Return empty list if no candidates were found
            if (candidates.isEmpty()) {
                Log.d("PollingReportRepository", "No candidates found")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("PollingReportRepository", "Error parsing basic candidate info", e)
        }
        return candidates
    }

    private fun parseCandidateObject(
        obj: JSONObject,
        candidates: MutableList<Candidate>,
        requiredScore: Int
    ) {
        if (obj.has("name")) {
            val name = obj.getString("name")

            // Try different possible field names for the rating percentage
            val possibleRatingFields = listOf(
                "recommendedPercentage", "rating", "percentage", "recommended_percentage",
                "inProcessRating", "in_process_rating", "score"
            )

            var recommendedPercentage = "0%"
            for (field in possibleRatingFields) {
                if (obj.has(field)) {
                    recommendedPercentage = obj.optString(field, "0%")
                    // Ensure percentage has % suffix
                    if (!recommendedPercentage.endsWith("%")) {
                        recommendedPercentage = "${recommendedPercentage}%"
                    }
                    break
                }
            }

            // Convert percentage to double for comparison
            val percentStr = recommendedPercentage.removeSuffix("%")
            val recommendedPercent = percentStr.toDoubleOrNull() ?: 0.0

            val recommendedText = if (requiredScore > 0.0) { if (recommendedPercent >= requiredScore) {
                "has been recommended to join the order with a rating of: "
            } else {
                "has NOT been recommended to join the order with a rating of: "
            }} else {""}

            // Extract notes if available
            val notesList = mutableListOf<Note>()
            if (obj.has("notes")) {
                try {
                    val notesArray = obj.getJSONArray("notes")
                    for (j in 0 until notesArray.length()) {
                        val noteObj = notesArray.getJSONObject(j)
                        val note = noteObj.optString("note", "")
                        if (note.isNotEmpty()) {
                            notesList.add(
                                Note(
                                    note = note,
                                    private = noteObj.optBoolean("private", false),
                                    memberName = noteObj.optString("memberName", "Unknown")
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Failed to parse notes
                }
            }

            // Create vote counts map
            val voteCountsMap = mutableMapOf<String, Int>()
            if (obj.has("votes")) {
                try {
                    val votesObj = obj.getJSONObject("votes")
                    val voteTypes = listOf("Yes", "No", "Wait", "Abstain")
                    for (voteType in voteTypes) {
                        if (votesObj.has(voteType)) {
                            voteCountsMap[voteType] = votesObj.getInt(voteType)
                        }
                    }
                } catch (e: Exception) {
                    // Failed to parse votes
                }
            }

            candidates.add(
                Candidate(
                    name = name,
                    recommended = recommendedText,
                    inProcessRating = recommendedPercentage,
                    voteCounts = voteCountsMap,
                    notes = notesList
                )
            )
        }
    }
}