package com.fenris.motion2coach.network.requests

data class SessionsUploadRequestModel(
    val loggedUserId: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val videoRating: String,
    val orientation: Int,
    val playerType: Int,
    val frameRate: Int,
    val deviceType: String,
    val orientationText: String,
    val playerTypeText: String,
    val height: Float,
    val weight: Float,
    val uploadingFor: String,
    val appVersion: String,
    val swingType: Int =1,
    val clubType: Int =1,
)
