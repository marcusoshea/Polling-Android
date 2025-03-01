package com.pollingandroid.util

import android.util.Log
import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.PollingOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PollingOrderUtils {
    fun fetchPollingOrders(callback: (List<PollingOrder>) -> Unit) {
        RetrofitInstance.api.getPollingOrders().enqueue(object : Callback<List<PollingOrder>> {
            override fun onResponse(call: Call<List<PollingOrder>>, response: Response<List<PollingOrder>>) {
                if (response.isSuccessful) {
                    val orders = response.body()?.sortedBy { it.polling_order_name }
                    if (orders.isNullOrEmpty()) {
                        Log.w("PollingOrderUtils", "Received empty or null polling orders")
                    }
                    callback(orders ?: emptyList())
                } else {
                    Log.e("PollingOrderUtils", "Error fetching polling orders: ${response.code()} - ${response.message()}")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<PollingOrder>>, t: Throwable) {
                Log.e("PollingOrderUtils", "Failed to fetch polling orders", t)
                callback(emptyList())
            }
        })
    }
}