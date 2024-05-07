package com.fenris.motion2coach.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fenris.motion2coach.databinding.FragmentAgeBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.util.Helper.datePicker
import com.fenris.motion2coach.util.Helper.replaceFromFragment
import com.fenris.motion2coach.view.activity.OldDashboardActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AgeFragment : Fragment() , SignupValidationHandler {
    val viewModel: AuthViewModel by viewModels()
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val binding = FragmentAgeBinding.inflate(inflater,container,false)
        (requireActivity() as OldDashboardActivity).showHideMenuView(false)

        binding.viewModel = viewModel
        binding.handler = this


        binding.month.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                datePicker(
                    binding.month,
                    binding.day,
                    binding.year,
                    requireActivity().supportFragmentManager
                )
                // Do what you want
                true
            } else false
        }
        binding.year.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                datePicker(
                    binding.month,
                    binding.day,
                    binding.year,
                    requireActivity().supportFragmentManager
                )
                // Do what you want
                true
            } else false
        }
        binding.day.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                datePicker(
                    binding.month,
                    binding.day,
                    binding.year,
                    requireActivity().supportFragmentManager
                )
                // Do what you want
                true
            } else false
        }
        viewModel.getAgeResult().observe(viewLifecycleOwner) {
            if (it.equals("valid")) {

                replaceFromFragment(container!!.id, GenderFragment())

            } else {

                Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
            }
        }





        return  binding.root
    }
    override fun onValidationClicked() {
        viewModel.performAgeValidation()
    }




}