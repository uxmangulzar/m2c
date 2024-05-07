package com.fenris.motion2coach.view.activity

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.Helper.preventSleep
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.callback.DownloadListener
import com.ixuea.android.downloader.domain.DownloadInfo
import com.ixuea.android.downloader.exception.DownloadException
import java.io.File

class SwingDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualize)
        preventSleep()

        val iv_visualize=findViewById<ImageView>(R.id.iv_visualize)

        DownloadService.downloadManager = DownloadService.getDownloadManager(applicationContext)
        val targetFile = File(filesDir, "abc.png")
        var downloadInfo = DownloadInfo.Builder().setUrl(intent.getStringExtra("urlOverlay"))
            .setPath(targetFile.absolutePath)
            .build()

        downloadInfo!!.downloadListener = object : DownloadListener {
            override fun onStart() {
                Toast.makeText(applicationContext,"Download started , please wait..",Toast.LENGTH_SHORT).show()
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
                Toast.makeText(applicationContext,"Download success",Toast.LENGTH_SHORT).show()

                iv_visualize.setImageURI(Uri.fromFile(targetFile))
    //                intent.getStringExtra("path")?.let { binding.videoview.setSource(it) }
            }

            override fun onDownloadFailed(e: DownloadException) {

                DownloadService.downloadManager.download(downloadInfo)


            }
        }


        DownloadService.downloadManager.download(downloadInfo)


    }

    fun onBackPressed(view: View) {

        finish()
    }
}