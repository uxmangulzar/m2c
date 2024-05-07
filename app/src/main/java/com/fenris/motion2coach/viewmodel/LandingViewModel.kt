package com.fenris.motion2coach.viewmodel
import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.ProfileUserRequestModel
import com.fenris.motion2coach.network.requests.SubscriptionUrlRequestModel
import com.fenris.motion2coach.network.responses.SubscriptionUrlResponse
import com.fenris.motion2coach.network.responses.UserResponse
import com.fenris.motion2coach.repository.LandingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LandingViewModel @Inject constructor(
    private val repository: LandingRepository,
    application: Application
) : AndroidViewModel(application){


    interface ClickHandler {
        fun onClicked()
    }

    private val _response_getSubscriptionUrl: MutableLiveData<NetworkResult<SubscriptionUrlResponse>> = MutableLiveData()
    val response_getSubscriptionUrl: LiveData<NetworkResult<SubscriptionUrlResponse>> = _response_getSubscriptionUrl
    fun getSubscriptionUrl(body: SubscriptionUrlRequestModel) = viewModelScope.launch {
        repository.getSubscriptionUrl(body).collectLatest {
            _response_getSubscriptionUrl.value = it
        }
    }
    private val _response_profile: MutableLiveData<NetworkResult<UserResponse>> =
        MutableLiveData()
    val response_profile: LiveData<NetworkResult<UserResponse>> = _response_profile
    fun fetchProfileResponse(body: ProfileUserRequestModel) = viewModelScope.launch {
        repository.getUserProfileResponse(body).collect {
            _response_profile.value = it
        }
    }
}