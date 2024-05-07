package com.fenris.motion2coach.view.activity.camera

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.*
import android.view.View
import androidx.activity.viewModels
import androidx.core.net.toUri
import com.fenris.motion2coach.databinding.ActivityCaptureImageBinding
import com.fenris.motion2coach.network.requests.ScanUploadRequestModel
import com.fenris.motion2coach.network.requests.SessionsRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.util.Helper
import com.fenris.motion2coach.util.Helper.allPermissionsGranted
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.loading.LoadingActivity
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.SwingDetailsActivity
import com.fenris.motion2coach.viewmodel.SessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class CaptureImageActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCaptureImageBinding
    private var fileName: String=""
    var stepPassed: Int? = 0
    private var public_url: String? = null

    private var mimeType: String =""
    private var path: String =""
    private var imageCapture: ImageCapture? = null

    val viewModel: SessionViewModel by viewModels<SessionViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCaptureImageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        preventSleep()

//        if(intent.getStringExtra(resources.getString(R.string.flag_from))?.equals("down") == true){
//            viewBinding.imageview.setImageDrawable(resources.getDrawable(R.drawable.ic_shoulotte,null))
//            viewBinding.visualize.text = "Record"
//            val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_group_camera)
//            viewBinding.visualize.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
//        }else if(intent.getStringExtra(resources.getString(R.string.flag_from))?.equals("face") == true){
//            viewBinding.imageview.setImageDrawable(resources.getDrawable(R.drawable.ic_shoulotte,null))
//            viewBinding.visualize.visibility = View.GONE
//            viewBinding.repeat.updateLayoutParams { width = WRAP_CONTENT }
//
//        }

        // Request camera permissions
        if (allPermissionsGranted(this)) {
            Handler(Looper.myLooper()!!).postDelayed({
                // code
                startCamera()

            }, 1000)


        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listeners for video capture button
        viewBinding.cross.setOnClickListener {

            finish()
        }
        viewBinding.capture.setOnClickListener {
            takePhoto()
        }

        viewBinding.btnUpload.setOnClickListener {
            if (path!=""){
                viewBinding.btnUpload.visibility = View.GONE
                viewBinding.visualize.visibility = View.VISIBLE
                val tmpFile= File(path).name
                fileName=tmpFile
                uploadMedia(mimeType,tmpFile,path)
            }

        }
        viewBinding.visualize.setOnClickListener {

            if(stepPassed==4){
//                val newString: String = fileName.replace(".png", "")
//                    val intent=Intent(con,ReportActivity::class.java)
//                val urlJson = "https://m2c-media.s3.eu-central-1.amazonaws.com/$newString.json"
//                    intent.putExtra("url","https://m2c-media.s3.eu-central-1.amazonaws.com/"+newString)
//                    con.startActivity(intent)
                val newString: String = fileName.replace(".png", "")
                val urlJson = "https://m2c-media.s3.eu-central-1.amazonaws.com/$newString.json"
                val urlOverlay = "https://m2c-media.s3.eu-central-1.amazonaws.com/$newString"+"_scan.png"
                val intent=Intent(applicationContext, SwingDetailsActivity::class.java)
                intent.putExtra("urlJson",urlJson)
                intent.putExtra("urlOverlay",urlOverlay)
                startActivity(intent)

            }else{
                if(path != ""){
                    val intent=Intent(applicationContext, LoadingActivity::class.java)
                    intent.putExtra("path",path)
                    intent.putExtra("from","image")
                    intent.putExtra("stepPassed",stepPassed)
                    startActivity(intent)
                }
            }




        }
//        viewBinding.repeat.setOnClickListener {
//            if(intent.getStringExtra(resources.getString(R.string.flag_from))?.equals("down") == true) {
//                viewBinding.imageview.setImageDrawable(
//                    resources.getDrawable(
//                        R.drawable.ic_shoulotte,
//                        null
//                    )
//                )
//            }else if(intent.getStringExtra(resources.getString(R.string.flag_from))?.equals("face") == true) {
//                viewBinding.imageview.setImageDrawable(
//                    resources.getDrawable(
//                        R.drawable.ic_shoulotte,
//                        null
//                    )
//                )
//            } else{
//                viewBinding.imageview.setImageDrawable(resources.getDrawable(R.drawable.ic_shoulotte,null))
//
//            }
////            viewBinding.lnStandInfo.visibility=View.VISIBLE
//            viewBinding.capture.visibility=View.VISIBLE
//            viewBinding.lnDetected.visibility=View.GONE
//            viewBinding.lnAfterImage.visibility=View.GONE
//
//
//        }



    }









    companion object {
        private const val TAG = "Motion2CoachApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                viewBinding.lnAfterImage.visibility=View.GONE
                viewBinding.capture.visibility=View.VISIBLE
                viewBinding.lnDetected.visibility=View.GONE
//                viewBinding.lnStandInfo.visibility=View.VISIBLE
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }




    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/M2C-Images")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    mimeType =
                        contentResolver.getType(output.savedUri!!).toString()
                    //Save file to upload on server
                    val file = saveImageToAppScopeStorage(applicationContext, output.savedUri, mimeType)

                    viewBinding.imageview.setImageURI(file!!.path.toUri())
                    viewBinding.lnStandInfo.visibility=View.GONE
                    viewBinding.capture.visibility=View.GONE
//                    viewBinding.lnDetected.visibility=View.VISIBLE
                    viewBinding.lnAfterImage.visibility=View.VISIBLE
                    viewBinding.btnUpload.visibility=View.VISIBLE
                    viewBinding.visualize.visibility=View.GONE


                    path = file.path.toString()



                }
            }
        )
    }

    fun saveImageToAppScopeStorage(context: Context, videoUri: Uri?, mimeType: String?): File? {
        if(videoUri == null || mimeType == null){
            return null
        }
        val timeDate=System.currentTimeMillis()
        val fileName = "m2c$timeDate.${mimeType.substring(mimeType.indexOf("/") + 1)}"

        val inputStream = context.contentResolver.openInputStream(videoUri)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), fileName)
        file.deleteOnExit()
        file.createNewFile()
        val out = FileOutputStream(file)
        val bos = BufferedOutputStream(out)


        val buf = ByteArray(1024)
        inputStream?.read(buf)
        do {
            bos.write(buf)
        } while (inputStream?.read(buf) !== -1)

        //out.close()
        bos.close()
        inputStream.close()

        return file
    }

    private fun uploadMedia(fileMimeType: String, fileName: String,path : String) {

        stepPassed =1
        val sessionsModel = SessionsRequestModel(fileMimeType,fileName,"0")
        viewModel.fetchUploadMediaOneResponse(sessionsModel)
        viewModel.response_upload_media_one.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // do something

                when (it) {
                    is NetworkResult.Success -> {

                        // bind data to the view


                        Log.e("signed_url",""+it.data!!.signedUrl)
                        Log.e("public_url",""+ it.data.publicUrl)
                        public_url= it.data.publicUrl
                        uploadActualFile(it.data.signedUrl!!,path,it.data.publicUrl!!,fileName)

                    }
                    is NetworkResult.Error -> {
                        if (it.statusCode==401){
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        it.message
                        // show error message
                        Toast.makeText(applicationContext,
                            "bucket add error"+" --> "+it.message,
                            Toast.LENGTH_SHORT).show()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                }
            }
        })

    }
    private fun uploadActualFile(url: String,path : String,publicUrl : String,fileName : String) {


        stepPassed =2


        val requestBody = RequestBody.create(
            "application/octet-stream".toMediaTypeOrNull(),
            Helper.convert(path)!!
        )


        viewModel.fetchActualUploadResponse(url,requestBody)
        viewModel.response.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // bind data to the view
//                Toast.makeText(
//                    applicationContext,
//                    "Video uploaded to server",
//                    Toast.LENGTH_SHORT
//                ).show()


                    addScanToServer(publicUrl, fileName)


            }
        })

    }
    private fun addScanToServer(url : String,fileName: String) {
        stepPassed =3

        val fileName1=fileName.substringBefore(".png")

        val scanUploadModel = ScanUploadRequestModel(SessionManager.getStringPref(HelperKeys.USER_ID,applicationContext),fileName1,url)
        viewModel.fetchAddScan(scanUploadModel)
        viewModel.response_add_scan.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

//                    progressDialog.dismiss()
                        // bind data to the view

//                        startActivity(Intent(applicationContext,SwingDetailsActivity::class.java))
//                        finish()

                        startVisualiseTimer()



                    }
                    is NetworkResult.Error -> {
//                    progressDialog.dismiss()
                        if (it.statusCode==401){
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        // show error message
                        Toast.makeText(
                            applicationContext,
                            "scan add error"+" --> "+it.message,
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
    private fun startVisualiseTimer(){
//        viewBinding!!.report.isClickable = false
//        viewBinding!!.visualize.isClickable = false

        object : CountDownTimer(1 * 60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                viewBinding.visualize.text = ""+SimpleDateFormat("mm:ss:SS").format(Date( millisUntilFinished))


            }

            override fun onFinish() {
                stepPassed = 4
                viewBinding.visualize.text = "Visualize"
                viewBinding.visualize.isClickable = true
            }
        }.start()
    }

}

