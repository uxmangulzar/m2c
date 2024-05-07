package com.fenris.motion2coach.network.requests

data class VideosRequestModel(val loggedUserId: Int,val userId: Int,val videoId: Int, val deviceType: String, val appVersion: String, val beautify: Boolean, val currentDate: String)
