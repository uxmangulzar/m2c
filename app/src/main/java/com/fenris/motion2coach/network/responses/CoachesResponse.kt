package com.fenris.motion2coach.network.responses


data class CoachesResponse(
    val accepted :Boolean,
    val createdAt : String?,
    val updatedAt : String?,
    val coachId : Int?,
    val userId : Int?,
    val userplayerTypeId : Int?,
    val useremail : String?,
    val userfirstName : String?,
    val userlastName : String?,
    val userpicture : String?,
    val coachplayerTypeId : Int?,
    val coachemail : String?,
    val coachfirstName : String?,
    val coachlastName : String?,
    val coachpicture : String?,
    val coachplayerType : String?,
    val userplayerType : String?,
    var userSelected : Boolean= true
)