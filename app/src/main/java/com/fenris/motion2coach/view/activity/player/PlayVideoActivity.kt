package com.fenris.motion2coach.view.activity.player

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fenris.motion2coach.databinding.ActivityPlayVideoBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.UrlRequestModel
import com.fenris.motion2coach.util.Coroutines
import com.fenris.motion2coach.util.Helper.observeOnce
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.ReportViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.DownloadService.downloadManager
import com.ixuea.android.downloader.callback.DownloadListener
import com.ixuea.android.downloader.domain.DownloadInfo
import com.ixuea.android.downloader.exception.DownloadException
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PlayVideoActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityPlayVideoBinding
    val viewModel: ReportViewModel by viewModels()
    private var exoPlayer: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var progressDialog: ProgressDialog? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preventSleep()

        progressDialog = android.app.ProgressDialog(this)
        progressDialog!!.setMessage("Loading.Please wait..")
        progressDialog!!.setCancelable(false)
        val targetFile = File(filesDir, intent.getStringExtra("file_name") + ".mp4")

        if (targetFile.exists()) {

            binding.ivLoading.visibility = View.GONE
            binding.tvDetailsTxt.visibility = View.GONE
            binding.videoview.visibility = View.VISIBLE

            preparePlayer(targetFile.absolutePath)

        }else{
            if (intent.getStringExtra("path")!!.contains("http")) {
                intent.getStringExtra("path")?.let { getUrl(it) }

            }else {
                binding.ivLoading.visibility = View.GONE
                binding.tvDetailsTxt.visibility = View.GONE
                binding.videoview.visibility = View.VISIBLE
                preparePlayer(intent.getStringExtra("path")!!)
            }

        }


        binding.onBack.setOnClickListener {
            finish()
        }
    }


    private fun preparePlayer(path : String) {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.playWhenReady = true
        binding.videoview.player = exoPlayer
        val defaultHttpDataSourceFactory = FileDataSource.Factory()
        val mediaItem =
            MediaItem.fromUri(Uri.fromFile(File(path)))
        val mediaSource =
            ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(mediaItem)
        exoPlayer?.setMediaSource(mediaSource)
        exoPlayer?.seekTo(playbackPosition)
        exoPlayer?.setPlaybackSpeed(0.5f)
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
        releasePlayer()

    }

    override fun onStop() {
        super.onStop()

        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()

        releasePlayer()
    }
    private fun getUrl(url : String) {

        progressDialog!!.show()
        val urlRequestModel = UrlRequestModel(url)
        viewModel.fetchGetUrl(urlRequestModel)
        viewModel.response_get_url.observeOnce(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog!!.dismiss()

                        downloadUrl(it.data!!)

                    }
                    is NetworkResult.Error -> {
                        progressDialog!!.dismiss()
                        if (it.statusCode==401){
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        // show error message
                        Toast.makeText(
                            applicationContext,
                            "Error"+" --> "+it.message,
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

    private fun downloadUrl(path: String) {

                val anim = RotateAnimation(
                    0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                anim.interpolator = LinearInterpolator()
                anim.repeatCount = Animation.INFINITE
                anim.duration = 4000

                // Start animating the image
                binding.ivLoading.startAnimation(anim)
                downloadManager = DownloadService.getDownloadManager(applicationContext)
                val targetFile = File(filesDir, intent.getStringExtra("file_name") + ".mp4")
                var downloadInfo = DownloadInfo.Builder().setUrl(path)
                    .setPath(targetFile.absolutePath)
                    .build()

                downloadInfo!!.downloadListener = object : DownloadListener {
                    override fun onStart() {
                        if (!progressDialog!!.isShowing){
                            if (!(this@PlayVideoActivity as Activity).isFinishing) {

                                progressDialog!!.show()
                            }
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
                        progressDialog!!.hide()
                        binding.ivLoading.visibility = View.GONE
                        binding.tvDetailsTxt.visibility = View.GONE
                        binding.videoview.visibility = View.VISIBLE
                        binding.ivLoading.visibility = View.GONE
                        binding.tvDetailsTxt.visibility = View.GONE
                        binding.videoview.visibility = View.VISIBLE
                        preparePlayer(targetFile.absolutePath)


                        //  intent.getStringExtra("path")?.let { binding.videoview.setSource(it) }
                    }

                    override fun onDownloadFailed(e: DownloadException) {
                        binding.tvDetailsTxt.text = "Download Failed , Trying again.."
                        downloadManager.download(downloadInfo)
                    }
                }
                downloadManager.download(downloadInfo)


    }
}



