package com.fenris.motion2coach.network.responses

import java.io.Serializable


data class UserResponse(
    val id : Int?,
    val firstName : String?,
    val lastName : String?,
    val dateOfBirth : String?,
    val email : String?,
    val contactNumber : String?,
    var picture : String?,
    val registrationDate : String?,
    val roleId : Int?,
    val playerTypeId : Int?,
    val genderId : Int?,
    val address : String?,
    val role : String?,
    val cityId : Int?,
    val houseNo : String?,
    val postCode : String?,
    val playerType : String?,
    val city : String?,
    val country : String?,
    val countryId : Int?,
    val subscriptionId : Int?,
    val isSubscriptionActive : Boolean?,
    val guestUser : Boolean?,
    val subscriptionExpiry : String?,
    ): Serializable