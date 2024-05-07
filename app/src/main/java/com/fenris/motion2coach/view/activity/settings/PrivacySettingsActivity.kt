package com.fenris.motion2coach.view.activity.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityPrivacySettingsBinding
import com.fenris.motion2coach.network.requests.ChangePasswordRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.util.Helper
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.util.snackbar
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.ChangePasswordViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivacySettingsActivity : AppCompatActivity() {

    var viewBinding: ActivityPrivacySettingsBinding? = null
    val viewModel: ChangePasswordViewModel by viewModels()
    var isValid = false
    var onClicked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPrivacySettingsBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        preventSleep()
        viewBinding!!.viewModel = viewModel

        viewBinding!!.password.addTextChangedListener(textWatcher)
        viewBinding!!.conPassword.addTextChangedListener(textWatcher)
        viewBinding!!.curPassword.addTextChangedListener(textWatcher)
        viewBinding!!.save.setOnClickListener {
            val id = SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt()
            if (
                viewBinding!!.curPassword.text.toString() == "" ||
                viewBinding!!.password.text.toString() == "" ||
                viewBinding!!.conPassword.text.toString() == "" ||
                !isValid ||
                viewBinding!!.conPassword.text.toString()!= viewBinding!!.password.text.toString()
            ){
                viewBinding!!.password.snackbar("All fields required")
            }else{
                changePasswordSettings(id)

            }
        }
        viewModel.getchangePasswordResults().observe(this) { result ->

            if (result.equals("valid")&&isValid) {
                viewBinding!!.save.isEnabled = true
            } else {
                viewBinding!!.save.isEnabled = false
                onClicked = false
            }

        }

    }

    fun onBackPressed(view: View) {
        finish()
    }
    override fun onResume() {
        super.onResume()
        onClicked = false
    }
    var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (viewBinding!!.password.text!!.length>=6){
                    isValid = true
                    viewBinding!!.tvCheckCharacter.setTextColor(resources.getColor(R.color.color_blue,null))
                    viewBinding!!.tvCheckCharacter.text = "✔"+getString(R.string.contains_at_least_6_characters)
                }else{
                    isValid = false
                    viewBinding!!.tvCheckCharacter.setTextColor(resources.getColor(R.color.darker_gray,null))
                    viewBinding!!.tvCheckCharacter.text = ""+getString(R.string.contains_at_least_6_characters)
                }
                if (Helper.hasUpperCase(viewBinding!!.password.text.toString())&&Helper.hasLowerCase(viewBinding!!.password.text.toString())) {
                    isValid = true
                    viewBinding!!.tvCheckAlpha.setTextColor(resources.getColor(R.color.color_blue,null))
                    viewBinding!!.tvCheckAlpha.text = "✔"+getString(R.string.contains_both_lower_a_z_and_upper_case_letters_a_z)
                }else{
                    isValid = false
                    viewBinding!!.tvCheckAlpha.setTextColor(resources.getColor(R.color.darker_gray,null))
                    viewBinding!!.tvCheckAlpha.text = ""+getString(R.string.contains_both_lower_a_z_and_upper_case_letters_a_z)
                }
                if (Helper.hasSymbol(viewBinding!!.password.text.toString())) {
                    isValid = true
                    viewBinding!!.tvCheckNum.setTextColor(resources.getColor(R.color.color_blue,null))
                    viewBinding!!.tvCheckNum.text = "✔"+getString(R.string.contains_at_least_one_number_0_9_or_a_symbol)
                }else{
                    isValid = false
                    viewBinding!!.tvCheckNum.setTextColor(resources.getColor(R.color.darker_gray,null))
                    viewBinding!!.tvCheckNum.text = ""+getString(R.string.contains_at_least_one_number_0_9_or_a_symbol)
                }

        }

        override fun afterTextChanged(editable: Editable) {
            onClicked=false
            viewModel.performValidation()
        }
    }
    private fun changePasswordSettings(id : Int) {

        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val changePasswordRequestModel = ChangePasswordRequestModel(id,viewBinding!!.curPassword.text.toString(),viewBinding!!.password.text.toString())
        viewModel.changePasswordResponse(changePasswordRequestModel)
        viewModel.response_changePassword.observeAsEvent(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    // bind data to the view

                    viewBinding!!.password.setText("")
                    viewBinding!!.conPassword.setText("")
                    viewBinding!!.curPassword.setText("")
                    viewBinding!!.password.snackbar("Password Updated Successfully")


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

}