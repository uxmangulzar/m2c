package com.fenris.motion2coach.network.responses


data class AcceptInvitationResponse(
    val coachId : Int?,
    val userId : Int?,
    val message : String?
)