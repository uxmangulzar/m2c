package com.fenris.motion2coach.view.activity.report

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityReportTwoBinding
import com.fenris.motion2coach.databinding.CustomDialogRightBinding
import com.fenris.motion2coach.model.OverlayModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.ReportRequestModel
import com.fenris.motion2coach.network.requests.UrlRequestModel
import com.fenris.motion2coach.network.responses.HighlightsTwoResponse
import com.fenris.motion2coach.network.responses.PositionsResponse
import com.fenris.motion2coach.network.responses.ReportResponse
import com.fenris.motion2coach.util.*
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.camera2utils.EncoderWrapper.Companion.TAG
import com.fenris.motion2coach.util.playerUtil.Utils
import com.fenris.motion2coach.util.playerUtil.decoder.Frame
import com.fenris.motion2coach.util.playerUtil.decoder.FrameExtractor
import com.fenris.motion2coach.util.playerUtil.decoder.IVideoFrameExtractor
import com.fenris.motion2coach.view.activity.camera.CaptureVideoActivity
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.camera.CameraSamsungActivity
import com.fenris.motion2coach.view.adapter.OverlayAdapter
import com.fenris.motion2coach.viewmodel.ReportViewModel
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.callback.DownloadListener
import com.ixuea.android.downloader.domain.DownloadInfo
import com.ixuea.android.downloader.exception.DownloadException
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@AndroidEntryPoint
class ReportTwoActivity : AppCompatActivity(), IVideoFrameExtractor {
    private var positionsResponse: PositionsResponse? = null
    private var highlightsTwoResponse: HighlightsTwoResponse? = null
    private lateinit var reportResponse: ReportResponse
    private var totalFrames: Int = 0
    private var pausePlay: Boolean = true
    private lateinit var adapterHighlight: OverlayAdapter
    private lateinit var adapterDescription: OverlayAdapter
    private var data: ArrayList<OverlayModel>? = null
    private var data1: ArrayList<OverlayModel>? = null
    var viewBinding: ActivityReportTwoBinding? = null
    val viewModel: ReportViewModel by viewModels()
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var imagePaths = ArrayList<Bitmap>()
    private var progressDialog: ProgressDialog? = null
    var updateHandler: Handler = Handler()
    var lastFrame = 0
    var highlightSwipe = false
    private var highlightFileId: String? = ""
    private var fileNameOverlay: String = ""
    private var fileName: String? = ""
    private var videoStatus: String? = ""
    private var fileId: Int = 0
    private var overlayPath: String? = ""
    private var originalVideoPath: String? = ""
    var isOriginalSelected: Boolean = true
    private lateinit var localOverlayFile: File
    private lateinit var localOriginalFile: File

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityReportTwoBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        preventSleep()

        overlayPath = intent.getStringExtra("path")
        originalVideoPath = intent.getStringExtra("original_video_path")
        fileId = intent.getLongExtra("file_id", 0).toInt()
        videoStatus = intent.getStringExtra("status")
        fileName = intent.getStringExtra("file_name")
        fileNameOverlay = "$fileName-overlay"
        highlightFileId = intent.getStringExtra("highlight_fileid")
        localOriginalFile = File(filesDir, "$fileName.mp4")
        localOverlayFile = File(filesDir, "$fileNameOverlay.mp4")
        progressDialog = android.app.ProgressDialog(this@ReportTwoActivity)
        progressDialog!!.setMessage("Loading.Please wait..")
        progressDialog!!.setCancelable(false)
        viewBinding!!.linMain.setOnTouchListener(object : OnSwipeTouchListener(applicationContext) {
            // Implement any function you need:
            override fun onSwipeRight(): Boolean {
//                Toast.makeText(context, "RIGHT", Toast.LENGTH_SHORT).show()
                finish()
                return true
            }

            override fun onSwipeLeft(): Boolean {
//                Toast.makeText(context, "LEFT", Toast.LENGTH_SHORT).show()
//                finish()
                return true
            }
        })

        viewBinding!!.seekBarVerticalForce.setOnTouchListener(OnTouchListener { v, event -> true })
        viewBinding!!.ivCapture.setOnClickListener {
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

        /*
        * Swipe listener between
        * highlights and description */
        viewBinding!!.swipeLayout.setSwipeListener(object : SwipeRevealLayout.SwipeListener {
            override fun onClosed(view: SwipeRevealLayout?) {
                highlightSwipe = false
            }

            override fun onOpened(view: SwipeRevealLayout?) {
                highlightSwipe = true
            }


            override fun onSlide(view: SwipeRevealLayout?, slideOffset: Float) {
                if (slideOffset > 0.01) {
                    if (!pausePlay) {
                        viewBinding!!.ivPlay.performClick()
                        viewBinding!!.ivPlay.performClick()
                    }
//                    highlightSwipe = !highlightSwipe
                }
            }
        })
        /*If swing type = putting then we need to show only p1-p4 ,
             * Otherwise we will show p1-p10
             * */
        if (SessionManager.getStringPref(
                HelperKeys.SWING_TYPE_ID,
                applicationContext
            ) == "3"
        ) {
            viewBinding!!.linP5.visibility = View.GONE
            viewBinding!!.linP6.visibility = View.GONE
            viewBinding!!.linP7.visibility = View.GONE
            viewBinding!!.linP8.visibility = View.GONE
            viewBinding!!.linP9.visibility = View.GONE
            viewBinding!!.linP10.visibility = View.GONE
            viewBinding!!.linPositions.weightSum = 8f
            val param = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                2f
            )
            if (SessionManager.getStringPref(
                    HelperKeys.SWING_TYPE_ID,
                    applicationContext
                ) == "3"){
                viewBinding!!.tvP2.text="P4"
                viewBinding!!.tvP3.text="P7"
                viewBinding!!.tvP4.text="P10"
            }
            viewBinding!!.linP1.layoutParams = param
            viewBinding!!.linP2.layoutParams = param
            viewBinding!!.linP3.layoutParams = param
            viewBinding!!.linP4.layoutParams = param
        }


        getPositionsData()
        viewBinding!!.ivMinus.setOnClickListener {
            if (lastFrame >= 0) {
                lastFrame -= 1
                viewBinding!!.seekBar.progress = lastFrame
            }
        }
        viewBinding!!.ivPlus.setOnClickListener {
            if (lastFrame < totalFrames) {
                lastFrame += 1
                viewBinding!!.seekBar.progress = lastFrame
            } else {
                viewBinding!!.seekBar.progress = 0
                lastFrame = 0
                viewBinding!!.ivPlay.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_vector_play_without_back,
                        null
                    )
                )
                pausePlay = !pausePlay
            }
        }

        viewBinding!!.ivPlay.setOnClickListener {

            if (pausePlay) {
                viewBinding!!.ivPlay.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_group_pause_without,
                        null
                    )
                )
                updateHandler.postDelayed(timerRunnable, 100)
            } else {
                updateHandler.removeCallbacks(timerRunnable)

                viewBinding!!.ivPlay.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_vector_play_without_back,
                        null
                    )
                )
            }
            pausePlay = !pausePlay
        }
        viewBinding!!.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {


                // this is when actually seekbar has been seeked to a new position
                if (progress < imagePaths.size) {
                    viewBinding!!.imageView.setImageBitmap(imagePaths[progress])
                }

                if (fromUser) {
                    seekBar.progress = progress
                    lastFrame = progress
                }
                if (highlightSwipe) {
                    descriptionRecycler(progress)
                }

            }
        })
        seekToClick()
        viewBinding!!.radiogroup.setOnCheckedChangeListener { radioGroup, i ->
            if (positionsResponse != null) {
                imagePaths.clear()
                if (positionsResponse != null) {
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                    lastFrame = positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                }
                if (i == R.id.radio_stick) {
                    isOriginalSelected = false
                    if (!pausePlay) {
                        viewBinding!!.ivPlay.performClick()
                    }

                    if (localOverlayFile.exists()) {
                        viewBinding!!.imageView.visibility = View.VISIBLE
                        startFrameExtraction(localOverlayFile.absolutePath)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Video downloading please wait..",
                            Toast.LENGTH_SHORT
                        ).show()
                        getUrl(overlayPath!!, fileNameOverlay)
                    }
                } else if (i == R.id.radio_video) {
                    isOriginalSelected = true
                    if (!pausePlay) {
                        viewBinding!!.ivPlay.performClick()
                    }
                    if (localOriginalFile.exists()) {
                        viewBinding!!.imageView.visibility = View.VISIBLE
                        startFrameExtraction(localOriginalFile.absolutePath)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Video downloading please wait..",
                            Toast.LENGTH_SHORT
                        ).show()
                        getUrl(originalVideoPath!!, fileName!!)
                    }
                }
            }

        }
    }

    private fun getUrl(url: String, file_name12: String) {

        progressDialog!!.show()
        val urlRequestModel = UrlRequestModel(url)
        viewModel.fetchGetUrl(urlRequestModel)
        viewModel.response_get_url.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog!!.dismiss()

                        downloadUrl(it.data!!, file_name12)

                    }
                    is NetworkResult.Error -> {
                        progressDialog!!.dismiss()
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        // show error message
                        Toast.makeText(
                            applicationContext,
                            "Error" + " --> " + it.message,
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

    private fun downloadUrl(url: String, fileName1: String) {
        DownloadService.downloadManager = DownloadService.getDownloadManager(applicationContext)
        val downloadUrlFile: File = if (isOriginalSelected) {
            localOriginalFile

        } else {
            localOverlayFile

        }
        var downloadInfo = DownloadInfo.Builder().setUrl(url)
            .setPath(downloadUrlFile.absolutePath)
            .build()

        downloadInfo!!.downloadListener = object : DownloadListener {
            override fun onStart() {
                if (!progressDialog!!.isShowing) {

                    progressDialog!!.show()
                }

            }

            override fun onWaited() {
            }

            override fun onPaused() {
            }

            override fun onDownloading(progress: Long, size: Long) {
            }

            override fun onRemoved() {
                downloadInfo = null
            }

            override fun onDownloadSuccess() {
                if (progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
                viewBinding!!.imageView.visibility = View.VISIBLE
                startFrameExtraction(downloadUrlFile.absolutePath)

            }

            override fun onDownloadFailed(e: DownloadException) {
                DownloadService.downloadManager.download(downloadInfo)
            }
        }

        DownloadService.downloadManager.download(downloadInfo)

    }

    fun seekToClick() {
        viewBinding!!.linP1.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
            }
        }
        viewBinding!!.linP2.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P2!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P2!!.minus(positionsResponse!!.P1!!)

            }
        }
        viewBinding!!.linP3.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P3!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P3!!.minus(positionsResponse!!.P1!!)

            }
        }
        viewBinding!!.linP4.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!)

            }
        }
        viewBinding!!.linP5.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P5!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P5!!.minus(positionsResponse!!.P1!!)

            }
        }
        viewBinding!!.linP6.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P6!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P6!!.minus(positionsResponse!!.P1!!)

            }
        }
        viewBinding!!.linP7.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P7!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P7!!.minus(positionsResponse!!.P1!!)

            }
        }
        viewBinding!!.linP8.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P8!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P8!!.minus(positionsResponse!!.P1!!)

            }
        }
        viewBinding!!.linP9.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P9!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P9!!.minus(positionsResponse!!.P1!!)

            }
        }
        viewBinding!!.linP10.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (positionsResponse != null) {
                lastFrame = positionsResponse!!.P10!!.minus(positionsResponse!!.P1!!)
                viewBinding!!.seekBar.progress =
                    positionsResponse!!.P10!!.minus(positionsResponse!!.P1!!)

            }
        }
    }


    /*
    * Setting Description data
    * */
    private fun descriptionRecycler(progress: Int) {
        if (highlightsTwoResponse != null) {
            data1!!.clear()

            if (progress < highlightsTwoResponse!!.head_turn!!.size && highlightsTwoResponse!!.head_turn!!.size != 0) {
                data1!!.add(
                    OverlayModel(
                        "Head Turn",
                        roundTheNumberSecDegree(
                            highlightsTwoResponse!!.head_turn?.get(progress) ?: 0.0
                        ) + "°",
                        "",
                        "degree",
                        false
                    )
                )

            }
            if (progress < highlightsTwoResponse!!.head_tilt!!.size && highlightsTwoResponse!!.head_tilt?.size ?: 0.0 != 0) {
                data1!!.add(
                    OverlayModel(
                        "Head Tilt",
                        roundTheNumberSecDegree(highlightsTwoResponse!!.head_tilt!![progress]) + "°",
                        "",
                        "degree",
                        false
                    )
                )

            }
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
                if (progress < highlightsTwoResponse!!.knee_angle!!.size && highlightsTwoResponse!!.knee_angle?.size ?: 0.0 != 0) {
                    data1!!.add(
                        OverlayModel(
                            "L.Knee Angle  ",
                            roundTheNumberSecDegree(highlightsTwoResponse!!.knee_angle!![progress]) + "°",
                            "",
                            "degree",
                            false
                        )
                    )

                }
            } else {
                if (progress < highlightsTwoResponse!!.head_bend!!.size && highlightsTwoResponse!!.head_bend?.size ?: 0.0 != 0) {
                    data1!!.add(
                        OverlayModel(
                            "Head Bend  ",
                            roundTheNumberSecDegree(highlightsTwoResponse!!.head_bend!![progress]) + "°",
                            "",
                            "degree",
                            false
                        )
                    )

                }
            }

            if (progress < highlightsTwoResponse!!.ut_turns!!.size && highlightsTwoResponse!!.ut_turns!!.size != 0) {
                data1!!.add(
                    OverlayModel(
                        "Shoulder Turn",
                        roundTheNumberSecDegree(
                            highlightsTwoResponse!!.ut_turns?.get(progress) ?: 0.0
                        ) + "°",
                        "",
                        "degree",
                        false
                    )
                )

            }
            if (progress < highlightsTwoResponse!!.ut_tilts!!.size && highlightsTwoResponse!!.ut_tilts!!.size != 0) {
                data1!!.add(
                    OverlayModel(
                        "Shoulder Tilt",
                        roundTheNumberSecDegree(
                            highlightsTwoResponse!!.ut_tilts?.get(progress) ?: 0.0
                        ) + "°",
                        "",
                        "degree",
                        false
                    )
                )

            }
            if (progress < highlightsTwoResponse!!.hand_speed!!.size && highlightsTwoResponse!!.hand_speed!!.size != 0) {
//                data1!!.add(OverlayModel("Shoulder Bend",roundTheNumberSecDegree(highlightsTwoResponse!!.ut_bends!![progress])+"°","","degree"))
                data1!!.add(
                    OverlayModel(
                        "Hand Speed",
                        roundTheNumberSecDegree(
                            highlightsTwoResponse!!.hand_speed?.get(progress) ?: 0.0
                        ),
                        "",
                        "mph",
                        false
                    )
                )

            }
            if (progress < highlightsTwoResponse!!.pelvis_turns!!.size && highlightsTwoResponse!!.pelvis_turns!!.size != 0) {
                data1!!.add(
                    OverlayModel(
                        "Pelvis Turn",
                        roundTheNumberSecDegree(
                            highlightsTwoResponse!!.pelvis_turns?.get(progress) ?: 0.0
                        ) + "°",
                        "",
                        "degree",
                        false
                    )
                )

            }

            if (progress < highlightsTwoResponse!!.pelivs_tilts!!.size && highlightsTwoResponse!!.pelivs_tilts!!.size != 0) {
                data1!!.add(
                    OverlayModel(
                        "Pelvis Tilt",
                        roundTheNumberSecDegree(
                            highlightsTwoResponse!!.pelivs_tilts?.get(progress) ?: 0.0
                        ) + "°",
                        "",
                        "degree",
                        false
                    )
                )

            }
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
                if (progress < highlightsTwoResponse!!.elbow_angle!!.size && highlightsTwoResponse!!.elbow_angle!!.size != 0) {
                    data1!!.add(
                        OverlayModel(
                            "L.Elbow Angle",
                            roundTheNumberSecDegree(highlightsTwoResponse!!.elbow_angle!![progress]) + "°",
                            "",
                            "degree",
                            false
                        )
                    )
//                data1!!.add(OverlayModel("Vertical Force",roundTheNumberSecDegree(highlightsTwoResponse!!.Wt_percent!![progress]),"","wt%",true))
//                    viewBinding!!.seekBarVerticalForce.setProgress(highlightsTwoResponse!!.elbow_angle!![progress].toInt())
                    viewBinding!!.frameVerticalForce.visibility = View.GONE
                }
            } else {
                if (progress < highlightsTwoResponse!!.Wt_percent!!.size && highlightsTwoResponse!!.Wt_percent!!.size != 0) {
                    data1!!.add(
                        OverlayModel(
                            "Vertical Force", roundTheNumberSecDegree(
                                highlightsTwoResponse!!.Wt_percent?.get(progress) ?: 0.0
                            ), "", "weight%", false
                        )
                    )
//                data1!!.add(OverlayModel("Vertical Force",roundTheNumberSecDegree(highlightsTwoResponse!!.Wt_percent!![progress]),"","wt%",true))
                    viewBinding!!.seekBarVerticalForce.progress =
                        highlightsTwoResponse!!.Wt_percent?.get(progress)?.toInt() ?: 0

                }
            }
            if (progress < highlightsTwoResponse!!.head_turn!!.size
                && highlightsTwoResponse!!.head_turn!!.size != 0
            ) {

            } else {
                data1!!.add(OverlayModel("Head Turn", "", "", "degree", false))
            }





            if (progress < highlightsTwoResponse!!.head_turn!!.size
                && highlightsTwoResponse!!.head_tilt!!.size != 0
            ) {

            } else {
                data1!!.add(OverlayModel("Head Tilt", "", "", "degree", false))

            }
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
                if (progress < highlightsTwoResponse!!.elbow_angle!!.size && highlightsTwoResponse!!.elbow_angle!!.size != 0) {

                } else {
                    data1!!.add(OverlayModel("Knee Angle", "", "", "degree", false))
                }
            } else {
                if (progress < highlightsTwoResponse!!.Wt_percent!!.size && highlightsTwoResponse!!.Wt_percent!!.size != 0) {

                } else {
                    data1!!.add(OverlayModel("Head Bend  ", "", "", "degree", false))

                }
            }
            if (progress < highlightsTwoResponse!!.head_turn!!.size
                && highlightsTwoResponse!!.ut_turns!!.size != 0
            ) {

            } else {
                data1!!.add(OverlayModel("Shoulder Turn", "", "", "degree", false))


            }
            if (progress < highlightsTwoResponse!!.head_turn!!.size
                && highlightsTwoResponse!!.ut_tilts!!.size != 0
            ) {

            } else {
                data1!!.add(OverlayModel("Shoulder Tilt", "", "", "degree", false))
//                data1!!.add(OverlayModel("Shoulder Bend","","","degree"))
            }
            if (progress < highlightsTwoResponse!!.head_turn!!.size
                && highlightsTwoResponse!!.hand_speed!!.size != 0
            ) {

            } else {
                data1!!.add(OverlayModel("Hand Speed", "", "", "mph", false))
            }

            if (progress < highlightsTwoResponse!!.head_turn!!.size
                && highlightsTwoResponse!!.pelvis_turns!!.size != 0
            ) {

            } else {
                data1!!.add(OverlayModel("Pelvis Turn", "", "", "degree", false))

            }
            if (progress < highlightsTwoResponse!!.pelivs_tilts!!.size
                && highlightsTwoResponse!!.head_turn!!.size != 0
            ) {

            } else {
                data1!!.add(OverlayModel("Pelvis Tilt", "", "", "degree", false))

            }
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
                if (progress < highlightsTwoResponse!!.elbow_angle!!.size && highlightsTwoResponse!!.elbow_angle!!.size != 0) {

                } else {
                    data1!!.add(OverlayModel("Elbow Angle", "", "", "degree", false))
                }
            } else {
                if (progress < highlightsTwoResponse!!.Wt_percent!!.size && highlightsTwoResponse!!.Wt_percent!!.size != 0) {

                } else {
                    data1!!.add(OverlayModel("Vertical Force", "", "", "weight%", false))

                }
            }


            adapterDescription.notifyDataSetChanged()

        }
    }

    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            // This will trigger itself every one second.
            if (lastFrame < totalFrames) {
                viewBinding!!.seekBar.progress = lastFrame
                lastFrame += 1
                updateHandler.postDelayed(this, 100)
            } else {
                viewBinding!!.seekBar.progress = 0
                lastFrame = 0
                viewBinding!!.ivPlay.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_vector_play_without_back,
                        null
                    )
                )
                pausePlay = !pausePlay
            }


        }
    }

    private fun roundTheNumberSecDegree(numInDouble: Double): String {
        DecimalFormat("#.##")
        val split: String = if (numInDouble.toString().length >= 4) {
            numInDouble.toString().substring(0, 4)
        } else {
            numInDouble.toString().substring(0, 3)
        }

        var newSplit = ""
        newSplit = if (split.endsWith(".")) {
            if (numInDouble.toString().length >= 4) {
                numInDouble.toString().substring(0, 3)

            } else {
                numInDouble.toString().substring(0, 2)

            }

        } else {
            split

        }
        return newSplit

    }

    private fun roundTheNumber(numInDouble: Double): String {
        val f = DecimalFormat("#.#")
        return f.format(numInDouble)

    }

    private fun getJsonData() {
        if (!progressDialog!!.isShowing) {

            progressDialog!!.show()
        }
        val uId: Int =
            if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE, applicationContext)) {
                SessionManager.getStringPref(HelperKeys.STRIKER_ID, applicationContext).toInt()
            } else {
                SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt()
            }
        val reportRequestModel =
            ReportRequestModel(uId.toString(), intent.getStringExtra("highlight_fileid")!!, -1)
        viewModel.fetchReportResponse(
            Constants.BASE_URL_BIO + Constants.URL_DOWNLOAD_JSON,
            reportRequestModel
        )
        viewModel.response_report_json.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // do something
                when (it) {
                    is NetworkResult.Success -> {
                        if (progressDialog!!.isShowing) {

                            progressDialog!!.dismiss()
                        }
//                        progressDialog.dismiss()
                        // bind data to the view
                        data = ArrayList<OverlayModel>()
                        viewBinding!!.rvHighlight.layoutManager = GridLayoutManager(this, 3)
                        adapterHighlight = OverlayAdapter(data!!, this)
                        viewBinding!!.rvHighlight.adapter = adapterHighlight
                        reportResponse = it.data!![0]

                        getHighlightsTwoData(reportResponse)


                    }
                    is NetworkResult.Error -> {
                        if (progressDialog!!.isShowing) {

                            progressDialog!!.dismiss()
                        }
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        Toast.makeText(
                            applicationContext,
                            it.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

//                        progressDialog.dismiss()
                        // show error message
//                        Toast.makeText(applicationContext,
//                            "error"+" --> "+it.message,
//                            Toast.LENGTH_SHORT).show()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
                }
            }
        })

    }

    /*
    * Setting highlights
    * */
    private fun highlightsRecycler(it: ReportResponse) {
        this.data!!.clear()
        try {

            this.data!!.add(
                OverlayModel(
                    "Start to Impact",
                    roundTheNumberSecDegree(it.Start_to_impact_time?.toDouble() ?: 0.0) + "",
                    "",
                    "sec",
                    false
                )
            )
            this.data!!.add(
                OverlayModel(
                    "Backswing",
                    roundTheNumberSecDegree(it.Back_swing_time?.toDouble() ?: 0.0),
                    "",
                    "sec",
                    false
                )
            )
            this.data!!.add(
                OverlayModel(
                    "Downswing ",
                    roundTheNumberSecDegree(it.downswing_time?.toDouble() ?: 0.0),
                    "",
                    "sec",
                    false
                )
            )
            this.data!!.add(
                OverlayModel(
                    "Tempo",
                    roundTheNumber(it.tempo?.toDouble() ?: 0.0) + ":1",
                    "",
                    "",
                    false
                )
            )
            this.data!!.add(
                OverlayModel(
                    "Handpath BS",
                    roundTheNumber(it.Hand_Path_Back_Swing?.toDouble() ?: 0.0),
                    "",
                    "cm",
                    false
                )
            )
            this.data!!.add(
                OverlayModel(
                    "Handpath DS",
                    roundTheNumber(it.Hand_Path_Down_Swing?.toDouble() ?: 0.0),
                    "",
                    "cm",
                    false
                )
            )
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
                this.data!!.add(
                    OverlayModel(
                        "Max. Hand Speed",
                        roundTheNumberSecDegree(it.Max_Hand_Speed?.toDouble() ?: 0.0),
                        "",
                        "mph",
                        false
                    )
                )
            }else{
                this.data!!.add(
                    OverlayModel(
                        "Max. Hand Speed(p4-p7)",
                        roundTheNumberSecDegree(it.Max_Hand_Speed?.toDouble() ?: 0.0),
                        "",
                        "mph",
                        false
                    )
                )
            }
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
                this.data!!.add(
                    OverlayModel(
                        "Shoulder Turn(P2)",
                        roundTheNumberSecDegree(it.Max_UT_turn?.toDouble() ?: 0.0) + "°",
                        "",
                        "degree",
                        false
                    )
                )
            }else{

                this.data!!.add(
                    OverlayModel(
                        "Shoulder Turn(P4)",
                        roundTheNumberSecDegree(it.Max_UT_turn?.toDouble() ?: 0.0) + "°",
                        "",
                        "degree",
                        false
                    )
                )
            }
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
                this.data!!.add(
                    OverlayModel(
                        "Pelvis Turn(P2)",
                        roundTheNumberSecDegree(it.Max_Pelvis_Turn?.toDouble() ?: 0.0) + "°",
                        "",
                        "degree",
                        false
                    )
                )
            }else{
                this.data!!.add(
                    OverlayModel(
                        "Pelvis Turn(P4)",
                        roundTheNumberSecDegree(it.Max_Pelvis_Turn?.toDouble() ?: 0.0) + "°",
                        "",
                        "degree",
                        false
                    )
                )
            }


        } catch (e: Exception) {
//            Toast.makeText(applicationContext,
//                "Some value error.. $e",
//                Toast.LENGTH_SHORT).show()
            Log.e("isuee", "$e")

        }
        adapterHighlight.notifyDataSetChanged()
    }


    override fun onCurrentFrameExtracted(currentFrame: Frame) {

        val startSavingTime = System.currentTimeMillis()
        // 1. Convert frame byte buffer to bitmap
        val imageBitmap = Utils.fromBufferToBitmap(
            currentFrame.byteBuffer,
            currentFrame.width,
            currentFrame.height
        )

        // 2. Get the frame file in app external file directory
        val allFrameFileFolder = File(this.getExternalFilesDir(null), UUID.randomUUID().toString())
        if (!allFrameFileFolder.isDirectory) {
            allFrameFileFolder.mkdirs()
        }
        val frameFile = File(
            allFrameFileFolder,
            "frame_num_${currentFrame.timestamp.toString().padStart(10, '0')}.jpeg"
        )


        // 3. Save current frame to storage
        imageBitmap?.let {
            if (currentFrame.position >= positionsResponse!!.P1!! && currentFrame.position <= positionsResponse!!.P10!!) {
//                swing types 1,2,3,4
                if (SessionManager.getStringPref(
                        HelperKeys.SWING_TYPE_ID,
                        applicationContext
                    ) == "3" || SessionManager.getStringPref(
                        HelperKeys.SWING_TYPE_ID,
                        applicationContext
                    ) == "2"
                ) {
                    if (currentFrame.position >= positionsResponse!!.P1!! && currentFrame.position <= positionsResponse!!.P4!!) {
                        val selectedIndex = viewBinding!!.radiogroup?.indexOfChild(
                            viewBinding!!.radiogroup.findViewById(
                                viewBinding!!.radiogroup.checkedRadioButtonId
                            )
                        )
                        if (selectedIndex == 0) {
                            val rot = Helper.rotate(it, 180f)
                            imagePaths.add(
                                Helper.flip(
                                    rot!!,
                                    horizontal = true,
                                    vertical = false
                                )!!
                            )
                        } else {
                            imagePaths.add(it)
                        }
                    }

                } else {
                    val selectedIndex = viewBinding!!.radiogroup?.indexOfChild(
                        viewBinding!!.radiogroup?.findViewById(
                            viewBinding!!.radiogroup.checkedRadioButtonId
                        )
                    )
                    if (selectedIndex == 0) {
                        val rot = Helper.rotate(it, 180f)
                        imagePaths.add(Helper.flip(rot!!, horizontal = true, vertical = false)!!)
                    } else {
                        imagePaths.add(it)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onAllFrameExtracted(processedFrameCount: Int, processedTimeMs: Long) {
        Log.d(TAG, "Save: $processedFrameCount frames in: $processedTimeMs ms.")
        this.runOnUiThread {
            progressDialog!!.dismiss()
            if (SessionManager.getStringPref(
                    HelperKeys.SWING_TYPE_ID,
                    applicationContext
                ) == "3" || SessionManager.getStringPref(
                    HelperKeys.SWING_TYPE_ID,
                    applicationContext
                ) == "2"
            ) {
                totalFrames = positionsResponse!!.P4?.minus(positionsResponse!!.P1!!)!!

            } else {
                totalFrames = positionsResponse!!.P10?.minus(positionsResponse!!.P1!!)!!

            }
            viewBinding!!.seekBar.max = totalFrames
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                viewBinding!!.seekBar.min = 0
            }
            viewBinding!!.imageView.setImageBitmap(imagePaths[0])
            getJsonData()
//            for (i in imagePaths.indices){
//                val indx = i + positionsResponse!!.P1!!
//                if (positionsResponse!!.P1==indx){
//                    viewBinding!!.ivPOne.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P2==indx){
//                    viewBinding!!.ivPTwo.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P3==indx){
//                    viewBinding!!.ivPThree.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P4==indx){
//                    viewBinding!!.ivPFour.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P5==indx){
//                    viewBinding!!.ivPFive.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P6==indx){
//                    viewBinding!!.ivPSix.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P7==indx){
//                    viewBinding!!.ivPSeven.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P8==indx){
//                    viewBinding!!.ivPEight.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P9==indx){
//                    viewBinding!!.ivPNine.setImageBitmap(imagePaths[i])
//                }
//                if (positionsResponse!!.P10==indx){
//                    viewBinding!!.ivPTen.setImageBitmap(imagePaths[i])
//                }
//            }

//            infoTextView.text = "Extract $processedFrameCount frames took $processedTimeMs ms| Saving took: $totalSavingTimeMS ms"
        }
    }

    private fun startFrameExtraction(videoInputFilePath: String) {
        if (!(this@ReportTwoActivity as Activity).isFinishing) {
            //show dialog
            if (!progressDialog!!.isShowing) {
                progressDialog!!.show()
            }

        }

        val frameExtractor = FrameExtractor(this)
        executorService.execute {
            try {

                frameExtractor.extractFrames(videoInputFilePath)
            } catch (exception: Exception) {
                exception.printStackTrace()
                this.runOnUiThread {
                    Toast.makeText(this, "Failed to extract frames", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    open class OnSwipeTouchListener(val context: Context?) : OnTouchListener {
        companion object {
            private const val SwipeThreshold = 1
            private const val SwipeVelocityThreshold = 1
        }

        private val gestureDetector = GestureDetector(context, GestureListener())

        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        open fun onSwipeRight(): Boolean {
            return false
        }

        open fun onSwipeLeft(): Boolean {
            return false
        }

        open fun onSwipeUp(): Boolean {
            return false
        }

        open fun onSwipeDown(): Boolean {
            return false
        }

        private inner class GestureListener : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float,
            ): Boolean {
                try {
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x

                    if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {
                        if (kotlin.math.abs(diffX) > SwipeThreshold && kotlin.math.abs(velocityX!!) > SwipeVelocityThreshold) {
                            return when {
                                diffX > 0 -> onSwipeRight()
                                else -> onSwipeLeft()
                            }
                        }
                    } else if (kotlin.math.abs(diffY) > SwipeThreshold && kotlin.math.abs(velocityY!!) > SwipeVelocityThreshold) {
                        return when {
                            diffY > 0 -> onSwipeDown()
                            else -> onSwipeUp()
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return false
            }
        }
    }


    private fun getPositionsData() {
//        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
//        progressDialog.show()
        if (!progressDialog!!.isShowing) {

            progressDialog!!.show()
        }
        var uId = 0
        uId = if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE, applicationContext)) {
            SessionManager.getStringPref(HelperKeys.STRIKER_ID, applicationContext).toInt()
        } else {
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt()
        }
        val reportRequestModel = ReportRequestModel(
            uId.toString(),
            highlightFileId!!,
            -1
        )
//      val reportRequestModel = ReportRequestModel("m2c1662965785664")
        viewModel.fetchPositionsResponse(
            Constants.BASE_URL_BIO + Constants.GET_POSITIONS,
            reportRequestModel
        )
        viewModel.response_positions.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // do something
                when (it) {
                    is NetworkResult.Success -> {
                        if (progressDialog!!.isShowing) {

                            progressDialog!!.dismiss()
                        }
//                        progressDialog.dismiss()
                        // bind data to the view
                        positionsResponse = it.data
                        val targetFile = File(filesDir, "$fileName.mp4")
                        if (targetFile.exists()) {
                            val targetFile = File(filesDir, "$fileName.mp4")
                            viewBinding!!.imageView.visibility = View.VISIBLE
                            startFrameExtraction(targetFile.absolutePath)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Video downloading please wait..",
                                Toast.LENGTH_SHORT
                            ).show()
                            getUrl(
                                originalVideoPath!!,
                                "$fileName"
                            )
                        }

                    }
                    is NetworkResult.Error -> {
                        if (progressDialog!!.isShowing) {

                            progressDialog!!.dismiss()
                        }
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        Toast.makeText(
                            applicationContext,
                            it.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

//                        progressDialog.dismiss()
                        // show error message
//                        Toast.makeText(applicationContext,
//                            "error"+" --> "+it.message,
//                            Toast.LENGTH_SHORT).show()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
                }
            }
        })

    }

    private fun getHighlightsTwoData(reportResponse: ReportResponse) {
//        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
//        progressDialog.show()
        if (!progressDialog!!.isShowing) {
            progressDialog!!.show()
        }
        var uId = 0
        uId = if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE, applicationContext)) {
            SessionManager.getStringPref(HelperKeys.STRIKER_ID, applicationContext).toInt()
        } else {
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt()
        }
        val reportRequestModel =
            ReportRequestModel(uId.toString(), intent.getStringExtra("highlight_fileid")!!, -1)
//      val reportRequestModel = ReportRequestModel("m2c1662965785664")
        viewModel.fetchHighlightsTwo(
            Constants.BASE_URL_BIO + Constants.GET_HIGHLIGHTS_TWO,
            reportRequestModel
        )
        viewModel.response_highlights_two.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // do something
                when (it) {
                    is NetworkResult.Success -> {

//                        progressDialog.dismiss()
                        // bind data to the view
                        highlightsTwoResponse = it.data
                        data1 = ArrayList<OverlayModel>()
                        viewBinding!!.rvDescription.layoutManager = GridLayoutManager(this, 3)
                        adapterDescription = OverlayAdapter(data1!!, this)
                        viewBinding!!.rvDescription.adapter = adapterDescription

//                        val startPoint= positionsResponse!!.P1!!
                        val startPoint = 0

                        if (highlightsTwoResponse!!.Wt_percent != null) {
                            try {
                                val min: Double? = highlightsTwoResponse!!.Wt_percent?.let { it1 ->
                                    Collections.min(
                                        it1
                                    )
                                }
                                val max: Double? = highlightsTwoResponse!!.Wt_percent?.let { it1 ->
                                    Collections.max(
                                        it1
                                    )
                                }
                                viewBinding!!.seekBarVerticalForce.max = 300
                                viewBinding!!.seekBarVerticalForce.min = 0
//                                viewBinding!!.tvVectorMax.text="2000"
//                                viewBinding!!.tvVectorMin.text=min!!.toInt().toString()
                            } catch (ex: Exception) {
                                viewBinding!!.seekBarVerticalForce.max = 300
                                viewBinding!!.seekBarVerticalForce.min = 0
//                                viewBinding!!.tvVectorMax.text="2000"
//                                viewBinding!!.tvVectorMin.text="0"
                            }

                        }
                        viewBinding!!.linPositions.visibility = View.VISIBLE
                        viewBinding!!.seekLayout.visibility = View.VISIBLE
                        descriptionRecycler(startPoint)


                        highlightsRecycler(reportResponse)
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
                        if (SessionManager.getStringPref(
                                HelperKeys.GIF_OVERLAY_SCREEN,
                                applicationContext
                            ) == ""
                        ) {
                            SessionManager.putStringPref(
                                HelperKeys.GIF_OVERLAY_SCREEN,
                                "done",
                                applicationContext
                            )
                            showGifDialog(this@ReportTwoActivity)
                        }

                    }
                    is NetworkResult.Error -> {
                        if (progressDialog!!.isShowing) {

                            progressDialog!!.dismiss()
                        }
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
//                        progressDialog.dismiss()
                        // show error message
                        Toast.makeText(
                            applicationContext,
                            "error" + " --> " + it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
                }
            }
        })

    }

    private fun showGifDialog(activity: Activity?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val viewBinding = CustomDialogRightBinding.inflate(activity.layoutInflater)
        dialog.setContentView(viewBinding.root)
        val window: Window = dialog.window!!
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val wlp = window.attributes

        wlp.gravity = Gravity.END or Gravity.BOTTOM
        wlp.x = 50  //x position
        wlp.y = 150
        window.attributes = wlp
        window.attributes = wlp



        viewBinding.ivGif.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        Handler(Looper.myLooper()!!).postDelayed({

            dialog.dismiss()

        }, 3000)
    }

    fun onBackPressed(view: View) {
        finish()
    }

}