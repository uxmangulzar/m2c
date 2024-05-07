package com.fenris.motion2coach.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.view.activity.report.SegmentDetailsActivity
import com.google.android.material.button.MaterialButton
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class KinematicDetailsActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kinematic_details)
        preventSleep()

        val graph = findViewById<View>(R.id.graph) as GraphView
        val series = LineGraphSeries(
            arrayOf<DataPoint>(
                DataPoint(0.0, 1.0),
                DataPoint(1.0, 5.0),
                DataPoint(2.0, 3.0),
                DataPoint(3.0, 2.0),
                DataPoint(4.0, 6.0)
            )
        )
        series.title = "Dataset 1"
        series.color = resources.getColor(R.color.light_yellow,null)
        series.isDrawDataPoints = true
        graph.addSeries(series)

        // second series

        // second series
        val series2 = LineGraphSeries(
            arrayOf<DataPoint>(
                DataPoint(0.0, 3.0),
                DataPoint(1.0, 3.0),
                DataPoint(2.0, 6.0),
                DataPoint(3.0, 2.0),
                DataPoint(4.0, 5.0)
            )
        )
        series2.title = "Dataset 2"
        series2.isDrawBackground = true
        series2.color = resources.getColor(R.color.light_purple,null)
//        series2.backgroundColor = resources.getColor(R.color.light_purple,null)
        series2.isDrawDataPoints = true
        graph.addSeries(series2)

        // legend

        // legend
        graph.legendRenderer.isVisible = true
        graph.legendRenderer.align = LegendRenderer.LegendAlign.TOP

        val segment=findViewById<MaterialButton>(R.id.segment)
        segment.setOnClickListener {
            startActivity(Intent(applicationContext, SegmentDetailsActivity::class.java))
        }
    }
}