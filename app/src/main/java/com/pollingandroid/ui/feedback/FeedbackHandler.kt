package com.pollingandroid.ui.feedback

import android.content.Context
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
            val feedbackRequest = FeedbackRequest(name, email, message)

            val api = RetrofitInstance.api
            val call = api.submitFeedback(feedbackRequest)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Feedback submitted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSuccess()
                        } else {
                            val errorMessage =
                                "Failed to submit feedback: ${response.code()} - ${response.message()}"
                            try {
                                val errorBody = response.errorBody()?.string()
                                if (errorBody != null) {
                                    // Error body processing remains but without logging
                                }
                            } catch (e: Exception) {
                                // Silent error handling
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            onError(errorMessage)
                        }
                    } catch (e: Exception) {
                        onError("Error processing response: ${e.message}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    try {
                        val errorMessage = "Network error: ${t.message}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onError(errorMessage)
                    } catch (e: Exception) {
                        onError("Critical error: ${e.message}")
                    }
                }
            })

        } catch (e: Exception) {
            val errorMessage = "Exception occurred: ${e.message}"
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onError(errorMessage)
        }
    }
}
