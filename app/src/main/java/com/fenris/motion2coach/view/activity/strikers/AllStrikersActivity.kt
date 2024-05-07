package com.fenris.motion2coach.view.activity.strikers

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityAllStrikersBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.CoachesRequestModel
import com.fenris.motion2coach.network.responses.CoachesResponse
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.dashboard.DashboardActivity
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.adapter.StrikerAdapter
import com.fenris.motion2coach.viewmodel.InvitationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class AllStrikersActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    var viewBinding: ActivityAllStrikersBinding? = null
    val viewModel: InvitationViewModel by viewModels()
    var data = ArrayList<CoachesResponse>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAllStrikersBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        preventSleep()

        progressDialog = ProgressDialog(this@AllStrikersActivity)
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
            val intent = Intent(applicationContext, DashboardActivity::class.java)
            if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,applicationContext)) {
                intent.putExtra("fromStriker", true)
            }
            startActivity(intent)
            finishAffinity()

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
        viewModel.response_getAllowedUsers.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog.dismiss()
                        // bind data to the view
                        var indexUsed: Int =8743
                        data.clear()
                        it.data?.let { it1 ->
                            for((index, value) in it1.withIndex()){
                                value.userSelected = false
                                if (SessionManager.getStringPref(HelperKeys.STRIKER_ID,applicationContext)!=""){
                                    if (SessionManager.getStringPref(HelperKeys.STRIKER_ID,applicationContext).toInt()==value.userId){
                                        value.userSelected = true
                                        indexUsed=index
                                    }
                                }
                                    data.add(value)

                            }
                            viewBinding!!.rv.layoutManager = LinearLayoutManager(this)
                            val adapter = StrikerAdapter(data, this, viewModel,viewBinding!!.linNoRequests,indexUsed)
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

    override fun onBackPressed() {
        onBackPressed(viewBinding!!.ivBack)
    }
}