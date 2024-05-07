package com.fenris.motion2coach.network.responses

import java.io.Serializable


data class LoginResponse(
    val userId : Int?,
    val firstName : String?,
    val lastName : String?,
    val message : String?,
    val picturePath : String?,
    val sessionToken : String?,
    val active : Boolean?,
    val roleName : String?,
    val playerTypeId : Int?,
    val roleId : Int?,
    val guestUser : Boolean?,
    val isSubscriptionActive : Boolean?,
): Serializable