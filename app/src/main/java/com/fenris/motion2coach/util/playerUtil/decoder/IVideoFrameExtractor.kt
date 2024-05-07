package com.fenris.motion2coach.util.playerUtil.decoder

/**
 * Created by Duc Ky Ngo on 9/13/2021.
 */
interface IVideoFrameExtractor {
    fun onCurrentFrameExtracted(currentFrame: Frame)
    fun onAllFrameExtracted(processedFrameCount: Int, processedTimeMs: Long)
}