package com.fenris.motion2coach.model

import java.io.Serializable

data class Video(
    val uid: Long,
    val video: String?,
    val date: Long?,
    val time: Long?,
    val status: String?,
    val mimetype: String?,
    val orient_id: String?,
    val fps: String?,
    val player_type: String?,
    val overlayUrl: String?,
    val user_id: Long?,
    val swingTypeId: Int? = 1,
    val swingTypename: String? = "Full Swing",
    val clubTypename: String? = "Iron",
    val title: String? = "",
) : Serializable