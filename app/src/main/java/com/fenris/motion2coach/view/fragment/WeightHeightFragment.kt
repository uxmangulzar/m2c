package com.fenris.motion2coach.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.FragmentWeightheightBinding
import com.fenris.motion2coach.interfaces.SignupValidationHandler
import com.fenris.motion2coach.util.Helper.replaceFromFragment
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeightHeightFragment : Fragment() , SignupValidationHandler {
    // TODO: Rename and change types of parameters
    val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentWeightheightBinding.inflate(inflater,container,false)

        binding.viewModel = viewModel
        binding.handler = this

        val typeWeight = arrayOf("Kg's", "lb's")
        val adapterWeight = ArrayAdapter(
            activity!!,
            android.R.layout.simple_spinner_dropdown_item,
            typeWeight
        )
        binding.spinWeightUnit.setAdapter(adapterWeight)


        val typeHeight = arrayOf("cm", "ft")
        val adapterHeight = ArrayAdapter(
            activity!!,
            android.R.layout.simple_spinner_dropdown_item,
            typeHeight
        )
        binding.spinHeightUnit.setAdapter(adapterHeight)

        viewModel.getWeightResult().observe(viewLifecycleOwner) {
            if (it.equals("valid")) {
                var weightValue =0f
                weightValue = if (binding.spinWeightUnit.text.toString()=="lb's"){
                    (binding.etWeight.text.toString().toFloat()/2.20462).toFloat()
                }else{
                    binding.etWeight.text.toString().toFloat()
                }
                var heightValue =0f
                heightValue = if (binding.spinHeightUnit.text.toString()=="ft"){
                    (30.48 *binding.etHeight.text.toString().toFloat()).toFloat()
                }else{
                    binding.etHeight.text.toString().toFloat()
                }
                SessionManager.putStringPref(HelperKeys.WEIGHT,weightValue.toString(),activity)
                SessionManager.putStringPref(HelperKeys.HEIGHT,heightValue.toString(),activity)
                replaceFromFragment(R.id.frame, RecordSwingFragment())

            } else {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
        binding.back.setOnClickListener {
            if (fragmentManager!!.backStackEntryCount == 0) {
                activity!!.finish()
            } else {
                activity!!.onBackPressed()
            }
        }
        return  binding!!.root
    }
    override fun onValidationClicked() {
        viewModel.performWeightValidation()
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