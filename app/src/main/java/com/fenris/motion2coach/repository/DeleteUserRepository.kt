package com.fenris.motion2coach.repository

import com.fenris.motion2coach.network.BaseApiResponse
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.RemoteDataSource
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.network.responses.*
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class DeleteUserRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource

) : BaseApiResponse()  {


    suspend fun deleteUser(body: DeleteUserRequestModel): Flow<NetworkResult<DeleteUserResponse>> {
        return flow<NetworkResult<DeleteUserResponse>> {
            emit(safeApiCall { remoteDataSource.deleteUser(body) })
        }.flowOn(Dispatchers.IO)
    }

}