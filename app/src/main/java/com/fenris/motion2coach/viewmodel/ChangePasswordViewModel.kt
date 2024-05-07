package com.fenris.motion2coach.viewmodel


import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.ChangePasswordRequestModel
import com.fenris.motion2coach.network.requests.ForgotPassRequestModel
import com.fenris.motion2coach.network.responses.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.fenris.motion2coach.repository.UserRepository
import com.fenris.motion2coach.util.Helper
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val repository: UserRepository,
    application: Application
) : AndroidViewModel(application){

    var password: String = ""
    var curPassword: String = ""
    var confirmPassword: String = ""
    private val changePasswordResults = MutableLiveData<String>()
    fun getchangePasswordResults(): LiveData<String> = changePasswordResults

    private val _response_changePassword: MutableLiveData<NetworkResult<UserResponse>> =
        MutableLiveData()
    val response_changePassword: LiveData<NetworkResult<UserResponse>> = _response_changePassword
    fun changePasswordResponse(body: ChangePasswordRequestModel) = viewModelScope.launch {
        repository.changePassword(body).collect {
            _response_changePassword.value = it
        }
    }

    private val _response_forgotPassword: MutableLiveData<NetworkResult<ForgotPassResponse>> =
        MutableLiveData()
    val response_forgotPassword: LiveData<NetworkResult<ForgotPassResponse>> = _response_forgotPassword
    fun forgotPasswordResponse(body: ForgotPassRequestModel) = viewModelScope.launch {
        repository.forgotPassword(body).collect {
            _response_forgotPassword.value = it
        }
    }

    fun performValidation() {


        if (curPassword.isBlank()) {
            changePasswordResults.value = "Invalid Current password"
            return
        }
        if (password.isBlank()) {
            changePasswordResults.value = "Invalid password"
            return
        }


        if (password != confirmPassword) {
            changePasswordResults.value = "Passwords should match"
            return
        }


        changePasswordResults.value = "valid"
    }


}