package com.fenris.motion2coach.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.network.responses.*
import com.fenris.motion2coach.repository.UserRepository
import com.fenris.motion2coach.util.Event
import com.fenris.motion2coach.util.Helper.isValidEmailAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: UserRepository,
    application: Application
) : AndroidViewModel(application) {


    var username: String = ""
    var password: String = ""
    var noOne: String = ""
    var noTwo: String = ""
    var noThree: String = ""
    var noFour: String = ""
    var noFive: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var dob: String = ""
    var gender: String = ""
    var role: String = ""
    var hand: String = ""
    var street: String = ""
    var house: String = ""
    var zip: String = ""
    var country: String = ""
    var city: String = ""
    var phone: String = ""
    var confirmPassword: String = ""
    var email: String = ""
    var month: String = ""
    var day: String = ""
    var year: String = ""
    var weight: String = ""
    var height: String = ""
    var weightUnit: String = ""
    var heightUnit: String = ""

    private val otpResult = MutableLiveData<String>()
    private val profileInfoResult = MutableLiveData<String>()
    private val addressInfoResult = MutableLiveData<String>()
    private val signupResult = MutableLiveData<String>()
    private val ageResult = Event<String>()
    private val weightResult = Event<String>()

    private val loginResult = MutableLiveData<String>()
    fun getLoginResult(): LiveData<String> = loginResult

    fun getOtpResult(): LiveData<String> = otpResult
    fun getProfileInfoResult(): LiveData<String> = profileInfoResult
    fun getAddressInfoResult(): LiveData<String> = addressInfoResult
    fun getAgeResult(): Event<String> = ageResult
    fun getSignupResult(): LiveData<String> = signupResult
    fun getWeightResult(): Event<String> = weightResult


    private val _response_player_types: MutableLiveData<NetworkResult<List<PlayerTypesResponse>>> =
        MutableLiveData()
    val response_player_types: LiveData<NetworkResult<List<PlayerTypesResponse>>> = _response_player_types
    fun fetchPlayerTypeResponse() = viewModelScope.launch {
        repository.getPlayerTypes().collect {
            _response_player_types.value = it
        }
    }


    private val _response: MutableLiveData<NetworkResult<LoginResponse>> = MutableLiveData()
    val response: LiveData<NetworkResult<LoginResponse>> = _response
    fun fetchLoginResponse(body: LoginRequestModel) = viewModelScope.launch {
        repository.loginUser(body).collect {
            _response.value = it
        }
    }

    private val _response_resendOtp: MutableLiveData<NetworkResult<ActiveUserResponse>> = MutableLiveData()
    val response_resendOtp: LiveData<NetworkResult<ActiveUserResponse>> = _response_resendOtp
    fun fetchresendOtpResponse(body: ResendOtpRequestModel) = viewModelScope.launch {
        repository.resendOtp(body).collect {
            _response_resendOtp.value = it
        }
    }

    private val _response_active_user: MutableLiveData<NetworkResult<ActiveUserResponse>> = MutableLiveData()
    val response_activate_user: LiveData<NetworkResult<ActiveUserResponse>> = _response_active_user
    fun fetchActiveUserResponse(body: ActivateUserRequestModel) = viewModelScope.launch {
        repository.activateUser(body).collect {
            _response_active_user.value = it
        }
    }


    private val _response_upload_media_one: MutableLiveData<NetworkResult<UploadResponse>> = MutableLiveData()
    val response_upload_media_one: LiveData<NetworkResult<UploadResponse>> = _response_upload_media_one
    fun fetchUploadMediaOneResponse(body: SessionsRequestModel) = viewModelScope.launch {
        repository.uploadMediaOne(body).collect {
            _response_upload_media_one.value = it
        }
    }


    private val _response_cities: MutableLiveData<NetworkResult<List<CitiesResponse>>> =
        MutableLiveData()
    val response_cities: LiveData<NetworkResult<List<CitiesResponse>>> = _response_cities
    fun fetchCitiesResponse(body: CitiesRequestModel) = viewModelScope.launch {
        repository.getCitiesResponse(body).collect {
            _response_cities.value = it
        }
    }


    private val _response_email_valid: MutableLiveData<NetworkResult<EmailValidResponse>> =
        MutableLiveData()
    val response_email_valid: LiveData<NetworkResult<EmailValidResponse>> = _response_email_valid
    fun fetchEmailValidResponse(body: EmailValidRequestModel) = viewModelScope.launch {
        repository.emailValid(body).collect {
            _response_email_valid.value = it
        }
    }

    private val _response_register: MutableLiveData<NetworkResult<RegisterResponse>> =
        MutableLiveData()
    val response_register: LiveData<NetworkResult<RegisterResponse>> = _response_register
    fun fetchSignupResponse(body: RegisterRequestModel) = viewModelScope.launch {
        repository.registerUser(body).collect {
            _response_register.value = it
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


    private val _response_gender: MutableLiveData<NetworkResult<List<GendersResponse>>> =
        MutableLiveData()
    val response_gender: LiveData<NetworkResult<List<GendersResponse>>> = _response_gender
    fun fetchGenderResponse() = viewModelScope.launch {
        repository.getGenders().collect {
            _response_gender.value = it
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


    private val _response_roles: MutableLiveData<NetworkResult<List<RolesResponse>>> =
        MutableLiveData()
    val response_roles: LiveData<NetworkResult<List<RolesResponse>>> = _response_roles
    fun fetchRolesResponse() = viewModelScope.launch {
        repository.getRoles().collect {
            _response_roles.value = it
        }
    }


    /**
     * Called from activity on login button click
     */
    fun performAgeValidation() {

        if (month.isBlank()) {
            ageResult.value = "Invalid month"
            return
        }
        if (day.isBlank()) {
            ageResult.value = "Invalid day"
            return
        }

        if (year.isBlank()) {
            ageResult.value = "Invalid year"
            return
        }

        ageResult.value = "valid"
    }

    fun performWeightValidation() {

        if (weight.isBlank()) {
            weightResult.value = "Invalid weight"
            return
        }
//        if (weightUnit.isBlank()) {
//            weightResult.value = "Invalid weight unit"
//            return
//        }
        if (height.isBlank()) {
            weightResult.value = "Invalid height"
            return
        }
//        if (heightUnit.isBlank()) {
//            weightResult.value = "Invalid height unit"
//            return
//        }

        weightResult.value = "valid"
    }

    fun performGenderValidation() {


        loginResult.value = "valid"
    }

    fun performLoginValidation() {

        if (username.isBlank()) {
            loginResult.value = "Invalid username"
            return
        }

        if (password.isBlank()) {
            loginResult.value = "Invalid password"
            return
        }

        loginResult.value = "valid"
    }

    fun performOtpValidation() {

        if (noOne.isBlank() || noTwo.isBlank() || noThree.isBlank() || noFour.isBlank() || noFive.isBlank()) {
            otpResult.value = "Please fill five digit code"
            return
        }

        otpResult.value = "valid"
    }
//
    fun performProfileInfoValidation() {

        if (firstName.isBlank()) {
            profileInfoResult.value = "Invalid first name"
            return
        }

        if (lastName.isBlank()) {
            profileInfoResult.value = "Invalid last name"
            return
        }
//        if (dob.isBlank()) {
//            profileInfoResult.value = "Invalid date of birth"
//            return
//        }
//        if (gender.isBlank()) {
//            profileInfoResult.value = "Invalid gender"
//            return
//        }
        if (role.isBlank()) {
            profileInfoResult.value = "Invalid role"
            return
        }
    if (hand.isBlank()) {
            profileInfoResult.value = "Invalid handedness"
            return
        }


        profileInfoResult.value = "valid"
    }

    fun performAddressValidation() {

//        if (street.isBlank()) {
//            addressInfoResult.value = "Invalid street name"
//            return
//        }
//
//        if (house.isBlank()) {
//            addressInfoResult.value = "Invalid house number"
//            return
//        }
//        if (zip.isBlank()) {
//            addressInfoResult.value = "Invalid house code"
//            return
//        }
//        if (country.isBlank()) {
//            addressInfoResult.value = "Invalid country"
//            return
//        }
//        if (city.isBlank()) {
//            addressInfoResult.value = "Invalid city"
//            return
//        }
//
//        if (phone.isBlank()) {
//            addressInfoResult.value = "Invalid phone number"
//            return
//        }

        addressInfoResult.value = "valid"
    }

    fun performSignupValidation() {

        if (email.isBlank() || !isValidEmailAddress(email)) {
            signupResult.value = "Invalid email"
            return
        }

        if (password.isBlank()) {
            signupResult.value = "Invalid password"
            return
        }


        if (!password.equals(confirmPassword)) {
            signupResult.value = "Passwords should match"
            return
        }


        signupResult.value = "valid"
    }

}