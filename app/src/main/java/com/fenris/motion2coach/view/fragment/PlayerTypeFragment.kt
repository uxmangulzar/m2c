package com.fenris.motion2coach.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.fenris.motion2coach.databinding.FragmentPlayerTypeBinding
import com.fenris.motion2coach.util.Helper.replaceFromFragment
import com.fenris.motion2coach.view.activity.OldDashboardActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerTypeFragment : Fragment() {
    val viewModel: AuthViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentPlayerTypeBinding.inflate(inflater,container,false)
        (requireActivity() as OldDashboardActivity).showHideMenuView(false)

        binding.viewModel = viewModel

        binding.next.setOnClickListener {
            replaceFromFragment(container!!.id, RecordSwingFragment())


        }
        return  binding.root
    }



}