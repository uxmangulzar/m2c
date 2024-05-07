package com.fenris.motion2coach.network.requests

data class ProfileUserRequestModel(val loggedUserId: Int, val userId: Int,
                                   val role: Int, val firstName: String, val lastName: String,
                                   val city: Int, val address: String, val houseNo: String,
                                   val postCode: String, val genderId: Int, val dateOfBirth: String,
                                   val email: String, val picture: String? = "", val contactNumber: String, val playerTypeId: Int, val cityId: Int, val deviceType: String, val appVersion: String
                                   )
