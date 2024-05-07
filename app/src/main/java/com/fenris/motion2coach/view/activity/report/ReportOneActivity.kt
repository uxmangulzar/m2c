package com.fenris.motion2coach.view.activity.report

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.*
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.network.responses.GetAnnotationResponseModel
import com.fenris.motion2coach.network.responses.PositionsResponse
import com.fenris.motion2coach.util.*
import com.fenris.motion2coach.util.Helper.hideKeyboard
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.camera2utils.EncoderWrapper
import com.fenris.motion2coach.util.playerUtil.Utils
import com.fenris.motion2coach.util.playerUtil.decoder.Frame
import com.fenris.motion2coach.util.playerUtil.decoder.FrameExtractor
import com.fenris.motion2coach.util.playerUtil.decoder.IVideoFrameExtractor
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.ReportViewModel
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.callback.DownloadListener
import com.ixuea.android.downloader.domain.DownloadInfo
import com.ixuea.android.downloader.exception.DownloadException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@AndroidEntryPoint
class ReportOneActivity : AppCompatActivity(), IVideoFrameExtractor {
    private var drawingIndex: Int=0
    private lateinit var addAnnotationRequestModel: AddAnnotationRequestModel

    private lateinit var shapeTypeSelected: CanvasView.Drawer
    private lateinit var canvas: CanvasView
    private lateinit var handleSave: Deferred<Boolean>
    private lateinit var localOverlayFile: File
    private lateinit var localOriginalFile: File
    private var fileNameOverlay: String = ""
    private var highlightFileId: String? = ""
    private var fileName: String? = ""
    private var videoStatus: String? = ""
    private var fileId: Int = 0
    private var overlayPath: String? = ""
    private var originalVideoPath: String? = ""
    var viewBinding: ActivityReportOneBinding? = null
    private var totalFrames: Int = 0
    private var pausePlay: Boolean = true
    val viewModel: ReportViewModel by viewModels()
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var imagePaths = ArrayList<Bitmap>()
    private var allDrawingList = ArrayList<SessionsRequestModel>()
    private var progressDialog: ProgressDialog? = null
    var updateHandler: Handler = Handler()
    var lastFrame = 1
    private var positionsResponse: PositionsResponse? = null
    private var positionsResponseOriginal: PositionsResponse? = null
    var flagEditPosition: Int = 0
    var isDrawingMode: Boolean = false
    var selectedPosition: Int = 11
    var greenItemsList: ArrayList<Int> = ArrayList()
    val hashMapDrawing: HashMap<Int, Bitmap> = HashMap<Int, Bitmap>()
    val hashMapDrawingServer: HashMap<Int, Bitmap> = HashMap<Int, Bitmap>()
    var movedNext: Boolean = false
    var isOriginalSelected: Boolean = true
    var bmp: Bitmap? = null
    var alteredBitmap: Bitmap? = null
    var processFinished = false
    var isDrawingShowing = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityReportOneBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        preventSleep()
        canvas = viewBinding!!.canvasTransparent as CanvasView
        canvas.baseColor = Color.TRANSPARENT
        shapeTypeSelected = CanvasView.Drawer.LINE
        viewBinding!!.canvasTransparent.visibility = View.GONE
        overlayPath = intent.getStringExtra("path")
        originalVideoPath = intent.getStringExtra("original_video_path")
        fileId = intent.getLongExtra("file_id", 0).toInt()
        videoStatus = intent.getStringExtra("status")
        fileName = intent.getStringExtra("file_name")
        fileNameOverlay = "$fileName-overlay"
        highlightFileId = intent.getStringExtra("highlight_fileid")
        localOriginalFile = File(filesDir, "$fileName.mp4")
        localOverlayFile = File(filesDir, "$fileNameOverlay.mp4")
        startTimer()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Loading.Please wait..")
        progressDialog!!.setCancelable(false)
        viewBinding!!.linMain.setOnTouchListener(object :
            Helper.OnSwipeTouchListener(applicationContext) {
            // Implement any function you need:
            override fun onSwipeRight(): Boolean {
                return true
            }

            override fun onSwipeLeft(): Boolean {
                if (positionsResponse != null) {
                    movedNext = true
                    val intent1 = Intent(applicationContext, ReportTwoActivity::class.java)
                    intent1.putExtra("file_id", fileId)
                    intent1.putExtra("path", overlayPath)
                    if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE, applicationContext)) {
                        intent1.putExtra(
                            "u_id",
                            SessionManager.getStringPref(HelperKeys.STRIKER_ID, applicationContext)
                                .toInt()
                        )
                    }
                    intent1.putExtra("status", videoStatus)
                    intent1.putExtra("highlight_fileid", highlightFileId)
                    intent1.putExtra("file_name", fileName)
                    startActivity(intent1)
                } else {
                    Toast.makeText(context, "No Highlights data found..", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })
        /*If swing type = putting then we need to show only p1-p4 ,
        * Otherwise we will show p1-p10
        * */
        if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
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
        if (positionsResponse == null) {
            getPositionsData()
        }
        viewBinding!!.ivDrawReset.setOnClickListener {
            canvas.undo()
        }
        viewBinding!!.ivViewDrawing.setOnClickListener {
            isDrawingShowing=!isDrawingShowing
            if (isDrawingShowing){
                viewBinding!!.imageViewTransparent.visibility=View.VISIBLE
            }else{
                viewBinding!!.imageViewTransparent.visibility=View.GONE

            }
        }
        viewBinding!!.editTextTextShow.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                canvas!!.setUpText(viewBinding!!.editTextTextShow.text.toString())
                viewBinding!!.editTextTextShow.setText("")
                hideKeyboard(this)
                return@setOnEditorActionListener true
            }
            false
        }
        viewBinding!!.ivDrawRedo.setOnClickListener {
            canvas.redo()
        }
        viewBinding!!.ivDrawDelete.setOnClickListener {
            canvas.undoSpecial()
        }
        viewBinding!!.ivDrawSelect.setOnClickListener {
            viewBinding!!.editTextTextShow.visibility=View.GONE
            canvas.mode = CanvasView.Mode.MOVE_SELECT

            canvas.drawer = CanvasView.Drawer.MOVE_SELECT
//            canvas.startTranslation(3000)
        }
        viewBinding!!.ivDrawCircle.setOnClickListener {
            viewBinding!!.editTextTextShow.visibility=View.GONE

            canvas.mode = CanvasView.Mode.DRAW
            canvas.drawer = CanvasView.Drawer.CIRCLE
            this.canvas.paintStyle = Paint.Style.STROKE
        }
        viewBinding!!.ivDrawText.setOnClickListener {
            canvas.mode = CanvasView.Mode.DRAW
            canvas.drawer = CanvasView.Drawer.LINE
            this.canvas.paintStyle = Paint.Style.STROKE
            canvas.mode = CanvasView.Mode.TEXT
            canvas!!.setEditText(viewBinding!!.editTextTextShow)
        }

        viewBinding!!.ivDrawLine.setOnClickListener {
            viewBinding!!.editTextTextShow.visibility=View.GONE
            canvas.mode = CanvasView.Mode.DRAW
            canvas.drawer = CanvasView.Drawer.LINE
            this.canvas.paintStyle = Paint.Style.STROKE
        }
        viewBinding!!.ivDrawZigzag.setOnClickListener {
            viewBinding!!.editTextTextShow.visibility=View.GONE

            canvas.mode = CanvasView.Mode.DRAW

            canvas.drawer = CanvasView.Drawer.PEN
            this.canvas.paintStyle = Paint.Style.STROKE
        }
        viewBinding!!.ivDrawAngle.setOnClickListener {
            viewBinding!!.editTextTextShow.visibility=View.GONE

            canvas.mode = CanvasView.Mode.DRAW

            canvas.drawer = CanvasView.Drawer.ANGLE_DRAW
            this.canvas.paintStyle = Paint.Style.STROKE
        }
        viewBinding!!.tvSave.setOnClickListener {
            viewBinding!!.editTextTextShow.visibility=View.GONE

            viewBinding!!.seekLayout.visibility = View.VISIBLE
            flagEditPosition = 0
            selectedPosition = 11
            greenItemsList.clear()
            viewBinding!!.canvasTransparent.visibility = View.GONE
            setBackgroundPointsUndo()
            if (!isDrawingMode) {
                updatePositionsOnServer()
            } else {
                viewBinding!!.ivDrawReset.visibility = View.GONE
                viewBinding!!.linDrawEdit.visibility = View.GONE
                positionsResponse = positionsResponseOriginal
                isDrawingMode = false
                viewBinding!!.seekBar.progress = 0
                viewBinding!!.imageView.setImageBitmap(imagePaths[0])

                if (hashMapDrawing.size != 0) {
//                    canvas = viewBinding!!.canvasTransparent as CanvasView
//                    canvas.baseColor = Color.TRANSPARENT
                    while (canvas.canUndo()) {
                        canvas.undo()
                    }
                    var uId = 0

                    uId = if (SessionManager.getBoolPref(
                            HelperKeys.STRIKER_MODE,
                            applicationContext
                        )
                    ) {
                        SessionManager.getStringPref(HelperKeys.STRIKER_ID, applicationContext)
                            .toInt()
                    } else {
                        SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt()
                    }
                    addAnnotationRequestModel = AddAnnotationRequestModel(
                        SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext)
                            .toInt(),
                        uId,
                        videoId = fileId
                    )
                    drawingIndex = 0
                    waitUntilTaskComplete()
                }
            }
        }

        viewBinding!!.tvCancel.setOnClickListener {
            viewBinding!!.editTextTextShow.visibility=View.GONE

            viewBinding!!.canvasTransparent.visibility = View.GONE
            viewBinding!!.ivDrawReset.visibility = View.GONE
            viewBinding!!.seekLayout.visibility = View.VISIBLE
            viewBinding!!.imageViewTransparent.visibility = View.VISIBLE
            viewBinding!!.linDrawEdit.visibility = View.GONE
            positionsResponse = positionsResponseOriginal
            flagEditPosition = 0
            selectedPosition = 11
            isDrawingMode = false
            greenItemsList.clear()
            setBackgroundPointsUndo()
            viewBinding!!.seekBar.progress = 0
            viewBinding!!.imageView.setImageBitmap(imagePaths[0]!!)
            hashMapDrawing.clear()
            while (canvas.canUndo()) {
                canvas.undo()
            }
        }

        seekToClick()
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
                // this is when actually seekbar has been sought to a new position

                if (progress < imagePaths.size) {
                    if (
                        lastFrame == positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!) ||
                        lastFrame == positionsResponse!!.P2!!.minus(positionsResponse!!.P1!!) ||
                        lastFrame == positionsResponse!!.P3!!.minus(positionsResponse!!.P1!!) ||
                        lastFrame == positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!) ||
                        lastFrame == positionsResponse!!.P5!!.minus(positionsResponse!!.P1!!) ||
                        lastFrame == positionsResponse!!.P6!!.minus(positionsResponse!!.P1!!) ||
                        lastFrame == positionsResponse!!.P7!!.minus(positionsResponse!!.P1!!) ||
                        lastFrame == positionsResponse!!.P8!!.minus(positionsResponse!!.P1!!) ||
                        lastFrame == positionsResponse!!.P10!!.minus(positionsResponse!!.P1!!)
                    ) {

                        bmp = imagePaths[progress]

//                        alteredBitmap = Bitmap.createBitmap(
//                            bmp!!.width,
//                            bmp!!.height, bmp!!.config
//                        )
                        viewBinding!!.imageView.setImageBitmap(bmp!!)
                        if (isDrawingMode) {
                            bmp = imagePaths[progress]
                            while (canvas.canUndo()) {
                                canvas.undo()
                            }
                            val transBmp = Bitmap.createBitmap(
                                bmp!!.width,
                                bmp!!.height, Bitmap.Config.ARGB_8888
                            )
                            canvas.drawBitmap(transBmp)
                            if ((lastFrame == positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)) && hashMapDrawingServer[1] != null) {

                                canvas.drawBitmap(hashMapDrawingServer[1]!!)


                            } else if ((lastFrame == positionsResponse!!.P2!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[2] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[2]!!)


                            } else if ((lastFrame == positionsResponse!!.P3!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[3] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[3]!!)

                            } else if ((lastFrame == positionsResponse!!.P4!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[4] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[4]!!)

                            } else if ((lastFrame == positionsResponse!!.P5!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[5] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[5]!!)

                            } else if ((lastFrame == positionsResponse!!.P6!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[6] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[6]!!)

                            } else if ((lastFrame == positionsResponse!!.P7!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[7] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[7]!!)

                            } else if ((lastFrame == positionsResponse!!.P8!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[8] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[8]!!)

                            } else if ((lastFrame == positionsResponse!!.P9!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[9] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[9]!!)

                            } else if ((lastFrame == positionsResponse!!.P10!!.minus(
                                    positionsResponse!!.P1!!
                                )) && hashMapDrawingServer[10] != null
                            ) {
                                canvas.drawBitmap(hashMapDrawingServer[10]!!)

                            }

                        } else {
                            if (isDrawingShowing){
                                if ((lastFrame == positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)) && hashMapDrawingServer[1] != null) {

                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[1]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P2!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[2] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[2]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P3!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[3] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[3]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P4!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[4] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[4]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P5!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[5] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[5]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P6!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[6] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[6]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P7!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[7] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[7]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P8!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[8] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[8]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P9!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[9] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[9]!!
                                    )

                                } else if ((lastFrame == positionsResponse!!.P10!!.minus(
                                        positionsResponse!!.P1!!
                                    )) && hashMapDrawingServer[10] != null
                                ) {
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        hashMapDrawingServer[10]!!
                                    )

                                } else {

                                    viewBinding!!.imageView.setImageBitmap(
                                        imagePaths[progress]
                                    )
                                    viewBinding!!.imageViewTransparent.setImageBitmap(
                                        imagePaths[progress]
                                    )
                                }
                            }
                            else {

                                viewBinding!!.imageView.setImageBitmap(
                                    imagePaths[progress]
                                )
                                viewBinding!!.imageViewTransparent.setImageBitmap(
                                    imagePaths[progress]
                                )
                            }
                        }

                    } else {
                        viewBinding!!.imageView.setImageBitmap(
                            imagePaths[progress]!!
                        )
                        viewBinding!!.imageViewTransparent.setImageBitmap(
                            imagePaths[progress]
                        )
                    }
                }
                if (fromUser) {
                    seekBar.progress = progress
                    lastFrame = progress
                }
            }
        })
        viewBinding!!.ivDrawColorChange.setOnClickListener {
            canvas.mode = CanvasView.Mode.DRAW
            showDialogColorPicker(this@ReportOneActivity)
        }
        viewBinding!!.ivDrawWidth.setOnClickListener {
            canvas.mode = CanvasView.Mode.DRAW
            showDialogWidthPicker(this@ReportOneActivity)
        }
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
        viewBinding!!.ivChangePositions.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            showWarningDialog(this@ReportOneActivity)
        }
        viewBinding!!.ivSaveDrawing.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            showWarningDialogForDrawing(this@ReportOneActivity, viewBinding!!)
        }
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

    private fun waitUntilTaskComplete() {
        // Wait for the task to complete
        // ...
        // Release the latch to trigger the waiting method to continue
        allDrawingList.clear()
        for (key in hashMapDrawing.keys){
            allDrawingList.add(SessionsRequestModel("annotation/image","P$key.png","0",fileId,hashMapDrawing[key],key))
        }
        uploadMedia(allDrawingList[0])
    }

    /*
    *
    * Change color
    * of
    * positions
    * */
    private fun setBackgroundPointsRed() {

        if (isDrawingMode) {
            viewBinding!!.linP1.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
            viewBinding!!.linP10.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        } else {
            viewBinding!!.linP1.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.gray_shade))
            viewBinding!!.linP10.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.gray_shade))
        }

        viewBinding!!.linP2.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        viewBinding!!.linP3.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
            if (isDrawingMode) {
                viewBinding!!.linP4.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.red_shade))
            } else {
                viewBinding!!.linP4.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.gray_shade))
            }

        } else {
            viewBinding!!.linP4.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        viewBinding!!.linP5.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        viewBinding!!.linP6.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        viewBinding!!.linP7.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        viewBinding!!.linP8.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        viewBinding!!.linP9.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.red_shade))


        viewBinding!!.tvP1.setTextColor(getColor(R.color.white))
        viewBinding!!.tvP2.setTextColor(getColor(R.color.white))
        viewBinding!!.tvP3.setTextColor(getColor(R.color.white))
        viewBinding!!.tvP4.setTextColor(getColor(R.color.white))
        viewBinding!!.tvP5.setTextColor(getColor(R.color.white))
        viewBinding!!.tvP6.setTextColor(getColor(R.color.white))
        viewBinding!!.tvP7.setTextColor(getColor(R.color.white))
        viewBinding!!.tvP8.setTextColor(getColor(R.color.white))
        viewBinding!!.tvP10.setTextColor(getColor(R.color.white))
        viewBinding!!.lyChangePosition.visibility = View.GONE
        viewBinding!!.lySaveAction.visibility = View.VISIBLE
    }

    private fun setBackgroundPointsUndo() {
        viewBinding!!.linP1.backgroundTintList = null
        viewBinding!!.linP2.backgroundTintList = null
        viewBinding!!.linP3.backgroundTintList = null
        viewBinding!!.linP4.backgroundTintList = null
        viewBinding!!.linP5.backgroundTintList = null
        viewBinding!!.linP6.backgroundTintList = null
        viewBinding!!.linP7.backgroundTintList = null
        viewBinding!!.linP8.backgroundTintList = null
        viewBinding!!.linP9.backgroundTintList = null
        viewBinding!!.linP10.backgroundTintList = null
        viewBinding!!.linP1.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP2.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP3.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP4.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP5.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP6.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP7.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP8.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP9.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.linP10.background = getDrawable(R.drawable.container_overlay_blue)
        viewBinding!!.tvP1.setTextColor(getColor(R.color.black))
        viewBinding!!.tvP2.setTextColor(getColor(R.color.black))
        viewBinding!!.tvP3.setTextColor(getColor(R.color.black))
        viewBinding!!.tvP4.setTextColor(getColor(R.color.black))
        viewBinding!!.tvP5.setTextColor(getColor(R.color.black))
        viewBinding!!.tvP6.setTextColor(getColor(R.color.black))
        viewBinding!!.tvP7.setTextColor(getColor(R.color.black))
        viewBinding!!.tvP8.setTextColor(getColor(R.color.black))
        viewBinding!!.tvP10.setTextColor(getColor(R.color.black))
        viewBinding!!.lySaveAction.visibility = View.GONE
        viewBinding!!.lyChangePosition.visibility = View.VISIBLE
        positionsResponseOriginal = positionsResponse
    }

    private fun setBackgroundPointsSingleGreen(view: View) {
        if (isDrawingMode) {
            if (!greenItemsList.contains(1)) {
                viewBinding!!.linP1.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.red_shade))
            }
        } else {
            viewBinding!!.linP1.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.gray_shade))
        }

        if (!greenItemsList.contains(2)) {
            viewBinding!!.linP2.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(3)) {
            viewBinding!!.linP3.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(4)) {
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) != "3") {
                viewBinding!!.linP4.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.red_shade))
            }
        }
        if (!greenItemsList.contains(5)) {
            viewBinding!!.linP5.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(6)) {
            viewBinding!!.linP6.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(7)) {
            viewBinding!!.linP7.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(8)) {
            viewBinding!!.linP8.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(9)) {
            viewBinding!!.linP9.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (isDrawingMode) {
            if (!greenItemsList.contains(10)) {
                viewBinding!!.linP10.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.red_shade))
            }
        } else {
            viewBinding!!.linP10.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.gray_shade))
        }

        view.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.green_shade))

    }

    private fun setBackgroundPointsSingleYellow(view: View) {
        if (isDrawingMode) {
            if (!greenItemsList.contains(1)) {
                viewBinding!!.linP1.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.red_shade))
            }
        } else {
            viewBinding!!.linP1.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.gray_shade))
        }

        if (!greenItemsList.contains(2)) {
            viewBinding!!.linP2.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(3)) {
            viewBinding!!.linP3.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(4)) {
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) != "3") {
                viewBinding!!.linP4.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.red_shade))
            }
        }
        if (!greenItemsList.contains(5)) {
            viewBinding!!.linP5.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(6)) {
            viewBinding!!.linP6.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(7)) {
            viewBinding!!.linP7.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(8)) {
            viewBinding!!.linP8.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (!greenItemsList.contains(9)) {
            viewBinding!!.linP9.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red_shade))
        }
        if (isDrawingMode) {
            if (!greenItemsList.contains(10)) {
                viewBinding!!.linP10.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.red_shade))
            }
        } else {
            viewBinding!!.linP10.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.gray_shade))
        }

        view.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.color_fen_yellow))

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


    /*
    * Current frame extraction,
    * only adding frame which is greater than p1 till p10*/

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
                    ) == "3"
                ) {
                    if (currentFrame.position >= positionsResponse!!.P1!! && currentFrame.position <= positionsResponse!!.P4!!) {
                        val selectedIndex = viewBinding!!.radiogroup.indexOfChild(
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
                    val selectedIndex = viewBinding!!.radiogroup.indexOfChild(
                        viewBinding!!.radiogroup.findViewById(
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
/*
* When frame extraction is completed ,
* if swing type = putter then we need to show only frames between p1-p4 ,
* otherwise between p1-p10*/

    @SuppressLint("SetTextI18n")
    override fun onAllFrameExtracted(processedFrameCount: Int, processedTimeMs: Long) {
        Log.d(EncoderWrapper.TAG, "Save: $processedFrameCount frames in: $processedTimeMs ms.")
        this.runOnUiThread {
            progressDialog!!.dismiss()
            if (SessionManager.getStringPref(HelperKeys.SWING_TYPE_ID, applicationContext) == "3") {
                totalFrames = positionsResponse!!.P4?.minus(positionsResponse!!.P1!!)!!

            } else {
                totalFrames = positionsResponse!!.P10?.minus(positionsResponse!!.P1!!)!!

            }
            viewBinding!!.seekBar.max = totalFrames
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                viewBinding!!.seekBar.min = 0
            }
            imagePaths.let {
                if (imagePaths.size != 0) {
//                    val bmpFactoryOptions = BitmapFactory.Options()
//                    bmpFactoryOptions.inJustDecodeBounds = true
//                    bmp =imagePaths[0]
//
//                    alteredBitmap = Bitmap.createBitmap(
//                        bmp!!.width,
//                        bmp!!.height, bmp!!.config
//                    )
//                    viewBinding!!.imageView.setNewImage(alteredBitmap!!, bmp!!)
                    viewBinding!!.imageView.setImageBitmap(imagePaths[0]!!)
                    getAnnotations()
                }
            }

            if (SessionManager.getStringPref(
                    HelperKeys.GIF_FULL_SCREEN,
                    applicationContext
                ) == ""
            ) {
                SessionManager.putStringPref(
                    HelperKeys.GIF_FULL_SCREEN,
                    "done",
                    applicationContext
                )

                showGifDialog(this@ReportOneActivity)
            }
//            infoTextView.text = "Extract $processedFrameCount frames took $processedTimeMs ms| Saving took: $totalSavingTimeMS ms"
        }
    }


    private fun startFrameExtraction(videoInputFilePath: String) {
        progressDialog!!.show()
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


    /*
    *
    *getUrl is called when we need to get actual url for downloading..
    * without getUrl , we cannot download file link of aws */


    private fun addAnnotations(addAnnotationRequestModel: AddAnnotationRequestModel) {
        hashMapDrawing.clear()
        progressDialog!!.show()


        viewModel.fetchAddAnnotations(addAnnotationRequestModel)
        viewModel.response_addAnnotations.removeObservers(this)
        viewModel.response_addAnnotations.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog!!.dismiss()


                        Toast.makeText(
                            applicationContext,
                            "Successfully uploaded",
                            Toast.LENGTH_SHORT
                        ).show()
                        processFinished = true
                        hashMapDrawing.clear()
                        allDrawingList.clear()
                        getAnnotations()

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
                            it.message,
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

    private fun replaceColor(src: Bitmap?, targetColor: Int): Bitmap? {
        if (src == null) {
            return null
        }
        // Source image size
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        //get pixels
        src.getPixels(pixels, 0, width, 0, 0, width, height)
        for (x in pixels.indices) {
            if (pixels[x] != -1) {
                pixels[x] = targetColor
            }
        }
        // create result bitmap output
        val result = Bitmap.createBitmap(width, height, src.config)
        //set pixels
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    private fun getAnnotations() {
        progressDialog!!.show()
        var uId = 0

        uId = if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE, applicationContext)) {
            SessionManager.getStringPref(HelperKeys.STRIKER_ID, applicationContext).toInt()
        } else {
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt()
        }
        val getAnnotationRequestModel = GetAnnotationRequestModel(
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt(),
            uId,
            videoId = fileId
        )
        viewModel.response_getAnnotations.removeObservers(this)
        viewModel.fetchGetAnnotations(getAnnotationRequestModel)
        viewModel.response_getAnnotations.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog!!.dismiss()
                        val listFileNames = ArrayList<String>()
                        val annotationResponseModel = it.data!!
                        if (annotationResponseModel.P1 != null && annotationResponseModel.P1 != "") {
                            listFileNames.add(annotationResponseModel.P1)
                        }
                        if (annotationResponseModel.P2 != null && annotationResponseModel.P2 != "") {
                            listFileNames.add(annotationResponseModel.P2!!)
                        }
                        if (annotationResponseModel.P3 != null && annotationResponseModel.P3 != "") {
                            listFileNames.add(annotationResponseModel.P3!!)
                        }
                        if (annotationResponseModel.P4 != null && annotationResponseModel.P4 != "") {
                            listFileNames.add(annotationResponseModel.P4!!)
                        }
                        if (annotationResponseModel.P5 != null && annotationResponseModel.P5 != "") {
                            listFileNames.add(annotationResponseModel.P5!!)
                        }
                        if (annotationResponseModel.P6 != null && annotationResponseModel.P6 != "") {
                            listFileNames.add(annotationResponseModel.P6!!)
                        }
                        if (annotationResponseModel.P7 != null && annotationResponseModel.P7 != "") {
                            listFileNames.add(annotationResponseModel.P7!!)
                        }
                        if (annotationResponseModel.P8 != null && annotationResponseModel.P8 != "") {
                            listFileNames.add(annotationResponseModel.P8!!)
                        }
                        if (annotationResponseModel.P9 != null && annotationResponseModel.P9 != "") {
                            listFileNames.add(annotationResponseModel.P9!!)
                        }
                        if (annotationResponseModel.P10 != null && annotationResponseModel.P10 != "") {
                            listFileNames.add(annotationResponseModel.P10!!)
                        }
                        getUrlForImages(listFileNames, annotationResponseModel)
//                        viewBinding!!.imageView.setNewImage(imagePaths[0]!!, imagePaths[0]!!)
                    }
                    is NetworkResult.Error -> {
                        progressDialog!!.dismiss()
                        if (it.statusCode == 401) {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        // show error message
//                        Toast.makeText(
//                            applicationContext,
//                            "Error" + " --> " + it.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
                }
            }
        })

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
                    else -> {}
                }
            }
        })

    }

    private fun getUrlForImages(
        listFileNames: List<String>,
        annotationResponseModel: GetAnnotationResponseModel
    ) {

        progressDialog!!.show()
        val urlRequestModel = UrlRequestModel(mode = "multiple", urls = listFileNames)
        viewModel.fetchMultipleUrl(urlRequestModel)
        viewModel.response_getMultipleUrl.removeObservers(this)
        viewModel.response_getMultipleUrl.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog!!.dismiss()
                        var nxt = 0
                        try {
                            if (annotationResponseModel.P1 != null && annotationResponseModel.P1 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[1] = resource
                                        }
                                    })


                                nxt += 1
                            }
                            if (annotationResponseModel.P2 != null && annotationResponseModel.P2 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[2] = resource
                                        }
                                    })
                                nxt += 1
                            }
                            if (annotationResponseModel.P3 != null && annotationResponseModel.P3 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[3] = resource
                                        }
                                    })
                                nxt += 1
                            }
                            if (annotationResponseModel.P4 != null && annotationResponseModel.P4 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[4] = resource
                                        }
                                    })
                                nxt += 1
                            }
                            if (annotationResponseModel.P5 != null && annotationResponseModel.P5 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[5] = resource
                                        }
                                    })
                                nxt += 1
                            }
                            if (annotationResponseModel.P6 != null && annotationResponseModel.P6 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[6] = resource
                                        }
                                    })
                                nxt += 1
                            }
                            if (annotationResponseModel.P7 != null && annotationResponseModel.P7 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[7] = resource
                                        }
                                    })
                                nxt += 1
                            }
                            if (annotationResponseModel.P8 != null && annotationResponseModel.P8 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[8] = resource
                                        }
                                    })
                                nxt += 1
                            }
                            if (annotationResponseModel.P9 != null && annotationResponseModel.P9 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[9] = resource
                                        }
                                    })
                                nxt += 1
                            }
                            if (annotationResponseModel.P10 != null && annotationResponseModel.P10 != "") {
                                Glide.with(this@ReportOneActivity)
                                    .asBitmap()
                                    .load(it.data!![nxt])
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                                        ) {
                                            hashMapDrawingServer[10] = resource
                                        }
                                    })
                                nxt += 1
                            }
                        } catch (ex: java.lang.Exception) {
                            Toast.makeText(
                                applicationContext,
                                "" + ex.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

//                        downloadImageUrl(it.data!!, file_name12)
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
                    else -> {}
                }
            }
        })

    }

    /*
    *This method is used for start downloading actual video from server*/
    private fun downloadUrl(url: String, fileName1: String) {
        DownloadService.downloadManager = DownloadService.getDownloadManager(applicationContext)
        val downloadUrlFile: File
        if (isOriginalSelected) {
            downloadUrlFile = localOriginalFile

        } else {
            downloadUrlFile = localOverlayFile

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

    private fun downloadImageUrl(url: String, fileName1: String) {
        DownloadService.downloadManager = DownloadService.getDownloadManager(applicationContext)


        var downloadInfo = DownloadInfo.Builder().setUrl(url)
            .setPath(fileName1)
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


            }

            override fun onDownloadFailed(e: DownloadException) {
                DownloadService.downloadManager.download(downloadInfo)
            }
        }

        DownloadService.downloadManager.download(downloadInfo)

    }

    // Timer If request takes too much time
    private fun startTimer() {
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                if (progressDialog!!.isShowing) {
                    showDialog(this@ReportOneActivity)
                }
            }
        }.start()
    }

    /*
    *
    * This dialog shows when we request time is much and ask
    * user to go back screen*/
    fun showDialog(activity: Activity?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val viewBinding = CustomDialogBackBinding.inflate(activity.layoutInflater)
        dialog.setContentView(viewBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
        viewBinding.tvYes.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDialogColorPicker(activity: Activity?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp = dialog.window!!.attributes

        wmlp.gravity = Gravity.TOP or Gravity.RIGHT
        wmlp.x = 110 //x position

        wmlp.y = 630 //y position

        dialog.setCancelable(true)
        val viewBinding = CustomDialogColorPickerBinding.inflate(activity.layoutInflater)
        dialog.setContentView(viewBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnCancelListener {

            canvas.paintStrokeColor = viewBinding.seekbarColorPicker.getColor()
        }
        viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDialogWidthPicker(activity: Activity?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp = dialog.window!!.attributes

        wmlp.gravity = Gravity.TOP or Gravity.RIGHT
        wmlp.x = 120 //x position

        wmlp.y = 890 //y position

        dialog.setCancelable(true)
        val viewBinding = CustomDialogWidthPickerBinding.inflate(activity.layoutInflater)
        dialog.setContentView(viewBinding.root)
        viewBinding.ivFirst.setOnClickListener {
            this.canvas.paintStrokeWidth = 10F
            dialog.dismiss()
        }
        viewBinding.ivSecond.setOnClickListener {
            this.canvas.paintStrokeWidth = 20F
            dialog.dismiss()
        }
        viewBinding.ivThree.setOnClickListener {
            this.canvas.paintStrokeWidth = 30F
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

//        viewBinding.seekbarColorPicker.getColor()
        dialog.show()
    }

    /*
    * This dialog show for showing gif to user to swipe right to go to next screen
    * */
    private fun showGifDialog(activity: Activity?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val viewBinding = CustomDialogLeftMoveBinding.inflate(activity.layoutInflater)
        dialog.setContentView(viewBinding.root)
        val window: Window = dialog.window!!
        val wlp = window.attributes
        wlp.gravity = Gravity.END or Gravity.BOTTOM
        wlp.x = 50  //x position
        wlp.y = 150
        window.attributes = wlp
        window.attributes = wlp

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

//        Glide.with(activity).asGif().load(R.raw.swipe_left).into(viewBinding.ivGif)
        viewBinding.ivGif.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()
        Handler(Looper.myLooper()!!).postDelayed({

            dialog.dismiss()

        }, 3000)
    }

    /*
    * Get position values from server
    * */
    private fun getPositionsData() {
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
                        positionsResponseOriginal = it.data
                        viewBinding!!.radioVideo.isChecked = true

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
    * Update positions to send to server*/
    private fun updatePositionsOnServer() {
//        val progressDialog = ProgressDialog(this) // for instantiating with Determinate mode
//        progressDialog.show()
        val arrayList = ArrayList<Int>()
        arrayList.add(positionsResponse!!.P1!!)
        arrayList.add(positionsResponse!!.P2!!)
        arrayList.add(positionsResponse!!.P3!!)
        arrayList.add(positionsResponse!!.P4!!)
        arrayList.add(positionsResponse!!.P5!!)
        arrayList.add(positionsResponse!!.P6!!)
        arrayList.add(positionsResponse!!.P7!!)
        arrayList.add(positionsResponse!!.P8!!)
        arrayList.add(positionsResponse!!.P9!!)
        arrayList.add(positionsResponse!!.P10!!)

        if (!progressDialog!!.isShowing) {

            progressDialog!!.show()
        }
        val updatePositionsRequestModel = UpdatePositionsRequestModel(
            highlightFileId!!,
            arrayList
        )
        viewModel.fetchUpdatePosition(
            Constants.BASE_URL_BIO + Constants.UPDATE_POSITIONS,
            updatePositionsRequestModel
        )
        viewModel.response_update_position.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // do something
                when (it) {
                    is NetworkResult.Success -> {
                        if (progressDialog!!.isShowing) {

                            progressDialog!!.dismiss()
                        }
//                        progressDialog.dismiss()
                        // bind data to the view

                        Toast.makeText(
                            applicationContext,
                            "Updated Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

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

//                        getPositionsData()
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
    * Click listener for p1-p10*/
    private fun seekToClick() {

        viewBinding!!.linP1.setOnClickListener {
            if (isDrawingMode) {
                if (!pausePlay) {
                    viewBinding!!.ivPlay.performClick()
                }
                if (flagEditPosition == 1 || (selectedPosition != 1 && selectedPosition != 11)) {
                    setBackgroundPointsSingleYellow(it)
                    flagEditPosition = 2
                    selectedPosition = 1
                    if (positionsResponse != null) {
                        lastFrame = positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                        viewBinding!!.seekBar.progress =
                            positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)

                    }
                } else if (flagEditPosition == 2) {
                    greenItemsList.add(1)
                    alteredBitmap = this.canvas.bitmap
                    alteredBitmap?.let { it1 -> hashMapDrawing.put(1, it1) }
                    setBackgroundPointsSingleGreen(it)
                    positionsResponse!!.P1 = lastFrame + positionsResponse!!.P1!!

                } else {
                    if (positionsResponse != null) {
                        lastFrame = positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                        viewBinding!!.seekBar.progress =
                            positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)

                    }
                }
            } else {

                if (!pausePlay) {
                    viewBinding!!.ivPlay.performClick()
                }
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                }
            }
        }


        viewBinding!!.linP2.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (flagEditPosition == 1 || (selectedPosition != 2 && selectedPosition != 11)) {
                setBackgroundPointsSingleYellow(it)
                flagEditPosition = 2
                selectedPosition = 2
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P2!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P2!!.minus(positionsResponse!!.P1!!)

                }
            } else if (flagEditPosition == 2) {
                greenItemsList.add(2)
                alteredBitmap = this.canvas.bitmap

                alteredBitmap?.let { it1 -> hashMapDrawing.put(2, it1) }
                setBackgroundPointsSingleGreen(it)
                positionsResponse!!.P2 = lastFrame + positionsResponse!!.P1!!

            } else {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P2!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P2!!.minus(positionsResponse!!.P1!!)

                }
            }
        }
        viewBinding!!.linP3.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (flagEditPosition == 1 || (selectedPosition != 3 && selectedPosition != 11)) {
                setBackgroundPointsSingleYellow(it)
                flagEditPosition = 2
                selectedPosition = 3
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P3!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P3!!.minus(positionsResponse!!.P1!!)

                }
            } else if (flagEditPosition == 2) {
                greenItemsList.add(3)
                alteredBitmap = this.canvas.bitmap

                alteredBitmap?.let { it1 -> hashMapDrawing.put(3, it1) }

                setBackgroundPointsSingleGreen(it)
                positionsResponse!!.P3 = lastFrame + positionsResponse!!.P1!!

            } else {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P3!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P3!!.minus(positionsResponse!!.P1!!)

                }
            }
        }
        viewBinding!!.linP4.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (SessionManager.getStringPref(
                    HelperKeys.SWING_TYPE_ID,
                    applicationContext
                ) == "3" || SessionManager.getStringPref(
                    HelperKeys.SWING_TYPE_ID,
                    applicationContext
                ) == "2"
            ) {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!)

                }
            } else {
                if (flagEditPosition == 1 || (selectedPosition != 4 && selectedPosition != 11)) {
                    setBackgroundPointsSingleYellow(it)
                    flagEditPosition = 2
                    selectedPosition = 4
                    if (positionsResponse != null) {
                        lastFrame = positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!)
                        viewBinding!!.seekBar.progress =
                            positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!)

                    }
                } else if (flagEditPosition == 2) {
                    greenItemsList.add(4)
                    alteredBitmap = this.canvas.bitmap

                    alteredBitmap?.let { it1 -> hashMapDrawing.put(4, it1) }

                    setBackgroundPointsSingleGreen(it)
                    positionsResponse!!.P4 = lastFrame + positionsResponse!!.P1!!

                } else {
                    if (positionsResponse != null) {
                        lastFrame = positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!)
                        viewBinding!!.seekBar.progress =
                            positionsResponse!!.P4!!.minus(positionsResponse!!.P1!!)

                    }
                }
            }

        }
        viewBinding!!.linP5.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (flagEditPosition == 1 || (selectedPosition != 5 && selectedPosition != 11)) {
                setBackgroundPointsSingleYellow(it)
                flagEditPosition = 2
                selectedPosition = 5
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P5!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P5!!.minus(positionsResponse!!.P1!!)

                }
            } else if (flagEditPosition == 2) {
                greenItemsList.add(5)
                alteredBitmap = this.canvas.bitmap

                alteredBitmap?.let { it1 -> hashMapDrawing.put(5, it1) }

                setBackgroundPointsSingleGreen(it)
                positionsResponse!!.P5 = lastFrame + positionsResponse!!.P1!!

            } else {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P5!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P5!!.minus(positionsResponse!!.P1!!)

                }
            }
        }
        viewBinding!!.linP6.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (flagEditPosition == 1 || (selectedPosition != 6 && selectedPosition != 11)) {
                setBackgroundPointsSingleYellow(it)
                flagEditPosition = 2
                selectedPosition = 6
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P6!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P6!!.minus(positionsResponse!!.P1!!)

                }
            } else if (flagEditPosition == 2) {
                greenItemsList.add(6)
                alteredBitmap = this.canvas.bitmap

                alteredBitmap?.let { it1 -> hashMapDrawing.put(6, it1) }

                setBackgroundPointsSingleGreen(it)
                positionsResponse!!.P6 = lastFrame + positionsResponse!!.P1!!

            } else {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P6!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P6!!.minus(positionsResponse!!.P1!!)


                }
            }
        }
        viewBinding!!.linP7.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (flagEditPosition == 1 || (selectedPosition != 7 && selectedPosition != 11)) {
                setBackgroundPointsSingleYellow(it)
                flagEditPosition = 2
                selectedPosition = 7
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P7!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P7!!.minus(positionsResponse!!.P1!!)

                }
            } else if (flagEditPosition == 2) {
                greenItemsList.add(7)
                alteredBitmap = this.canvas.bitmap

                alteredBitmap?.let { it1 -> hashMapDrawing.put(7, it1) }

                setBackgroundPointsSingleGreen(it)
                positionsResponse!!.P7 = lastFrame + positionsResponse!!.P1!!

            } else {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P7!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P7!!.minus(positionsResponse!!.P1!!)

                }
            }
        }
        viewBinding!!.linP8.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (flagEditPosition == 1 || (selectedPosition != 8 && selectedPosition != 11)) {
                setBackgroundPointsSingleYellow(it)
                flagEditPosition = 2
                selectedPosition = 8
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P8!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P8!!.minus(positionsResponse!!.P1!!)

                }
            } else if (flagEditPosition == 2) {
                greenItemsList.add(8)
                alteredBitmap = this.canvas.bitmap

                alteredBitmap?.let { it1 -> hashMapDrawing.put(8, it1) }

                setBackgroundPointsSingleGreen(it)
                positionsResponse!!.P8 = lastFrame + positionsResponse!!.P1!!

            } else {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P8!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P8!!.minus(positionsResponse!!.P1!!)

                }
            }
        }
        viewBinding!!.linP9.setOnClickListener {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (flagEditPosition == 1 || (selectedPosition != 9 && selectedPosition != 11)) {
                setBackgroundPointsSingleYellow(it)
                flagEditPosition = 2
                selectedPosition = 9
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P9!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P9!!.minus(positionsResponse!!.P1!!)

                }
            } else if (flagEditPosition == 2) {
                greenItemsList.add(9)
                alteredBitmap = this.canvas.bitmap

                alteredBitmap?.let { it1 -> hashMapDrawing.put(9, it1) }

                setBackgroundPointsSingleGreen(it)
                positionsResponse!!.P9 = lastFrame + positionsResponse!!.P1!!

            } else {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P9!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P9!!.minus(positionsResponse!!.P1!!)

                }
            }
        }
        viewBinding!!.linP10.setOnClickListener {

            if (isDrawingMode) {

                if (!pausePlay) {
                    viewBinding!!.ivPlay.performClick()
                }
                if (flagEditPosition == 1 || (selectedPosition != 10 && selectedPosition != 11)) {
                    setBackgroundPointsSingleYellow(it)
                    flagEditPosition = 2
                    selectedPosition = 10
                    if (positionsResponse != null) {
                        lastFrame = positionsResponse!!.P10!!.minus(positionsResponse!!.P1!!)
                        viewBinding!!.seekBar.progress =
                            positionsResponse!!.P10!!.minus(positionsResponse!!.P1!!)

                    }
                } else if (flagEditPosition == 2) {
                    greenItemsList.add(10)
                    alteredBitmap = this.canvas.bitmap

                    alteredBitmap?.let { it1 -> hashMapDrawing.put(10, it1) }

                    setBackgroundPointsSingleGreen(it)
                    positionsResponse!!.P10 = lastFrame + positionsResponse!!.P1!!

                } else {
                    if (positionsResponse != null) {
                        lastFrame = positionsResponse!!.P10!!.minus(positionsResponse!!.P1!!)
                        viewBinding!!.seekBar.progress =
                            positionsResponse!!.P10!!.minus(positionsResponse!!.P1!!)

                    }
                }
            } else {

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
    }

    /*
    *
    * Confirmation from user If he want to edit positions or not
    * */
    private fun showWarningDialog(activity: Activity?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val viewBinding = CustomDialogDeleteBinding.inflate(activity.layoutInflater)
        dialog.setContentView(viewBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewBinding.tvMessage.text = "Are you sure you want to edit the P Positions?"
        viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
        viewBinding.tvYes.setOnClickListener {
            dialog.dismiss()
            flagEditPosition = 1
            setBackgroundPointsRed()
        }
        viewBinding.tvNo.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()
    }

    private fun showWarningDialogForDrawing(
        activity: Activity?,
        viewBinding1: ActivityReportOneBinding
    ) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val viewBinding = CustomDialogDeleteBinding.inflate(activity.layoutInflater)
        dialog.setContentView(viewBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewBinding.tvMessage.text = "Are you sure you want to draw on video?"
        viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
        viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
        viewBinding.tvYes.setOnClickListener {
            dialog.dismiss()
            viewBinding1.canvasTransparent.visibility = View.VISIBLE
            viewBinding1.seekLayout.visibility = View.GONE
            canvas.mode = CanvasView.Mode.DRAW
            canvas.drawer = shapeTypeSelected
            viewBinding1.imageView.setImageBitmap(bmp!!)
            viewBinding1.ivDrawReset.visibility = View.VISIBLE
            viewBinding1!!.imageViewTransparent.visibility = View.GONE

            viewBinding1.linDrawEdit.visibility = View.VISIBLE
            flagEditPosition = 1
            isDrawingMode = true
            setBackgroundPointsRed()
            selectFirstIndexYellow()
        }
        viewBinding.tvNo.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()
    }

    fun selectFirstIndexYellow(){
        if (isDrawingMode) {
            if (!pausePlay) {
                viewBinding!!.ivPlay.performClick()
            }
            if (flagEditPosition == 1 || (selectedPosition != 1 && selectedPosition != 11)) {
                setBackgroundPointsSingleYellow(viewBinding!!.linP1)
                flagEditPosition = 2
                selectedPosition = 1
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)

                }
            } else if (flagEditPosition == 2) {
                greenItemsList.add(1)
                alteredBitmap = this.canvas.bitmap
                alteredBitmap?.let { it1 -> hashMapDrawing.put(1, it1) }
                setBackgroundPointsSingleGreen(viewBinding!!.linP1)
                positionsResponse!!.P1 = lastFrame + positionsResponse!!.P1!!

            } else {
                if (positionsResponse != null) {
                    lastFrame = positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)
                    viewBinding!!.seekBar.progress =
                        positionsResponse!!.P1!!.minus(positionsResponse!!.P1!!)

                }
            }
        }
    }
    private fun uploadMedia(
        sessionsRequestModel: SessionsRequestModel
    ) {
        if (!progressDialog!!.isShowing) {
            progressDialog!!.show()
        }

        viewModel.fetchUploadMediaOneResponse(sessionsRequestModel)
        viewModel.response_upload_media_one.removeObservers(this)
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
                            it.data!!.publicUrl!!,
                            sessionsRequestModel
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

                        // show error message
//                        Toast.makeText(applicationContext,
//                            "bucket add error"+" --> "+it.message,
//                            Toast.LENGTH_SHORT).show()
//                        showDialog(this@CaptureVideoActivity)
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
        publicUrl: String,
        sessionsRequestModel: SessionsRequestModel
        ) {


        val requestBody = RequestBody.create(
            "application/octet-stream".toMediaTypeOrNull(),
            Helper.encodeTobase64(sessionsRequestModel.bitmap!!)
        )

        viewModel.response.removeObservers(this)

        viewModel.fetchActualUploadResponse(url, requestBody)
        viewModel.response.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // bind data to the view
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }

                when (sessionsRequestModel.key) {
                    1 -> {
                        addAnnotationRequestModel.P1 = publicUrl
                    }
                    2 -> {
                        addAnnotationRequestModel.P2 = publicUrl
                    }
                    3 -> {
                        addAnnotationRequestModel.P3 = publicUrl
                    }
                    4 -> {
                        addAnnotationRequestModel.P4 = publicUrl
                    }
                    5 -> {
                        addAnnotationRequestModel.P5 = publicUrl
                    }
                    6 -> {
                        addAnnotationRequestModel.P6 = publicUrl
                    }
                    7 -> {
                        addAnnotationRequestModel.P7 = publicUrl

                    }
                    8 -> {
                        addAnnotationRequestModel.P8 = publicUrl

                    }
                    9 -> {
                        addAnnotationRequestModel.P9 = publicUrl

                    }
                    10 -> {
                        addAnnotationRequestModel.P10 = publicUrl

                    }
                }
                if (drawingIndex < allDrawingList.size - 1) {
                    drawingIndex += 1
                    uploadMedia(allDrawingList[drawingIndex])
                } else {
                    addAnnotations(addAnnotationRequestModel)

                }



            }
        })

    }
   fun getDrawingNextIndex(){
       for (key in hashMapDrawing.keys){

       }
   }
}