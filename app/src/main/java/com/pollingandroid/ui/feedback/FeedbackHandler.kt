package com.pollingandroid.ui.feedback

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.FeedbackRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

class FeedbackHandler(private val context: Context) {
    private val TAG = "FeedbackHandler"

    fun submitFeedback(
        name: String,
        email: String,
        message: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "=== FEEDBACK SUBMISSION START ===")
            Log.d(TAG, "Name: '$name'")
            Log.d(TAG, "Email: '$email'")
            Log.d(TAG, "Message length: ${message.length}")
            Log.d(TAG, "RetrofitInstance available: ${RetrofitInstance != null}")

            val feedbackRequest = FeedbackRequest(name, email, message)
            Log.d(TAG, "Created feedback request: $feedbackRequest")

            Log.d(TAG, "Getting API instance...")
            val api = RetrofitInstance.api
            Log.d(TAG, "API instance: $api")

            Log.d(TAG, "Creating call...")
            val call = api.submitFeedback(feedbackRequest)
            Log.d(TAG, "Call created: $call")
            Log.d(TAG, "Call request URL: ${call.request().url}")
            Log.d(TAG, "Call request method: ${call.request().method}")

            Log.d(TAG, "Enqueuing call...")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        Log.d(TAG, "=== RESPONSE RECEIVED ===")
                        Log.d(TAG, "Response code: ${response.code()}")
                        Log.d(TAG, "Response message: ${response.message()}")
                        Log.d(TAG, "Response headers: ${response.headers()}")

                        if (response.isSuccessful) {
                            Log.d(TAG, "✓ Feedback submitted successfully")
                            Toast.makeText(
                                context,
                                "Feedback submitted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSuccess()
                        } else {
                            val errorMessage =
                                "Failed to submit feedback: ${response.code()} - ${response.message()}"
                            Log.e(TAG, "✗ $errorMessage")
                            try {
                                val errorBody = response.errorBody()?.string()
                                Log.e(TAG, "Error body: $errorBody")
                            } catch (e: Exception) {
                                Log.e(TAG, "Could not read error body", e)
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            onError(errorMessage)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "✗ Exception in onResponse", e)
                        onError("Error processing response: ${e.message}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    try {
                        Log.e(TAG, "=== CALL FAILED ===")
                        Log.e(TAG, "Throwable type: ${t.javaClass.simpleName}")
                        Log.e(TAG, "Throwable message: ${t.message}")
                        Log.e(TAG, "API call failed", t)

                        val errorMessage = "Network error: ${t.message}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onError(errorMessage)
                    } catch (e: Exception) {
                        Log.e(TAG, "✗ Exception in onFailure", e)
                        onError("Critical error: ${e.message}")
                    }
                }
            })

            Log.d(TAG, "Call enqueued successfully")

        } catch (e: Exception) {
            Log.e(TAG, "=== EXCEPTION IN SUBMIT_FEEDBACK ===")
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Exception in submitFeedback", e)

            val errorMessage = "Exception occurred: ${e.message}"
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onError(errorMessage)
        }
    }
}
