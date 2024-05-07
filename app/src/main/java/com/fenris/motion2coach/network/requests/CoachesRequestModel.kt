package com.fenris.motion2coach.network.requests

data class CoachesRequestModel(val loggedUserId: Int, val roleId: Int,
                               val accepted: Boolean, val deviceType: String, val appVersion: String
                                   )
