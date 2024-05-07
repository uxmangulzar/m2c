package com.fenris.motion2coach.network.requests

data class InvitationRequestModel(val userFirstName: String,val userLastName: String,val userGenderId: Int,val userPlayerType: Int,val userEmail: String, val loggedUserId: Int, val roleId: Int
)
