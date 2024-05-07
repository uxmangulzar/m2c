package com.fenris.motion2coach.network.responses

import java.io.Serializable


data class HighlightsTwoResponse(
    val ut_turns : ArrayList<Double>?,
    val ut_tilts : ArrayList<Double>?,
    val ut_bends : ArrayList<Double>?,
    val pelvis_turns : ArrayList<Double>?,
    val pelivs_tilts : ArrayList<Double>?,
    val pelvis_bends : ArrayList<Double>?,
    val head_turn : ArrayList<Double>?,
    val head_tilt : ArrayList<Double>?,
    val head_bend : ArrayList<Double>?,
    val hand_speed : ArrayList<Double>?,
//    val Fz_t_est : ArrayList<Double>?,
    val Wt_percent  : ArrayList<Double>?,
    val knee_angle  : ArrayList<Double>?,
    val elbow_angle  : ArrayList<Double>?
) : Serializable