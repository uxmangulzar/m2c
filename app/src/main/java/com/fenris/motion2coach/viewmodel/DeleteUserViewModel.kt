package com.fenris.motion2coach.viewmodel


import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.network.responses.*
import com.fenris.motion2coach.repository.DeleteUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteUserViewModel @Inject constructor(
    private val repository: DeleteUserRepository,
    application: Application
) : AndroidViewModel(application){


    private val _response_deleteUser: MutableLiveData<NetworkResult<DeleteUserResponse>> =
        MutableLiveData()
    val response_deleteUser: LiveData<NetworkResult<DeleteUserResponse>> = _response_deleteUser
    fun fetchDeleteUser(body: DeleteUserRequestModel) = viewModelScope.launch {
        repository.deleteUser(body).collect {
            _response_deleteUser.value = it
        }
    }






}