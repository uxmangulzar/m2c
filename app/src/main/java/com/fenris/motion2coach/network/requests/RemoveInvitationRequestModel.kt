package com.fenris.motion2coach.network.requests

data class RemoveInvitationRequestModel(val loggedUserId: Int, val otherUserId: Int, val roleId: Int
                                   )
