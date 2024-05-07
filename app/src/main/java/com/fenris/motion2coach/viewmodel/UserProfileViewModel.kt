package com.fenris.motion2coach.viewmodel


import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.ProfileUserRequestModel
import com.fenris.motion2coach.network.requests.SessionsRequestModel
import com.fenris.motion2coach.network.responses.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.fenris.motion2coach.repository.UserRepository
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    application: Application
) : AndroidViewModel(application){

    var first: String = ""
    var last: String = ""
    var phone: String = ""
    var email: String = ""
    var role: String = ""
    var hand: String = ""
    var street: String = ""
    var house: String = ""
    var zip: String = ""
    var country: String = ""
    var city: String = ""
    var weight: String = ""
    var height: String = ""

    private val profileInfoResult = MutableLiveData<String>()
    fun getProfileInfoResult(): LiveData<String> = profileInfoResult

    fun performProfileInfoValidation() {

        if (first.isBlank()) {
            profileInfoResult.value = "Invalid first name"
            return
        }

        if (last.isBlank()) {
            profileInfoResult.value = "Invalid last name"
            return
        }
//        if (phone.isBlank()) {
//            profileInfoResult.value = "Invalid phone number"
//            return
//        }
        if (email.isBlank()) {
            profileInfoResult.value = "Invalid email"
            return
        }
        if (role.isBlank()) {
            profileInfoResult.value = "Invalid role"
            return
        }
        if (hand.isBlank()) {
            profileInfoResult.value = "Invalid player type"
            return
        }
//        if (street.isBlank()) {
//            profileInfoResult.value = "Invalid street"
//            return
//        }
//        if (house.isBlank()) {
//            profileInfoResult.value = "Invalid house"
//            return
//        }
//        if (zip.isBlank()) {
//            profileInfoResult.value = "Invalid zip code"
//            return
//        }
//        if (country.isBlank()) {
//            profileInfoResult.value = "Invalid country"
//            return
//        }
//        if (city.isBlank()) {
//            profileInfoResult.value = "Invalid city"
//            return
//        }
        if (weight.isBlank()) {
            profileInfoResult.value = "Invalid weight"
            return
        }
        if (height.isBlank()) {
            profileInfoResult.value = "Invalid height"
            return
        }


        profileInfoResult.value = "valid"
    }

    private val _response_profile: MutableLiveData<NetworkResult<UserResponse>> =
        MutableLiveData()
    val response_profile: LiveData<NetworkResult<UserResponse>> = _response_profile
    fun fetchProfileResponse(body: ProfileUserRequestModel) = viewModelScope.launch {
        repository.getUserProfileResponse(body).collect {
            _response_profile.value = it
        }
    }

    private val _response_updateUser: MutableLiveData<NetworkResult<UserResponse>> =
        MutableLiveData()
    val response_updateUser: LiveData<NetworkResult<UserResponse>> = _response_updateUser
    fun updateUserResponse(body: ProfileUserRequestModel) = viewModelScope.launch {
        repository.updateUser(body).collect {
            _response_updateUser.value = it
        }
    }

    private val _response_upload_media_one: MutableLiveData<NetworkResult<UploadResponse>> = MutableLiveData()
    val response_upload_media_one: LiveData<NetworkResult<UploadResponse>> = _response_upload_media_one
    fun fetchUploadMediaOneResponse(body: SessionsRequestModel) = viewModelScope.launch {
        repository.uploadMediaOne(body).collect {
            _response_upload_media_one.value = it
        }
    }

    private val _response_actualUpload: MutableLiveData<NetworkResult<Void>> =
        MutableLiveData()
    val response_actualUpload: LiveData<NetworkResult<Void>> = _response_actualUpload
    fun fetchActualUploadResponse(url: String, image: RequestBody) = viewModelScope.launch {
        repository.actualUpload(url, image).collect {
            _response_actualUpload.value = it
        }
    }
    private val _response_master: MutableLiveData<NetworkResult<MasterDataResponse>> =
        MutableLiveData()
    val response_master: LiveData<NetworkResult<MasterDataResponse>> = _response_master
    fun fetchMasterResponse() = viewModelScope.launch {
        repository.getMasterData().collect {
            _response_master.value = it
        }
    }




}