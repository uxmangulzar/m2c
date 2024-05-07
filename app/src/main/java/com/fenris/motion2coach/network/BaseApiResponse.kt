package com.fenris.motion2coach.network

import org.json.JSONObject
import retrofit2.Response


abstract class BaseApiResponse {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    return NetworkResult.Success(body)
                }
            }

            val jObjError = JSONObject(response.errorBody()!!.string())

            return error("${jObjError.getString("message")}",response.code(),jObjError)
        } catch (e: Exception) {
            val jObjError = JSONObject()

            return error("Server Error or no internet connection", 404, jObjError)
        }
    }
    private fun <T> error(errorMessage: String, code: Int, jObjError: JSONObject): NetworkResult<T> =
        NetworkResult.Error("$errorMessage",code,jObjError)
}