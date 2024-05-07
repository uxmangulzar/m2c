package com.fenris.motion2coach.view.activity.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityAccountInfoBinding
import com.fenris.motion2coach.network.requests.EmailValidRequestModel
import com.fenris.motion2coach.network.requests.RegisterRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.util.Helper
import com.fenris.motion2coach.util.Helper.getCurrentDate
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.terms.TermsActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountInfoActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityAccountInfoBinding
    val viewModel: AuthViewModel by viewModels()
    var onClicked = false
    var isValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding =
            DataBindingUtil.setContentView<ActivityAccountInfoBinding>(this, R.layout.activity_account_info)
        viewBinding.viewModel = viewModel
        viewBinding.tvTerms.setOnClickListener {
            startActivity(Intent(applicationContext, TermsActivity::class.java))
        }

        viewBinding.email.addTextChangedListener(textWatcher)
        viewBinding.password.addTextChangedListener(textWatcher)
        viewBinding.cpassword.addTextChangedListener(textWatcher)


        viewModel.getSignupResult().observe(this) { result ->

            if (result.equals("valid")&&isValid) {
                viewBinding.next.isEnabled = true
            } else {
                viewBinding.next.isEnabled = false
                onClicked = false
            }

        }
        viewBinding.next.setOnClickListener {
            emailValidation(EmailValidRequestModel(viewBinding.email.text.toString()))
        }
    }



    var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (viewBinding.password.text!!.length>=6){
                isValid = true
                viewBinding.tvCheckCharacter.setTextColor(resources.getColor(R.color.color_blue,null))
                viewBinding.tvCheckCharacter.text = "✔"+getString(R.string.contains_at_least_6_characters)
            }else{
                isValid = false
                viewBinding.tvCheckCharacter.setTextColor(resources.getColor(R.color.darker_gray,null))
                viewBinding.tvCheckCharacter.text = ""+getString(R.string.contains_at_least_6_characters)
            }
            if (Helper.hasUpperCase(viewBinding.password.text!!.toString())&& Helper.hasLowerCase(viewBinding.password.text!!.toString())) {
                isValid = true
                viewBinding!!.tvCheckAlpha.setTextColor(resources.getColor(R.color.color_blue,null))
                viewBinding!!.tvCheckAlpha.text = "✔"+getString(R.string.contains_both_lower_a_z_and_upper_case_letters_a_z)
            }else{
                isValid = false
                viewBinding!!.tvCheckAlpha.setTextColor(resources.getColor(R.color.darker_gray,null))
                viewBinding!!.tvCheckAlpha.text = ""+getString(R.string.contains_both_lower_a_z_and_upper_case_letters_a_z)
            }
            if (Helper.hasSymbol(viewBinding.password.text!!.toString())) {
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
            viewModel.performSignupValidation()
        }
    }



    override fun onResume() {
        super.onResume()
        onClicked = false
    }
    fun onBackPressed(view: View) {
        finish()
    }
    private fun emailValidation(requestModel: EmailValidRequestModel) {
        
        var alreadyGone = true
        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        viewModel.fetchEmailValidResponse(requestModel)
        viewModel.response_email_valid.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // do something

                when (it) {
                    is NetworkResult.Success -> {
                        progressDialog.dismiss()
                        // bind data to the view
                        if (it.data!!.active==null && it.data.userName==null){
                            if (alreadyGone){
                                alreadyGone = false
                                val model = intent.getSerializableExtra("model") as RegisterRequestModel
                                val intent = Intent(applicationContext, ProfilePhotoActivity::class.java)
                                intent.putExtra(
                                    "model", RegisterRequestModel(
                                        model.firstName,
                                        model.lastName,
                                        model.dateOfBirth,
                                        model.genderId,
                                        model.role,
                                        model.address,
                                        model.postCode,
                                        model.contactNumber,
                                        model.houseNo,
                                        model.city,
                                        viewBinding.email.text.toString(),
                                        viewBinding.password.text.toString(),
                                        getCurrentDate(),
                                        "",
                                        playerType = model.playerType
                                    )
                                )
                                startActivity(intent)
                            }
                        }else{
                            alreadyGone = false
                            Toast.makeText(baseContext, "Email is already taken", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is NetworkResult.Error -> {
                        progressDialog.dismiss()
                        if (it.statusCode==401){
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        Toast.makeText(applicationContext, it.message.toString(),Toast.LENGTH_SHORT).show()
                        // show error message
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
                }
            }
        })

    }

}