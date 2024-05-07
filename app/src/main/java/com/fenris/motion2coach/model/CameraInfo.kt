package com.fenris.motion2coach.model

import android.util.Size

 data class CameraInfo(
    val title: String,
    val cameraId: String,
    val size: Size,
    val fps: Int)