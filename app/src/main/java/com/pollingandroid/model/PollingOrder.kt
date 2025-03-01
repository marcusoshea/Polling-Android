package com.pollingandroid.model

data class PollingOrder(
    val polling_order_id: Int,
    val polling_order_name: String,
    val polling_order_admin: Int,
    val polling_order_admin_assistant: Int
) {
    override fun toString(): String {
        return polling_order_name
    }
}