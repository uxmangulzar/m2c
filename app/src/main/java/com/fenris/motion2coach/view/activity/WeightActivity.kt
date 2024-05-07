package com.fenris.motion2coach.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityWeightBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.view.activity.camera.CaptureImageActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeightActivity : AppCompatActivity(), SignupValidationHandler {
    val viewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding =
            DataBindingUtil.setContentView<ActivityWeightBinding>(this, R.layout.activity_weight)

        viewBinding.viewModel = viewModel
        viewBinding.handler = this
        preventSleep()
        val typeWeight = arrayOf("Kg's", "lb's")
        val adapterWeight = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            typeWeight
        )
        viewBinding.spinWeightUnit.setAdapter(adapterWeight)


        val typeHeight = arrayOf("cm", "ft")
        val adapterHeight = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            typeHeight
        )
        viewBinding.spinHeightUnit.setAdapter(adapterHeight)

        viewModel.getWeightResult().observe(this) {
            if (it.equals("valid")) {


                val intent = Intent(this, CaptureImageActivity::class.java)
                intent.putExtra(resources.getString(R.string.flag_from), "straight")
                startActivity(intent)


            } else {

                Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
            }
        }
        viewBinding.back
            .setOnClickListener {
            finish()
        }
    }
    override fun onValidationClicked() {
        viewModel.performWeightValidation()
    }
}