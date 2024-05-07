package com.fenris.motion2coach.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.fenris.motion2coach.BuildConfig
import com.fenris.motion2coach.R
import com.fenris.motion2coach.model.Video
import com.fenris.motion2coach.network.requests.SessionsRequestModel
import com.fenris.motion2coach.network.requests.SessionsUploadRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.repository.SessionsRepository
import com.fenris.motion2coach.util.Helper
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class FileUploadService : Service() {
    private lateinit var videoModel: Video
    private var videoId: Long = 0
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    var handler: Handler = Handler()
    var runnable: Runnable? = null
    var delay = 15 * 1000

    @Inject
    lateinit var repository: SessionsRepository


    val observer = Observer<Video> { data ->
        if(data!=null){
            videoId = data.uid
            videoModel = data
            //Live data value has changed
            scope.launch {

                uploadMedia(
                    data.mimetype!!,
                    File(data.video).name,
                    data.video!!
                )
            }
        }else{
            stopForeground(true)
            stopSelf()
        }

    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }
    companion object{
        const val  ACTION_STOP_FOREGROUND = "${BuildConfig.APPLICATION_ID}.stopforeground"
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null && intent.action.equals(
                ACTION_STOP_FOREGROUND, ignoreCase = true)) {
            stopForeground(true)
            stopSelf()
        }
        generateForegroundNotification()
        return START_STICKY
        //Normal Service To test sample service comment the above    generateForegroundNotification() && return START_STICKY
        // Uncomment below return statement And run the app.
//        return START_NOT_STICKY
    }

    //Notififcation for ON-going
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, DashboardActivity::class.java)
//            val pendingIntent =
//                PendingIntent.getActivity(this, 0, intentMainLanding, 0)

            var pendingIntent: PendingIntent? = null
            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, 0, intentMainLanding, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(this, 0, intentMainLanding, PendingIntent.FLAG_ONE_SHOT)
            }
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("chats_group", "Chats")
                )
                val notificationChannel =
                    NotificationChannel("service_channel", "Service Notifications",
                        NotificationManager.IMPORTANCE_MIN)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")

            builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" Files are uploading..").toString())
                .setTicker(StringBuilder(resources.getString(R.string.app_name)).append("service is running").toString())
                .setContentText("Touch to open") //                    , swipe down for more options.
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }
            builder.color = resources.getColor(R.color.purple_200,null)
            notification = builder.build()
            startForeground(mNotificationId, notification)
        }

    }

    private suspend fun addVideoToServer(url : String, fileName: String) {

        val fileName1=fileName.substringBefore(".mp4")
        val sessionsModel = SessionsUploadRequestModel(
            SessionManager.getStringPref(HelperKeys.USER_ID,applicationContext),
            fileName1,fileName1,url,"5",
            videoModel.orient_id!!.toInt(),
            videoModel.player_type!!.toInt(),
            videoModel.fps!!.toInt(),
            "android",
            "",
            "",0f,0f,
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext),""
        )


        repository.addVideoService(sessionsModel).apply {
            if (this != null) {
                when (this) {
                    is NetworkResult.Success -> {

                        // bind data to the view
//                        Toast.makeText(
//                            applicationContext,
//                            "success while uploading..",
//                            Toast.LENGTH_SHORT
//                        ).show()


//                        repository.updateVideoStatus("uploaded", videoId.toInt())

                        handler.postDelayed(Runnable { //do something
                            handler.postDelayed(runnable!!, delay.toLong())
                        }.also { runnable = it }, delay.toLong())
                    }
                    is NetworkResult.Error -> {

//                    progressDialog.dismiss()

                        // show error message
//                        Toast.makeText(
//                            applicationContext,
//                            "video add error"+" --> "+this.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                }
            }
        }


    }

    private suspend fun uploadMedia(fileMimeType: String, fileName: String, path : String) {
        val sessionsModel = SessionsRequestModel(fileMimeType,fileName,"0")
        repository.uploadMediaService1(sessionsModel).apply {
            if (this != null) {
                // do something

                when (this) {
                    is NetworkResult.Success -> {

                        // bind data to the view
//                        Log.e("signed_url",""+this.data!!.signedUrl)
//                        Log.e("public_url",""+ this.data.publicUrl)
                        Log.e("signed_url",""+this.data!!.signedUrl!!)
                        Log.e("public_url",""+this.data!!.publicUrl!!)
//                        uploadActualFile(this.data.signedUrl!!,path,this.data.publicUrl!!,fileName)
                        uploadActualFile(this.data.signedUrl!!,path,this.data!!.publicUrl!!,fileName)

                    }
                    is NetworkResult.Error -> {
                        Log.e("issue::",""+this.message)

                        Toast.makeText(applicationContext, this.message.toString(), Toast.LENGTH_SHORT).show()

                        // show error message
//                        Toast.makeText(applicationContext,
//                            "bucket add error"+" --> "+this.message,
//                            Toast.LENGTH_SHORT).show()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                }
            }
        }


    }
    private suspend fun uploadActualFile(url: String, path : String, publicUrl : String, fileName : String) {




        val requestBody = RequestBody.create(
            "application/octet-stream".toMediaTypeOrNull(),
            Helper.convert(path)!!
        )
        repository.actualUploadService(url,requestBody).apply {

            if (this != null) {
                // bind data to the view
//                Toast.makeText(
//                    applicationContext,
//                    "Video uploaded to server",
//                    Toast.LENGTH_SHORT
//                ).show()
                if (videoModel.player_type=="null"||videoModel.player_type==""){
                }else{
                    addVideoToServer(publicUrl, fileName)

                }
//                repository.updateVideoStatus("uploaded", videoId.toInt())
            }
        }


    }
}