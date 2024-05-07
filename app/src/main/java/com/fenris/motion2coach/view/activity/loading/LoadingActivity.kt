package com.fenris.motion2coach.view.activity.loading

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityLoadingBinding
import com.fenris.motion2coach.databinding.CustomDialogErrorBinding
import com.fenris.motion2coach.model.Video
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.SessionsRequestModel
import com.fenris.motion2coach.network.requests.SessionsUploadRequestModel
import com.fenris.motion2coach.network.responses.AddMediaResponse
import com.fenris.motion2coach.util.*
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.camera.CameraSamsungActivity
import com.fenris.motion2coach.view.activity.camera.CaptureVideoActivity
import com.fenris.motion2coach.view.activity.report.ReportOneActivity
import com.fenris.motion2coach.viewmodel.SessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class LoadingActivity : AppCompatActivity() {
    private lateinit var videoModelFromServer: AddMediaResponse
    private lateinit var videoModel: Video
    private lateinit var actualUrl: String
    private var uploadData = ArrayList<Video>()

    private lateinit var viewBinding: ActivityLoadingBinding
    val viewModel: SessionViewModel by viewModels()
    private var webSocket: WebSocket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        preventSleep()

        val anim = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.duration = 4000

        // Start animating the image
        viewBinding.ivLoading.startAnimation(anim)
        val list = ArrayList<Video>()
        list.add(intent.getSerializableExtra("list") as Video)
        uploadData.addAll(list)
//        uploadData = intent.getSerializableExtra("list") as ArrayList<Video>
        viewBinding.ivProgress.setImageDrawable(
            resources.getDrawable(
                R.drawable.ic_group_progress_one,
                null
            )
        )

        videoModel = uploadData[0]
        uploadMedia(
            videoModel.mimetype!!,
            File(videoModel.video).name,
            videoModel.video!!,
            0,
            videoModel
        )


    }


    private fun uploadMedia(
        fileMimeType: String,
        fileName: String,
        path: String,
        index: Int,
        videoModel: Video
    ) {
        var sessionsModel: SessionsRequestModel? = null

        sessionsModel =
            if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE, applicationContext)) {
                SessionsRequestModel(
                    fileMimeType,
                    fileName,
                    SessionManager.getStringPref(HelperKeys.STRIKER_ID, applicationContext)
                )
            } else {
                SessionsRequestModel(fileMimeType, fileName, "0")
            }

        viewModel.response_upload_media_one.removeObservers(this)
        viewModel.fetchUploadMediaOneResponse(sessionsModel)
        viewModel.response_upload_media_one.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // do something

                when (it) {
                    is NetworkResult.Success -> {

                        // bind data to the view

                        Log.e("signed_url", "" + it.data!!.signedUrl)
                        Log.e("public_url", it.data!!.publicUrl!!)
//                        public_url= it.data.publicUrl
                        uploadActualFile(
                            it.data.signedUrl!!,
                            path,
                            it.data!!.publicUrl!!,
                            fileName,
                            index,
                            videoModel
                        )

                    }
                    is NetworkResult.Error -> {
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        // show error message
//                        Toast.makeText(applicationContext,
//                            "bucket add error"+" --> "+it.message,
//                            Toast.LENGTH_SHORT).show()
                        showDialog(this@LoadingActivity)
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                }
            }
        })

    }

    private fun uploadActualFile(
        url: String,
        path: String,
        publicUrl: String,
        fileName: String,
        index: Int,
        videoModel: Video
    ) {


        val requestBody = RequestBody.create(
            "application/octet-stream".toMediaTypeOrNull(),
            Helper.convert(path)!!
        )

        viewModel.response.removeObservers(this)
        viewModel.fetchActualUploadResponse(url, requestBody)
        viewModel.response.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // bind data to the view
//                Toast.makeText(
//                    applicationContext,
//                    "Video uploaded to server",
//                    Toast.LENGTH_SHORT
//                ).show()


                addVideoToServer(publicUrl, fileName, index, videoModel)


            }
        })

    }

    private fun addVideoToServer(url: String, fileName: String, index: Int, videoModel: Video) {
        val fileName1 = fileName.substringBefore(".mp4")
        var sessionsModel: SessionsUploadRequestModel? = null
        val code = "${getAppVersionName()}.${getVersionCode()}"

        if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE, applicationContext)) {
            sessionsModel = SessionsUploadRequestModel(
                SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext),
                fileName1,
                fileName1,
                url,
                "5",
                SessionManager.getStringPref(HelperKeys.ORIENTATION, applicationContext).toInt(),
                SessionManager.getStringPref(HelperKeys.HAND_USED, applicationContext).toInt(),
                120,
                "android",
                "",
                "",
                SessionManager.getStringPref(HelperKeys.STRIKER_HEIGHT, applicationContext)
                    .toString().toFloat(),
                SessionManager.getStringPref(HelperKeys.STRIKER_WEIGHT, applicationContext)
                    .toString().toFloat(),
                SessionManager.getStringPref(HelperKeys.STRIKER_ID, applicationContext), code,
                SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext).toInt(),
                SessionManager.getStringPref(HelperKeys.CLUB_TYPE, applicationContext).toInt()

            )
        } else {
            sessionsModel = SessionsUploadRequestModel(
                SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext),
                fileName1,
                fileName1,
                url,
                "5",
                SessionManager.getStringPref(HelperKeys.ORIENTATION, applicationContext).toInt(),
                SessionManager.getStringPref(HelperKeys.HAND_USED, applicationContext).toInt(),
                120,
                "android",
                "",
                "",
                SessionManager.getStringPref(HelperKeys.HEIGHT, applicationContext).toString()
                    .toFloat(),
                SessionManager.getStringPref(HelperKeys.WEIGHT, applicationContext).toString()
                    .toFloat(),
                SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext), code,
                SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext).toInt(),
                SessionManager.getStringPref(HelperKeys.CLUB_TYPE, applicationContext).toInt()

            )
        }


        viewModel.fetchAddVideo(sessionsModel)
        viewModel.response_add.removeObservers(this)
        viewModel.response_add.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

//                    progressDialog.dismiss()
                        // bind data to the view

//                        startActivity(Intent(applicationContext,SwingDetailsActivity::class.java))
//                        finish()


                        uploadData.remove(uploadData[index])
                        actualUrl = it.data!!.url!!
                        videoModelFromServer = it.data!!

                        if (SessionManager.getBoolPref(HelperKeys.GUEST_USER, applicationContext)) {
                            showDialog1(this@LoadingActivity)
                        } else {
                            if (it.data.trackingServer == "busy" || it.data.trackingServer == "snoring") {
                                showServerStatusDialog(this@LoadingActivity, it.data.trackingServer)
                            }
                            instantiateWebSocket()
                        }


                    }
                    is NetworkResult.Error -> {
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
//                    progressDialog.dismiss()
                        showDialog(this@LoadingActivity)

                        // show error message
//                        Toast.makeText(
//                            applicationContext,
//                            "scan add error"+" --> "+it.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
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

    fun showDialog(activity1: Activity?) {
        val dialog = Dialog(activity1!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val viewBinding = CustomDialogErrorBinding.inflate(activity1.layoutInflater)
        dialog.setContentView(viewBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        viewBinding.btnReturn.setOnClickListener {
            dialog.dismiss()
            val manufacturer = Build.MANUFACTURER
            if (manufacturer == "samsung") {
                val intent1 = Intent(applicationContext, CameraSamsungActivity::class.java)
                intent1.putExtra("capture_mode", "no")
                intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
                startActivity(intent1)
            } else {
                val intent1 = Intent(applicationContext, CaptureVideoActivity::class.java)
                intent1.putExtra("capture_mode", "no")
                intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
                startActivity(intent1)
            }


            finish()
        }
        viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
        if (!(activity1 as Activity).isFinishing) {
            //show dialog
            dialog.show()
        }

    }

    private fun showServerStatusDialog(activity1: Activity?, status: String) {
        val dialog = Dialog(activity1!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val viewBinding = CustomDialogErrorBinding.inflate(activity1.layoutInflater)
        dialog.setContentView(viewBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewBinding.tvTitle.text = "Alert!"
        viewBinding.tvDesc.text = "Server is processing the video. This may take longer than usual."
        viewBinding.btnReturn.setOnClickListener {
            dialog.dismiss()
            val manufacturer = Build.MANUFACTURER
            if (manufacturer == "samsung") {
                val intent1 = Intent(applicationContext, CameraSamsungActivity::class.java)
                intent1.putExtra("capture_mode", "no")
                intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
                startActivity(intent1)
            } else {
                val intent1 = Intent(applicationContext, CaptureVideoActivity::class.java)
                intent1.putExtra("capture_mode", "no")
                intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
                startActivity(intent1)
            }


            finish()
        }
        if (status == "snoring") {
            viewBinding.ivCross.visibility = View.INVISIBLE
        }
        viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
        if (!(activity1).isFinishing) {
            //show dialog
            dialog.show()
        }

    }

    private fun showDialog1(activity1: Activity?) {
        val dialog = Dialog(activity1!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val viewBinding = CustomDialogErrorBinding.inflate(activity1.layoutInflater)
        dialog.setContentView(viewBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewBinding.tvTitle.text = "You are a Guest User"
        viewBinding.tvDesc.text = "Please subscribe to see history."
        viewBinding.btnReturn.setOnClickListener {
            dialog.dismiss()
            val manufacturer = Build.MANUFACTURER
            if (manufacturer == "samsung") {
                val intent1 = Intent(applicationContext, CameraSamsungActivity::class.java)
                intent1.putExtra("capture_mode", "no")
                intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
                startActivity(intent1)

            } else {
                val intent1 = Intent(applicationContext, CaptureVideoActivity::class.java)
                intent1.putExtra("capture_mode", "no")
                intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
                startActivity(intent1)

            }

            finish()
        }
        viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
        if (!(activity1 as Activity).isFinishing) {
            //show dialog
            dialog.show()
        }

    }

    private fun instantiateWebSocket() {
        //Create OKhttp client Object
        val client = OkHttpClient()
        //Build Okhttp request
        val request: Request =
            Request.Builder().url(Constants.BASE_URL_SOCKETS)
                .build() //Ip address
        //Create Object of socket listener and pass Main Activity its parameter
        val socketListner =
            SocketListner(this)
        //Create Web Socket Object pass
        webSocket = client.newWebSocket(request, socketListner)
    }

    //Create SocketListener class that extend websocket listener
    // that implement different methods that will will called after query fires up
    //All thses methods call from background threads we cannot touch directly android views
    class SocketListner(activity: LoadingActivity) :
        WebSocketListener() {
        var activity: LoadingActivity

        //These constructor will make a refrence to main activity class
        init {
            this.activity = activity
        }

        //Call when connection establish with the server
        override fun onOpen(webSocket: WebSocket, response: Response) {
            activity.runOnUiThread(Runnable {
//                Toast.makeText(
//                    activity,
//                    "Connection Successfully Established",
//                    Toast.LENGTH_LONG
//                ).show()
            })

//Declare the timer
            //Declare the timer
            val t = Timer()
//Set the schedule function and rate
            t.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        //Called each time when 1000 milliseconds (1 second) (the period parameter)
                        val jsonObject = JSONObject()
                        try {
                            jsonObject.put("action", "status")
                            //Tell whether the message is send by the server or not true mean yes message is sent by the server
                            jsonObject.put(
                                "file_id",
                                activity.actualUrl
                            )

                            webSocket.send(jsonObject.toString()) //If message is not empty the we will send message to the server

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                },  //Set how long before to start calling the TimerTask (in milliseconds)
                0,  //Set the amount of time between each execution (in milliseconds)
                4000
            )


        }

        //Call when their is a new messages in strings
        override fun onMessage(webSocket: WebSocket, text: String) {
            activity.runOnUiThread(Runnable {
                activity.viewBinding.ivBack.visibility = View.VISIBLE

                try {
                    val obj = JSONObject(text)
                    val status = obj.getString("status")
                    if (status == "uploaded") {
                        activity.viewBinding.ivProgress.setImageDrawable(
                            activity.resources.getDrawable(
                                R.drawable.ic_group_progress_two,
                                null
                            )
                        )
                        activity.viewBinding.tvDetailsTxt.text =
                            "It might take a few seconds to upload your swing on the cloud..."
                    } else if (status == "processing") {
                        activity.viewBinding.ivProgress.setImageDrawable(
                            activity.resources.getDrawable(
                                R.drawable.ic_group_progress_three,
                                null
                            )
                        )
                        activity.viewBinding.tvDetailsTxt.text =
                            "It might take a few seconds to process your swing..."
                    } else if (status == "done") {
                        webSocket.cancel()
                        activity.viewBinding.ivProgress.setImageDrawable(
                            activity.resources.getDrawable(
                                R.drawable.ic_group_progress_four,
                                null
                            )
                        )
                        activity.viewBinding.tvDetailsTxt.text =
                            "It might take a few seconds to retrieve the results"

                        val newString: String =
                            activity.videoModelFromServer!!.overlayUrl!!.replace(".mp4", "")

                        val fileName1 =
                            activity.videoModel.video?.let { File(it).name.substringBefore(".mp4") }

                        val splitArray: List<String> = newString.split("/")
                        val url123 =
                            "https://motion2coach-computervision-results.s3.eu-central-1.amazonaws.com/$fileName1.mp4"
                        val intent =
                            Intent(activity, ReportOneActivity::class.java)
                        intent.putExtra("file_id", activity.videoModel.uid)
                        intent.putExtra("path", activity.videoModelFromServer!!.overlayUrl!!)
                        intent.putExtra("overlayUrl", activity.videoModelFromServer!!.overlayUrl!!)
                        intent.putExtra("status", "uploaded")
                        intent.putExtra("file_name", fileName1)
                        intent.putExtra("original_video_path", activity.videoModel.video)

                        intent.putExtra(
                            "highlight_fileid",
                            splitArray[3] + "/" + splitArray[4] + ".json"
                        )
                        activity.startActivity(intent)
                        activity.finish()
                    } else if (status == "error") {
                        webSocket.cancel()
                        activity.showDialog(activity)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

//                if (uploadData.size == 0) {
//                    //Do something after 100ms


//
//                } else {
//                    val intnt = Intent(applicationContext, LoadingActivity::class.java)
//                    intnt.putExtra("capture_mode", "no")
//                    intnt.putExtra("flag_from", intent.getStringExtra("flag_from"))
//                    intnt.putExtra("list", uploadData as Serializable)
//
//                    startActivity(intnt)
//                    finish()
//                }
            })
        }

        //Call when their is a new messages in bytes
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
        }

        //Call just before the connecton is close
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
        }

        //Call when the connection closes
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
        }

        //Call when some error occurs
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            activity.runOnUiThread(Runnable {
//                Toast.makeText(
//                    activity,
//                    "Some Error Occur $t",
//                    Toast.LENGTH_LONG
//                ).show()
            })
        }
    }

}