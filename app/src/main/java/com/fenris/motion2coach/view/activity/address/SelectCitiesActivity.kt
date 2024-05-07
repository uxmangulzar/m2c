package com.fenris.motion2coach.view.activity.address

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fenris.motion2coach.databinding.ActivitySelectCityBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.CitiesRequestModel
import com.fenris.motion2coach.network.responses.CitiesResponse
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.adapter.CitiesAdapter
import com.fenris.motion2coach.viewmodel.AuthViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SelectCitiesActivity : AppCompatActivity() {
    private lateinit var adapter: CitiesAdapter
    private lateinit var data: ArrayList<CitiesResponse>
    private lateinit var binding: ActivitySelectCityBinding
    val viewModel: AuthViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectCityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preventSleep()

        binding.etSearch.addTextChangedListener(textWatcher)

        intent.getStringExtra("id")?.let { getCities(it) }

    }
    private fun getCities(id : String) {
        data = ArrayList<CitiesResponse>()
        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val citiesRequestModel = CitiesRequestModel(id)
        viewModel.fetchCitiesResponse(citiesRequestModel)
        viewModel.response_cities.observe(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    // bind data to the view


                    data.clear()
                    response.data?.let { data.addAll(it) }

                    binding.rv.layoutManager = LinearLayoutManager(this)
                    adapter = CitiesAdapter(data, this)
                    binding.rv.adapter = adapter


                }
                is NetworkResult.Error -> {

                    progressDialog.dismiss()
                    if (response.statusCode==401){
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                        finishAffinity()
                    }
                    // show error message
                    Toast.makeText(applicationContext, response.message.toString(),Toast.LENGTH_SHORT).show()



                }
                is NetworkResult.Loading -> {
                    // show a progress bar
                }
            }
        }
    }

    var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            filter(binding.etSearch.text.toString())
        }

        override fun afterTextChanged(editable: Editable) {

        }
    }
    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<CitiesResponse> = ArrayList()

        // running a for loop to compare elements.
        for (item in data) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
//            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            adapter.filterList(filteredlist)
        }
    }

    fun onBackPressed(view: View) {
        finish()
    }


}