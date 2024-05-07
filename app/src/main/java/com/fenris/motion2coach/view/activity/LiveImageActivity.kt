package com.fenris.motion2coach.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import android.graphics.Point
import android.os.*
import android.util.*
import android.view.View
import com.fenris.motion2coach.databinding.ActivityLiveImageBinding
import com.fenris.motion2coach.util.Draw
import com.fenris.motion2coach.util.Helper.preventSleep
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class LiveImageActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityLiveImageBinding
    private lateinit var poseDetector: PoseDetector
    private lateinit var videoCapture: VideoCapture
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityLiveImageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        preventSleep()

        if (allPermissionsGranted()) {
            Handler(Looper.myLooper()!!).postDelayed({
                // code
                viewBinding.lnStandInfo.visibility=View.INVISIBLE
                viewBinding.imageview.visibility=View.INVISIBLE
                cameraProviderFuture = ProcessCameraProvider.getInstance(this)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindPreview(cameraProvider)

                },ContextCompat.getMainExecutor(this))

                val options = PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                    .build()

                poseDetector = PoseDetection.getClient(options)
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

    }





    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }




    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Handler(Looper.myLooper()!!).postDelayed({
                    // code
                    viewBinding.lnStandInfo.visibility=View.INVISIBLE
                    viewBinding.imageview.visibility=View.INVISIBLE
                    cameraProviderFuture = ProcessCameraProvider.getInstance(this)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        bindPreview(cameraProvider)

                    },ContextCompat.getMainExecutor(this))

                    val options = PoseDetectorOptions.Builder()
                        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                        .build()

                    poseDetector = PoseDetection.getClient(options)


                }, 1000)
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    @SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError", "NewApi")
    private fun bindPreview(cameraProvider: ProcessCameraProvider){

        Log.d("Check:","inside bind preview")

        val preview = Preview.Builder().build()

        preview.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        val point = Point()

        videoCapture = VideoCapture.Builder()
            .setTargetResolution(Size(point.x,point.y))
            .build()



        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(point.x,point.y))
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->

            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = imageProxy.image

            if (image != null) {

                val processImage = InputImage.fromMediaImage(image, rotationDegrees)

                poseDetector.process(processImage)
                    .addOnSuccessListener {

                        if (viewBinding.parentLayout.childCount > 3) {
                            viewBinding.parentLayout.removeViewAt(3)
                        }
                        if (it.allPoseLandmarks.isNotEmpty()) {

                            if (viewBinding.parentLayout.childCount > 3) {
                                viewBinding.parentLayout.removeViewAt(3)
                            }

                            val element = Draw(applicationContext, it)
                            viewBinding.parentLayout.addView(element)
                        }

                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        imageProxy.close()
                    }
            }


        }

        cameraProvider.bindToLifecycle(this,cameraSelector,imageAnalysis,preview)

    }


}

