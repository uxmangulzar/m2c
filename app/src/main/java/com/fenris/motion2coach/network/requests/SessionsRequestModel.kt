package com.fenris.motion2coach.network.requests

import android.graphics.Bitmap

data class SessionsRequestModel(val fileMimeType: String, val fileName: String, val uploadingFor: String, val videoId: Int?=0, val bitmap: Bitmap?=null, val key: Int?=0)
