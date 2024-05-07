package com.fenris.motion2coach.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fenris.motion2coach.model.Video
import com.fenris.motion2coach.network.BaseApiResponse
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.RemoteDataSource
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.util.Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import com.fenris.motion2coach.network.responses.AddMediaResponse
import com.fenris.motion2coach.network.responses.DeleteVideoResponse
import com.fenris.motion2coach.network.responses.UploadResponse
import com.fenris.motion2coach.network.responses.VideoResponse
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionsRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : BaseApiResponse() {

    private val videos = MutableLiveData<Video>()

    init {

    }


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

    suspend fun addVideo(body: SessionsUploadRequestModel): Flow<NetworkResult<AddMediaResponse>> {
        return flow<NetworkResult<AddMediaResponse>> {
            emit(safeApiCall { remoteDataSource.addVideo(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getVideosFromServer(body: VideosRequestModel): Flow<NetworkResult<List<VideoResponse>>> {
        return flow<NetworkResult<List<VideoResponse>>> {
            emit(safeApiCall { remoteDataSource.getVideosFromServer(body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun deleteVideo(body: DeleteVideoRequestModel): Flow<NetworkResult<DeleteVideoResponse>> {
        return flow<NetworkResult<DeleteVideoResponse>> {
            emit(safeApiCall { remoteDataSource.deleteVideo(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addScan(body: ScanUploadRequestModel): Flow<NetworkResult<AddMediaResponse>> {
        return flow<NetworkResult<AddMediaResponse>> {
            emit(safeApiCall { remoteDataSource.addScan(body) })
        }.flowOn(Dispatchers.IO)
    }




    suspend fun actualUploadService(url: String, image: RequestBody): NetworkResult<Void> {
        return safeApiCall { remoteDataSource.actualUpload(url, image) }
    }

    suspend fun addVideoService(body: SessionsUploadRequestModel): NetworkResult<AddMediaResponse> {
        return safeApiCall { remoteDataSource.addVideo(body) }
    }

    suspend fun uploadMediaService1(body: SessionsRequestModel): NetworkResult<UploadResponse> {
        return safeApiCall { remoteDataSource.uploadMediaOne(body) }
    }


}