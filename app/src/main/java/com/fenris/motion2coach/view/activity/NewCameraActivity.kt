package com.fenris.motion2coach.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fenris.motion2coach.R
import com.priyankvasa.android.cameraviewex.AudioEncoder
import com.priyankvasa.android.cameraviewex.CameraView
import com.priyankvasa.android.cameraviewex.Modes
import java.io.File

class NewCameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_camera)
        val camera = findViewById<CameraView>(R.id.camera)
        // enable only video capture mode
        camera.setCameraMode(Modes.CameraMode.VIDEO_CAPTURE)
// OR keep other modes as is and enable video capture mode
        camera.enableCameraMode(Modes.CameraMode.VIDEO_CAPTURE)

// Callback on UI thread
        camera.addVideoRecordStartedListener { /* Video recording started */ }

// Callback on UI thread
        camera.addVideoRecordStoppedListener { isSuccess ->
            // Video recording stopped
            // isSuccess is true if video was recorded and saved successfully
        }
        val targetFile = File(filesDir, "asdascass.mp4")

        camera.startVideoRecording(targetFile.absoluteFile) {
            // Configure video (MediaRecorder) parameters
            audioEncoder = AudioEncoder.Aac
            videoFrameRate = 30
            videoStabilization = true
        }
        camera.capture()
// When done recording
//        camera.stopVideoRecording()
//
//// On APIs 24 and above video recording can be paused and resumed as well
//        camera.pauseVideoRecording()
//        camera.resumeVideoRecording()
//
//// Disable video capture mode
//        camera.disableCameraMode(Modes.CameraMode.VIDEO_CAPTURE)
    }
}