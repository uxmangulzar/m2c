package com.fenris.motion2coach.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.Helper.preventSleep

class StrikerHeighWeightActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_striker_heigh_weight)
        preventSleep()

    }
}