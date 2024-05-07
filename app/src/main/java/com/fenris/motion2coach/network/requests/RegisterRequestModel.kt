package com.fenris.motion2coach.network.requests

import java.io.Serializable

data class RegisterRequestModel(val firstName: String,
                                val lastName: String,
                                val dateOfBirth: String,
                                val genderId: Int,
                                val role: Int,
                                var address: String,
                                val postCode: String,
                                val contactNumber: String,
                                val houseNo: String,
                                val city: Int,
                                val email: String,
                                val password: String,
                                val registrationDate: String,
                                var picture: String,
                                var playerType: Int,
                                ) : Serializable
