package com.fenris.motion2coach.view.activity.signup

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityOtpBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.network.requests.ActivateUserRequestModel
import com.fenris.motion2coach.network.requests.ResendOtpRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.util.Helper.configOtpEditText
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.util.snackbar
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class OtpActivity : AppCompatActivity(), SignupValidationHandler {
    private var viewBinding: ActivityOtpBinding? = null
    private lateinit var emailId: String
    val viewModel: AuthViewModel by viewModels()
    var onClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_otp)

        viewBinding!!.viewModel = viewModel
        viewBinding!!.handler = this
        emailId= intent.getStringExtra("email").toString()
        viewBinding!!.otpAt.text = resources.getString(R.string.enter_the_5_digit_code_sent_to_you_at_n30_5)+emailId
        configOtpEditText(
            viewBinding!!.et1,
            viewBinding!!.et2,
            viewBinding!!.et3,
            viewBinding!!.et4,
            viewBinding!!.et5
        )

        viewBinding!!.et1.addTextChangedListener(textWatcher)
        viewBinding!!.et2.addTextChangedListener(textWatcher)
        viewBinding!!.et3.addTextChangedListener(textWatcher)
        viewBinding!!.et4.addTextChangedListener(textWatcher)
        viewBinding!!.et5.addTextChangedListener(textWatcher)


        viewModel.getOtpResult().observe(this) { result ->
            if (result.equals("valid")) {
                viewBinding!!.next.isEnabled = true
                if(onClicked) {
                    val finalCode = viewBinding!!.et1.text.toString() +
                            viewBinding!!.et2.text.toString() +
                            viewBinding!!.et3.text.toString() +
                            viewBinding!!.et4.text.toString() +
                            viewBinding!!.et5.text.toString()
                        activateUser(finalCode)

                }

            } else {
                viewBinding!!.next.isEnabled = false
                onClicked = false
            }
        }
        viewBinding!!.tvResent.setOnClickListener {
            resendOtp(emailId,"c")
        }

        if (intent.getStringExtra("token_flag").equals("login")){
            resendOtp(emailId,"c")
        }else{
            startTimer()
        }

    }
    override fun onValidationClicked() {
        onClicked = true
        viewModel.performOtpValidation()
    }
    private fun activateUser(finalCode: String) {

        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val activateUserRequestModel = ActivateUserRequestModel(finalCode,"c")
        viewModel.fetchActiveUserResponse(activateUserRequestModel)
        viewModel.response_activate_user.observeAsEvent(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    SessionManager.putStringPref(
                        HelperKeys.USER_ID,
                        response.data!!.userId.toString(),applicationContext)
                    SessionManager.putStringPref(
                        HelperKeys.HAND_USED,
                        response.data.playerTypeId.toString(),applicationContext)
                    SessionManager.putStringPref(HelperKeys.USER_EMAIL,
                        response.data.userName,applicationContext)
//                    SessionManager.putBoolPref(HelperKeys.SUBSCRIPTION_ACTIVE,
//                        response.data.isSubscriptionActive,applicationContext)
                    SessionManager.putStringPref(HelperKeys.USER_FIRST,
                        response.data.firstName.toString(),applicationContext)
                    SessionManager.putStringPref(HelperKeys.USER_LAST,
                        response.data.lastName.toString(),applicationContext)
                    SessionManager.putStringPref(HelperKeys.ROLE_NAME,
                        response.data.roleName.toString(),applicationContext)
                    SessionManager.putStringPref(HelperKeys.USER_IMAGE,
                        response.data.picturePath.toString(),applicationContext)
                    SessionManager.putStringPref(HelperKeys.ROLE_ID,
                        response.data.roleId.toString(),applicationContext)

                    val intent = Intent(applicationContext, CongratulationsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finishAffinity()


                }
                is NetworkResult.Error -> {

                    progressDialog.dismiss()
                    if (response.statusCode==401){
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                        finishAffinity()
                    }
                    // show error message

                    Toast.makeText(applicationContext, response.message.toString(), Toast.LENGTH_SHORT).show()

                }
                is NetworkResult.Loading -> {
                    // show a progress bar
                }
            }
        }
    }

    private fun resendOtp(email: String,token: String) {

        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val resendOtpRequestModel = ResendOtpRequestModel(email,token)
        viewModel.fetchresendOtpResponse(resendOtpRequestModel)
        viewModel.response_resendOtp.observeAsEvent(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    // bind data to the view
                    viewBinding!!.otpAt.snackbar("Otp resent successfully")

                    startTimer()
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


        }

        override fun afterTextChanged(editable: Editable) {
            viewModel.performOtpValidation()
        }
    }

    override fun onResume() {
        super.onResume()
        onClicked = false
    }
    fun onBackPressed(view: View) {
        finish()
    }
    private fun startTimer(){
        viewBinding!!.tvResent.visibility = View.GONE
        object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                viewBinding!!.tvTimer.text = "Your OTP will expire in "+ SimpleDateFormat("mm:ss").format(Date( millisUntilFinished))
            }

            override fun onFinish() {
                viewBinding!!.tvResent.visibility = View.VISIBLE
            }
        }.start()
    }
}