package com.fenris.motion2coach.util.playerUtil.decoder

import java.nio.ByteBuffer


/**
 * Created by Duc Ky Ngo on 9/15/2021.
 */
data class Frame(
    val byteBuffer: ByteBuffer,
    val width: Int,
    val height: Int,
    val position: Int,
    val timestamp: Long,
    val rotation:Int,
    val isFlipX: Boolean,
    val isFlipY: Boolean)
