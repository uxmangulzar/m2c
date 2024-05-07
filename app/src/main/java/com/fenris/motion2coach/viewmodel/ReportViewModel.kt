package com.fenris.motion2coach.viewmodel


import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.network.responses.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.fenris.motion2coach.repository.ReportRepository
import com.fenris.motion2coach.util.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: ReportRepository,
    application: Application
) : AndroidViewModel(application){

    private val _response_upload_media_one: MutableLiveData<NetworkResult<UploadResponse>> = MutableLiveData()
    val response_upload_media_one: LiveData<NetworkResult<UploadResponse>> = _response_upload_media_one
    fun fetchUploadMediaOneResponse(body: SessionsRequestModel) = viewModelScope.launch {
        repository.uploadMediaOne(body).collect {
            _response_upload_media_one.postValue(it)
        }
    }

    private val _response: MutableLiveData<NetworkResult<Void>> = MutableLiveData()
    val response: LiveData<NetworkResult<Void>> = _response
    fun fetchActualUploadResponse(url: String,image: RequestBody) = viewModelScope.launch {
        repository.actualUpload(url,image).collect {
            _response.postValue(it)
        }
    }


    private val _response_report: MutableLiveData<NetworkResult<VideoReportResponse>> = MutableLiveData()
    val response_report: LiveData<NetworkResult<VideoReportResponse>> = _response_report
    fun fetchReportResponse(body: String) = viewModelScope.launch {
        repository.getVidoeReport(body).collect {
            _response_report.value = it
        }
    }
    private val _response_get_url: MutableLiveData<NetworkResult<String>> = Helper.SingleLiveEvent()
    val response_get_url: LiveData<NetworkResult<String>> = _response_get_url
    fun fetchGetUrl(body: UrlRequestModel) = viewModelScope.launch {
        repository.getUrl(body).collectLatest {
            _response_get_url.value = it
        }
    }

    private val _response_getMultipleUrl: MutableLiveData<NetworkResult<List<String>>> = Helper.SingleLiveEvent()
    val response_getMultipleUrl: LiveData<NetworkResult<List<String>>> = _response_getMultipleUrl
    fun fetchMultipleUrl(body: UrlRequestModel) = viewModelScope.launch {
        repository.getMultipleUrl(body).collectLatest {
            _response_getMultipleUrl.value = it
        }
    }

    private val _response_addAnnotations: MutableLiveData<NetworkResult<AddAnnotationsResponse>> = Helper.SingleLiveEvent()
    val response_addAnnotations: LiveData<NetworkResult<AddAnnotationsResponse>> = _response_addAnnotations
    fun fetchAddAnnotations(body: AddAnnotationRequestModel) = viewModelScope.launch {
        repository.addAnnotations(body).collectLatest {
            _response_addAnnotations.value = it
        }
    }

    private val _response_getAnnotations: MutableLiveData<NetworkResult<GetAnnotationResponseModel>> = Helper.SingleLiveEvent()
    val response_getAnnotations: LiveData<NetworkResult<GetAnnotationResponseModel>> = _response_getAnnotations
    fun fetchGetAnnotations(body: GetAnnotationRequestModel) = viewModelScope.launch {
        repository.getAnnotations(body).collectLatest {
            _response_getAnnotations.value = it
        }
    }
    private val _response_kynamticReport: MutableLiveData<NetworkResult<KinamaticReportResponse>> = MutableLiveData()
    val response_kynamticReport: LiveData<NetworkResult<KinamaticReportResponse>> = _response_kynamticReport
    fun fetchKynamticReport(url:String,body: ReportRequestModel) = viewModelScope.launch {
        repository.getKynamaticReport(url,body).collect {
            _response_kynamticReport.value = it
        }
    }

    private val _response_update_position: MutableLiveData<NetworkResult<UpdatePositionsResponse>> = MutableLiveData()
    val response_update_position: LiveData<NetworkResult<UpdatePositionsResponse>> = _response_update_position
    fun fetchUpdatePosition(url:String,body: UpdatePositionsRequestModel) = viewModelScope.launch {
        repository.updatePositions(url,body).collect {
            _response_update_position.value = it
        }
    }

    private val _response_highlights_two: MutableLiveData<NetworkResult<HighlightsTwoResponse>> = MutableLiveData()
    val response_highlights_two: LiveData<NetworkResult<HighlightsTwoResponse>> = _response_highlights_two
    fun fetchHighlightsTwo(url:String,body: ReportRequestModel) = viewModelScope.launch {
        repository.getHighlightsTwo(url,body).collect {
            _response_highlights_two.value = it
        }
    }


    private val _response_positions: MutableLiveData<NetworkResult<PositionsResponse>> = MutableLiveData()
    val response_positions: LiveData<NetworkResult<PositionsResponse>> = _response_positions
    fun fetchPositionsResponse(url:String,body: ReportRequestModel) = viewModelScope.launch {
        repository.getPositions(url,body).collect {
            _response_positions.value = it
        }
    }

    private val _response_report_json: MutableLiveData<NetworkResult<List<ReportResponse>>> = MutableLiveData()
    val response_report_json: LiveData<NetworkResult<List<ReportResponse>>> = _response_report_json
    fun fetchReportResponse(url: String,body: ReportRequestModel) = viewModelScope.launch {
        repository.getJsonReport(url,body).collect {
            _response_report_json.value = it
        }
    }






}