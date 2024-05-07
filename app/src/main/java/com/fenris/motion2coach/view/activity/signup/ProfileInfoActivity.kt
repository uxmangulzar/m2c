package com.fenris.motion2coach.view.activity.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityProfileInfoBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.network.requests.RegisterRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.responses.CountriesResponse
import com.fenris.motion2coach.network.responses.GendersResponse
import com.fenris.motion2coach.network.responses.PlayerTypesResponse
import com.fenris.motion2coach.network.responses.RolesResponse
import com.fenris.motion2coach.util.Helper.datePicker
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.terms.TermsActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfileInfoActivity : AppCompatActivity() , SignupValidationHandler {
    private var countriesList: List<CountriesResponse>? = null
    private lateinit var adapterRole: ArrayAdapter<String>
    private lateinit var adapterGender: ArrayAdapter<String>
    private lateinit var dataGender: ArrayList<GendersResponse>
    private lateinit var dataRoles: ArrayList<RolesResponse>
    var onClicked = false
    private var selectedId: Int?=0
    val viewModel: AuthViewModel by viewModels()
    var viewBinding: ActivityProfileInfoBinding? = null
    private lateinit var dataPlayerTypes: java.util.ArrayList<PlayerTypesResponse>


    override fun onResume() {
        super.onResume()
        onClicked = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding =
            DataBindingUtil.setContentView<ActivityProfileInfoBinding>(this, R.layout.activity_profile_info)
        viewBinding!!.viewModel = viewModel
        viewBinding!!.handler = this
        viewBinding!!.lifecycleOwner = this
        viewBinding!!.dob.datePicker(supportFragmentManager,"tag")
        viewBinding!!.tvTerms.setOnClickListener {
            startActivity(Intent(applicationContext, TermsActivity::class.java))
        }
        viewBinding!!.first.addTextChangedListener(textWatcher)
        viewBinding!!.last.addTextChangedListener(textWatcher)
//        viewBinding!!.dob.addTextChangedListener(textWatcher)
//        viewBinding!!.spinGender.addTextChangedListener(textWatcher)
        viewBinding!!.spinRole.addTextChangedListener(textWatcher)
        viewBinding!!.spinHand.addTextChangedListener(textWatcher)


        viewModel.performProfileInfoValidation()
        viewModel.getProfileInfoResult().observe(this) { result ->
            if (result.equals("valid")) {
                viewBinding!!.next.isEnabled = true
                if(onClicked){
                    var genderId=4
                    if (viewBinding!!.spinGender.text.toString()!=""){
                        genderId =
                            dataGender[adapterGender.getPosition(viewBinding!!.spinGender.text.toString())].id!!
                    }


                    val intent = Intent(applicationContext, AddressInfoActivity::class.java)
                    intent.putExtra("countriesList",countriesList as java.io.Serializable)
                    intent.putExtra(
                        "model", RegisterRequestModel(
                            viewBinding!!.first.text.toString(),
                            viewBinding!!.last.text.toString(),
                            viewBinding!!.dob.text.toString(),
                            genderId,
                            dataRoles[adapterRole.getPosition(viewBinding!!.spinRole.text.toString())].id!!,
                            "",
                            "",
                            "",
                            "",
                            0,
                            "",
                            "",
                            "",
                            "",
                            selectedId!!
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

        getMasterData()

        viewBinding!!.spinHand.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                selectedId = dataPlayerTypes[position].id

            }
    }

    override fun onValidationClicked() {
        onClicked = true
        viewModel.performProfileInfoValidation()
    }

    private fun getMasterData() {
        val dataGenderName = ArrayList<String>()
        dataGender = ArrayList<GendersResponse>()
        val dataRoleName = ArrayList<String>()
        dataRoles = ArrayList<RolesResponse>()
        dataPlayerTypes = ArrayList<PlayerTypesResponse>()
        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        viewModel.fetchMasterResponse()
        viewModel.response_master.observeAsEvent(this) {
            if (it != null) {
                // do something

                when (it) {
                    is NetworkResult.Success -> {
                        progressDialog.dismiss()
                        dataGender.clear()
                        it.data!!.genders?.let { it1 -> dataGender.addAll(it1) }

                        for (item in dataGender) {

                            item.name?.let { it1 -> dataGenderName.add(it1) }
                        }
//                        viewBinding!!.spinGender.setText(dataGender.get(0).name)
                        adapterGender = ArrayAdapter(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            dataGenderName
                        )
                        viewBinding!!.spinGender.setAdapter(adapterGender)

//                      Set roles
                        dataRoles.clear()


                        for (item in it.data.roles!!) {

                            item.roleName?.let { it1 -> if (it1=="Coach"||it1=="Striker"){
                                dataRoleName.add(it1)
                                dataRoles.add(item)
                            }
                            }
                        }
//                        viewBinding!!.spinRole.setText(dataRoles[0].roleName)

                        adapterRole = ArrayAdapter(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            dataRoleName
                        )


                        viewBinding!!.spinRole.setAdapter(adapterRole)
//                        setting player types
                        it.data.playerTypes?.let { it1 -> dataPlayerTypes.addAll(it1) }
//                    viewBinding!!.spinHand.setText(dataPlayerTypes.get(0).name)

                        val data1 = ArrayList<String>()
                        for (item in dataPlayerTypes){
                            data1.add(item.name.toString())
                        }
                        val adapter= ArrayAdapter(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            data1
                        )
                        viewBinding!!.spinHand.setAdapter(adapter)
                        selectedId = dataPlayerTypes[0].id

                        countriesList=it.data.countries
                    }
                    is NetworkResult.Error -> {
                        progressDialog.dismiss()

                        if (it.statusCode==401){
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        getMasterData()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                }
            }
        }
    }


    fun onBackPressed(view: View) {
        finish()
    }
    var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {


        }

        override fun afterTextChanged(editable: Editable) {
            viewModel.performProfileInfoValidation()
        }
    }

}