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
class InvitationRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource

) : BaseApiResponse()  {

    suspend fun getMasterData(): Flow<NetworkResult<MasterDataResponse>> {
        return flow<NetworkResult<MasterDataResponse>> {
            emit(safeApiCall { remoteDataSource.getMasterData() })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getAllowedUsers(body: CoachesRequestModel): Flow<NetworkResult<List<CoachesResponse>>> {
        return flow<NetworkResult<List<CoachesResponse>>> {
            emit(safeApiCall { remoteDataSource.getAllowedUsers(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun acceptInvitation(body: AcceptInvitationRequestModel): Flow<NetworkResult<AcceptInvitationResponse>> {
        return flow<NetworkResult<AcceptInvitationResponse>> {
            emit(safeApiCall { remoteDataSource.acceptInvitation(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendCoachInvitation(body: InvitationRequestModel): Flow<NetworkResult<InvitationResponse>> {
        return flow<NetworkResult<InvitationResponse>> {
            emit(safeApiCall { remoteDataSource.sendCoachInvitation(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun sendGuestInvitation(body: InvitationRequestModel): Flow<NetworkResult<InvitationResponse>> {
        return flow<NetworkResult<InvitationResponse>> {
            emit(safeApiCall { remoteDataSource.sendGuestInvitation(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun removeInvitation(body: RemoveInvitationRequestModel): Flow<NetworkResult<RemoveInvitationResponse>> {
        return flow<NetworkResult<RemoveInvitationResponse>> {
            emit(safeApiCall { remoteDataSource.removeInvitation(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun deleteUser(body: DeleteUserRequestModel): Flow<NetworkResult<DeleteUserResponse>> {
        return flow<NetworkResult<DeleteUserResponse>> {
            emit(safeApiCall { remoteDataSource.deleteUser(body) })
        }.flowOn(Dispatchers.IO)
    }

}