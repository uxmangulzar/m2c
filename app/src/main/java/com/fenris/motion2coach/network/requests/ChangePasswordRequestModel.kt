package com.fenris.motion2coach.network.requests

data class ChangePasswordRequestModel(val loggedUserId: Int,val oldPassword: String, val newPassword: String)
