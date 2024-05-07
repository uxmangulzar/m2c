package com.fenris.motion2coach.network

import org.json.JSONObject

sealed class NetworkResult<T>(

    val data: T? = null,
    val message: String? = null,
    val statusCode: Int,
    val jsonObject: JSONObject? = null

) {


    class Success<T>(data: T) : NetworkResult<T>(data, statusCode = 200)
    class Error<T>(message: String, code: Int, jsonObject: JSONObject, data: T? = null) : NetworkResult<T>(data, message,code,jsonObject)
    class Loading<T> : NetworkResult<T>(statusCode = 200)
}
