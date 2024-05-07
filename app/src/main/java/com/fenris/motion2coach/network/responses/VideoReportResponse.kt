package com.fenris.motion2coach.network.responses

import java.io.Serializable


data class VideoReportResponse(
    val analysis : AnalysisResponse?
): Serializable