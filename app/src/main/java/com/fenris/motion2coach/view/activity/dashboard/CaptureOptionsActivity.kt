package com.fenris.motion2coach.view.activity.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.Helper.replaceFromActivity
import com.fenris.motion2coach.view.fragment.CaptureOptionsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CaptureOptionsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_options)
        preventSleep()

        replaceFromActivity(R.id.frame, CaptureOptionsFragment())


    }
    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }


    }




}