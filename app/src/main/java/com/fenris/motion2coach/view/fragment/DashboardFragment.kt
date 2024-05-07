package com.fenris.motion2coach.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.Helper.replaceFromFragment
import com.fenris.motion2coach.view.activity.OldDashboardActivity
import com.google.android.material.button.MaterialButton

class DashboardFragment : Fragment() {




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        (requireActivity() as OldDashboardActivity).showHideMenuView(true)

        view.findViewById<MaterialButton>(R.id.swing).setOnClickListener {

            replaceFromFragment(container!!.id, AgeFragment())

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
        return view
    }


        fun onBackPressed(): Boolean {
            val childFragment = childFragmentManager.findFragmentByTag(RecordSwingFragment::class.java.simpleName)
            if (childFragment != null && childFragment.isVisible) {
                // Only for that case, pop the BackStack (perhaps when other child fragments are visible don't)
                childFragmentManager.popBackStack()
                return true
            }
            return false
        }


}