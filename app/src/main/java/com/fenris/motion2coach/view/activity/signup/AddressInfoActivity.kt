package com.fenris.motion2coach.view.activity.signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityAddressInfoBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.network.requests.RegisterRequestModel
import com.fenris.motion2coach.network.responses.CitiesResponse
import com.fenris.motion2coach.network.responses.CountriesResponse
import com.fenris.motion2coach.view.activity.address.SelectCitiesActivity
import com.fenris.motion2coach.view.activity.address.SelectCountryActivity
import com.fenris.motion2coach.view.activity.terms.TermsActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddressInfoActivity : AppCompatActivity() , SignupValidationHandler {
    private var countryId: String=""
    private var cityId: String="0"
    var viewBinding: ActivityAddressInfoBinding? = null
    val viewModel: AuthViewModel by viewModels()
    var data1 = ArrayList<String>()
    private lateinit var dataCities: java.util.ArrayList<CitiesResponse>
    var onClicked = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding =
            DataBindingUtil.setContentView<ActivityAddressInfoBinding>(this, R.layout.activity_address_info)
        viewBinding!!.viewModel = viewModel
        viewBinding!!.handler = this

        viewBinding!!.tvTerms.setOnClickListener {
            startActivity(Intent(applicationContext, TermsActivity::class.java))
        }

        viewBinding!!.country.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                viewBinding!!.spinCity.setText("")
                // Do what you want
                val intent = Intent(this, SelectCountryActivity::class.java)
                intent.putExtra("countriesList",getIntent().getSerializableExtra("countriesList") as java.io.Serializable)
                resultLauncher.launch(intent)


                true
            } else false
        }
        viewBinding!!.spinCity.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {

                if (countryId != ""){
                    val intent = Intent(this, SelectCitiesActivity::class.java)
                    intent.putExtra("id",countryId)
                    resultLauncher1.launch(intent)
                }



                true
            } else false
        }

        viewBinding!!.street.addTextChangedListener(textWatcher)
        viewBinding!!.house.addTextChangedListener(textWatcher)
        viewBinding!!.zip.addTextChangedListener(textWatcher)
        viewBinding!!.phone.addTextChangedListener(textWatcher)
        viewBinding!!.country.addTextChangedListener(textWatcher)
        viewBinding!!.spinCity.addTextChangedListener(textWatcher)
        viewBinding!!.street.setText(" ")
        var zip = "0"
        var phone = ""
        if (viewBinding!!.zip.text.toString()!=""){
            zip=viewBinding!!.zip.text.toString()
        }
        if (viewBinding!!.phone.text.toString()!=""){
            phone=viewBinding!!.phone.text.toString()
        }
        viewModel.getAddressInfoResult().observe(this) { result ->
            if (result.equals("valid")) {
                viewBinding!!.next.isEnabled = true
                if(onClicked) {
                    val model = intent.getSerializableExtra("model") as RegisterRequestModel
                    val intent = Intent(applicationContext, AccountInfoActivity::class.java)
                    intent.putExtra(
                        "model", RegisterRequestModel(
                            model.firstName,
                            model.lastName,
                            model.dateOfBirth,
                            model.genderId,
                            model.role,
                            viewBinding!!.street.text.toString(),
                            zip,
                            phone,
                            viewBinding!!.house.text.toString(),
                            cityId.toInt()!!,
                            "",
                            "",
                            "",
                            "",
                            playerType = model.playerType
                        )
                    )

                    startActivity(intent)
                }

            } else {
                onClicked = false
                viewBinding!!.next.isEnabled = false

//                viewBinding!!.next.snackbar(result)
            }

        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // your operation...
            if (data != null) {
              val countriesResponse =data.getSerializableExtra("model") as CountriesResponse
                viewBinding!!.country.setText(countriesResponse.name)
                countryId=countriesResponse.id.toString()

            }
        }
    }
    var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // your operation...
            if (data != null) {
              val citiesResponse =data.getSerializableExtra("model") as CitiesResponse
                viewBinding!!.spinCity.setText(citiesResponse.name)
                cityId=citiesResponse.id.toString()

            }
        }
    }
    override fun onValidationClicked() {
        onClicked = true
        viewModel.performAddressValidation()
    }

    var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {


        }

        override fun afterTextChanged(editable: Editable) {
            viewModel.performAddressValidation()
        }
    }

    override fun onResume() {
        super.onResume()
        onClicked = false
    }

    fun onBackPressed(view: View) {
        finish()
    }

}