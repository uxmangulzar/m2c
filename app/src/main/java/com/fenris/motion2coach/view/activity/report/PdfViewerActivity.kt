package com.fenris.motion2coach.view.activity.report

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityPdfViewerBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.ReportRequestModel
import com.fenris.motion2coach.util.Constants
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.ReportViewModel
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.callback.DownloadListener
import com.ixuea.android.downloader.domain.DownloadInfo
import com.ixuea.android.downloader.exception.DownloadException
import com.pdfview.PDFView
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class PdfViewerActivity : AppCompatActivity() {
    private lateinit var targetFile: File
    val viewModel: ReportViewModel by viewModels()
    private lateinit var viewBinding: ActivityPdfViewerBinding
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        progressDialog = ProgressDialog(this)
        preventSleep()

        getKinamaticData()

        viewBinding.ivShare.setOnClickListener {
            if (targetFile!=null){
                val file =
                    targetFile
                if (file.exists()) {
                    val builder = VmPolicy.Builder()
                    StrictMode.setVmPolicy(builder.build())

                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "application/pdf"

                    val sharingFile: File = File(file.path)

                    val outputPdfUri: Uri = FileProvider.getUriForFile(
                        this,
                        "$packageName.provider",
                        file
                    )

                    shareIntent.putExtra(Intent.EXTRA_STREAM, outputPdfUri)

                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    //Write Permission might not be necessary
                    //Write Permission might not be necessary
                    shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    startActivity(Intent.createChooser(shareIntent, "Share PDF using.."))


                }
            }

        }
    }
    private fun getKinamaticData() {
        progressDialog.show()
//        val reportRequestModel = ReportRequestModel(SessionManager.getStringPref(HelperKeys.USER_ID,applicationContext),"nasir@gmail.com/m2c-and-87-1665494615182.json"!!)
        val reportRequestModel = ReportRequestModel(SessionManager.getStringPref(HelperKeys.USER_ID,applicationContext),intent.getStringExtra("highlight_fileid")!!,-1)
//      val reportRequestModel = ReportRequestModel("m2c1662965785664")
        viewModel.fetchKynamticReport(Constants.BASE_URL_BIO+Constants.GET_REPORT_KINAMATIC,reportRequestModel)
        viewModel.response_kynamticReport.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                // do something
                when (it) {
                    is NetworkResult.Success -> {

                        // bind data to the view

                        downloadUrl(it.data!!.url!!)



                    }
                    is NetworkResult.Error -> {
                        progressDialog.dismiss()
                        if (it.statusCode==401){
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finishAffinity()
                        }
                        // show error message
                        Toast.makeText(applicationContext, it.message.toString(),Toast.LENGTH_SHORT).show()

                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
                }
            }
        })

    }
    fun downloadUrl(url: String){
        DownloadService.downloadManager = DownloadService.getDownloadManager(applicationContext)

        targetFile = File(filesDir, "kinamatic.pdf")

        var downloadInfo = DownloadInfo.Builder().setUrl(url)
            .setPath(targetFile.absolutePath)
            .build()

        downloadInfo!!.downloadListener = object : DownloadListener {
            override fun onStart() {

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
                progressDialog.dismiss()
                val pdfView = findViewById<PDFView>(R.id.pdfView)
                pdfView.fromFile(targetFile.absolutePath).show()
            }

            override fun onDownloadFailed(e: DownloadException) {
                Toast.makeText(applicationContext,"Download failed",Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
//                DownloadService.downloadManager.download(downloadInfo)
            }
        }

        DownloadService.downloadManager.download(downloadInfo)

    }

    fun onBackPressed(view: View) {
        finish()
    }
}