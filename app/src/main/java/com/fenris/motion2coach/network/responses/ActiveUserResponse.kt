package com.fenris.motion2coach.network.responses


data class ActiveUserResponse(
    val message : String?,
    val userId : Int?,
    val playerTypeId : Int?,
    val firstName : String?,
    val lastName : String?,
    val roleName : String?,
    val picturePath : String?,
    val userName : String?,
    val roleId : String?,
    val isSubscriptionActive : Boolean?,
)