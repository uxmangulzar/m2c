package com.fenris.motion2coach.network.responses

import java.io.Serializable


data class RegisterResponse(
    val userId : Int?,
    val userName : String?,
    var email : String?,
    val sessionToken : String?,
    val verificationToken : Int?,
    val message : String?,
    val playerTypeId : String?,
): Serializable