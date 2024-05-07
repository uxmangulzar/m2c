package com.fenris.motion2coach.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.FragmentCaptureOptionsBinding
import com.fenris.motion2coach.util.Helper.allPermissionsGranted
import com.fenris.motion2coach.util.Helper.replaceFromFragment
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.strikers.AllStrikersActivity
import com.fenris.motion2coach.view.activity.camera.CaptureImageActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CaptureOptionsFragment : Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentCaptureOptionsBinding.inflate(inflater,container,false)
        binding.radioScan.setOnClickListener {
//            val intent = Intent(activity, WeightActivity::class.java)
//            startActivity(intent)
        }
        binding.radioLive.setOnClickListener {
            if (allPermissionsGranted(activity!!)) {
//                replaceFromFragment(R.id.frame, LiveOrientationFragment())
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    CaptureImageActivity.REQUIRED_PERMISSIONS,
                    CaptureImageActivity.REQUEST_CODE_PERMISSIONS
                )
            }
        }
        binding!!.ivBack.setOnClickListener {
            if (fragmentManager!!.backStackEntryCount == 0) {
                activity!!.finish()
            } else {
                activity!!.onBackPressed()
            }
        }
        binding.frameRecord.setOnClickListener {
            if (allPermissionsGranted(activity!!)) {
                    replaceFromFragment(R.id.frame, CaptureSwingOptionsFragment())
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    CaptureImageActivity.REQUIRED_PERMISSIONS,
                    CaptureImageActivity.REQUEST_CODE_PERMISSIONS
                )
            }
        }
        binding.radioRecord.setOnClickListener {
            if (allPermissionsGranted(activity!!)) {
                    replaceFromFragment(R.id.frame, CaptureSwingOptionsFragment())
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    CaptureImageActivity.REQUIRED_PERMISSIONS,
                    CaptureImageActivity.REQUEST_CODE_PERMISSIONS
                )
            }
        }

        if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,activity)){
            binding.cardStriker.visibility=View.VISIBLE
            binding.tvName.text = SessionManager.getStringPref(HelperKeys.STRIKER_NAME,activity)
            if (SessionManager.getStringPref(HelperKeys.STRIKER_HAND_USED,activity).contains("left")){
                binding.tvType.text = "Left Handed"

            }else{
                binding.tvType.text = "Right Handed"

            }
            val options = RequestOptions()
                .placeholder(R.drawable.elipse_avatar)
                .error(R.drawable.elipse_avatar)
            Glide.with(activity!!)
                .load(SessionManager.getStringPref(HelperKeys.STRIKER_IMAGE,activity))
                .apply(options)
                .into(binding.ivStriker)
        }
        binding.ivStrikerBack.setOnClickListener {

            startActivity(Intent(activity, AllStrikersActivity::class.java))
            activity!!.finishAffinity()
        }
        return  binding.root
    }

}