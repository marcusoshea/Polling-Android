package com.pollingandroid.util

import com.pollingandroid.api.RetrofitInstance
import com.pollingandroid.model.PollingOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PollingOrderUtils {
    private const val TAG = "PollingOrderUtils"

    fun fetchPollingOrders(callback: (List<PollingOrder>) -> Unit) {
        RetrofitInstance.api.getPollingOrders().enqueue(object : Callback<List<PollingOrder>> {
            override fun onResponse(call: Call<List<PollingOrder>>, response: Response<List<PollingOrder>>) {
                if (response.isSuccessful) {
                    val orders = response.body()?.sortedBy { it.polling_order_name }
                    callback(orders ?: emptyList())
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<PollingOrder>>, t: Throwable) {
                callback(emptyList())
            }
        })
    }
}
