package com.pollingandroid.repository

import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.ui.candidates.models.Candidate
import com.pollingandroid.ui.candidates.models.CandidateImage
import com.pollingandroid.ui.candidates.models.ExternalNote
import com.pollingandroid.ui.candidates.models.PollingNote
import com.pollingandroid.ui.candidates.models.PollingGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class CandidateRepository {

    suspend fun getAllCandidates(orderId: Int, authToken: String): List<Candidate> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                val response = RetrofitInstance.api.getAllCandidates(orderId, headers).execute()

                if (response.isSuccessful) {
                    val jsonString = response.body()?.string() ?: "[]"
                    parseAllCandidates(jsonString)
                } else {
                    emptyList()
                }
            } catch (e: IOException) {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getCandidateImages(candidateId: Int, authToken: String): List<CandidateImage> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                val response =
                    RetrofitInstance.api.getAllCandidateImages(candidateId.toString(), headers)
                        .execute()

                if (response.isSuccessful) {
                    val jsonString = response.body()?.string() ?: "[]"
                    parseCandidateImages(jsonString)
                } else {
                    emptyList()
                }
            } catch (e: IOException) {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun toggleWatchlist(
        candidateId: Int,
        isOnWatchlist: Boolean,
        authToken: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                val body = mapOf(
                    "candidate_id" to candidateId.toString(),
                    "watch_list" to isOnWatchlist.toString()
                )

                // This is a placeholder - the actual endpoint would need to be implemented in the API
                // For now, just return success
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getPollingNotes(candidateId: Int, authToken: String): List<PollingNote> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                val response =
                    RetrofitInstance.api.getPollingNoteByCandidateId(candidateId, headers).execute()

                if (response.isSuccessful) {
                    val jsonString = response.body()?.string() ?: "[]"
                    parsePollingNotes(jsonString)
                } else {
                    emptyList()
                }
            } catch (e: IOException) {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getExternalNotes(candidateId: Int, authToken: String): List<ExternalNote> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")
                val response =
                    RetrofitInstance.api.getExternalNoteByCandidateId(candidateId, headers)
                        .execute()

                if (response.isSuccessful) {
                    val jsonString = response.body()?.string() ?: "[]"
                    parseExternalNotes(jsonString)
                } else {
                    emptyList()
                }
            } catch (e: IOException) {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun createExternalNote(
        candidateId: Int,
        note: String,
        isPrivate: Boolean,
        authToken: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")

                // Get the current date in the format YYYY-MM-DD
                val currentDate = java.time.LocalDate.now().toString()

                // Get the member ID from SecureStorage
                val memberId = com.pollingandroid.ui.login.SecureStorage.retrieve("memberId") ?: "0"

                // Use the format required by the API
                val body = mapOf(
                    "external_note" to note,
                    "candidate_id" to candidateId.toString(),
                    "polling_order_member_id" to memberId,
                    "en_created_at" to currentDate,
                    "authToken" to authToken
                )

                val response = RetrofitInstance.api.createExternalNote(body, headers).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun deleteExternalNote(noteId: Int, authToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf("Authorization" to "Bearer $authToken")

                // Get the member ID from SecureStorage
                val memberId = com.pollingandroid.ui.login.SecureStorage.retrieve("memberId") ?: "0"

                // Convert string values to integers for the API
                val memberIdInt = memberId.toIntOrNull() ?: 0

                // Create JSON in the exact format needed by the server
                val jsonBody = """
                    {
                        "external_notes_id": $noteId,
                        "polling_order_member_id": $memberIdInt,
                        "authToken": "$authToken"
                    }
                """.trimIndent()

                // Create the request body with the correct media type
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonBody.toRequestBody(mediaType)

                // Use OkHttp directly to make the request with the exact JSON format
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api-polling.aethelmearc.org/externalnote/delete")
                    .post(requestBody)
                    .header("Authorization", "Bearer $authToken")
                    .build()

                val response = client.newCall(request).execute()

                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun parseAllCandidates(jsonString: String): List<Candidate> {
        val candidateList = mutableListOf<Candidate>()

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

                candidateList.add(candidate)
            }
        } catch (e: Exception) {
            // Error parsing candidate JSON
        }

        return candidateList
    }

    private fun parsePollingNotes(jsonString: String): List<PollingNote> {
        val notesList = mutableListOf<PollingNote>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val noteJson = jsonArray.getJSONObject(i)

                // Make sure to handle null notes properly
                val noteText = if (noteJson.isNull("note")) null else noteJson.optString("note", "")

                val note = PollingNote(
                    pollingNotesId = noteJson.optInt("polling_notes_id", 0),
                    note = noteText,
                    vote = noteJson.optInt("vote", 0),
                    pollingId = noteJson.optInt("polling_id", 0),
                    candidateId = noteJson.optInt("candidate_id", 0),
                    pollingOrderId = noteJson.optInt("polling_order_id", 0),
                    createdAt = noteJson.optString("pn_created_at", ""),
                    pollingOrderMemberId = noteJson.optInt("polling_order_member_id", 0),
                    completed = noteJson.optBoolean("completed", false),
                    isPrivate = noteJson.optBoolean("private", false),
                    memberName = noteJson.optString("name", ""),
                    pollingName = noteJson.optString("polling_name", ""),
                    startDate = noteJson.optString("start_date", ""),
                    endDate = noteJson.optString("end_date", "")
                )

                notesList.add(note)
            }
        } catch (e: Exception) {
            // Error parsing polling notes JSON
        }

        return notesList
    }

    private fun parseExternalNotes(jsonString: String): List<ExternalNote> {
        val notesList = mutableListOf<ExternalNote>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val noteJson = jsonArray.getJSONObject(i)

                // Extract member name from nested object if available
                var memberName = ""
                var memberId = 0
                if (noteJson.has("polling_order_member_id") && !noteJson.isNull("polling_order_member_id")) {
                    val memberValue = noteJson.get("polling_order_member_id")
                    if (memberValue is JSONObject) {
                        // If it's a JSON object, extract name and id from it
                        val memberObj = noteJson.getJSONObject("polling_order_member_id")
                        memberName = memberObj.optString("name", "")
                        memberId = memberObj.optInt(
                            "polling_order_member_id",
                            0
                        ) // Fixed to use "polling_order_member_id" instead of "id"
                    } else if (memberValue is Int) {
                        // If it's an integer, directly use it as the member ID
                        memberId = memberValue
                    } else {
                        // If it's a string, try to parse it as an integer
                        try {
                            memberId = memberValue.toString().toInt()
                        } catch (e: NumberFormatException) {
                            // Failed to parse member ID from string
                        }
                    }
                }

                // Format date to show only the date part (YYYY-MM-DD)
                val fullDate = noteJson.optString("en_created_at", "")
                val formattedDate = if (fullDate.isNotEmpty()) {
                    fullDate.split("T")[0]  // Take only the part before 'T'
                } else {
                    ""
                }

                val note = ExternalNote(
                    externalNoteId = noteJson.optInt("external_notes_id", 0),
                    candidateId = noteJson.optInt("candidate_id", 0),
                    note = noteJson.optString("external_note", ""),
                    createdAt = formattedDate,
                    updatedAt = noteJson.optString("updated_at", ""),
                    memberName = memberName,
                    memberId = memberId,
                    isPrivate = noteJson.optBoolean("private", false)
                )

                notesList.add(note)
            }
        } catch (e: Exception) {
            // Error parsing external notes JSON
        }

        return notesList
    }

    private fun parseCandidateImages(jsonString: String): List<CandidateImage> {
        val imagesList = mutableListOf<CandidateImage>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val imageJson = jsonArray.getJSONObject(i)

                val image = CandidateImage(
                    imageId = imageJson.optInt("image_id", 0),
                    candidateId = imageJson.optInt("candidate_id", 0),
                    imageUrl = imageJson.optString("aws_key", ""),
                    description = imageJson.optString("image_description", ""),
                    uploadedBy = imageJson.optString("uploaded_by", ""),
                    uploadDate = imageJson.optString("upload_date", "")
                )

                imagesList.add(image)
            }
        } catch (e: Exception) {
            // Error parsing candidate images JSON
        }

        return imagesList
    }

    fun groupPollingNotesByPolling(notes: List<PollingNote>): List<PollingGroup> {
        // Group notes by pollingId
        val groupedNotes = notes.groupBy { it.pollingId }

        // Create PollingGroup objects
        val pollingGroups = mutableListOf<PollingGroup>()

        groupedNotes.forEach { (pollingId, pollingNotes) ->
            // Extract polling name, start date, and end date from the first note in the group
            // All notes in a polling group should have the same polling information
            val firstNote = pollingNotes.firstOrNull()
            val pollingName = firstNote?.pollingName ?: "Polling #$pollingId"
            val startDate = firstNote?.startDate ?: ""
            val endDate = firstNote?.endDate ?: ""

            // Sort notes so that non-null notes come first
            val sortedNotes =
                pollingNotes.sortedWith(compareByDescending { !it.note.isNullOrBlank() })

            pollingGroups.add(
                PollingGroup(
                    pollingId = pollingId,
                    pollingName = pollingName,
                    startDate = startDate,
                    endDate = endDate,
                    notes = sortedNotes
                )
            )
        }

        // Sort by pollingId descending (most recent first)
        return pollingGroups.sortedByDescending { it.pollingId }
    }
}