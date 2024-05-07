package com.fenris.motion2coach.view.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.FragmentRecordSwingBinding
import com.fenris.motion2coach.network.requests.UserIdRequestModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.responses.OrientationResponse
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.strikers.AllStrikersActivity
import com.fenris.motion2coach.view.activity.camera.CaptureVideoActivity
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.camera.CameraSamsungActivity
import com.fenris.motion2coach.viewmodel.OrientationViewModel
import com.techiness.progressdialoglibrary.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RecordSwingFragment : Fragment() {
    private lateinit var progressDialog: ProgressDialog
    val viewModel: OrientationViewModel by viewModels()
    var binding: FragmentRecordSwingBinding? = null

    private lateinit var dataOrientations: java.util.ArrayList<OrientationResponse>

    override fun onResume() {
        super.onResume()
//        if (progressDialog!=null){
//            progressDialog.dismiss()
//        }
        if (!SessionManager.getStringPref(HelperKeys.WEIGHT, context).equals("")) {
            binding!!.etWeight.setText(
                SessionManager.getStringPref(
                    HelperKeys.WEIGHT,
                    context
                ))
            binding!!.etHeight.setText(
                SessionManager.getStringPref(
                    HelperKeys.HEIGHT,
                    context
                ))


        }
        if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,activity)) {
            binding!!.etWeight.setText(
                SessionManager.getStringPref(
                    HelperKeys.STRIKER_WEIGHT,
                    context
                ))
            binding!!.etHeight.setText(
                SessionManager.getStringPref(
                    HelperKeys.STRIKER_HEIGHT,
                    context
                ))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRecordSwingBinding.inflate(inflater,container,false)
//        (requireActivity() as DashboardActivity).showHideMenuView(true)
        if (SessionManager.getStringPref(HelperKeys.HAND_USED,activity).equals("1")){
            binding!!.radioLeft.isChecked = true
        }else{
            binding!!.radioRight.isChecked = true
        }

        val typeWeight = arrayOf("Kg's", "lb's")
        val adapterWeight = ArrayAdapter(
            activity!!,
            android.R.layout.simple_spinner_dropdown_item,
            typeWeight
        )
        binding!!.spinWeightUnit.setAdapter(adapterWeight)


        val typeHeight = arrayOf("cm", "ft")
        val adapterHeight = ArrayAdapter(
            activity!!,
            android.R.layout.simple_spinner_dropdown_item,
            typeHeight
        )

        binding!!.spinHeightUnit.setAdapter(adapterHeight)
        dataOrientations = ArrayList()
        progressDialog = ProgressDialog(activity!!) // for instantiating with Determinate mode
        progressDialog.isCancelable = true
        binding!!.downLine.setOnClickListener {
//            if (dataOrientations.size!=0){
//                SessionManager.putStringPref(HelperKeys.ORIENTATION,
//                    dataOrientations[0].id.toString(),activity
//                )
//                if (checkWeightValidation()){
//                    val manufacturer = Build.MANUFACTURER
//                    if (manufacturer=="samsung"){
//                        val intent = Intent(activity, CameraSamsungActivity::class.java)
//                        intent.putExtra("capture_mode", "no")
//                        intent.putExtra(resources.getString(R.string.flag_from), "down")
//                        startActivity(intent)
//                    }else{
//
//                        val intent = Intent(activity, CaptureVideoActivity::class.java)
//                        intent.putExtra("capture_mode", "no")
//                        intent.putExtra(resources.getString(R.string.flag_from), "down")
//                        startActivity(intent)
//                    }
//                }else{
//                    Toast.makeText(activity,
//                        "Weight and Height should not be empty",
//                        Toast.LENGTH_SHORT).show()
//                }
//            }else{
//                Toast.makeText(activity,
//                    "Unable to get orientations",
//                    Toast.LENGTH_SHORT).show()
//            }
        }
        binding!!.faceOn.setOnClickListener {
            if (dataOrientations.size!=0){
                SessionManager.putStringPref(HelperKeys.ORIENTATION,
                    dataOrientations[1].id.toString(),activity
                )
                if (checkWeightValidation()){
                    val manufacturer = Build.MANUFACTURER
                    if (manufacturer=="samsung"){
                        val intent = Intent(activity, CameraSamsungActivity::class.java)
                        intent.putExtra("capture_mode", "no")
                        intent.putExtra(resources.getString(R.string.flag_from), "face")
                        startActivity(intent)
                    }else{

                        val intent = Intent(activity, CaptureVideoActivity::class.java)
                        intent.putExtra("capture_mode", "no")
                        intent.putExtra(resources.getString(R.string.flag_from), "face")
                        startActivity(intent)
                    }
                }else{
                    Toast.makeText(activity,
                        "Weight and Height should not be empty",
                        Toast.LENGTH_SHORT).show()
                }


            }else{
                Toast.makeText(activity,
                    "Unable to get orientations",
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding!!.ivBack.setOnClickListener {
            if (fragmentManager!!.backStackEntryCount == 0) {
                activity!!.finish()
            } else {
                activity!!.onBackPressed()
            }
        }
        getOrientations(SessionManager.getStringPref(HelperKeys.USER_ID,activity).toInt())

        binding!!.radiogroup.setOnCheckedChangeListener { radioGroup, i ->

            if (i == R.id.radio_left){
                SessionManager.putStringPref(HelperKeys.HAND_USED,
                   "1",activity
                )
            }
            if (i == R.id.radio_right){
                SessionManager.putStringPref(HelperKeys.HAND_USED,
                    "2",activity
                )
            }
        }

        if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,activity)){
            binding!!.cardStriker.visibility=View.VISIBLE
            binding!!.tvName.text = SessionManager.getStringPref(HelperKeys.STRIKER_NAME,activity)
            if (SessionManager.getStringPref(HelperKeys.STRIKER_HAND_USED,activity).contains("left")){
                binding!!.tvType.text = "Left Handed"

            }else{
                binding!!.tvType.text = "Right Handed"

            }
            val options = RequestOptions()
                .placeholder(R.drawable.elipse_avatar)
                .error(R.drawable.elipse_avatar)
            Glide.with(activity!!)
                .load(SessionManager.getStringPref(HelperKeys.STRIKER_IMAGE,activity))
                .apply(options)
                .into(binding!!.ivStriker)
        }
        binding!!.ivStrikerBack.setOnClickListener {

            startActivity(Intent(activity, AllStrikersActivity::class.java))
            activity!!.finishAffinity()
        }
        return  binding!!.root
    }

    private fun checkWeightValidation(): Boolean{
        if (binding!!.etWeight.text.toString()!=""&&binding!!.etHeight.text.toString()!=""){
            var weightValue =0f
            weightValue = if (binding!!.spinWeightUnit.text.toString()=="lb's"){
                (binding!!.etWeight.text.toString().toFloat()/2.20462).toFloat()
            }else{
                binding!!.etWeight.text.toString().toFloat()
            }
            var heightValue =0f
            heightValue = if (binding!!.spinHeightUnit.text.toString()=="ft"){
                (30.48 *binding!!.etHeight.text.toString().toFloat()).toFloat()
            }else{
                binding!!.etHeight.text.toString().toFloat()
            }
            if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,activity)) {
                SessionManager.putStringPref(HelperKeys.STRIKER_WEIGHT,weightValue.toString(),activity)
                SessionManager.putStringPref(HelperKeys.STRIKER_HEIGHT,heightValue.toString(),activity)
            }else{
                SessionManager.putStringPref(HelperKeys.WEIGHT,weightValue.toString(),activity)
                SessionManager.putStringPref(HelperKeys.HEIGHT,heightValue.toString(),activity)
            }

            return true
        }else{
            return false
        }

    }
    private fun getOrientations(id : Int) {
        dataOrientations = ArrayList<OrientationResponse>()

        progressDialog.show()

        val code="${(activity as AppCompatActivity).getAppVersionName()}.${(activity as AppCompatActivity).getVersionCode()}"

        val citiesRequestModel = UserIdRequestModel(id,"android",code)
        viewModel.fetchOrientationResponse(citiesRequestModel)
        viewModel.response_orientations.observeAsEvent(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    // bind data to the view
                    binding!!.btnOrientOne.text = response.data!![0].name
                    binding!!.btnOrientTwo.text = response.data[1].name

                    response.data.let { dataOrientations.addAll(it) }

                }
                is NetworkResult.Error -> {

                    progressDialog.dismiss()
                    if (response.statusCode==401){
                        startActivity(Intent(context, LoginActivity::class.java))
                        activity!!.finishAffinity()
                    }
                    Toast.makeText(activity, response.message.toString(),Toast.LENGTH_SHORT).show()

                }
                is NetworkResult.Loading -> {
                    // show a progress bar
                }
            }
        }
    }


}