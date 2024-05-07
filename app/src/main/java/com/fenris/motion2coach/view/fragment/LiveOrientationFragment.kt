package com.fenris.motion2coach.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.fenris.motion2coach.R
import com.fenris.motion2coach.view.activity.live.LivePreviewActivity


class LiveOrientationFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_live_orientation, container, false)
//        (requireActivity() as DashboardActivity).showHideMenuView(true)

        val downLine = view.findViewById<CardView>(R.id.down_line)
        val faceOn = view.findViewById<CardView>(R.id.face_on)
        downLine.setOnClickListener {
            val intent = Intent(activity, LivePreviewActivity::class.java)
//            intent.putExtra(resources.getString(R.string.flag_from),"down")
            startActivity(intent)
        }
        faceOn.setOnClickListener {
            val intent = Intent(activity, LivePreviewActivity::class.java)
//            intent.putExtra(resources.getString(R.string.flag_from),"face")
            startActivity(intent)
        }
        return view
    }


}