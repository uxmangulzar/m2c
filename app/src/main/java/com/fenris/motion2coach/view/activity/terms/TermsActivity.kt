package com.fenris.motion2coach.view.activity.terms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import com.fenris.motion2coach.databinding.ActivityTermsBinding
import com.fenris.motion2coach.util.Helper.preventSleep

class TermsActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityTermsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        preventSleep()


        viewBinding.tvTerms.movementMethod = ScrollingMovementMethod()
        viewBinding.agree.setOnClickListener {
            finish()
        }

    }
}