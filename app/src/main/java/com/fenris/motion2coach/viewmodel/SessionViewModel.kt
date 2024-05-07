package com.fenris.motion2coach.viewmodel


import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.model.Video
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.repository.SessionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.fenris.motion2coach.network.responses.AddMediaResponse
import com.fenris.motion2coach.network.responses.DeleteVideoResponse
import com.fenris.motion2coach.network.responses.UploadResponse
import com.fenris.motion2coach.network.responses.VideoResponse
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: SessionsRepository,
    application: Application
) : AndroidViewModel(application){








    private val _response: MutableLiveData<NetworkResult<Void>> = MutableLiveData()
    val response: LiveData<NetworkResult<Void>> = _response

    private val _response_upload_media_one: MutableLiveData<NetworkResult<UploadResponse>> = MutableLiveData()
    val response_upload_media_one: LiveData<NetworkResult<UploadResponse>> = _response_upload_media_one
    fun fetchUploadMediaOneResponse(body: SessionsRequestModel) = viewModelScope.launch {
        repository.uploadMediaOne(body).collect {
            _response_upload_media_one.postValue(it)
        }
    }

    fun fetchActualUploadResponse(url: String,image: RequestBody) = viewModelScope.launch {
        repository.actualUpload(url,image).collect {
            _response.postValue(it)
        }
    }

    private val _response_add_scan: MutableLiveData<NetworkResult<AddMediaResponse>> = MutableLiveData()
    val response_add_scan: LiveData<NetworkResult<AddMediaResponse>> = _response_add_scan
    fun fetchAddScan(body: ScanUploadRequestModel) = viewModelScope.launch {
        repository.addScan(body).collectLatest {
            _response_add_scan.value = it
        }
    }



    private val _response_add: MutableLiveData<NetworkResult<AddMediaResponse>> = MutableLiveData()
    val response_add: LiveData<NetworkResult<AddMediaResponse>> = _response_add
    fun fetchAddVideo(body: SessionsUploadRequestModel) = viewModelScope.launch {
        repository.addVideo(body).collectLatest {
            _response_add.postValue(it)
        }
    }

    private val _response_videosFromServer: MutableLiveData<NetworkResult<List<VideoResponse>>> = MutableLiveData()
    val response_videosFromServer: LiveData<NetworkResult<List<VideoResponse>>> = _response_videosFromServer
    fun fetchVideosFromServer(body: VideosRequestModel) = viewModelScope.launch {
        repository.getVideosFromServer(body).collectLatest {
            _response_videosFromServer.value = it
        }
    }

    private val _response_deleteVideo: MutableLiveData<NetworkResult<DeleteVideoResponse>> = MutableLiveData()
    val response_deleteVideo: LiveData<NetworkResult<DeleteVideoResponse>> = _response_deleteVideo
    fun fetchDeleteVideo(body: DeleteVideoRequestModel) = viewModelScope.launch {
        repository.deleteVideo(body).collectLatest {
            _response_deleteVideo.value = it
        }
    }




}