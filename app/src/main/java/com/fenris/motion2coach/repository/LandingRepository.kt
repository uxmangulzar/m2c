package com.fenris.motion2coach.repository

import com.fenris.motion2coach.network.BaseApiResponse
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.RemoteDataSource
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.network.responses.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LandingRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : BaseApiResponse() {



    suspend fun getSubscriptionUrl(body: SubscriptionUrlRequestModel): Flow<NetworkResult<SubscriptionUrlResponse>> {
        return flow<NetworkResult<SubscriptionUrlResponse>> {
            emit(safeApiCall { remoteDataSource.getSubscriptionUrl(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getUserProfileResponse(body: ProfileUserRequestModel): Flow<NetworkResult<UserResponse>> {
        return flow<NetworkResult<UserResponse>> {
            emit(safeApiCall { remoteDataSource.getUserProfile(body) })
        }.flowOn(Dispatchers.IO)
    }


}