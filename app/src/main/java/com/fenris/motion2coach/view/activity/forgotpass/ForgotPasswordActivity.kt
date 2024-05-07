package com.fenris.motion2coach.view.activity.forgotpass

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.fenris.motion2coach.databinding.ActivityForgotPasswordBinding
import com.fenris.motion2coach.network.requests.ForgotPassRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.snackbar
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.ChangePasswordViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {
    var viewBinding: ActivityForgotPasswordBinding? = null
    val viewModel: ChangePasswordViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        preventSleep()


        viewBinding!!.btnSubmit.setOnClickListener {

            if (viewBinding!!.etEmail.text.toString() != ""){
                forgotPasswordSettings()
            }
        }
    }

    fun onBackPressed(view: View) {
        finish()
    }
    private fun forgotPasswordSettings() {


        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val forgotPasswordRequestModel = ForgotPassRequestModel(viewBinding!!.etEmail.text.toString())
        viewModel.forgotPasswordResponse(forgotPasswordRequestModel)
        viewModel.response_forgotPassword.observe(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    // bind data to the view

                    viewBinding!!.etEmail.setText("")

                    viewBinding!!.etEmail.snackbar("Email sent Successfully")


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
}