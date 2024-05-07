package com.fenris.motion2coach.view.activity.contactus

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.fenris.motion2coach.databinding.ActivityContactUsBinding


class ContactUsActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var url_Api: String
    private lateinit var progressDialog: ProgressDialog

    private lateinit var binding: ActivityContactUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            progressDialog = ProgressDialog(this)
            progressDialog.isIndeterminate = false
            progressDialog.setCancelable(true)
            url_Api = "https://www.motion2coach.com/contact-us"
            webView = binding.webView
            progressDialog.setMessage("Please wait")
            progressDialog.show()
            LoadUrlWebView(url_Api)
        } catch (e: Exception) {
            Log.w("TAG", "onCreate", e)
        }
    }

    private fun LoadUrlWebView(url_api: String) {
        try {
            webView.webViewClient = WebViewClient()
            webView.webChromeClient = MyWebChromeClient(url_api,progressDialog)
            webView.settings.javaScriptEnabled = true
            webView.settings.setSupportZoom(false)
            webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)

            webView.settings.allowContentAccess = true
            webView.settings.domStorageEnabled = true
            webView.settings.builtInZoomControls = false
            webView.settings.displayZoomControls = false
            webView.loadUrl(url_api)
        } catch (e: Exception) {
            Log.w("TAG", "setUpNavigationView", e)
        }
    }
    private class MyWebChromeClient(private val urlAccount: String,private val progressDialog: ProgressDialog) : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            try {
                //Tools.LogCat(context, "INSIDE MyWebChromeClient | onProgressChanged / newProgress1:" + newProgress);
                progressDialog.setMessage("$newProgress% loaded ,please wait..")
                if (newProgress < 100 && !progressDialog.isShowing) {
                    if (progressDialog != null) progressDialog.show()
                }
                if (newProgress == 100) {
                    if (progressDialog != null) progressDialog.dismiss()
                }
            } catch (e: java.lang.Exception) {
                Log.w("onProgressChanged", e)
            }
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
        }
    }
}