package com.fenris.motion2coach.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fenris.motion2coach.databinding.ActivityActionPreviewBinding
import com.fenris.motion2coach.model.ActionToWorkModel
import com.fenris.motion2coach.view.adapter.ActionPlanAdapter
import java.util.ArrayList

class ActionPreviewActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityActionPreviewBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityActionPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.rv.layoutManager = LinearLayoutManager(this)
        val data = ArrayList<ActionToWorkModel>()
        val adapter = ActionPlanAdapter(data,this)
        viewBinding.rv.adapter = adapter
        viewBinding.btnWatch.setOnClickListener {
            val intent = Intent(applicationContext, SwingDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}