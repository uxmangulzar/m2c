package com.fenris.motion2coach.view.activity.report

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.Helper.preventSleep

class SegmentDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segment_details)
        preventSleep()

    }
}