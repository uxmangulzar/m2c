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
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import javax.inject.Inject

@ActivityRetainedScoped
class ReportRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : BaseApiResponse()  {

    suspend fun uploadMediaOne(body: SessionsRequestModel): Flow<NetworkResult<UploadResponse>> {
        return flow<NetworkResult<UploadResponse>> {
            emit(safeApiCall { remoteDataSource.uploadMediaOne(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun actualUpload(url: String, image: RequestBody): Flow<NetworkResult<Void>> {
        return flow {
            emit(safeApiCall { remoteDataSource.actualUpload(url, image) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getVidoeReport(body: String): Flow<NetworkResult<VideoReportResponse>> {
        return flow<NetworkResult<VideoReportResponse>> {
            emit(safeApiCall { remoteDataSource.getVideoReport(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getJsonReport(url:String,body: ReportRequestModel): Flow<NetworkResult<List<ReportResponse>>> {
        return flow<NetworkResult<List<ReportResponse>>> {
            emit(safeApiCall { remoteDataSource.getJsonReport(url,body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPositions(url:String,body: ReportRequestModel): Flow<NetworkResult<PositionsResponse>> {
        return flow<NetworkResult<PositionsResponse>> {
            emit(safeApiCall { remoteDataSource.getPositions(url,body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getHighlightsTwo(url:String,body: ReportRequestModel): Flow<NetworkResult<HighlightsTwoResponse>> {
        return flow<NetworkResult<HighlightsTwoResponse>> {
            emit(safeApiCall { remoteDataSource.getHighlightsTwo(url,body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getKynamaticReport(url:String,body: ReportRequestModel): Flow<NetworkResult<KinamaticReportResponse>> {
        return flow<NetworkResult<KinamaticReportResponse>> {
            emit(safeApiCall { remoteDataSource.getKynamaticReport(url,body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updatePositions(url:String,body: UpdatePositionsRequestModel): Flow<NetworkResult<UpdatePositionsResponse>> {
        return flow<NetworkResult<UpdatePositionsResponse>> {
            emit(safeApiCall { remoteDataSource.updatePositions(url,body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getUrl(body: UrlRequestModel): Flow<NetworkResult<String>> {
        return flow<NetworkResult<String>> {
            emit(safeApiCall { remoteDataSource.getUrl(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getMultipleUrl(body: UrlRequestModel): Flow<NetworkResult<List<String>>> {
        return flow<NetworkResult<List<String>>> {
            emit(safeApiCall { remoteDataSource.getMultipleUrl(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addAnnotations(body: AddAnnotationRequestModel): Flow<NetworkResult<AddAnnotationsResponse>> {
        return flow<NetworkResult<AddAnnotationsResponse>> {
            emit(safeApiCall { remoteDataSource.addAnnotations(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAnnotations(body: GetAnnotationRequestModel): Flow<NetworkResult<GetAnnotationResponseModel>> {
        return flow<NetworkResult<GetAnnotationResponseModel>> {
            emit(safeApiCall { remoteDataSource.getAnnotations(body) })
        }.flowOn(Dispatchers.IO)
    }





}