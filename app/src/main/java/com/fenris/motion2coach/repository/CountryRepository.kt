package com.fenris.motion2coach.repository

import com.fenris.motion2coach.network.BaseApiResponse
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.RemoteDataSource
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.fenris.motion2coach.network.responses.CountriesResponse
import javax.inject.Inject

@ActivityRetainedScoped
class CountryRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : BaseApiResponse() {

    suspend fun getCountries(): Flow<NetworkResult<List<CountriesResponse>>> {
        return flow<NetworkResult<List<CountriesResponse>>> {
            emit(safeApiCall { remoteDataSource.getCountries() })
        }.flowOn(Dispatchers.IO)
    }







}