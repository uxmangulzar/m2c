package com.fenris.motion2coach.view.activity.history

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import com.fenris.motion2coach.databinding.ActivitySessionsBinding
import com.fenris.motion2coach.model.Video
import com.fenris.motion2coach.model.SessionsMainModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.VideosRequestModel
import com.fenris.motion2coach.util.Coroutines
import com.fenris.motion2coach.util.Helper
import com.fenris.motion2coach.util.Helper.calendarToDate
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.dashboard.DashboardActivity
import com.fenris.motion2coach.view.activity.strikers.AllStrikersActivity
import com.fenris.motion2coach.view.adapter.SessionAdapter
import com.fenris.motion2coach.viewmodel.SessionViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class SessionsActivity : AppCompatActivity() {
    private var totalVideos: Int = 0
    private lateinit var binding: ActivitySessionsBinding
    val viewModel: SessionViewModel by viewModels()
    var data = ArrayList<SessionsMainModel>()
    var dataFiltered = ArrayList<SessionsMainModel>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preventSleep()

        if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,applicationContext)){
            binding.ivStrikerBack.visibility=View.VISIBLE
        }
        binding.from.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showPickerClick()
                true
            } else false
        }
        binding.to.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showPickerClick()
                // Do what you want
                true
            } else false
        }
        binding.ivStrikerBack.setOnClickListener {
            startActivity(Intent(applicationContext, AllStrikersActivity::class.java))
            finishAffinity()
        }
        binding.goToSwing.setOnClickListener {
            val intent = Intent(applicationContext, DashboardActivity::class.java)
            if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,applicationContext)) {
                intent.putExtra("fromStriker", true)
            }
            startActivity(intent)
        }
        getVideosFromServer()
    }

    private fun bindUI(
        start: Long,
        end: Long,
    ) = Coroutines.main {
        val min = Date(start)
        val max = Date(end)

        val curFormater = SimpleDateFormat("yyyy-MM-dd")
        val dateObjMinString = curFormater.format(min)
        val dateObjMaxString = curFormater.format(max)


        dataFiltered.clear()
        for (i in data) {
            val dd = Date(i.dateTime)
            val dateObjCurrentString = curFormater.format(dd)
            if (dateObjCurrentString== dateObjMinString ||dateObjCurrentString== dateObjMaxString ||(dd.after(min) && dd.before(max))) {
                dataFiltered.add(i)
            }
        }
        var totalVideos = 0
        for (j in dataFiltered){
            totalVideos += j.videoData.size
        }

        binding.rv.layoutManager = LinearLayoutManager(this)
        val adapter = SessionAdapter(dataFiltered, this, viewModel, totalVideos)
        binding.rv.adapter = adapter
        adapter.notifyDataSetChanged()

        if (dataFiltered.size == 0) {
            binding.lyError.visibility = View.VISIBLE
            binding.rv.visibility = View.GONE
        } else {
            binding.lyError.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE
        }
    }

    private fun bindUIStart() {

        val distinctLocations = data.distinctBy { it.dateTime }
        data = distinctLocations as ArrayList<SessionsMainModel>
        binding.rv.layoutManager = LinearLayoutManager(this)
        val adapter = SessionAdapter(data, this, viewModel, totalVideos)
        binding.rv.adapter = adapter

        if (data.size == 0) {
            binding.lyError.visibility = View.VISIBLE
            binding.rv.visibility = View.GONE
        } else {
            binding.lyError.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE
        }
    }

    private fun showPickerClick() {
        val calendarConstraintBuilder = CalendarConstraints.Builder()
        calendarConstraintBuilder.setValidator(DateValidatorPointBackward.now())
        val dateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        dateBuilder.setTitleText("Select Date Range")
        dateBuilder.setCalendarConstraints(calendarConstraintBuilder.build())
        val picker: MaterialDatePicker<Pair<Long?, Long?>> = dateBuilder.build()
        picker.setMenuVisibility(false)
        picker.allowEnterTransitionOverlap = false
        picker.show(this.supportFragmentManager, picker.toString())

        picker.addOnPositiveButtonClickListener { selection: Pair<Long?, Long?> ->
            val utc: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = selection.first!!
            val startDate: String = calendarToDate(applicationContext, utc, "dd/MM/yyyy")!!
            utc.timeInMillis = selection.second!!
            val endDate: String = calendarToDate(applicationContext, utc, "dd/MM/yyyy")!!
            binding.from.setText(startDate)
            binding.to.setText(endDate)
            bindUI(selection.first!!, selection.second!!)
        }
    }

    fun onBackPressed(view: View) {
        finish()
    }

    private fun getVideosFromServer() {
        val progressDialog = ProgressDialog(this@SessionsActivity)
        progressDialog.setMessage("Loading.Please wait..")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val code="${getAppVersionName()}.${getVersionCode()}"

            var uId =0
        uId = if(SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,applicationContext)) {
            SessionManager.getStringPref(HelperKeys.STRIKER_ID,applicationContext).toInt()
        }else{
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt()
        }
        val videosRequestModel = VideosRequestModel(
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt(),
            uId,
            0,"android",code,false,"2023-02-22"
        )
        viewModel.fetchVideosFromServer(videosRequestModel)
        viewModel.response_videosFromServer.observeAsEvent(this, androidx.lifecycle.Observer {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog.dismiss()
                        // bind data to the view

                        data.clear()
                        val dataDates = ArrayList<String>()
                        totalVideos = it.data!!.size
                        for (i in it.data!!) {


                            val dtStart = i.createdAt
                            val format = SimpleDateFormat("yyyy-MM-dd")
                            val dateMain = format.parse(dtStart)
                            val format1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

                            val splitArray: List<String> = dtStart!!.split("T")
//                                    val splitArrayTime: List<String> = splitArray[1].split(".")


                            val dataVideo = ArrayList<Video>()

                            for (j in it.data) {
                                val splitArrayInside: List<String> = j.createdAt!!.split("T")

                                if (splitArray[0] == splitArrayInside[0] && !dataDates.contains(
                                        splitArray[0]
                                    )) {
                                    val dateInside = format1.parse(j.createdAt)
                                    dataVideo.add(
                                        Video(
                                            j.id!!.toLong(),
                                            j.url,
                                            Helper.convertDateToLong1(dateInside),
                                            Helper.convertDateToLong1(dateInside),
                                            "server",
                                            "video/mp4",
                                            j.orientationId.toString(),
                                            j.frameRate.toString(),
                                            j.playerTypeId.toString(),
                                            j.overlayUrl.toString(),
                                            j.userId!!.toLong(),
                                            j.swingTypeId,
                                            j.swingTypename,
                                            j.clubTypename,
                                            j.title
                                        )
                                    )
                                }

                            }

                            dataVideo.reverse()
                            if (!dataDates.contains(splitArray[0])) {
                                data.add(
                                    SessionsMainModel(
                                        Helper.convertDateToLong1(dateMain),
                                        "",
                                        dataVideo
                                    )
                                )
                                dataDates.add(splitArray[0])
                            }

                        }
                        data.reverse()

                        bindUIStart()

                    }
                    is NetworkResult.Error -> {
                        progressDialog.dismiss()

                        // show error message
                        Toast.makeText(
                            applicationContext,
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                    else -> {}
                }
            }
        })

    }


}