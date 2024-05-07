package com.fenris.motion2coach.view.activity.settings

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Range
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fenris.motion2coach.databinding.ActivityRecordSettingsBinding
import com.fenris.motion2coach.model.CameraInfo
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.camera.CameraSamsungActivity
import com.fenris.motion2coach.view.activity.camera.CaptureVideoActivity
import java.util.concurrent.TimeUnit


class RecordSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordSettingsBinding
    val dataFps = ArrayList<String>()
    val dataResolution = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preventSleep()

//        getRange()
        val data1 = ArrayList<String>()
        data1.add("1")
        data1.add("2")
        data1.add("3")
        data1.add("4")
        data1.add("5")
        data1.add("6")
        data1.add("7")
        data1.add("8")
        data1.add("9")
        data1.add("10")
        val typeWeight = arrayOf("Kg's", "lb's")
        val adapterWeight = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            typeWeight
        )
        binding.spinWeightUnit.setAdapter(adapterWeight)


        val typeHeight = arrayOf("cm", "ft")
        val adapterHeight = ArrayAdapter(
            this!!,
            android.R.layout.simple_spinner_dropdown_item,
            typeHeight
        )
        binding.spinHeightUnit.setAdapter(adapterHeight)
        if (!SessionManager.getStringPref(HelperKeys.FPS_RANGE, applicationContext).equals("")) {
            binding.autoFrame.setText(
                SessionManager.getStringPref(
                    HelperKeys.FPS_RANGE,
                    applicationContext
                )+"-"+  SessionManager.getStringPref(
                    HelperKeys.FPS_RESOLUTION,
                    applicationContext
                ), false
            )

          }

        if (!SessionManager.getStringPref(HelperKeys.WEIGHT, applicationContext).equals("")) {

                binding.etWeight.setText(
                    SessionManager.getStringPref(
                        HelperKeys.WEIGHT,
                        applicationContext
                    ))
                binding.etHeight.setText(
                    SessionManager.getStringPref(
                        HelperKeys.HEIGHT,
                        applicationContext
                    ))


        }
        if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,applicationContext)) {
            binding.etWeight.setText(
                SessionManager.getStringPref(
                    HelperKeys.STRIKER_WEIGHT,
                    applicationContext
                ))
            binding.etHeight.setText(
                SessionManager.getStringPref(
                    HelperKeys.STRIKER_HEIGHT,
                    applicationContext
                ))
        }
        val cameraManager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager
//
//        enumerateHighSpeedCameras(cameraManager)
//        if (SessionManager.getStringPref(HelperKeys.ROLE_NAME,applicationContext)=="Coach"){
//            val capabilitiesBack = cameraManager.getCameraCharacteristics("0").get(REQUEST_AVAILABLE_CAPABILITIES)!!
//            val capabilitiesFront = cameraManager.getCameraCharacteristics("1").get(REQUEST_AVAILABLE_CAPABILITIES)!!
//            val canReadSensorSettingsBack = capabilitiesBack.contains(
//                REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS)
//            val hasManualSensorBack = capabilitiesBack.contains(
//                REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)
//
//            val canReadSensorSettingsFront = capabilitiesFront.contains(
//                REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS)
//            val hasManualSensorFront = capabilitiesFront.contains(
//                REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)
//            if(canReadSensorSettingsBack && hasManualSensorBack){
//                val rangeSSBack: Range<Long> = getExposureTimeRange(cameraManager.getCameraCharacteristics("0"))
//                val rangeIsoBack: Range<Int> = cameraManager.getCameraCharacteristics("0")
//                    .get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)!!
//
//
//                val maxSSBack = TimeUnit.MILLISECONDS.toMinutes(rangeSSBack.upper)
//                val minSSBack: Long? = TimeUnit.MILLISECONDS.toMinutes(rangeSSBack.lower)
//
//                val maxIsoBack: Int = rangeIsoBack.upper
//                val minIsoBack: Int = rangeIsoBack.lower
//
//                binding.tvSsBackMax.text =maxSSBack!!.toInt().toString()
//                binding.tvSsBackMid.text =(maxSSBack!!.toInt()/2).toString()
//                binding.tvSsBackMin.text =minSSBack!!.toInt().toString()
//
//
//                binding.tvIsoBackMax.text =maxIsoBack!!.toInt().toString()
//                binding.tvIsoBackMid.text =(maxIsoBack!!.toInt()/2).toString()
//                binding.tvIsoBackMin.text =minIsoBack!!.toInt().toString()
//
//
//
//
//
//                binding.seekBarSsRear.max=maxSSBack!!.toInt()
//                binding.seekBarSsRear.min=minSSBack!!.toInt()
//                binding.seekBarIsoRear.max=maxIsoBack
//                binding.seekBarIsoRear.min=minIsoBack
//
//
//
//                if (SessionManager.getStringPref(HelperKeys.ISO_BACK,applicationContext)!=""){
//                    binding.seekBarIsoRear.progress=SessionManager.getStringPref(HelperKeys.ISO_BACK,applicationContext).toInt()
//
//                }
//                if (SessionManager.getStringPref(HelperKeys.SS_BACK,applicationContext)!=""){
//                    binding.seekBarSsRear.progress=TimeUnit.MILLISECONDS.toMinutes(SessionManager.getStringPref(HelperKeys.SS_BACK,applicationContext).toLong()).toInt()
//
//                }
//
//            }else{
//                binding.linRearSettings.visibility= View.GONE
//            }
//            if(canReadSensorSettingsFront && hasManualSensorFront){
//                val rangeSSFront: Range<Long> = getExposureTimeRange(cameraManager.getCameraCharacteristics("1"))
//                val rangeIsoFront: Range<Int> = cameraManager.getCameraCharacteristics("1")
//                    .get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)!!
//
//                val maxSSFront: Long? =  TimeUnit.MILLISECONDS.toMinutes(rangeSSFront.upper)
//                val minSSFront: Long? = TimeUnit.MILLISECONDS.toMinutes(rangeSSFront.lower)
//
//                val maxIsoFront: Int = rangeIsoFront.upper
//                val minIsoFront: Int = rangeIsoFront.lower
//
//
//                binding.tvSsFrontMax.text =maxSSFront!!.toInt().toString()
//                binding.tvSsFrontMid.text =(maxSSFront!!.toInt()/2).toString()
//                binding.tvSsFrontMin.text =minSSFront!!.toInt().toString()
//
//
//                binding.tvIsoFrontMax.text =maxIsoFront!!.toInt().toString()
//                binding.tvIsoFrontMid.text =(maxIsoFront!!.toInt()/2).toString()
//                binding.tvIsoFrontMin.text =minIsoFront!!.toInt().toString()
//
//
//
//
//                binding.seekBarSsFront.max= maxSSFront!!.toInt()
//                binding.seekBarSsFront.min=minSSFront!!.toInt()
//
//                binding.seekBarIsoFront.max=maxIsoFront
//                binding.seekBarIsoFront.min=minIsoFront
//
//
//                if (SessionManager.getStringPref(HelperKeys.ISO_BACK,applicationContext)!=""){
//                    binding.seekBarIsoRear.progress=SessionManager.getStringPref(HelperKeys.ISO_BACK,applicationContext).toInt()
//
//                }
//
//                if (SessionManager.getStringPref(HelperKeys.SS_FRONT,applicationContext)!=""){
//                    binding.seekBarSsFront.progress=TimeUnit.MILLISECONDS.toMinutes(SessionManager.getStringPref(HelperKeys.SS_FRONT,applicationContext).toLong()).toInt()
//
//                }
//            }else{
//                binding.linFrontSettings.visibility= View.GONE
//
//            }
//        }else{
            binding.linRearSettings.visibility= View.GONE
            binding.linFrontSettings.visibility= View.GONE
//        }





        val adapterNoRecord = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            data1
        )
        binding.autoNoRecord.setAdapter(adapterNoRecord)

        if (!SessionManager.getStringPref(HelperKeys.CONT_RECORD, applicationContext).equals("")) {
            binding.switchContinuous.isChecked = true
        }

        if (!SessionManager.getStringPref(HelperKeys.NO_OF_RECORD, applicationContext).equals("")) {
            binding.autoNoRecord.setText(
                SessionManager.getStringPref(
                    HelperKeys.NO_OF_RECORD,
                    applicationContext
                ), false
            )
        }

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            val splitArray: List<String> = binding.autoFrame.text.toString().split("-")

            var goNext = false
            if (binding.autoFrame.text.toString() != "") {
                goNext = true
                SessionManager.putStringPref(
                    HelperKeys.FPS_RANGE,
                    "" + splitArray[0],
                    applicationContext
                )
                SessionManager.putStringPref(
                    HelperKeys.FPS_RESOLUTION,
                    "" + splitArray[1],
                    applicationContext
                )

            }

            if (binding.switchContinuous.isChecked) {
                if (binding.autoNoRecord.text.toString() != "") {
                    goNext = true
                    SessionManager.putStringPref(
                        HelperKeys.CONT_RECORD,
                        "" + binding.switchContinuous.isChecked,
                        applicationContext
                    )
                    SessionManager.putStringPref(
                        HelperKeys.NO_OF_RECORD,
                        "" + binding.autoNoRecord.text.toString(),
                        applicationContext
                    )

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please select no of recordings",
                        Toast.LENGTH_SHORT
                    ).show()
                    goNext = false
                    SessionManager.putStringPref(HelperKeys.CONT_RECORD, "", applicationContext)
                    SessionManager.putStringPref(HelperKeys.NO_OF_RECORD, "", applicationContext)

                }

            } else {

                SessionManager.putStringPref(HelperKeys.CONT_RECORD, "", applicationContext)
                SessionManager.putStringPref(HelperKeys.NO_OF_RECORD, "", applicationContext)
                goNext = true

            }
            if (binding.etWeight.text.toString()==""||binding.etHeight.text.toString()==""){
                goNext = false
            }else{

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
                if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,applicationContext)) {
                    SessionManager.putStringPref(HelperKeys.STRIKER_WEIGHT,weightValue.toString(),this@RecordSettingsActivity)
                    SessionManager.putStringPref(HelperKeys.STRIKER_HEIGHT,heightValue.toString(),this@RecordSettingsActivity)
                }else{
                    SessionManager.putStringPref(HelperKeys.WEIGHT,weightValue.toString(),this@RecordSettingsActivity)
                    SessionManager.putStringPref(HelperKeys.HEIGHT,heightValue.toString(),this@RecordSettingsActivity)
                }

                goNext = true
            }
            if (goNext) {
                val manufacturer = Build.MANUFACTURER
                if (manufacturer=="samsung"){
                    val intent1 = Intent(applicationContext, CameraSamsungActivity::class.java)
                    intent1.putExtra("capture_mode", "no")
                    intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
                    startActivity(intent1)
                }else{
                    val intent1 = Intent(applicationContext, CaptureVideoActivity::class.java)
                    intent1.putExtra("capture_mode", "no")
                    intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
                    startActivity(intent1)
                }


                finish()
            }
        }

        binding!!.seekBarSsRear.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {

                // this is when actually seekbar has been seeked to a new position
                SessionManager.putStringPref(HelperKeys.SS_BACK,TimeUnit.MINUTES.toMillis(progress.toLong()).toString(),applicationContext)
            }
        })
        binding!!.seekBarSsFront.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {

                // this is when actually seekbar has been seeked to a new position
                SessionManager.putStringPref(HelperKeys.SS_FRONT,TimeUnit.MINUTES.toMillis(progress.toLong()).toString(),applicationContext)

            }
        })
        binding!!.seekBarIsoFront.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {

                // this is when actually seekbar has been seeked to a new position
                SessionManager.putStringPref(HelperKeys.ISO_FRONT,progress.toLong().toString(),applicationContext)

            }
        })
        binding!!.seekBarIsoRear.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {

                // this is when actually seekbar has been seeked to a new position
                SessionManager.putStringPref(HelperKeys.ISO_BACK,progress.toLong().toString(),applicationContext)

            }
        })
    }

    override fun onBackPressed() {
        val manufacturer = Build.MANUFACTURER
        if (manufacturer=="samsung"){
            val intent1 = Intent(applicationContext, CameraSamsungActivity::class.java)
            intent1.putExtra("capture_mode", "no")
            intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
            startActivity(intent1)
        }else{
            val intent1 = Intent(applicationContext, CaptureVideoActivity::class.java)
            intent1.putExtra("capture_mode", "no")
            intent1.putExtra("flag_from", intent.getStringExtra("flag_from"))
            startActivity(intent1)
        }

        finish()
    }

    private fun enumerateHighSpeedCameras(cameraManager: CameraManager): List<CameraInfo> {
        val availableCameras: MutableList<CameraInfo> = mutableListOf()

        // Iterate over the list of cameras and add those with high speed video recording
        //  capability to our output. This function only returns those cameras that declare
        //  constrained high speed video recording, but some cameras may be capable of doing
        //  unconstrained video recording with high enough FPS for some use cases and they will
        //  not necessarily declare constrained high speed video capability.
        cameraManager.cameraIdList.forEach { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val orientation = 1

            // Query the available capabilities and output formats
            val capabilities = characteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
            )!!
            val cameraConfig = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )!!

            // Return cameras that support constrained high video capability
            if (capabilities.contains(
                    CameraCharacteristics
                        .REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO
                )
            ) {
                // For each camera, list its compatible sizes and FPS ranges
                cameraConfig.highSpeedVideoSizes.forEach { size ->
                    cameraConfig.getHighSpeedVideoFpsRangesFor(size).forEach { fpsRange ->
                        val fps = fpsRange.lower
                        val info = CameraInfo(
                            "$orientation ($id) $size $fps FPS", id, size, fps
                        )
                        if (id.toString() == "0"&&fps <=120){
                            if (fps ==120){
                                if (size.width.toString()+"x"+size.height.toString() == "1920x1080"){
                                    dataFps.add("$fps-1280x720")

                                }
                            }else{
                                dataFps.add("$fps-$size")
                            }
                            availableCameras.add(info)
                    }

                    }
                }




            }

        }
        if (availableCameras.size==0){
                dataFps.add("30-1280x720")


            val adapterFrame = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                dataFps
            )
            binding.autoFrame.setAdapter(adapterFrame)
            adapterFrame.notifyDataSetChanged()

//            enumerateVideoCameras(cameraManager)
        }

        return availableCameras
    }
    private fun getExposureTimeRange(cameraCharacteristics: CameraCharacteristics): Range<Long> {
        return cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)!!
    }
    private fun enumerateVideoCameras(cameraManager: CameraManager): List<CameraInfo> {
        val availableCameras: MutableList<CameraInfo> = mutableListOf()

        // Iterate over the list of cameras and add those with high speed video recording
        //  capability to our output. This function only returns those cameras that declare
        //  constrained high speed video recording, but some cameras may be capable of doing
        //  unconstrained video recording with high enough FPS for some use cases and they will
        //  not necessarily declare constrained high speed video capability.
        cameraManager.cameraIdList.forEach { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val orientation = 1

            // Query the available capabilities and output formats
            val capabilities = characteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!
            val cameraConfig = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!

            // Return cameras that declare to be backward compatible
            if (capabilities.contains(CameraCharacteristics
                    .REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE)) {
                // Recording should always be done in the most efficient format, which is
                //  the format native to the camera framework
                val targetClass = MediaRecorder::class.java

                // For each size, list the expected FPS
                cameraConfig.getOutputSizes(targetClass).forEach { size ->
                    // Get the number of seconds that each frame will take to process
                    val secondsPerFrame =
                        cameraConfig.getOutputMinFrameDuration(targetClass, size) /
                                1_000_000_000.0
                    // Compute the frames per second to let user select a configuration
                    val fps = if (secondsPerFrame > 0) (1.0 / secondsPerFrame).toInt() else 0
                    val fpsLabel = if (fps > 0) "$fps" else "N/A"
                    if (id.toString() == "0"){
                        dataFps.add("$fps-$size")
                    }

                    availableCameras.add(CameraInfo(
                        "$orientation ($id) $size $fpsLabel FPS", id, size, fps))
                }
                val adapterFrame = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    dataFps
                )
                binding.autoFrame.setAdapter(adapterFrame)
                adapterFrame.notifyDataSetChanged()
            }
        }

        return availableCameras
    }
}