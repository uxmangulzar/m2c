package com.fenris.motion2coach.network.responses

import java.io.Serializable


data class AnalysisResponse(
    val pelvis_turn : ArrayList<Double>?,
    val pelvis_tilt : ArrayList<Double>?,
    val pelvis_bend : ArrayList<Double>?,
    val UT_turn : ArrayList<Double>?,
    val UT_tilt : ArrayList<Double>?,
    val UT_bend : ArrayList<Double>?,
) : Serializable