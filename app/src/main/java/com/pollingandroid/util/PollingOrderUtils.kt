package com.pollingandroid.util

import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.PollingOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

object PollingOrderUtils {
    private const val TAG = "PollingOrderUtils"

    fun fetchPollingOrders(callback: (List<PollingOrder>) -> Unit) {
        Log.d(TAG, "Fetching polling orders...")
        RetrofitInstance.api.getPollingOrders().enqueue(object : Callback<List<PollingOrder>> {
            override fun onResponse(call: Call<List<PollingOrder>>, response: Response<List<PollingOrder>>) {
                Log.d(
                    TAG,
                    "API response: isSuccessful=${response.isSuccessful} code=${response.code()} body=${response.body()}"
                )
                if (response.isSuccessful) {
                    val orders = response.body()?.sortedBy { it.polling_order_name }
                    Log.d(TAG, "Polling orders count: ${orders?.size ?: 0}")
                    callback(orders ?: emptyList())
                } else {
                    Log.e(
                        TAG,
                        "Received unsuccessful API response: ${response.errorBody()?.string()}"
                    )
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<PollingOrder>>, t: Throwable) {
                Log.e(TAG, "API call failed", t)
                callback(emptyList())
            }
        })
    }
}
