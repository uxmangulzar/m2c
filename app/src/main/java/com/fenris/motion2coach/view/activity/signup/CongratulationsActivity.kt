package com.fenris.motion2coach.view.activity.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fenris.motion2coach.databinding.ActivityCongratulationsBinding
import com.fenris.motion2coach.view.activity.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CongratulationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCongratulationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCongratulationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dashboard.setOnClickListener {
            val intent = Intent(applicationContext, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finishAffinity()
        }
    }

    fun onBackPress(view: View) {
        finish()
    }
}