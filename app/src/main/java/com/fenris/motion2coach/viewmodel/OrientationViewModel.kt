package com.fenris.motion2coach.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.UserIdRequestModel
import com.fenris.motion2coach.network.responses.*
import com.fenris.motion2coach.repository.OrientationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrientationViewModel @Inject constructor(
    private val repository: OrientationRepository,
    application: Application
) : AndroidViewModel(application) {





    private val _response_orientation: MutableLiveData<NetworkResult<List<OrientationResponse>>> =
        MutableLiveData()
    val response_orientations: LiveData<NetworkResult<List<OrientationResponse>>> = _response_orientation
    fun fetchOrientationResponse(body: UserIdRequestModel) = viewModelScope.launch {
        repository.getOrientations(body).collect {
            _response_orientation.value = it
        }
    }









}