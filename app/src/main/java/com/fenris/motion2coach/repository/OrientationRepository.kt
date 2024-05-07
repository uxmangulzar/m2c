package com.fenris.motion2coach.repository

import com.fenris.motion2coach.network.BaseApiResponse
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.RemoteDataSource
import com.fenris.motion2coach.network.requests.UserIdRequestModel
import com.fenris.motion2coach.network.responses.*
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class OrientationRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource

) : BaseApiResponse()  {



    suspend fun getOrientations(body: UserIdRequestModel): Flow<NetworkResult<List<OrientationResponse>>> {
        return flow<NetworkResult<List<OrientationResponse>>> {
            emit(safeApiCall { remoteDataSource.getOrientations(body) })
        }.flowOn(Dispatchers.IO)
    }





}