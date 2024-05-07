package com.fenris.motion2coach.network.responses


data class RemoveInvitationResponse(
    val coachId : Int?,
    val userId : Int?,
    val message : String?
)