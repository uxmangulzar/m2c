package com.fenris.motion2coach.network.requests

data class DeleteUserRequestModel(val email: String, val password: String, val loggedUserId: Int, val roleId: String)
