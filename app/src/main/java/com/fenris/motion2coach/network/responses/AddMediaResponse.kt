package com.fenris.motion2coach.network.responses


data class AddMediaResponse(
    val id : String?,
    val title : String?,
    val description : String?,
    val url : String?,
    val rating : String?,
    val userId : String?,
    val updatedAt : String?,
    val createdAt : String?,
    val overlayUrl : String?,
    val trackingServer : String?,
)