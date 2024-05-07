package com.fenris.motion2coach.viewmodel


import android.app.Application
import androidx.lifecycle.*
import com.fenris.motion2coach.network.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.fenris.motion2coach.network.responses.CountriesResponse
import com.fenris.motion2coach.repository.CountryRepository
import javax.inject.Inject

@HiltViewModel
class CountryViewModel @Inject constructor(
    private val repository: CountryRepository,
    application: Application
) : AndroidViewModel(application){


    private val _response: MutableLiveData<NetworkResult<List<CountriesResponse>>> = MutableLiveData()
    val response: LiveData<NetworkResult<List<CountriesResponse>>> = _response

    fun fetchCountriesResponse() = viewModelScope.launch {
        repository.getCountries().collect {
            _response.value = it
        }
    }




}