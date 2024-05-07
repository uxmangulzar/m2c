package com.fenris.motion2coach.view.activity.account

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import com.fenris.motion2coach.databinding.ActivityDeleteAccountBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.DeleteUserRequestModel
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.DeleteUserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    val viewModel: DeleteUserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preventSleep()

        val code="${getAppVersionName()}.${getVersionCode()}"
        binding.tvVersion.text = "Version $code"

        binding.save.setOnClickListener {

            if (binding.etPassword.text.toString()!=""){
                fetchUserDelete(this@DeleteAccountActivity)
            }
        }
    }
    private fun fetchUserDelete(
        activity: Activity
    ) {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Loading.Please wait..")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val deleteUserRequestModel = DeleteUserRequestModel(
            SessionManager.getStringPref(HelperKeys.USER_EMAIL, activity),
            binding.etPassword.text.toString(),
            SessionManager.getStringPref(HelperKeys.USER_ID, activity).toInt(),
            SessionManager.getStringPref(HelperKeys.ROLE_ID, activity)
        )
        viewModel.fetchDeleteUser(deleteUserRequestModel)
        viewModel.response_deleteUser.observeAsEvent(
            (activity as? LifecycleOwner)!!,
            androidx.lifecycle.Observer {
                if (it != null) {
                    when (it) {
                        is NetworkResult.Success -> {

                            progressDialog.dismiss()
                            // bind data to the view
                            Toast.makeText(activity, "Your account has been deleted successfully.", Toast.LENGTH_SHORT).show()
                            SessionManager.clearsession(activity)
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        is NetworkResult.Error -> {
                            progressDialog.dismiss()
                            if (it.statusCode==401){
                                startActivity(Intent(applicationContext, LoginActivity::class.java))
                                finishAffinity()
                            }
                            // show error message
                            Toast.makeText(
                                activity,
                                it.message,
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                        is NetworkResult.Loading -> {
                            // show a progress bar
                        }
                    }
                }
            })

    }

    fun onBackPressed(view: View) {
        finish()
    }
}