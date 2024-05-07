package com.fenris.motion2coach.network.responses

import java.io.Serializable


data class CountriesResponse(
    val id : Int?,
    val name : String?,
    val phoneCode : Int?,
    val sortName : String?,
    val unicode : String?,
    val image : String?,
) : Serializable