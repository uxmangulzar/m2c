package com.fenris.motion2coach.view.activity.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.fenris.motion2coach.databinding.ActivityEditProfileBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.network.requests.ProfileUserRequestModel
import com.fenris.motion2coach.network.requests.SessionsRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.responses.*
import com.fenris.motion2coach.util.*
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.view.activity.address.SelectCitiesActivity
import com.fenris.motion2coach.view.activity.address.SelectCountryActivity
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.UserProfileViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File


@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity(), SignupValidationHandler {
    private var countriesList: List<CountriesResponse>? = null
    private var id: Int = 0
    private var resposeModel: UserResponse? = null
    private lateinit var binding: ActivityEditProfileBinding
    val viewModel: UserProfileViewModel by viewModels()
    private var filePathUri: Uri? = null
    private var countryId: String=""
    private var cityId: String="0"
    var onClicked = false

    override fun onResume() {
        super.onResume()
        onClicked = false
        binding.etWeight.setText(SessionManager.getStringPref(HelperKeys.WEIGHT,applicationContext))
        binding.etHeight.setText(SessionManager.getStringPref(HelperKeys.HEIGHT,applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this fragment
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.handler = this
        binding.lifecycleOwner = this
        preventSleep()

        id=SessionManager.getStringPref(HelperKeys.USER_ID,applicationContext).toInt()
        getProfile(id)
        getMasterData()

        binding.ivCamera.setOnClickListener {
            Helper.onImageCapture(this,"choose")
        }
        binding.etCountry.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (!countriesList.isNullOrEmpty()){
                    // Do what you want
                    binding!!.etCity.setText("")

                    val intent = Intent(this, SelectCountryActivity::class.java)
                    intent.putExtra("countriesList",countriesList as java.io.Serializable)
                    resultLauncher.launch(intent)

                }


                true
            } else false
        }
        binding.etCity.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {

                if (countryId != ""){
                    val intent = Intent(this, SelectCitiesActivity::class.java)
                    intent.putExtra("id",countryId)
                    resultLauncher1.launch(intent)
                }
                true
            } else false
        }

        viewModel.performProfileInfoValidation()
        viewModel.getProfileInfoResult().observe(this) { result ->
            if (result.equals("valid")) {
                binding!!.btnUpdate.isEnabled = true
                if(onClicked){
                    if (filePathUri==null){
                        updateProfile(id,"")
                    }else{
                        val mimeType = Helper.getMimeType(applicationContext, filePathUri!!)
                        val tmpFile= File(filePathUri!!.path.toString()).name
                        uploadMedia(mimeType.toString(),tmpFile, filePathUri!!.path.toString())
                    }
                }



            } else {
                onClicked = false
                binding!!.btnUpdate.isEnabled = false

//                viewBinding!!.next.snackbar(result)

            }

        }
        binding.etFirst.addTextChangedListener(textWatcher)
        binding!!.etLast.addTextChangedListener(textWatcher)
        binding!!.etWeight.addTextChangedListener(textWatcher)
        binding!!.etHeight.addTextChangedListener(textWatcher)
//        binding!!.etAddress.addTextChangedListener(textWatcher)
//        binding!!.etHouse.addTextChangedListener(textWatcher)
//        binding!!.etZip.addTextChangedListener(textWatcher)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            filePathUri = data!!.data
            if (filePathUri!=null){
                Log.i("Tag", filePathUri.toString())
                Glide.with(applicationContext)
                    .load(filePathUri)
                    .transform(CenterCrop(), RoundedCorners(24))
                    .into(binding.ivProfile)
                viewModel.performProfileInfoValidation()

            }
        }
    }
    fun onBackPressed(view: View) {
        finish()
    }
    private fun getProfile(id : Int) {

//        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
//        progressDialog.show()
        val code="${getAppVersionName()}.${getVersionCode()}"

        val profileUserRequestModel = ProfileUserRequestModel(id,id,0,"","",0,"","","",0,"","","","",0,0,"android",code)
        viewModel.fetchProfileResponse(profileUserRequestModel)
        viewModel.response_profile.observeAsEvent(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    //progressDialog.dismiss()
                    //bind data to the view
                    resposeModel=response.data
                    binding.etEmail.setText(response.data!!.email)
                    binding.etPhone.setText(response.data.contactNumber)
                    binding.etAddress.setText(response.data.address)
                    binding.etRole.setText(response.data.role)
                    binding.etHand.setText(response.data.playerType)
                    binding.etHouse.setText(response.data.houseNo)
                    binding.etZip.setText(response.data.postCode)
                    binding.etFirst.setText(response.data.firstName)
                    binding.etLast.setText(response.data.lastName)
                    binding.etCity.setText(response.data.city)
                    binding.etCountry.setText(response.data.country)
                    cityId= resposeModel?.cityId.toString()
                    countryId= resposeModel?.countryId.toString()
                    Glide.with(applicationContext)
                        .load(response.data.picture)
                        .transform(CenterCrop(), RoundedCorners(24))
                        .into(binding.ivProfile)
                    if (resposeModel!!.picture ==null){
                        resposeModel!!.picture = ""
                    }
                }
                is NetworkResult.Error -> {
                    if (response.statusCode==401){
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                        finishAffinity()
                    }
//                    progressDialog.dismiss()
//                    show error message
                    binding.etPhone.snackbar("error")

                }
                is NetworkResult.Loading -> {
                    // show a progress bar
                }
            }
        }
    }
    private fun updateProfile(id: Int, publicUrl: String) {
        val myList: List<String> =
            ArrayList<String>(binding.etFirst.text.toString().split(" "))
        val playerTypeId =
            SessionManager.getStringPref(HelperKeys.HAND_USED, applicationContext).toInt()

        val firstName = binding.etFirst.text.toString()
        val lastName = binding.etLast.text.toString()
//        for (i in myList){
//            if (i!=firstName){
//                lastName =lastName+" "+i
//            }
//        }
        val code="${getAppVersionName()}.${getVersionCode()}"

        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val profileUserRequestModel = ProfileUserRequestModel(id,id,resposeModel!!.roleId!!,
            firstName,lastName,cityId.toInt(),binding.etAddress.text.toString(),binding.etHouse.text.toString(),binding.etZip.text.toString().ifEmpty { "0" },
            resposeModel!!.genderId!!,resposeModel!!.dateOfBirth!!,binding.etEmail.text.toString(),
            publicUrl.ifEmpty { resposeModel!!.picture!! },binding.etPhone.text.toString(),playerTypeId,cityId.toInt(),"android",code)
        viewModel.updateUserResponse(profileUserRequestModel)
        viewModel.response_updateUser.observeAsEvent(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    // bind data to the view

                    Toast.makeText(applicationContext,"Updated Successfully",Toast.LENGTH_SHORT).show()
                    SessionManager.putStringPref(HelperKeys.WEIGHT,binding.etWeight.text.toString(),applicationContext)
                    SessionManager.putStringPref(HelperKeys.HEIGHT,binding.etHeight.text.toString(),applicationContext)

                    SessionManager.putStringPref(HelperKeys.USER_FIRST,
                        response.data!!.firstName.toString(),applicationContext)
                    SessionManager.putStringPref(HelperKeys.USER_LAST,
                        response.data.lastName.toString(),applicationContext)

                    SessionManager.putStringPref(HelperKeys.USER_IMAGE,
                        response.data.picture.toString(),applicationContext)
                    finish()
                }
                is NetworkResult.Error -> {
                    progressDialog.dismiss()
                    if (response.statusCode==401){
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                        finishAffinity()
                    }
                    // show error message
                    binding.etPhone.snackbar("error")
                }
                is NetworkResult.Loading -> {
                    // show a progress bar
                }
            }
        }
    }
    private fun uploadMedia(fileMimeType: String, fileName: String,path : String) {
        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val sessionsModel = SessionsRequestModel(fileMimeType,fileName,"0")
        viewModel.fetchUploadMediaOneResponse(sessionsModel)
        viewModel.response_upload_media_one.observeAsEvent(this, Observer {
            if (it != null) {
                // do something

                when (it) {
                    is NetworkResult.Success -> {
                        progressDialog.dismiss()
                        // bind data to the view


                        Log.e("signed_url",""+it.data!!.signedUrl)
                        Log.e("public_url",""+ it.data.publicUrl)

                        uploadActualFile(it.data.signedUrl!!,path,it.data.publicUrl!!)

                    }
                    is NetworkResult.Error -> {
                        progressDialog.dismiss()
                        if (it.statusCode==401){
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        // show error message
                        Toast.makeText(applicationContext, it.message.toString(),Toast.LENGTH_SHORT).show()


                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                }
            }
        })

    }
    private fun uploadActualFile(url: String,path : String,publicUrl : String) {
        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val requestBody = RequestBody.create(
            "application/octet-stream".toMediaTypeOrNull(),
            Helper.convert(path)!!
        )


        viewModel.fetchActualUploadResponse(url,requestBody)
        viewModel.response_actualUpload.observeAsEvent(this, Observer {
            if (it != null) {
                progressDialog.dismiss()
                // bind data to the view
//                binding!!.etPhone.snackbar("Image uploaded to server")

                updateProfile(id,publicUrl)

            }
        })

    }
    private fun showKeyboard() {
        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun closeKeyboard() {
        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // your operation...
            if (data != null) {
                val countriesResponse =data.getSerializableExtra("model") as CountriesResponse
                binding!!.etCountry.setText(countriesResponse.name)
                countryId=countriesResponse.id.toString()
                cityId="0"
                binding!!.etCity.setText("")
            }
        }
    }
    var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // your operation...
            if (data != null) {
                val citiesResponse =data.getSerializableExtra("model") as CitiesResponse
                binding.etCity.setText(citiesResponse.name)
                cityId=citiesResponse.id.toString()

            }
        }
    }
    private fun getMasterData() {
        val dataGenderName = ArrayList<String>()
        var dataGender = ArrayList<GendersResponse>()
        val dataRoleName = ArrayList<String>()
        var dataRoles = ArrayList<RolesResponse>()
        var dataPlayerTypes = ArrayList<PlayerTypesResponse>()
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


//                        setting player types

                        it.data.playerTypes?.let { it1 -> dataPlayerTypes.addAll(it1) }
//                    viewBinding!!.spinHand.setText(dataPlayerTypes.get(0).name)

                        val data1 = ArrayList<String>()
                        for (item in dataPlayerTypes){
                            data1.add(item.name.toString())
                        }




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

    override fun onValidationClicked() {
        onClicked = true
        viewModel.performProfileInfoValidation()
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