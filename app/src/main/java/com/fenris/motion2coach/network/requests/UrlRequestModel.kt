package com.fenris.motion2coach.network.requests

data class UrlRequestModel(val url: String?=null,val mode: String?="single",val urls: List<String>?=ArrayList()
                                   )
