package com.fenris.motion2coach.network.responses


data class SubscriptionUrlResponse(
    val id : Int?,
    val expiry : String?,
    val userId : Int?,
    val subscriptionId : Int?,
    val url : String?
)