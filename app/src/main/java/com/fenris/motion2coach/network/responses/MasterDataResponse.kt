package com.fenris.motion2coach.network.responses


data class MasterDataResponse(
    val roles : List<RolesResponse>?,
    val countries : List<CountriesResponse>?,
    val playerTypes : List<PlayerTypesResponse>?,
    val genders : List<GendersResponse>?,
)