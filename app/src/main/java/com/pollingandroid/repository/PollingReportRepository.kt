package com.pollingandroid.repository

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
                val response = RetrofitInstance.api.getPollingReport(orderId, headers).await()
                val jsonString = response.string()
                val pollingSummary = extractPollingSummary(jsonString)

                pollingSummary?.let { summary ->
                    val pollingId = extractPollingId(jsonString)
                    if (pollingId > 0) {
                        val pollingIdMap = mapOf(
                            "polling_notes_id" to pollingId.toString(),
                            "authToken" to authToken
                        )

                        val notesResponse =
                            RetrofitInstance.api.getAllPollingNotesById(pollingIdMap, headers)
                                .await()
                        val allNotes = parseNotes(notesResponse.string())

                        val totalsResponse =
                            RetrofitInstance.api.getPollingReportTotals(pollingId, headers).await()
                        val voteTotals = parseVoteTotals(totalsResponse.string())

                        val candidates =
                            parsePollingCandidates(allNotes, voteTotals, summary.pollingOrderScore)

                        return@withContext ReportData(
                            summary = summary,
                            candidates = candidates,
                            voteTotals = voteTotals
                        )
                    }
                }

                val candidates = parseBasicCandidateInfo(jsonString)
                return@withContext ReportData(
                    summary = pollingSummary,
                    candidates = candidates,
                    voteTotals = emptyList()
                )
            } catch (e: UnknownHostException) {
                ReportData()
            } catch (e: Exception) {
                ReportData()
            }
        }
    }

    suspend fun getInProcessPollingReport(orderId: Int, authToken: String): ReportData {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                val response =
                    RetrofitInstance.api.getInProcessPollingReport(orderId, headers).await()
                val jsonString = response.string()
                val pollingSummary = extractPollingSummary(jsonString)

                pollingSummary?.let { summary ->
                    val pollingId = extractPollingId(jsonString)
                    if (pollingId > 0) {
                        val pollingIdMap = mapOf(
                            "polling_notes_id" to pollingId.toString(),
                            "authToken" to authToken
                        )

                        val notesResponse =
                            RetrofitInstance.api.getAllPollingNotesById(pollingIdMap, headers)
                                .await()
                        val allNotes = parseNotes(notesResponse.string())

                        val totalsResponse =
                            RetrofitInstance.api.getPollingReportTotals(pollingId, headers).await()
                        val voteTotals = parseVoteTotals(totalsResponse.string())

                        val candidates =
                            parsePollingCandidates(allNotes, voteTotals, summary.pollingOrderScore)

                        return@withContext ReportData(
                            summary = summary,
                            candidates = candidates,
                            voteTotals = voteTotals
                        )
                    }
                }

                val candidates = parseBasicCandidateInfo(jsonString)
                return@withContext ReportData(
                    summary = pollingSummary,
                    candidates = candidates,
                    voteTotals = emptyList()
                )
            } catch (e: UnknownHostException) {
                ReportData()
            } catch (e: Exception) {
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
            val jsonArray = JSONArray(jsonString)

            pollingSummary = extractPollingSummary(jsonString)

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
                        val vote = noteJson.optString("vote", "")

                        notesList.add(
                            Note(
                                note = noteText,
                                private = isPrivate,
                                memberName = memberName,
                                vote = vote
                            )
                        )
                    }
                }

                val voteCountsMap = mutableMapOf<String, Int>()
                if (candidateJson.has("votes")) {
                    try {
                        val votesObj = candidateJson.getJSONObject("votes")
                        val voteTypes = listOf("Yes", "No", "Wait", "Abstain")
                        for (voteType in voteTypes) {
                            if (votesObj.has(voteType)) {
                                voteCountsMap[voteType] = votesObj.getInt(voteType)
                            } else {
                                voteCountsMap[voteType] = 0
                            }
                        }
                    } catch (e: Exception) {
                        voteCountsMap["Yes"] = 0
                        voteCountsMap["No"] = 0
                        voteCountsMap["Wait"] = 0
                        voteCountsMap["Abstain"] = 0
                    }
                } else {
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

            // Create a map to track the latest response for each member-candidate pair
            val latestNotesByMemberCandidate = mutableMapOf<Pair<String, Int>, Map<String, Any>>()

            // Process all notes and keep track of the latest one for each member-candidate pair
            for (i in 0 until jsonArray.length()) {
                val noteJson = jsonArray.getJSONObject(i)

                val noteMap = mutableMapOf<String, Any>()

                noteMap["polling_notes_id"] = noteJson.optInt("polling_notes_id", 0)
                noteMap["candidate_id"] = noteJson.optInt("candidate_id", 0)
                noteMap["vote"] = noteJson.optInt("vote", 4)
                noteMap["note"] = noteJson.optString("note", "")
                noteMap["private"] = noteJson.optBoolean("private", false)
                noteMap["member_name"] = noteJson.optString("member_name", "")

                // Add timestamp information for sorting
                noteMap["pn_created_at"] = noteJson.optString("pn_created_at", "")

                val memberName = noteMap["member_name"] as String
                val candidateId = noteMap["candidate_id"] as Int
                val memberCandidatePair = Pair(memberName, candidateId)

                // Get the existing note for this member-candidate pair, if any
                val existingNote = latestNotesByMemberCandidate[memberCandidatePair]

                if (existingNote == null) {
                    // No existing note, add this one
                    latestNotesByMemberCandidate[memberCandidatePair] = noteMap
                } else {
                    // Compare timestamps to keep only the latest
                    val existingTimestamp = existingNote["pn_created_at"] as String
                    val newTimestamp = noteMap["pn_created_at"] as String

                    // If new timestamp is greater (more recent), replace the existing note
                    if (newTimestamp > existingTimestamp) {
                        latestNotesByMemberCandidate[memberCandidatePair] = noteMap
                    }
                }

                val voteValue = noteMap["vote"] as? Int
                val voteString = when (voteValue) {
                    1 -> "Yes"
                    2 -> "Wait"
                    3 -> "No"
                    4 -> "Abstain"
                    else -> ""
                }
                noteMap["vote"] = voteString
            }

            // Add only the latest notes to the final list
            notesList.addAll(latestNotesByMemberCandidate.values)
        } catch (e: Exception) {
        }
        return notesList
    }

    private fun parseVoteTotals(jsonString: String): List<com.pollingandroid.ui.report.VoteTotal> {
        val voteTotals = mutableListOf<com.pollingandroid.ui.report.VoteTotal>()
        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val voteJson = jsonArray.getJSONObject(i)

                val rawVoteText = voteJson.optString("vote", "")
                val voteValue = when (rawVoteText) {
                    "1", "Yes", "yes" -> "Yes"
                    "2", "Wait", "wait" -> "Wait"
                    "3", "No", "no" -> "No"
                    "4", "Abstain", "abstain" -> "Abstain"
                    else -> "Null"
                }

                val totalStr = voteJson.optString("total", "0")
                val total = totalStr.toIntOrNull() ?: voteJson.optInt("total", 0)

                val name = voteJson.optString("name", "")

                val candidateId = voteJson.optInt("candidate_id", 0)
                val pollingOrderId = voteJson.optInt("polling_order_id", 0)

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

            val candidateNames = voteTotals.map { it.name }.distinct()
            candidateNames.forEach { name ->
                val candidateVotes = voteTotals.filter { it.name == name }
            }

            return voteTotals
        } catch (e: Exception) {
        }
        return emptyList()
    }

    private fun extractPollingSummary(jsonString: String): PollingSummary? {
        try {
            val jsonArray = JSONArray(jsonString)

            if (jsonArray.length() >= 3) {
                val pollingData = jsonArray.getJSONObject(0)
                val activeMembers = if (jsonArray.length() > 1) jsonArray.getJSONObject(1)
                    .optInt("active_members", 0) else 0
                val participatingMembers = if (jsonArray.length() > 2) jsonArray.getJSONObject(2)
                    .optInt("member_participation", 0) else 0

                val pollingName = pollingData.optString("polling_name", "Polling Report")
                val startDate = formatDate(pollingData.optString("start_date", ""))
                val endDate = formatDate(pollingData.optString("end_date", ""))
                val pollingOrderScore = pollingData.optInt("polling_order_polling_score", 0)
                val pollingOrderPollingType = pollingData.optInt("polling_order_polling_type", 0)
                val pollingOrderParticipation =
                    pollingData.optInt("polling_order_polling_participation", 0)

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
        }
        return null
    }

    private fun parsePollingCandidates(
        notes: List<Map<String, Any>>,
        voteTotals: List<com.pollingandroid.ui.report.VoteTotal>,
        requiredScore: Int
    ): List<Candidate> {
        val votesByCandidate = voteTotals.groupBy { it.name }
        val candidates = mutableListOf<Candidate>()

        votesByCandidate.forEach { (name, votes) ->
            val voteCountMap = mutableMapOf<String, Int>().apply {
                listOf("Yes", "No", "Wait", "Abstain").forEach { voteType ->
                    put(voteType, 0)
                }
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

            val recommendedPercentage = String.format("%.2f%%", recommendedPercent)
            val recommendedText = if (requiredScore > 0) { if (recommendedPercent >= requiredScore) {
                "has been recommended to join the order with a rating of: "
            } else {
                "has NOT been recommended to join the order with a rating of: "
            } } else { "" }

            val candidateNotes = mutableListOf<Note>()
            val candidateId = votes.firstOrNull()?.candidateId ?: 0
            notes.filter { note ->
                (note["candidate_id"] as? Int) == candidateId
            }.forEach { note ->
                val noteText = note["note"] as? String
                val voteString = note["vote"] as? String ?: ""

                // Always add the note, even if the text is blank
                candidateNotes.add(
                    Note(
                        note = noteText ?: "",
                        private = note["private"] as? Boolean ?: false,
                        memberName = note["member_name"] as? String ?: "Unknown",
                        vote = voteString
                    )
                )
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
        }
        return 0
    }

    private fun parseBasicCandidateInfo(jsonString: String): List<Candidate> {
        val candidates = mutableListOf<Candidate>()
        try {
            val jsonArray = JSONArray(jsonString)
            val requiredScore = 75

            var candidateInfo: JSONArray? = null

            for (i in 0 until jsonArray.length()) {
                try {
                    val item = jsonArray.get(i)
                    if (item is JSONArray) {
                        candidateInfo = item
                        break
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            if (candidateInfo != null) {
                for (i in 0 until candidateInfo.length()) {
                    try {
                        val candidateObj = candidateInfo.getJSONObject(i)
                        parseCandidateObject(candidateObj, candidates, requiredScore)
                    } catch (e: Exception) {
                    }
                }
            } else {
                for (i in 3 until jsonArray.length()) {
                    try {
                        val obj = jsonArray.getJSONObject(i)
                        parseCandidateObject(obj, candidates, requiredScore)
                    } catch (e: Exception) {
                    }
                }
            }

            if (candidates.isEmpty()) {
                try {
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.opt(i)
                        if (item is JSONObject) {
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
                }
            }

            if (candidates.isEmpty()) {
                return emptyList()
            }
        } catch (e: Exception) {
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

            val possibleRatingFields = listOf(
                "recommendedPercentage", "rating", "percentage", "recommended_percentage",
                "inProcessRating", "in_process_rating", "score"
            )

            var recommendedPercentage = "0%"
            for (field in possibleRatingFields) {
                if (obj.has(field)) {
                    recommendedPercentage = obj.optString(field, "0%")
                    if (!recommendedPercentage.endsWith("%")) {
                        recommendedPercentage = "${recommendedPercentage}%"
                    }
                    break
                }
            }

            val percentStr = recommendedPercentage.removeSuffix("%")
            val recommendedPercent = percentStr.toDoubleOrNull() ?: 0.0

            val recommendedText = if (requiredScore > 0.0) { if (recommendedPercent >= requiredScore) {
                "has been recommended to join the order with a rating of: "
            } else {
                "has NOT been recommended to join the order with a rating of: "
            }} else {""}

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
                                    memberName = noteObj.optString("memberName", "Unknown"),
                                    vote = noteObj.optString("vote", "")
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                }
            }

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