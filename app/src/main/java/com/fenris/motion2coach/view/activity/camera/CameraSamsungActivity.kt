package com.fenris.motion2coach.view.activity.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.hardware.camera2.*
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.MediaCodec
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.androchef.happytimer.countdowntimer.HappyTimer
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityCaptureVideoBinding
import com.fenris.motion2coach.model.CameraInfo
import com.fenris.motion2coach.model.Video
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.SessionsRequestModel
import com.fenris.motion2coach.network.requests.SessionsUploadRequestModel
import com.fenris.motion2coach.util.Helper
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.util.camera2utils.OrientationLiveData
import com.fenris.motion2coach.util.camera2utils.SIZE_1080P
import com.fenris.motion2coach.util.camera2utils.SmartSize
import com.fenris.motion2coach.util.camera2utils.getDisplaySmartSize
import com.fenris.motion2coach.view.activity.*
import com.fenris.motion2coach.view.activity.loading.LoadingActivity
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.player.PlayVideoActivity
import com.fenris.motion2coach.view.activity.settings.RecordSettingsActivity
import com.fenris.motion2coach.viewmodel.SessionViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.io.Serializable
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@AndroidEntryPoint
class CameraSamsungActivity : AppCompatActivity() {

    private lateinit var cameraProviderX: ProcessCameraProvider
    private lateinit var previewCamX: Preview
    private var recordingStarted: Boolean = false
    /** Android ViewBinding */
    private var viewBinding: ActivityCaptureVideoBinding? = null
    private lateinit var zoom: Rect
    private var finger_spacing: Float = 0f
    private var zoom_level = 1
    private lateinit var recordBuilder: CaptureRequest.Builder
    private lateinit var previewBuilder: CaptureRequest.Builder
    var flag_start_end = true
    private lateinit var param: Bundle
    private var textToSpeechSystem: TextToSpeech? = null
    private var exoPlayer: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var timerTime: Int = 3
    private var fileName: String = ""
    private var mimeType: String = ""
    private var path: String = ""
    private var clubType: Int = 1
    var screenPause = false
    private var fpsRangesFront: Int = 120
    private var mCameraId: String = "0"
    var fpsRanges: Int? = 120
    var resolutionSelected1Back: String? = "1280"
    var resolutionSelected2Back: String? = "720"
    var resolutionSelected1Front: String? = "1280"
    var resolutionSelected2Front: String? = "720"
    var stepPassed: Int? = 0
    val viewModel: SessionViewModel by viewModels<SessionViewModel>()
    private var recordType: String = ""
    private var remainingSecondsFinal: Int? = 0
    private var file: File? = null
    private var progressDialog: ProgressDialog? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>


    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        previewCamX = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector
        if (mCameraId == "1") {
            cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()
        } else {
            cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
        }
        viewBinding!!.previewXView.visibility = View.VISIBLE
        previewCamX.setSurfaceProvider(viewBinding!!.previewXView.surfaceProvider)
        var camera =
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, previewCamX)
    }

    private fun preparePlayer(path: String) {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.playWhenReady = true
        viewBinding!!.exo.player = exoPlayer
        val defaultHttpDataSourceFactory = FileDataSource.Factory()
        val mediaItem =
            MediaItem.fromUri(Uri.fromFile(File(path)))
        val mediaSource =
            ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(mediaItem)
        exoPlayer?.setMediaSource(mediaSource)
        exoPlayer?.setPlaybackSpeed(0.5f)

        exoPlayer?.seekTo(playbackPosition)
        exoPlayer?.playWhenReady = playWhenReady
        exoPlayer?.prepare()
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            playWhenReady = player.playWhenReady
            player.release()
            exoPlayer = null
        }
    }

    override fun onPause() {
        super.onPause()
        screenPause = true
        releasePlayer()
    }

    private fun mute() {
        param.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.0f)
    }

    private fun unMute() {
        param.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
    }

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** [CameraCharacteristics] corresponding to the provided Camera ID */
    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(mCameraId)
    }

    override fun onBackPressed() {
        if (!recordingStarted) {
            finish()
        }

    }

    /** File where the recording will be saved */
    private val outputFile: File by lazy { createFile(applicationContext, "mp4") }

    /**
     * Setup a persistent [Surface] for the recorder so we can use it as an output target for the
     * camera session without preparing the recorder
     */
    private val recorderSurface: Surface by lazy {

        // Get a persistent Surface from MediaCodec, don't forget to release when done
        val surface = MediaCodec.createPersistentInputSurface()

        // Prepare and release a dummy MediaRecorder with our new surface
        // Required to allocate an appropriately sized buffer before passing the Surface as the
        //  output target to the high speed capture session
        createRecorder(surface).apply {
            prepare()
            release()
        }

        surface
    }

    /** Saves the video recording */
    private val recorder: MediaRecorder by lazy { createRecorder(recorderSurface) }

    /** [HandlerThread] where all camera operations run */
    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    /** [Handler] corresponding to [cameraThread] */
    private val cameraHandler = Handler(cameraThread.looper)

    /** Performs recording animation of flashing screen */
    private val animationTask: Runnable by lazy {
        Runnable {
        }
    }

    /** Captures high speed frames from a [CameraDevice] for our slow motion video recording */
    private lateinit var session: CameraConstrainedHighSpeedCaptureSession

    /** The [CameraDevice] that will be opened in this fragment */
    private lateinit var camera: CameraDevice

    /** Requests used for preview only in the [CameraConstrainedHighSpeedCaptureSession] */
    private val previewRequestList: List<CaptureRequest> by lazy {
        // Capture request holds references to target surfaces
        session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            previewBuilder = this

            // Add the preview surface target
            addTarget(viewBinding!!.viewFinder.holder.surface)
            // High speed capture session requires a target FPS range, even for preview only

//            ISO
            if (SessionManager.getStringPref(
                    HelperKeys.ISO_FRONT,
                    applicationContext
                ) != "" && mCameraId == "1"
            ) {
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)

                set(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    SessionManager.getStringPref(HelperKeys.ISO_FRONT, applicationContext).toInt()
                )
            }
            if (SessionManager.getStringPref(
                    HelperKeys.ISO_BACK,
                    applicationContext
                ) != "" && mCameraId == "0"
            ) {
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)

                set(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    SessionManager.getStringPref(HelperKeys.ISO_BACK, applicationContext).toInt()
                )
            }
//            Shutter speed
            if (SessionManager.getStringPref(
                    HelperKeys.SS_BACK,
                    applicationContext
                ) != "" && mCameraId == "0"
            ) {
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)

                set(
                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                    SessionManager.getStringPref(HelperKeys.SS_BACK, applicationContext).toLong()
                )
            }
            if (SessionManager.getStringPref(
                    HelperKeys.SS_FRONT,
                    applicationContext
                ) != "" && mCameraId == "1"
            ) {
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)

                set(
                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                    SessionManager.getStringPref(HelperKeys.SS_FRONT, applicationContext).toLong()
                )
            }

            set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(FPS_PREVIEW_ONLY, fpsRanges!!))
//            set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getOptimalFpsRange())
            if (::zoom.isInitialized) {
                set(
                    CaptureRequest.SCALER_CROP_REGION,
                    zoom
                )
            }
        }.let {
            // Creates a list of highly optimized capture requests sent to the camera for a high
            // speed video session. Important note: Must use repeating burst request type
            session.createHighSpeedRequestList(it.build())
        }
    }

    /** Requests used for preview and recording in the [CameraConstrainedHighSpeedCaptureSession] */
    private val recordRequestList: List<CaptureRequest> by lazy {
        // Capture request holds references to target surfaces
        session.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            recordBuilder = this
//            ISO
            if (SessionManager.getStringPref(
                    HelperKeys.ISO_FRONT,
                    applicationContext
                ) != "" && mCameraId == "1"
            ) {
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                set(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    SessionManager.getStringPref(HelperKeys.ISO_FRONT, applicationContext).toInt()
                )
            }
            if (SessionManager.getStringPref(
                    HelperKeys.ISO_BACK,
                    applicationContext
                ) != "" && mCameraId == "0"
            ) {
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)

                set(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    SessionManager.getStringPref(HelperKeys.ISO_BACK, applicationContext).toInt()
                )
            }
//            Shutter speed
            if (SessionManager.getStringPref(
                    HelperKeys.SS_BACK,
                    applicationContext
                ) != "" && mCameraId == "0"
            ) {
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)

                set(
                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                    SessionManager.getStringPref(HelperKeys.SS_BACK, applicationContext).toLong()
                )
            }
            if (SessionManager.getStringPref(
                    HelperKeys.SS_FRONT,
                    applicationContext
                ) != "" && mCameraId == "1"
            ) {
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)

                set(
                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                    SessionManager.getStringPref(HelperKeys.SS_FRONT, applicationContext).toLong()
                )
            }
            // Add the preview and recording surface targets
            addTarget(viewBinding!!.viewFinder.holder.surface)
            addTarget(recorderSurface)
            // Sets user requested FPS for all targets
            if (mCameraId == "1") {
                set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(30!!, fpsRangesFront!!))

            } else {
                set(
                    CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                    Range(fpsRanges!!.toInt(), fpsRanges!!)
                )

            }
            if (::zoom.isInitialized) {
                set(
                    CaptureRequest.SCALER_CROP_REGION,
                    zoom
                )
            }
        }.let {
            // Creates a list of highly optimized capture requests sent to the camera for a high
            // speed video session. Important note: Must use repeating burst request type
            session.createHighSpeedRequestList(it.build())
        }
    }

    private var recordingStartMillis: Long = 0L

    /** Live data listener for changes in the device orientation relative to the camera */
    private lateinit var relativeOrientation: OrientationLiveData

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCaptureVideoBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        preventSleep()

        progressDialog = ProgressDialog(this@CameraSamsungActivity)
        progressDialog!!.setMessage("Uploading video.Please wait..")
        progressDialog!!.setCancelable(false)
        onCreateFunctionality()
//        getCamerasMegaPixel()?.let { Log.e("megapixel", it) }


    }

    @Throws(CameraAccessException::class)
    fun getCamerasMegaPixel(): String? {
        var output = ""
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIds = manager.cameraIdList
        var characteristics = manager.getCameraCharacteristics(cameraIds[0])
        output = "back camera mega pixel: " + calculateMegaPixel(
            characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!
                .width.toFloat(),
            characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!.height.toFloat()
        ) + "\n"
        characteristics = manager.getCameraCharacteristics(cameraIds[1])
        output += "front camera mega pixel: " + calculateMegaPixel(
            characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!
                .width.toFloat(),
            characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!
                .height.toFloat()
        ) + "\n"
        return output
    }

    private fun calculateMegaPixel(width: Float, height: Float): Int {
        return Math.round(width * height / 1024000)
    }
    private fun openClubTypeDialog(activity: Activity){

        val builder = AlertDialog.Builder(activity)
        builder
            .setTitle("Select Club Type")
//            .setMessage("Your camera is supporting 30fps . Do you want to continue video recording?")
            .setPositiveButton(
                "Driver"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
                clubType=2
                viewBinding!!.ivClub.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.vector_driver,
                        null
                    )
                )
                SessionManager.putStringPref(HelperKeys.CLUB_TYPE, "2",applicationContext)
            }
            .setNegativeButton("Iron") { dialogInterface, _ ->
                dialogInterface.dismiss()
                clubType=1
                viewBinding!!.ivClub.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.vector_iron,
                        null
                    )
                )
                SessionManager.putStringPref(HelperKeys.CLUB_TYPE, "1",applicationContext)

            }
            .setNeutralButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->

            })
        builder.create().show()
    }

    /** Creates a [MediaRecorder] instance using the provided [Surface] as input */
    private fun createRecorder(surface: Surface) = MediaRecorder().apply {
//        setAudioSource(MediaRecorder.AudioSource.MIC)
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setOutputFile(outputFile.absolutePath)
        setVideoEncodingBitRate(RECORDER_VIDEO_BITRATE)
        if (mCameraId == "1") {
            setVideoFrameRate(fpsRangesFront)
        } else {
            setVideoFrameRate(fpsRanges!!)
        }

        if (mCameraId == "1") {
            setVideoSize(resolutionSelected1Front!!.toInt(), resolutionSelected2Front!!.toInt())
        } else {
            setVideoSize(resolutionSelected1Back!!.toInt(), resolutionSelected2Back!!.toInt())
        }

        setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setInputSurface(surface)
    }

    /**
     * Preview size is subject to the same rules compared to a normal capture session with the
     * additional constraint that the selected size must also be available as one of possible
     * constrained high-speed session sizes.
     */
    private fun <T> getConstrainedPreviewOutputSize(
        display: Display,
        characteristics: CameraCharacteristics,
        targetClass: Class<T>,
        format: Int? = null
    ): Size {

        // Find which is smaller: screen or 1080p
        val screenSize = getDisplaySmartSize(display)
        val hdScreen = screenSize.long >= SIZE_1080P.long || screenSize.short >= SIZE_1080P.short
        val maxSize = if (hdScreen) SIZE_1080P else screenSize

        // If image format is provided, use it to determine supported sizes; else use target class
        val config = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )!!
        if (format == null) {
            assert(StreamConfigurationMap.isOutputSupportedFor(targetClass))
        } else {
            assert(config.isOutputSupportedFor(format))
        }

        val allSizes = if (format == null)
            config.getOutputSizes(targetClass) else config.getOutputSizes(format)

        // Get a list of potential high speed video sizes for the selected FPS
        var range: Range<Int>? = null
        if (mCameraId == "1") {
            range = Range(fpsRangesFront!!.toInt(), fpsRangesFront!!.toInt())
        } else {
            range = Range(fpsRanges!!.toInt(), fpsRanges!!.toInt())
        }
        val highSpeedSizes = config.getHighSpeedVideoSizesFor(range)

        // Filter sizes which are part of the high speed constrained session
        val validSizes = allSizes
            .filter { highSpeedSizes.contains(it) }
            .sortedWith(compareBy { it.height * it.width })
            .map { SmartSize(it.width, it.height) }.reversed()

        // Then, get the largest output size that is smaller or equal than our max size
        return validSizes.first { it.long <= maxSize.long && it.short <= maxSize.short }.size
    }

    /**
     * Begin all camera operations in a coroutine in the main thread. This function:
     * - Opens the camera
     * - Configures the camera session
     * - Starts the preview by dispatching a repeating burst request
     */
    @SuppressLint("ClickableViewAccessibility", "IntentReset")
    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {

        // Open the selected camera
//        camera = openCamera(cameraManager, mCameraId, cameraHandler)

        // Creates list of Surfaces where the camera will output frames
//        val targets = listOf(viewBinding!!.viewFinder.holder.surface, recorderSurface)

        // Start a capture session using our open camera and list of Surfaces where frames will go
//        session = createCaptureSession(camera, targets, cameraHandler)
//        viewBinding!!.viewFinder.setOnTouchListener { v, event ->
//            onTouch(event)
//        }
        // Ensures the requested size and FPS are compatible with this camera
//        var fpsRange: Range<Int>? =null
//        fpsRange = if (mCameraId=="1"){
//            Range(fpsRangesFront!!, fpsRangesFront!!)
//        }else{
//            Range(fpsRanges!!, fpsRanges!!)
//        }
//        assert(true == characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//                ?.getHighSpeedVideoFpsRangesFor(Size(1280, 720))?.contains(fpsRange))

        // Sends the capture request as frequently as possible until the session is torn down or
        // session.stopRepeating() is called

//        session.setRepeatingBurst(previewRequestList, null, cameraHandler)
        cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)

        cameraProviderFuture.addListener(Runnable {
            cameraProviderX = cameraProviderFuture.get()
            bindPreview(cameraProviderX)
        }, ContextCompat.getMainExecutor(applicationContext))

        // React to user touching the capture button

        viewBinding!!.stop.setOnClickListener {

//            viewBinding!!.secondbottom.visibility = View.INVISIBLE

            viewBinding!!.ivAudio.visibility = View.INVISIBLE
            viewBinding!!.ivClub.visibility = View.INVISIBLE
            lifecycleScope.launch(Dispatchers.Main) {
//                cameraProviderX.unbind(previewCamX)
//                session.stopRepeating()
//                session.setRepeatingBurst(previewRequestList, null, cameraHandler)
                if (!SessionManager.getStringPref(HelperKeys.CONT_RECORD, applicationContext)
                        .equals("")
                ) {
                    val intValue =
                        SessionManager.getStringPref(HelperKeys.NO_OF_RECORD, applicationContext)
                            .toInt() - 1
                    SessionManager.putStringPref(
                        HelperKeys.CAPTURE_COUNT_TEMP,
                        "" + intValue,
                        applicationContext
                    )
                }
                recordingStarted = true
                captureVideo()
            }


        }

        if (intent.getStringExtra("capture_mode").equals("yes")) {
            if (!SessionManager.getStringPref(HelperKeys.CONT_RECORD, applicationContext)
                    .equals("")
            ) {
                val intValue =
                    SessionManager.getStringPref(HelperKeys.CAPTURE_COUNT_TEMP, applicationContext)
                        .toInt() - 1
                SessionManager.putStringPref(
                    HelperKeys.CAPTURE_COUNT_TEMP,
                    "" + intValue.toString(),
                    applicationContext
                )
                captureVideo()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun captureCoreVIdeo() {
        if (flag_start_end) {
            flag_start_end = !flag_start_end
            lifecycleScope.launch(Dispatchers.IO) {

                // Prevents screen rotation during the video recording
                requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LOCKED
                runOnUiThread {
                    viewBinding!!.previewXView.visibility = View.GONE
                }

                // Stops preview requests, and start record requests
                session.stopRepeating()
                session.setRepeatingBurst(recordRequestList, null, cameraHandler)

                // Finalizes recorder setup and starts recording
                recorder.apply {
                    // Sets output orientation based on current sensor value at start time
                    relativeOrientation.value?.let { setOrientationHint(it) }
                    prepare()
                    start()
                }
                recordingStartMillis = System.currentTimeMillis()
                Log.d(TAG, "Recording started")

                // Starts recording animation
//                viewBinding!!.overlay.post(animationTask)
                // Starts recording animation
                viewBinding!!.overlay.post(animationTask)
                runOnUiThread {
                    startCameraTimer()
                }
            }

        } else if (!flag_start_end) {
            flag_start_end = !flag_start_end
            // Unlocks screen rotation after recording finished
            recordingStarted = false

            requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            // Requires recording of at least MIN_REQUIRED_RECORDING_TIME_MILLIS
            val elapsedTimeMillis = System.currentTimeMillis() - recordingStartMillis
//            if (elapsedTimeMillis < MIN_REQUIRED_RECORDING_TIME_MILLIS) {
//                delay(MIN_REQUIRED_RECORDING_TIME_MILLIS - elapsedTimeMillis)
//            }

            Log.d(TAG, "Recording stopped. Output file: $outputFile")
            recorder.stop()
            // Removes recording animation
            viewBinding!!.overlay.removeCallbacks(animationTask)
            // Broadcasts the media file to the rest of the system
            MediaScannerConnection.scanFile(
                applicationContext, arrayOf(outputFile.absolutePath), null, null
            )

            // Launch external activity via intent to play video recorded using our provider
//            startActivity(Intent().apply {
//                action = Intent.ACTION_VIEW
//                type = MimeTypeMap.getSingleton()
//                    .getMimeTypeFromExtension(outputFile.extension)
//                val authority = "${BuildConfig.APPLICATION_ID}.provider"
//                data = FileProvider.getUriForFile(applicationContext, authority, outputFile)
//                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
//                        Intent.FLAG_ACTIVITY_CLEAR_TOP
//            })

            // Finishes our current camera screen
            //Permission needed if you want to retain access even after reboot
            // Perform operations on the document using its URI.
            //Save file to upload on server
            file = outputFile
            path = outputFile.absolutePath
            mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(outputFile.extension).toString()


            runOnUiThread {
                // Stuff that updates the UI
                val valInt = SessionManager.getStringPref(
                    HelperKeys.CAPTURE_COUNT_TEMP,
                    applicationContext
                )


                textToSpeechSystem!!.speak("Finished", TextToSpeech.QUEUE_ADD, param, null)
                if (valInt.equals("0") || valInt.equals("") || SessionManager.getStringPref(
                        HelperKeys.CONT_RECORD,
                        applicationContext
                    ).equals("")
                ) {
                    if (!SessionManager.getStringPref(
                            HelperKeys.CONT_RECORD,
                            applicationContext
                        ).equals("")
                    ) {

                    }
//                        hello
                    viewBinding!!.tvRecordNoShow.visibility = View.GONE
                    viewBinding!!.fifthcenter.visibility = View.GONE
                    viewBinding!!.stop.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.video_play_button,
                            null
                        )
                    )
                    viewBinding!!.stop.visibility = View.INVISIBLE
                    viewBinding!!.lnAfterVideo.visibility = View.VISIBLE
//                    viewBinding!!.secondtop.visibility = View.INVISIBLE
//                    viewBinding!!.secondbottom.visibility = View.INVISIBLE

                    viewBinding!!.ivAudio.visibility = View.INVISIBLE
                    viewBinding!!.ivGrid.setImageResource(android.R.color.transparent)
//                        animMoveToTop!!.cancel()
                    if (recordType == "down") {
//                        viewBinding!!.ivAnimateRectVertical.clearAnimation()
//                        viewBinding!!.ivAnimateRectVertical.visibility = View.GONE
//                        viewBinding!!.ivLineVertical.visibility = View.GONE
                    } else if (recordType == "face") {
//                            viewBinding!!.ivAnimateRectVertical.clearAnimation()
//                            viewBinding!!.ivAnimateRectHorizontal.visibility=View.GONE
//                            viewBinding!!.ivLineHorizontal.visibility=View.GONE
                    }
                    viewBinding!!.lnReport.visibility = View.GONE
                    viewBinding!!.exo.visibility = View.VISIBLE
                    preparePlayer(path)
                } else {

                    uploadMedia(
                        mimeType, File(file!!.path).name, file!!.path, 0, Video(
                            0,
                            file?.path,
                            Helper.convertDateToLong(),
                            Helper.convertTimeToLong(),
                            "saved",
                            mimeType,
                            SessionManager.getStringPref(
                                HelperKeys.ORIENTATION,
                                applicationContext
                            ),
                            "120",
                            SessionManager.getStringPref(HelperKeys.HAND_USED, applicationContext),
                            "",
                            0,1
                        )
                    )

                }
            }
        }
    }

    private fun startCameraTimer() {
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {


            }

            override fun onFinish() {
                captureVideo()
            }
        }.start()
    }

    private fun captureVideo() {
        viewBinding!!.viewFinder.isEnabled = false

        if (!flag_start_end) {
            captureCoreVIdeo()
        } else {

            if (timerTime != 0) {

                viewBinding!!.thirdcenter.visibility = View.GONE
                viewBinding!!.fourthcenter.visibility = View.VISIBLE
                viewBinding!!.stop.visibility = View.INVISIBLE
                viewBinding!!.circularCountDownView.startTimer()
            } else {


                if (SessionManager.getStringPref(HelperKeys.TIMER_TIME, applicationContext).equals("")) {
                    cameraProviderX.unbind(previewCamX)
                    viewBinding!!.previewXView.visibility=View.GONE
                    lifecycleScope.launch(Dispatchers.Main) {
                        camera = openCamera(cameraManager, mCameraId, cameraHandler)
                        val targets = listOf(viewBinding!!.viewFinder.holder.surface, recorderSurface)

                        // Start a capture session using our open camera and list of Surfaces where frames will go
                        session = createCaptureSession(camera, targets, cameraHandler)
                        var fpsRange: Range<Int>? = null
                        fpsRange = if (mCameraId == "1") {
                            Range(fpsRangesFront, fpsRangesFront)
                        } else {
                            Range(fpsRanges!!, fpsRanges!!)
                        }

                        assert(
                            true == characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                ?.getHighSpeedVideoFpsRangesFor(Size(1280, 720))?.contains(fpsRange)
                        )
                        session.stopRepeating()
                        session.setRepeatingBurst(previewRequestList, null, cameraHandler)
                        if (remainingSecondsFinal == 0) {
                            textToSpeechSystem!!.speak("Go", TextToSpeech.QUEUE_ADD, param, null)
                        }
                        viewBinding!!.stop.setImageDrawable(resources.getDrawable(R.drawable.stop, null))
                        viewBinding!!.stop.visibility = View.INVISIBLE
                        viewBinding!!.fourthcenter.visibility = View.GONE
                        viewBinding!!.fifthcenter.visibility = View.VISIBLE
                        captureCoreVIdeo()
                    }
                }
//                captureCoreVIdeo()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (allPermissionsGranted()) {
            // code
            intent.putExtra("capture_mode", "no")
            intent.putExtra("flag_from", intent.getStringExtra("flag_from"))

            recreate()
        } else {
            Helper.openPermissionSettings(this@CameraSamsungActivity)
        }

    }

    /** Opens the camera and returns the opened device (as the result of the suspend coroutine) */
    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        handler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                Log.w(TAG, "Camera $cameraId has been disconnected")
                finish()
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Log.e(TAG, exc.message, exc)
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }

    /**
     * Creates a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine)
     */
    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
        handler: Handler? = null
    ): CameraConstrainedHighSpeedCaptureSession = suspendCoroutine { cont ->

        // Creates a capture session using the predefined targets, and defines a session state
        // callback which resumes the coroutine once the session is configured
        device.createConstrainedHighSpeedCaptureSession(
            targets, object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(session: CameraCaptureSession) =
                    cont.resume(session as CameraConstrainedHighSpeedCaptureSession)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    val exc = RuntimeException("Camera ${device.id} session configuration failed")
                    Log.e(TAG, exc.message, exc)

                    cont.resumeWithException(exc)
                }
            }, handler
        )
    }

    override fun onStop() {
        super.onStop()
        try {
            if (textToSpeechSystem != null) {
                textToSpeechSystem!!.shutdown()
            }
            if (this::camera.isInitialized){
                camera.close()
            }
        } catch (exc: Throwable) {
            Log.e(TAG, "Error closing camera", exc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
        recorder.release()
        recorderSurface.release()
    }


    companion object {
        private val TAG = CameraSamsungActivity::class.java.simpleName

        private const val RECORDER_VIDEO_BITRATE: Int = 10000000
        private const val MIN_REQUIRED_RECORDING_TIME_MILLIS: Long = 1000L

        /**
         * FPS rate for preview-only requests, 30 is *guaranteed* by framework. See:
         * [StreamConfigurationMap.getHighSpeedVideoFpsRanges]
         */
        private const val FPS_PREVIEW_ONLY: Int = 30
        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        /** Creates a [File] named with the current date and time */
        private fun createFile(context: Context, extension: String): File {
            val timeDate = System.currentTimeMillis()
            val usrId = SessionManager.getStringPref(HelperKeys.USER_ID, context)
            val und = "-"
            val fileName = "m2c-and-$usrId$und$timeDate.${extension}"
            val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
            return File(context.filesDir, fileName)
        }
    }

    //Determine the space between the first two fingers
    private fun getFingerSpacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    fun onTouch(event: MotionEvent): Boolean {
        try {
            val activity: Activity = this@CameraSamsungActivity
            val manager = activity.getSystemService(CAMERA_SERVICE) as CameraManager
            val characteristics = manager.getCameraCharacteristics(mCameraId)
            val maxzoom =
                characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)!! * 10
            val m: Rect? = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            val action = event.action
            val current_finger_spacing: Float
            if (event.pointerCount > 1) {
                // Multi touch logic
                current_finger_spacing = getFingerSpacing(event)
                if (finger_spacing != 0f) {
                    if (current_finger_spacing > finger_spacing && maxzoom > zoom_level) {
                        zoom_level++
                    } else if (current_finger_spacing < finger_spacing && zoom_level > 1) {
                        zoom_level--
                    }
                    val minW = (m!!.width() / maxzoom).toInt()
                    val minH = (m.height() / maxzoom).toInt()
                    val difW: Int = m.width() - minW
                    val difH: Int = m.height() - minH
                    var cropW = difW / 100 * zoom_level as Int
                    var cropH = difH / 100 * zoom_level as Int
                    cropW -= cropW and 3
                    cropH -= cropH and 3
                    zoom = Rect(cropW, cropH, m.width() - cropW, m.height() - cropH)
                    previewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom)
//                    if (recordBuilder!=null){
//                        recordBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom)
//                    }

                }
                finger_spacing = current_finger_spacing
            } else {
                if (action == MotionEvent.ACTION_UP) {
                    //single touch logic
                }
            }
            try {
                session
                    .setRepeatingRequest(previewBuilder.build(), null, null)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            } catch (ex: NullPointerException) {
                ex.printStackTrace()
            }
        } catch (e: CameraAccessException) {
            throw java.lang.RuntimeException("can not access camera.", e)
        }
        return true
    }

    private fun onCreateFunctionality() {

        val club = SessionManager.getStringPref(HelperKeys.CLUB_TYPE, applicationContext)
        if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID,applicationContext)=="1"){
            if (club=="2"){
                clubType=2
                viewBinding!!.ivClub.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.vector_driver,
                        null
                    )
                )
            }else{

                clubType=1
                viewBinding!!.ivClub.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.vector_iron,
                        null
                    )
                )
                SessionManager.putStringPref(HelperKeys.CLUB_TYPE,"1",applicationContext)
            }
        }else{

            clubType=1
            viewBinding!!.ivClub.setImageDrawable(
                resources.getDrawable(
                    R.drawable.vector_iron,
                    null
                )
            )
            SessionManager.putStringPref(HelperKeys.CLUB_TYPE,"1",applicationContext)
        }

        val switch = SessionManager.getStringPref(HelperKeys.CAM_SWITCH, applicationContext)
        mCameraId = if (switch == "1") {
            "1"
        } else {
            "0"
        }
        val fps12 = SessionManager.getStringPref(HelperKeys.FPS_RANGE, applicationContext)
        if (!fps12.equals("") && mCameraId == "0") {
            fpsRanges = fps12.toInt()
        } else if (mCameraId == "0") {
            val cameraManager =
                getSystemService(Context.CAMERA_SERVICE) as CameraManager
            enumerateHighSpeedCamerasBack(cameraManager)
        }

        val fps123 = SessionManager.getStringPref(HelperKeys.FPS_RANGE_FRONT, applicationContext)
        if (!fps123.equals("") && mCameraId == "1") {
            fpsRangesFront = fps123.toInt()
        } else if (mCameraId == "1") {
            val cameraManager =
                getSystemService(Context.CAMERA_SERVICE) as CameraManager
            enumerateHighSpeedCamerasFront(cameraManager)
        }

        val fpsResolution =
            SessionManager.getStringPref(HelperKeys.FPS_RESOLUTION, applicationContext)
        if (!fpsResolution.equals("") && mCameraId == "0") {
            val splitArray: List<String> = fpsResolution.split("x")
            resolutionSelected1Back = splitArray[0]
            resolutionSelected2Back = splitArray[1]
        }

        val fpsResolutionFront =
            SessionManager.getStringPref(HelperKeys.FPS_RESOLUTION_FRONT, applicationContext)
        if (!fpsResolutionFront.equals("") && mCameraId == "1") {
            val splitArray: List<String> = fpsResolutionFront.split("x")
            resolutionSelected1Front = splitArray[0]
            resolutionSelected2Front = splitArray[1]
        }


        if (intent.getStringExtra("flag_from") != null) {
            recordType = intent.getStringExtra("flag_from")!!
        }

        viewBinding!!.ivSwitch.setOnClickListener {
            var switch1 = SessionManager.getStringPref(HelperKeys.CAM_SWITCH, applicationContext)
            switch1 = if (switch1 == "0") {
                "1"
            } else {
                "0"
            }
            SessionManager.putStringPref(HelperKeys.CAM_SWITCH, "" + switch1, applicationContext)

            intent.putExtra("capture_mode", "no")
            intent.putExtra("flag_from", intent.getStringExtra("flag_from"))

            recreate()
        }


        if (recordType == "down") {
//            viewBinding!!.ivAnimateRectVertical.visibility = View.VISIBLE
//            viewBinding!!.ivLineVertical.visibility = View.VISIBLE
        } else if (recordType == "face") {

        }
        var x = 0.0
        var y = 0.0
        viewBinding!!.ivAnimateRectHorizontal.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = (v.x - event.rawX).toDouble()
                    y = (v.y - event.rawY).toDouble()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    viewBinding!!.ivAnimateRectHorizontal.animate()
                        .x(event.rawX + x.toFloat())
                        .setDuration(0)
                        .start()
                    true
                }
                else -> true
            }
        }
        x = 0.0
        y = 0.0
        viewBinding!!.ivAnimateRectVertical.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = (v.x - event.rawX).toDouble()
                    y = (v.y - event.rawY).toDouble()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    viewBinding!!.ivAnimateRectVertical.animate()
                        .y(event.rawY + y.toFloat())
                        .setDuration(0)
                        .start()
                    true
                }
                else -> true
            }
        }


        if (allPermissionsGranted()) {
            // code
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        if (!SessionManager.getStringPref(HelperKeys.TIMER_TIME, applicationContext).equals("")) {
            timerTime =
                SessionManager.getStringPref(HelperKeys.TIMER_TIME, applicationContext).toInt()
            viewBinding!!.tvTimershow.text = timerTime.toString() + "s"
            timerTime += 1

        } else {
            timerTime=0
            viewBinding!!.tvTimershow.text = "Off"

        }


        val noRECORD =
            SessionManager.getStringPref(HelperKeys.NO_OF_RECORD, applicationContext)
        val noTEMP =
            SessionManager.getStringPref(HelperKeys.CAPTURE_COUNT_TEMP, applicationContext)

        if (noRECORD == "") {
            viewBinding!!.tvRecordNoShow.text = "0/1 Recorded"
        } else if (noTEMP == "") {
            viewBinding!!.tvRecordNoShow.text = "0/$noRECORD Recorded"
        } else {
            viewBinding!!.tvRecordNoShow.text = "$noTEMP/$noRECORD Recorded"
        }

        viewBinding!!.circularCountDownView.initTimer(timerTime)
        viewBinding!!.circularCountDownView.setOnTickListener(object : HappyTimer.OnTickListener {

            //OnTick
            override fun onTick(completedSeconds: Int, remainingSeconds: Int) {
                remainingSecondsFinal = remainingSeconds
                if (remainingSeconds==1){
                    cameraProviderX.unbind(previewCamX)
                    viewBinding!!.previewXView.visibility=View.GONE
                    lifecycleScope.launch(Dispatchers.Main) {
                        camera = openCamera(cameraManager, mCameraId, cameraHandler)
                        val targets = listOf(viewBinding!!.viewFinder.holder.surface, recorderSurface)

                        // Start a capture session using our open camera and list of Surfaces where frames will go
                        session = createCaptureSession(camera, targets, cameraHandler)
                        var fpsRange: Range<Int>? = null
                        fpsRange = if (mCameraId == "1") {
                            Range(fpsRangesFront, fpsRangesFront)
                        } else {
                            Range(fpsRanges!!, fpsRanges!!)
                        }

                        assert(
                            true == characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                ?.getHighSpeedVideoFpsRangesFor(Size(1280, 720))?.contains(fpsRange)
                        )
                        session.stopRepeating()
                        session.setRepeatingBurst(previewRequestList, null, cameraHandler)
                    }
                }
                if (remainingSeconds != 0) {
                    textToSpeechSystem!!.speak(
                        "" + remainingSeconds,
                        TextToSpeech.QUEUE_ADD,
                        param,
                        null
                    )
                }
            }

            //OnTimeUp
            override fun onTimeUp() {


                if (remainingSecondsFinal == 0) {
                    textToSpeechSystem!!.speak("Go", TextToSpeech.QUEUE_ADD, param, null)
                }
                viewBinding!!.stop.setImageDrawable(resources.getDrawable(R.drawable.stop, null))
                viewBinding!!.stop.visibility = View.INVISIBLE
                viewBinding!!.fourthcenter.visibility = View.GONE
                viewBinding!!.fifthcenter.visibility = View.VISIBLE
                captureCoreVIdeo()
            }
        })


        // Set up the listeners for video capture button
        viewBinding!!.ivBack.setOnClickListener {
            if (!recordingStarted) {
                finish()
            }
//            startActivity(Intent(applicationContext, HistoryActivity::class.java))
        }
        viewBinding!!.ivClub.setOnClickListener {
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID,applicationContext)=="1") {
                openClubTypeDialog(this@CameraSamsungActivity)
            }
        }


        viewBinding!!.ivAudio.setOnClickListener {
            audioSetting()
        }
        viewBinding!!.ivSettings.setOnClickListener {
            val intentSet = Intent(applicationContext, RecordSettingsActivity::class.java)
            intentSet.putExtra("flag_from", intent.getStringExtra("flag_from"))
            startActivity(intentSet)
            finish()
        }

        viewBinding!!.btnSession.setOnClickListener {
//            if (path != ""){
//                viewBinding!!.btnUpload.visibility=View.INVISIBLE
//                viewBinding!!.lnReport.visibility=View.VISIBLE
//                val tmpFile= File(path).name
//                fileName =tmpFile
//                uploadMedia(mimeType,tmpFile, path)
//            }
            if (SessionManager.getStringPref(HelperKeys.CONT_RECORD, applicationContext)
                    .equals("")
            ) {
                val videoModel = Video(
                    0,
                    file?.path,
                    Helper.convertDateToLong(),
                    Helper.convertTimeToLong(),
                    "saved",
                    mimeType,
                    SessionManager.getStringPref(HelperKeys.ORIENTATION, applicationContext),
                    "120",
                    SessionManager.getStringPref(HelperKeys.HAND_USED, applicationContext),
                    "",
                    0,1
                )

                val intnt = Intent(applicationContext, LoadingActivity::class.java)
                intnt.putExtra("capture_mode", "no")
                intnt.putExtra("flag_from", intent.getStringExtra("flag_from"))
                intnt.putExtra("list", videoModel as Serializable)
                startActivity(intnt)
                finish()

            } else {
                val videoModel = Video(
                    0,
                    file?.path,
                    Helper.convertDateToLong(),
                    Helper.convertTimeToLong(),
                    "saved",
                    mimeType,
                    SessionManager.getStringPref(HelperKeys.ORIENTATION, applicationContext),
                    "120",
                    SessionManager.getStringPref(HelperKeys.HAND_USED, applicationContext),
                    "",
                    0,1
                )
                val intnt = Intent(applicationContext, LoadingActivity::class.java)
                intnt.putExtra("capture_mode", "no")
                intnt.putExtra("flag_from", intent.getStringExtra("flag_from"))
                intnt.putExtra("list", videoModel as Serializable)
                startActivity(intnt)
                finish()
            }
        }
        viewBinding!!.ivTimer.setOnClickListener {
            showHideTimer()
        }
        viewBinding!!.tvTimershow.setOnClickListener {
            showHideTimer()
        }
        viewBinding!!.repeat.setOnClickListener {
            intent.putExtra("capture_mode", "no")
            intent.putExtra("flag_from", intent.getStringExtra("flag_from"))
            recreate()
        }
        viewBinding!!.tvTenSec.setOnClickListener {

            viewBinding!!.secondbottom.visibility = View.VISIBLE
            viewBinding!!.lnAfterTimer.visibility = View.INVISIBLE
            timerTime = 10
            SessionManager.putStringPref(
                HelperKeys.TIMER_TIME,
                timerTime.toString(),
                applicationContext
            )
            viewBinding!!.circularCountDownView.initTimer(timerTime + 1)
            viewBinding!!.tvTimershow.text = timerTime.toString() + "s"

        }

        viewBinding!!.tvThreeSec.setOnClickListener {

            viewBinding!!.secondbottom.visibility = View.VISIBLE
            viewBinding!!.lnAfterTimer.visibility = View.INVISIBLE
            timerTime = 3
            SessionManager.putStringPref(
                HelperKeys.TIMER_TIME,
                timerTime.toString(),
                applicationContext
            )
            viewBinding!!.circularCountDownView.initTimer(timerTime + 1)
            viewBinding!!.tvTimershow.text = timerTime.toString() + "s"

        }

        viewBinding!!.tvFiveSec.setOnClickListener {
            viewBinding!!.secondbottom.visibility = View.VISIBLE
            viewBinding!!.lnAfterTimer.visibility = View.INVISIBLE

            timerTime = 5
            SessionManager.putStringPref(
                HelperKeys.TIMER_TIME,
                timerTime.toString(),
                applicationContext
            )

            viewBinding!!.circularCountDownView.initTimer(timerTime + 1)
            viewBinding!!.tvTimershow.text = timerTime.toString() + "s"
        }

        viewBinding!!.tvTimerOff.setOnClickListener {

            viewBinding!!.secondbottom.visibility = View.VISIBLE
                viewBinding!!.lnAfterTimer.visibility = View.GONE

            timerTime = 0
            SessionManager.putStringPref(HelperKeys.TIMER_TIME, "", applicationContext)

            viewBinding!!.circularCountDownView.initTimer(timerTime)
            viewBinding!!.tvTimershow.text = "Off"


        }

        viewBinding!!.visualize.setOnClickListener {

            if (stepPassed == 4) {

                val newString: String = fileName.replace(".mp4", "")
                val url =
                    "https://m2c-media.s3.eu-central-1.amazonaws.com/" + newString + "_vis.mp4"
                Log.e("TAG", url)
                val intent = Intent(applicationContext, PlayVideoActivity::class.java)
                intent.putExtra("path", url)
                startActivity(intent)

            }
        }
        viewBinding!!.report.setOnClickListener {

            if (stepPassed == 4) {
                val newString: String = fileName.replace(".mp4", "")
                val url =
                    "https://m2c-media.s3.eu-central-1.amazonaws.com/" + newString + "_overlay.mp4"
                Log.e("TAG", url)

                val intent = Intent(applicationContext, PlayVideoActivity::class.java)
                intent.putExtra("path", url)
                startActivity(intent)
            }
        }
        textToSpeechSystem = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result: Int = textToSpeechSystem!!.setLanguage(Locale.ENGLISH)
                param = Bundle()
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                }
                audioSettingDefault()
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }

    private fun showHideTimer() {
        viewBinding!!.secondbottom.visibility = View.INVISIBLE
        viewBinding!!.ivAudio.visibility = View.INVISIBLE
//       viewBinding!!.lnAfterTimer.visibility = View.VISIBLE

//         val animation = TranslateAnimation(
//             500.0f,
//             0.0f,
//             0.0f,
//             0.0f
//         )
//
//
//         animation.duration = 1500 // animation duration, change accordingly
//
//         animation.repeatCount = 0 // animation repeat count
//
//         animation.fillAfter = false
//         viewBinding!!.lnAfterTimer.startAnimation(animation)
        viewBinding!!.lnAfterTimer.visibility = View.VISIBLE

//            overridePendingTransition(R.anim.`in`, R.anim.out)
//            viewBinding!!.lnAfterTimer.fadeVisibility(View.VISIBLE, 1000)
//            viewBinding!!.lnAfterTimer.visibility = View.VISIBLE
    }

    private fun startCamera() {
        viewBinding!!.viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) = Unit

            override fun surfaceCreated(holder: SurfaceHolder) {

                // Selects appropriate preview size and configures view finder
                val previewSize = getConstrainedPreviewOutputSize(
                    viewBinding!!.viewFinder.display, characteristics, SurfaceHolder::class.java
                )
                Log.d(
                    TAG,
                    "View finder size: ${viewBinding!!.viewFinder.width} x ${viewBinding!!.viewFinder.height}"
                )
                Log.d(TAG, "Selected preview size: $previewSize")
                viewBinding!!.viewFinder.setAspectRatio(previewSize.width, previewSize.height)

                // To ensure that size is set, initialize camera in the view's thread
                viewBinding!!.viewFinder.post { initializeCamera() }
            }
        })

        // Used to rotate the output media to match device orientation
        relativeOrientation = OrientationLiveData(applicationContext, characteristics).apply {
            observe(this@CameraSamsungActivity) { orientation ->
                Log.d(TAG, "Orientation changed: $orientation")
            }
        }
    }

    private fun audioSetting() {
        if (SessionManager.getStringPref(HelperKeys.MUTE_STATE, applicationContext)
                .equals("")
        ) {
            SessionManager.putStringPref(
                HelperKeys.MUTE_STATE,
                "mute",
                applicationContext
            )
            viewBinding!!.ivAudio.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_vector_mute,
                    null
                )
            )
            mute()

        } else {
            SessionManager.putStringPref(
                HelperKeys.MUTE_STATE,
                "",
                applicationContext
            )
            viewBinding!!.ivAudio.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_vector_unmute,
                    null
                )
            )
            unMute()
        }
    }

    private fun audioSettingDefault() {
        if (SessionManager.getStringPref(HelperKeys.MUTE_STATE, applicationContext)
                .equals("")
        ) {

            SessionManager.putStringPref(
                HelperKeys.MUTE_STATE,
                "",
                applicationContext
            )
            viewBinding!!.ivAudio.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_vector_unmute,
                    null
                )
            )
            unMute()


        } else {

            SessionManager.putStringPref(
                HelperKeys.MUTE_STATE,
                "mute",
                applicationContext
            )
            viewBinding!!.ivAudio.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_vector_mute,
                    null
                )
            )
            mute()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun uploadMedia(
        fileMimeType: String,
        fileName: String,
        path: String,
        index: Int,
        videoModel: Video
    ) {

        if (!progressDialog!!.isShowing) {
            progressDialog!!.show()
        }
        val sessionsModel = SessionsRequestModel(fileMimeType, fileName, "0")
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
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        viewBinding!!.tvRecordNoShow.visibility = View.VISIBLE
                        intent.putExtra("capture_mode", "yes")
                        intent.putExtra("flag_from", intent.getStringExtra("flag_from"))
                        recreate()
                        // show error message
//                        Toast.makeText(applicationContext,
//                            "bucket add error"+" --> "+it.message,
//                            Toast.LENGTH_SHORT).show()
//                        showDialog(this@CaptureVideoActivity)
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
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


        viewModel.fetchActualUploadResponse(url, requestBody)
        viewModel.response.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // bind data to the view


                addVideoToServer(publicUrl, fileName, index, videoModel)


            }
        })

    }

    private fun addVideoToServer(url: String, fileName: String, index: Int, videoModel: Video) {
        val code = "${getAppVersionName()}.${getVersionCode()}"

        val fileName1 = fileName.substringBefore(".mp4")
        val sessionsModel = SessionsUploadRequestModel(
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
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext), code
        )
        viewModel.fetchAddVideo(sessionsModel)
        viewModel.response_add.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
//                    progressDialog.dismiss()
                        // bind data to the view

//                        startActivity(Intent(applicationContext,SwingDetailsActivity::class.java))
//                        finish()


                        viewBinding!!.tvRecordNoShow.visibility = View.VISIBLE
                        intent.putExtra("capture_mode", "yes")
                        intent.putExtra("flag_from", intent.getStringExtra("flag_from"))
                        recreate()


                    }
                    is NetworkResult.Error -> {
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        viewBinding!!.tvRecordNoShow.visibility = View.VISIBLE
                        intent.putExtra("capture_mode", "yes")
                        intent.putExtra("flag_from", intent.getStringExtra("flag_from"))
                        recreate()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
                }
            }
        })

    }

    private fun enumerateHighSpeedCamerasBack(cameraManager: CameraManager): List<CameraInfo> {
        val availableCameras: MutableList<CameraInfo> = mutableListOf()
        var checked = false

        // Iterate over the list of cameras and add those with high speed video recording
        //  capability to our output. This function only returns those cameras that declare
        //  constrained high speed video recording, but some cameras may be capable of doing
        //  unconstrained video recording with high enough FPS for some use cases and they will
        //  not necessarily declare constrained high speed video capability.
        cameraManager.cameraIdList.forEach { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val orientation = 1

            // Query the available capabilities and output formats
            val capabilities = characteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
            )!!
            val cameraConfig = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )!!

            // Return cameras that support constrained high video capability
            if (capabilities.contains(
                    CameraCharacteristics
                        .REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO
                )
            ) {
                // For each camera, list its compatible sizes and FPS ranges
                cameraConfig.highSpeedVideoSizes.forEach { size ->
                    cameraConfig.getHighSpeedVideoFpsRangesFor(size).forEach { fpsRange ->
                        val fps = fpsRange.lower
                        val info = CameraInfo(
                            "$orientation ($id) $size $fps FPS", id, size, fps
                        )
                        if (id.toString() == "0" && fps <= 120) {
                            if (fps == 120) {
                                checked = true

                                fpsRanges = 120
//                                    resolutionSelected1 ="1280"
//                                    resolutionSelected2 ="720"
                                resolutionSelected1Back = size.width.toString()
                                resolutionSelected2Back = size.height.toString()
                                SessionManager.putStringPref(
                                    HelperKeys.FPS_RANGE,
                                    "120",
                                    applicationContext
                                )
                                SessionManager.putStringPref(
                                    HelperKeys.FPS_RESOLUTION,
                                    "1280x720",
                                    applicationContext
                                )


                            }
                            availableCameras.add(info)
                        }


                    }
                }


            }

        }
        if (!checked) {
            openPermissionSettings(this@CameraSamsungActivity)
        }
        if (availableCameras.size == 0) {
            openPermissionSettings(this@CameraSamsungActivity)
        }

        return availableCameras
    }

    private fun enumerateHighSpeedCamerasFront(cameraManager: CameraManager): List<CameraInfo> {
        val availableCameras: MutableList<CameraInfo> = mutableListOf()
        var checked = false
        // Iterate over the list of cameras and add those with high speed video recording
        //  capability to our output. This function only returns those cameras that declare
        //  constrained high speed video recording, but some cameras may be capable of doing
        //  unconstrained video recording with high enough FPS for some use cases and they will
        //  not necessarily declare constrained high speed video capability.
        cameraManager.cameraIdList.forEach { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val orientation = 1

            // Query the available capabilities and output formats
            val capabilities = characteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
            )!!
            val cameraConfig = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )!!


            // Return cameras that support constrained high video capability
            if (capabilities.contains(
                    CameraCharacteristics
                        .REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO
                )
            ) {
                // For each camera, list its compatible sizes and FPS ranges
                cameraConfig.highSpeedVideoSizes.forEach { size ->
                    cameraConfig.getHighSpeedVideoFpsRangesFor(size).forEach { fpsRange ->
                        val fps = fpsRange.lower
                        val info = CameraInfo(
                            "$orientation ($id) $size $fps FPS", id, size, fps
                        )
                        availableCameras.add(info)

                        if (id.toString() == "1" && (fps in 60..120)) {
                            if (size.width.toString() != "1920") {
                                checked = true
                                fpsRangesFront = fps
                                resolutionSelected1Front = size.width.toString()
                                resolutionSelected2Front = size.height.toString()

                                SessionManager.putStringPref(
                                    HelperKeys.FPS_RANGE_FRONT,
                                    fpsRangesFront.toString(),
                                    applicationContext
                                )
                                SessionManager.putStringPref(
                                    HelperKeys.FPS_RESOLUTION_FRONT,
                                    resolutionSelected1Front + "x" + resolutionSelected2Front,
                                    applicationContext
                                )
                            }
                        }

                    }
                }


            }

        }
        if (!checked) {
            openPermissionSettings(this@CameraSamsungActivity)
        }
        if (availableCameras.size == 0) {
            openPermissionSettings(this@CameraSamsungActivity)
        }

        return availableCameras
    }

    fun openPermissionSettings(activity: Activity) {
        var dialo = AlertDialog.Builder(activity)
            .setTitle("Fps Alert")
            .setMessage("Your camera is supporting 30fps . Do you want to continue video recording?")
            .setPositiveButton(
                "Yes "
            ) { dsad, i ->
                dsad.dismiss()
            }
            .setNegativeButton("No") { _, i ->
                finish()
            }
            .show()
    }

    @Nullable
    fun getOptimalFpsRange(): Range<Int>? {
        val MIN_FPS_RANGE = 0
        val MAX_FPS_RANGE = 30
        val rangeList =
            characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
        if (rangeList == null || rangeList.size == 0) {
            Log.e(TAG, "Failed to get FPS ranges.")
            return null
        }
        var result: Range<Int>? = null
        for (entry in rangeList) {
            var candidateLower = entry.lower
            var candidateUpper = entry.upper
            if (candidateUpper > 1000) {
                Log.w(TAG, "Device reports FPS ranges in a 1000 scale. Normalizing.")
                candidateLower /= 1000
                candidateUpper /= 1000
            }

            // Discard candidates with equal or out of range bounds
            val discard = (candidateLower == candidateUpper
                    || candidateLower < MIN_FPS_RANGE
                    || candidateUpper > MAX_FPS_RANGE)
            if (discard == false) {
                // Update if none resolved yet, or the candidate
                // has a >= upper bound and spread than the current result
                val update = (result == null
                        || candidateUpper >= result.upper && candidateUpper - candidateLower >= result.upper - result.lower)
                if (update == true) {
                    result = Range.create(candidateLower, candidateUpper)
                }
            }
        }
        return result
    }
}
