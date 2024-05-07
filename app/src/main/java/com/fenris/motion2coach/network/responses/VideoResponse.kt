package com.fenris.motion2coach.network.responses


data class VideoResponse(
    val id : Long?,
    val title : String?,
    val description : String?,
    val url : String?,
    val createdAt : String?,
    val updatedAt : String?,
    val overlayUrl : String?,
    val rating : Int?,
    val frameRate : Int?,
    val userId : Int?,
    val playerTypeId : Int?,
    val orientationId : Int?,
    val swingTypeId : Int?,
    val swingTypename : String?,
    val clubTypename : String?
)