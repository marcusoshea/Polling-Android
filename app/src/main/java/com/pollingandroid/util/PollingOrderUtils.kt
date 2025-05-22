package com.pollingandroid.util

import android.util.Log
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.PollingOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PollingOrderUtils {
    private const val TAG = "PollingOrderUtils"

    fun fetchPollingOrders(callback: (List<PollingOrder>) -> Unit) {
        Log.d(TAG, "Starting fetchPollingOrders API call")
        RetrofitInstance.api.getPollingOrders().enqueue(object : Callback<List<PollingOrder>> {
            override fun onResponse(call: Call<List<PollingOrder>>, response: Response<List<PollingOrder>>) {
                if (response.isSuccessful) {
                    val orders = response.body()?.sortedBy { it.polling_order_name }
                    Log.d(TAG, "API call successful! Orders received: ${orders?.size}")
                    orders?.forEach {
                        Log.d(TAG, "Order: ${it.polling_order_id} - ${it.polling_order_name}")
                    }
                    callback(orders ?: emptyList())
                } else {
                    Log.e(
                        TAG,
                        "API call failed with code: ${response.code()}, Error: ${
                            response.errorBody()?.string()
                        }"
                    )
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<PollingOrder>>, t: Throwable) {
                Log.e(TAG, "API call failed with exception", t)
                callback(emptyList())
            }
        })
    }
}