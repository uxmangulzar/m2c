package com.fenris.motion2coach.network.responses


data class UploadResponse(
    val signedUrl : String?,
    val publicUrl: String?
)