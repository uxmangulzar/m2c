package com.fenris.motion2coach.view.activity.signup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityProfilePhotoBinding
import com.fenris.motion2coach.network.requests.RegisterRequestModel
import com.fenris.motion2coach.network.requests.SessionsRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.util.Helper.convert
import com.fenris.motion2coach.util.Helper.getMimeType
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.onImageCapture
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.util.snackbar
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class ProfilePhotoActivity : AppCompatActivity() {
    var viewBinding: ActivityProfilePhotoBinding? = null
    val viewModel: AuthViewModel by viewModels()
    private var filePathUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding =
            DataBindingUtil.setContentView<ActivityProfilePhotoBinding>(this, R.layout.activity_profile_photo)
        viewBinding!!.viewModel = viewModel

        viewBinding!!.take.setOnClickListener {
            onImageCapture(this,"take")
        }
        viewBinding!!.choose.setOnClickListener {
            onImageCapture(this,"choose")
        }

        viewBinding!!.upload.setOnClickListener {
            if (filePathUri != null) {
                val mimeType =
                    getMimeType(applicationContext, filePathUri!!)
                val tmpFile = File(filePathUri!!.path.toString()).name
                uploadMedia(mimeType.toString(), tmpFile, filePathUri!!.path.toString())
            }
        }

        viewBinding!!.later.setOnClickListener {
            val model = intent.getSerializableExtra("model") as RegisterRequestModel
            model.picture = ""
            signUser(model)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            filePathUri = data!!.data
            Log.i("Tah", filePathUri.toString())
            viewBinding!!.ivProfile.setImageURI(filePathUri)
            viewBinding!!.upload.visibility = View.VISIBLE
        }
    }


    private fun signUser(registerRequestModel: RegisterRequestModel) {
        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        viewModel.fetchSignupResponse(registerRequestModel)
        viewModel.response_register.observeAsEvent(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    // bind data to the view

                    viewBinding!!.upload.snackbar(response.data!!.message.toString())

                    val model=response.data
                    model.email = registerRequestModel.email

                    SessionManager.putStringPref(
                        HelperKeys.HAND_USED,
                        model.playerTypeId,applicationContext)

                    val intent=Intent(applicationContext, OtpActivity::class.java)
                    intent.putExtra("email",model.email)
                    intent.putExtra("token_flag", "photo")

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
                else -> {}
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

                        uploadActualFile(it.data.signedUrl!!,path,it.data.publicUrl!!,fileName)

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
                    else -> {}
                }
            }
        })

    }
    private fun uploadActualFile(url: String,path : String,publicUrl : String,fileName : String) {
        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val requestBody = RequestBody.create(
            "application/octet-stream".toMediaTypeOrNull(),
            convert(path)!!
        )


        viewModel.fetchActualUploadResponse(url,requestBody)
        viewModel.response_actualUpload.observeAsEvent(this, Observer {
            if (it != null) {
                progressDialog.dismiss()
                // bind data to the view
                viewBinding!!.upload.snackbar("Image uploaded to server")


                        val model=intent.getSerializableExtra("model") as RegisterRequestModel
                        model.picture=publicUrl
                        signUser(model)
            }
        })

    }
    fun onBackPressed(view: View) {
        finish()
    }
}