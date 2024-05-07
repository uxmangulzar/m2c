package com.fenris.motion2coach.view.activity.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.fenris.motion2coach.databinding.ActivityProfileBinding
import com.fenris.motion2coach.network.requests.ProfileUserRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.responses.UserResponse
import com.fenris.motion2coach.util.*
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.UserProfileViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private var id: Int = 0
    private var resposeModel: UserResponse? = null
    private lateinit var binding: ActivityProfileBinding
    val viewModel: UserProfileViewModel by viewModels()
    private var filePathUri: Uri? = null

    override fun onResume() {
        super.onResume()
        id=SessionManager.getStringPref(HelperKeys.USER_ID,applicationContext).toInt()
        binding.etWeight.setText(SessionManager.getStringPref(HelperKeys.WEIGHT,applicationContext)+" kg")
        binding.etHeight.setText(SessionManager.getStringPref(HelperKeys.HEIGHT,applicationContext)+" cm")
        getProfile(id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this fragment
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preventSleep()


        binding.ivProfile.setOnClickListener {
//            Helper.onImageCapture(this,"choose")
        }
        binding.ivEditAddress.setOnClickListener {
            binding.etAddress.isFocusableInTouchMode = true
            binding.etEmail.isFocusableInTouchMode = false
            binding.etName.isFocusableInTouchMode = false
            binding.etPhone.isFocusableInTouchMode = false
            binding.etAddress.setSelection(binding.etAddress.length())
            binding.etAddress.requestFocus()
            showKeyboard()
        }
        binding.tvEditEmail.setOnClickListener {
            binding.etAddress.isFocusableInTouchMode = false
            binding.etEmail.isFocusableInTouchMode = true
            binding.etName.isFocusableInTouchMode = false
            binding.etPhone.isFocusableInTouchMode = false
            binding.etEmail.setSelection(binding.etEmail.length())
            binding.etEmail.requestFocus()
            showKeyboard()
        }
        binding.tvEdit.setOnClickListener {
            val intent = Intent(applicationContext, EditProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivEditPhone.setOnClickListener {
            binding.etAddress.isFocusableInTouchMode = false
            binding.etEmail.isFocusableInTouchMode = false
            binding.etName.isFocusableInTouchMode = false
            binding.etPhone.isFocusableInTouchMode = true
            binding.etPhone.setSelection(binding.etPhone.length())
            binding.etPhone.requestFocus()
            showKeyboard()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            filePathUri = data!!.data
            Log.i("Tag", filePathUri.toString())
            Glide.with(applicationContext)
                .load(filePathUri)
                .transform(CenterCrop(), RoundedCorners(24))
                .into(binding.ivProfile)
        }
    }
    fun onBackPressed(view: View) {
        finish()
    }
    private fun getProfile(id : Int) {

        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val code="${getAppVersionName()}.${getVersionCode()}"

        val profileUserRequestModel = ProfileUserRequestModel(id,id,0,"","",0,"","","",0,"","","","",0,0,"android",code)
        viewModel.fetchProfileResponse(profileUserRequestModel)
        viewModel.response_profile.observeAsEvent(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    //bind data to the view
                    resposeModel=response.data
                    binding.etEmail.setText(response.data!!.email)
                    binding.etPhone.setText(response.data.contactNumber)
                    binding.etAddress.setText(response.data.address)
                    binding.etRole.setText(response.data.role)
                    binding.etHand.setText(response.data.playerType)
                    binding.etHouse.setText(response.data.houseNo)
                    binding.etZip.setText(response.data.postCode)
                    binding.etCountry.setText(response.data.country)
                    binding.etCity.setText(response.data.city)
                    binding.etName.setText(response.data.firstName+" "+ response.data.lastName)

                    Glide.with(applicationContext)
                        .load(response.data.picture)
                        .transform(CenterCrop(), RoundedCorners(24))
                        .into(binding.ivProfile)
                }
                is NetworkResult.Error -> {

                    progressDialog.dismiss()
                    if (response.statusCode==401){
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                        finishAffinity()
                    }
//                    show error message

                    Toast.makeText(applicationContext, response.message.toString(),Toast.LENGTH_SHORT).show()


                }
                is NetworkResult.Loading -> {
                    // show a progress bar
                }
            }
        }
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

}