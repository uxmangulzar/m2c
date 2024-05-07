package com.fenris.motion2coach.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.fenris.motion2coach.databinding.FragmentGenderBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.util.Helper.replaceFromFragment
import com.fenris.motion2coach.view.activity.OldDashboardActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenderFragment : Fragment() , SignupValidationHandler {
    // TODO: Rename and change types of parameters
    val viewModel: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentGenderBinding.inflate(inflater,container,false)
        (requireActivity() as OldDashboardActivity).showHideMenuView(false)

        binding.viewModel = viewModel
        binding.handler = this
        binding.next.setOnClickListener {
            replaceFromFragment(container!!.id, WeightFragment())


        }
//        viewModel.getGenderResult().observe(requireActivity(), androidx.lifecycle.Observer {
//            if(it.equals("valid")){
//                replaceFromFragment(container!!.id,WeightFragment())
//
//            }
//        })
        return  binding.root
    }
    override fun onValidationClicked() {
        viewModel.performGenderValidation()
    }

}