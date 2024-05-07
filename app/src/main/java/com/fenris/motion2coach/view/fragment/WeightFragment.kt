package com.fenris.motion2coach.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.fenris.motion2coach.databinding.FragmentWeightBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.util.Helper.replaceFromFragment
import com.fenris.motion2coach.view.activity.OldDashboardActivity
import com.fenris.motion2coach.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeightFragment : Fragment() , SignupValidationHandler {
    // TODO: Rename and change types of parameters
    val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentWeightBinding.inflate(inflater,container,false)
        (requireActivity() as OldDashboardActivity).showHideMenuView(false)

        binding.viewModel = viewModel
        binding.handler = this

        val typeCity = arrayOf("Kg", "lb")
        val adapterCity = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_dropdown_item,
            typeCity
        )
        binding.spinUnit.setAdapter(adapterCity)

        viewModel.getWeightResult().observe(viewLifecycleOwner) {
            if (it.equals("valid")) {

                replaceFromFragment(container!!.id, PlayerTypeFragment())

            } else {

                Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
            }
        }
        return  binding.root
    }
    override fun onValidationClicked() {
        viewModel.performWeightValidation()
    }
}