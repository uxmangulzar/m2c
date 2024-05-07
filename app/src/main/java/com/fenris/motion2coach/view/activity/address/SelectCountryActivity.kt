package com.fenris.motion2coach.view.activity.address

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fenris.motion2coach.databinding.ActivitySelectCountryBinding
import com.fenris.motion2coach.network.responses.CountriesResponse
import com.fenris.motion2coach.util.Helper
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.view.adapter.CountriesAdapter
import com.fenris.motion2coach.viewmodel.CountryViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SelectCountryActivity : AppCompatActivity() {
    private lateinit var adapter: CountriesAdapter
    private lateinit var data: ArrayList<CountriesResponse>
    private lateinit var binding: ActivitySelectCountryBinding
    val viewModel: CountryViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectCountryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preventSleep()

        binding.etSearch.addTextChangedListener(textWatcher)

        getCountries()

    }
    private fun getCountries() {

        data = ArrayList<CountriesResponse>()
        var data1 = ArrayList<CountriesResponse>()
        data1.clear()
        data.clear()
        data1 = intent.getSerializableExtra("countriesList") as ArrayList<CountriesResponse>

        val phoneCountry = Helper.getUserCountry(this)?.lowercase()

//        for (item in data1) {
//            // body of loop
//            val serverCountry = item.sortName?.lowercase()
//            if (phoneCountry.equals(serverCountry)) {
//                data.add(item)
//            }
//        }
        data.addAll(data1)
        binding.rv.layoutManager = LinearLayoutManager(this)
        adapter = CountriesAdapter(data, this)
        binding.rv.adapter = adapter

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
        val filteredlist: ArrayList<CountriesResponse> = ArrayList()

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