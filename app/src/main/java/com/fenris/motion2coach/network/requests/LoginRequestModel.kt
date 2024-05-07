package com.fenris.motion2coach.network.requests

data class LoginRequestModel(val username: String, val password: String, val deviceType: String, val appVersion: String)
