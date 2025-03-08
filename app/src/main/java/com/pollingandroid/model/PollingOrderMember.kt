package com.pollingandroid.model

data class PollingOrderMember (
   val access_token: String,
   val isOrderAdmin: Boolean,
   val pollingOrder: Int,
   val memberId: Int,
   val name: String,
   val email: String,
   val active: Boolean
){}
