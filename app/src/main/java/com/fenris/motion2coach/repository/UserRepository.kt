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
import okhttp3.RequestBody
import javax.inject.Inject

@ActivityRetainedScoped
class UserRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : BaseApiResponse()  {


    suspend fun getPlayerTypes(): Flow<NetworkResult<List<PlayerTypesResponse>>> {
        return flow<NetworkResult<List<PlayerTypesResponse>>> {
            emit(safeApiCall { remoteDataSource.getPlayerTypes() })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun loginUser(body: LoginRequestModel): Flow<NetworkResult<LoginResponse>> {
        return flow<NetworkResult<LoginResponse>> {
            emit(safeApiCall { remoteDataSource.loginUser(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun emailValid(body: EmailValidRequestModel): Flow<NetworkResult<EmailValidResponse>> {
        return flow<NetworkResult<EmailValidResponse>> {
            emit(safeApiCall { remoteDataSource.emailValid(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun activateUser(body: ActivateUserRequestModel): Flow<NetworkResult<ActiveUserResponse>> {
        return flow<NetworkResult<ActiveUserResponse>> {
            emit(safeApiCall { remoteDataSource.activateUser(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun resendOtp(body: ResendOtpRequestModel): Flow<NetworkResult<ActiveUserResponse>> {
        return flow<NetworkResult<ActiveUserResponse>> {
            emit(safeApiCall { remoteDataSource.resendOtp(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun uploadMediaOne(body: SessionsRequestModel): Flow<NetworkResult<UploadResponse>> {
        return flow<NetworkResult<UploadResponse>> {
            emit(safeApiCall { remoteDataSource.uploadMediaOne(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun actualUpload(url: String,image: RequestBody):Flow<NetworkResult<Void>>{
        return  flow {
            emit(safeApiCall { remoteDataSource.actualUpload(url,image) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun registerUser(body: RegisterRequestModel): Flow<NetworkResult<RegisterResponse>> {
        return flow<NetworkResult<RegisterResponse>> {
            emit(safeApiCall { remoteDataSource.registerUser(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getCitiesResponse(body: CitiesRequestModel): Flow<NetworkResult<List<CitiesResponse>>> {
        return flow<NetworkResult<List<CitiesResponse>>> {
            emit(safeApiCall { remoteDataSource.getCities(body) })
        }.flowOn(Dispatchers.IO)
    }


    suspend fun getUserProfileResponse(body: ProfileUserRequestModel): Flow<NetworkResult<UserResponse>> {
        return flow<NetworkResult<UserResponse>> {
            emit(safeApiCall { remoteDataSource.getUserProfile(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateUser(body: ProfileUserRequestModel): Flow<NetworkResult<UserResponse>> {
        return flow<NetworkResult<UserResponse>> {
            emit(safeApiCall { remoteDataSource.updateUser(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun changePassword(body: ChangePasswordRequestModel): Flow<NetworkResult<UserResponse>> {
        return flow<NetworkResult<UserResponse>> {
            emit(safeApiCall { remoteDataSource.changePassword(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun forgotPassword(body: ForgotPassRequestModel): Flow<NetworkResult<ForgotPassResponse>> {
        return flow<NetworkResult<ForgotPassResponse>> {
            emit(safeApiCall { remoteDataSource.forgotPassword(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getRoles(): Flow<NetworkResult<List<RolesResponse>>> {
        return flow<NetworkResult<List<RolesResponse>>> {
            emit(safeApiCall { remoteDataSource.getRoles() })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getGenders(): Flow<NetworkResult<List<GendersResponse>>> {
        return flow<NetworkResult<List<GendersResponse>>> {
            emit(safeApiCall { remoteDataSource.getGenders() })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getMasterData(): Flow<NetworkResult<MasterDataResponse>> {
        return flow<NetworkResult<MasterDataResponse>> {
            emit(safeApiCall { remoteDataSource.getMasterData() })
        }.flowOn(Dispatchers.IO)
    }


}