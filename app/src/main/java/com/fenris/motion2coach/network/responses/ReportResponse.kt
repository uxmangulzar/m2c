package com.fenris.motion2coach.network.responses


data class ReportResponse(
    val Start_to_impact_time : String?,
    val Back_swing_time: String?,
    val downswing_time: String?,
    val tempo: String?,
    val Hand_Path_Back_Swing: String?,
    val Hand_Path_Down_Swing: String?,
    val Max_Hand_Speed: String?,
    val Max_UT_turn: String?,
    val Max_Pelvis_Turn: String?
)