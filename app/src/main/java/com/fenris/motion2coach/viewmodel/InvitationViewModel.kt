package com.fenris.motion2coach.viewmodel


import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.network.responses.*
import com.fenris.motion2coach.repository.InvitationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val repository: InvitationRepository,
    application: Application
) : AndroidViewModel(application){

    private val _response_master: MutableLiveData<NetworkResult<MasterDataResponse>> =
        MutableLiveData()
    val response_master: LiveData<NetworkResult<MasterDataResponse>> = _response_master
    fun fetchMasterResponse() = viewModelScope.launch {
        repository.getMasterData().collect {
            _response_master.value = it
        }
    }


    private val _response_getAllowedUsers: MutableLiveData<NetworkResult<List<CoachesResponse>>> =
        MutableLiveData()
    val response_getAllowedUsers: LiveData<NetworkResult<List<CoachesResponse>>> = _response_getAllowedUsers
    fun fetchAllowedUsers(body: CoachesRequestModel) = viewModelScope.launch {
        repository.getAllowedUsers(body).collect {
            _response_getAllowedUsers.value = it
        }
    }

    private val _response_acceptInvitation: MutableLiveData<NetworkResult<AcceptInvitationResponse>> =
        MutableLiveData()
    val response_acceptInvitation: LiveData<NetworkResult<AcceptInvitationResponse>> = _response_acceptInvitation
    fun fetchAcceptInvitation(body: AcceptInvitationRequestModel) = viewModelScope.launch {
        repository.acceptInvitation(body).collect {
            _response_acceptInvitation.value = it
        }
    }

    private val _response_sendCoachInvitation: MutableLiveData<NetworkResult<InvitationResponse>> =
        MutableLiveData()
    val response_sendCoachInvitation: LiveData<NetworkResult<InvitationResponse>> = _response_sendCoachInvitation
    fun fetchSendCoachInvitation(body: InvitationRequestModel) = viewModelScope.launch {
        repository.sendCoachInvitation(body).collect {
            _response_sendCoachInvitation.value = it
        }
    }

    private val _response_sendGuestInvitation: MutableLiveData<NetworkResult<InvitationResponse>> =
        MutableLiveData()
    val response_sendGuestInvitation: LiveData<NetworkResult<InvitationResponse>> = _response_sendGuestInvitation
    fun fetchSendGuestInvitation(body: InvitationRequestModel) = viewModelScope.launch {
        repository.sendGuestInvitation(body).collect {
            _response_sendGuestInvitation.value = it
        }
    }

    private val _response_removeInvitation: MutableLiveData<NetworkResult<RemoveInvitationResponse>> =
        MutableLiveData()
    val response_removeInvitation: LiveData<NetworkResult<RemoveInvitationResponse>> = _response_removeInvitation
    fun fetchRemoveInvitation(body: RemoveInvitationRequestModel) = viewModelScope.launch {
        repository.removeInvitation(body).collect {
            _response_removeInvitation.value = it
        }
    }

    private val _response_deleteUser: MutableLiveData<NetworkResult<DeleteUserResponse>> =
        MutableLiveData()
    val response_deleteUser: LiveData<NetworkResult<DeleteUserResponse>> = _response_deleteUser
    fun fetchDeleteUser(body: DeleteUserRequestModel) = viewModelScope.launch {
        repository.deleteUser(body).collect {
            _response_deleteUser.value = it
        }
    }






}