package com.fenris.motion2coach.network.requests

data class AddAnnotationRequestModel(val loggedUserId: Int,
                                     val userId: Int,
                                     val videoId: Int,
                                     var P1: String?="",
                                     var P2: String?="",
                                     var P3: String?="",
                                     var P4: String?="",
                                     var P5: String?="",
                                     var P6: String?="",
                                     var P7: String?="",
                                     var P8: String?="",
                                     var P9: String?="",
                                     var P10: String?=""
                                   )
