package com.fenris.motion2coach.network.requests

data class GetAnnotationRequestModel(val loggedUserId: Int,
                                     val userId: Int,
                                     val videoId: Int
                                   )
