package com.fenris.motion2coach.view.activity.coaches

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityAllCoachesBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.CoachesRequestModel
import com.fenris.motion2coach.network.responses.CoachesResponse
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.adapter.CoachAdapter
import com.fenris.motion2coach.viewmodel.InvitationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class AllCoachesActivity : AppCompatActivity() {

    lateinit var progressDialog: ProgressDialog
    var viewBinding: ActivityAllCoachesBinding? = null
    val viewModel: InvitationViewModel by viewModels()
    var data = ArrayList<CoachesResponse>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAllCoachesBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        preventSleep()

        progressDialog = ProgressDialog(this@AllCoachesActivity)
        progressDialog.setMessage("Loading.Please wait..")
        progressDialog.setCancelable(false)
        getDataFromServer(true)
        viewBinding!!.radiogroup.setOnCheckedChangeListener { radioGroup, i ->

            if (i == R.id.radio_approve) {

                getDataFromServer(true)
            } else if (i == R.id.radio_pending) {
                getDataFromServer(false)
            }


        }
    }

    fun onBackPressed(view: View) {
        finish()
    }
    private fun getDataFromServer(b: Boolean) {
        val code="${getAppVersionName()}.${getVersionCode()}"

        progressDialog.show()
        val coachesRequestModel = CoachesRequestModel(
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt(),
            SessionManager.getStringPref(HelperKeys.ROLE_ID, applicationContext).toInt(),
            b,"android",code
        )
        viewModel.fetchAllowedUsers(coachesRequestModel)
        viewModel.response_getAllowedUsers.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog.dismiss()
                        // bind data to the view

                        data.clear()
                        it.data?.let {
                                it1 -> data.addAll(it1)
                            viewBinding!!.rv.layoutManager = LinearLayoutManager(this)
                            val adapter = CoachAdapter(data, this, viewModel,viewBinding!!.linNoRequests)
                            viewBinding!!.rv.adapter = adapter
                            viewBinding!!.rv.adapter!!.notifyDataSetChanged()
                            if (data.size==0){
                                viewBinding!!.linNoRequests.visibility=View.VISIBLE
                            }else{
                                viewBinding!!.linNoRequests.visibility=View.GONE

                            }
                        }



                    }
                    is NetworkResult.Error -> {
                        progressDialog.dismiss()
                        if (it.statusCode==401){
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        // show error message
                        Toast.makeText(
                            applicationContext,
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
}