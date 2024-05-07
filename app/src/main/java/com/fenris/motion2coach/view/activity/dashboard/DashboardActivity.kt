package com.fenris.motion2coach.view.activity.dashboard

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ActivityDashboardBinding
import com.fenris.motion2coach.databinding.CustomDialogPremiumBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.ProfileUserRequestModel
import com.fenris.motion2coach.network.requests.SubscriptionUrlRequestModel
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getCurrentDateAppFormat
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.Helper.printDifference12
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.coaches.AllCoachesActivity
import com.fenris.motion2coach.view.activity.history.SessionsActivity
import com.fenris.motion2coach.view.activity.invite.GuestInvitationActivity
import com.fenris.motion2coach.view.activity.invite.InvitationActivity
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.settings.GeneralSettingsActivity
import com.fenris.motion2coach.view.activity.strikers.AllStrikersActivity
import com.fenris.motion2coach.view.fragment.DashboardFragment
import com.fenris.motion2coach.viewmodel.LandingViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var dialogShowing: Boolean = false
    private lateinit var binding: ActivityDashboardBinding
    val viewModel: LandingViewModel by viewModels()
    lateinit var progressDialog: com.techiness.progressdialoglibrary.ProgressDialog
    lateinit var prevMenuItem: Menu
    var isMenuExpanded: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = com.techiness.progressdialoglibrary.ProgressDialog(this@DashboardActivity)
        progressDialog.setMessage("Loading.Please wait..")
        progressDialog.isCancelable = false
        preventSleep()
        binding.navView.setNavigationItemSelectedListener(this)
        binding.navView.itemIconTintList = null
        val code = "${getAppVersionName()}.${getVersionCode()}"
        binding.tvVersion.text = "Version $code"
        prevMenuItem = binding.navView.menu

        if (SessionManager.getStringPref(HelperKeys.ROLE_NAME, applicationContext) == "Coach") {
            prevMenuItem.setGroupVisible(R.id.group_label_striker, true)
            prevMenuItem.setGroupVisible(R.id.group_label_coaches, false)
            prevMenuItem.setGroupVisible(R.id.group_label_guest, false)
            prevMenuItem.setGroupVisible(R.id.groupStrikers, false)
            prevMenuItem.setGroupVisible(R.id.groupCoaches, false)
            prevMenuItem.setGroupVisible(R.id.groupGuest, false)
        } else {
            prevMenuItem.setGroupVisible(R.id.group_label_striker, false)
            prevMenuItem.setGroupVisible(R.id.group_label_coaches, true)
            prevMenuItem.setGroupVisible(R.id.group_label_guest, false)
            binding.navView.menu.setGroupVisible(R.id.groupCoaches, false)
            binding.navView.menu.setGroupVisible(R.id.groupStrikers, false)
            binding.navView.menu.setGroupVisible(R.id.groupGuest, false)

            if (SessionManager.getBoolPref(HelperKeys.GUEST_USER, applicationContext)) {
                prevMenuItem.setGroupVisible(R.id.group_label_striker, false)
                prevMenuItem.setGroupVisible(R.id.group_label_coaches, false)
                prevMenuItem.setGroupVisible(R.id.group_label_guest, true)
                binding.navView.menu.setGroupVisible(R.id.groupCoaches, false)
                binding.navView.menu.setGroupVisible(R.id.groupStrikers, false)
                binding.navView.menu.setGroupVisible(R.id.groupGuest, false)
            }
        }
        if (SessionManager.getBoolPref(
                HelperKeys.STRIKER_MODE,
                applicationContext
            ) && intent.getBooleanExtra("fromStriker", false)
        ) {
            binding.cardStriker.visibility = View.VISIBLE
            binding.tvName.text =
                SessionManager.getStringPref(HelperKeys.STRIKER_NAME, applicationContext)
            if (SessionManager.getStringPref(HelperKeys.STRIKER_HAND_USED, applicationContext)
                    .contains("left")
            ) {
                binding.tvType.text = "Left Handed"
            } else {
                binding.tvType.text = "Right Handed"
            }
            val options = RequestOptions()
                .placeholder(R.drawable.elipse_avatar)
                .error(R.drawable.elipse_avatar)
            Glide.with(applicationContext)
                .load(SessionManager.getStringPref(HelperKeys.STRIKER_IMAGE, applicationContext))
                .apply(options)
                .into(binding.ivStriker)
        }
        binding.ivBack.setOnClickListener {

            startActivity(Intent(applicationContext, AllStrikersActivity::class.java))
            finishAffinity()
        }
        binding.ivPaymentDetail.setOnClickListener {
            if (!SessionManager.getBoolPref(
                    HelperKeys.SUBSCRIPTION_ACTIVE,
                    applicationContext
                ) || binding.ivTimeLeft.text.toString().contains("Hours Left")
            ) {
                if (!dialogShowing) {

                    showDialog(this@DashboardActivity)
                }
            } else if (SessionManager.getStringPref(
                    HelperKeys.SUBSCRIPTION_ID,
                    applicationContext
                ) == "1"
            ) {
                if (!dialogShowing) {

                    showDialog(this@DashboardActivity)
                }
            }
        }
        binding.hamButton.setOnClickListener {
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            } else binding.drawerLayout.closeDrawer(GravityCompat.END)

        }
        binding.tvLogout.setOnClickListener {
            gotoLogout()
        }

    }


    fun gotoCapture(view: View) {
        if (SessionManager.getBoolPref(
                HelperKeys.SUBSCRIPTION_ACTIVE,
                applicationContext
            ) || SessionManager.getBoolPref(HelperKeys.GUEST_USER, applicationContext)
        ) {
            startActivity(Intent(applicationContext, CaptureOptionsActivity::class.java))
        } else {
            if (!dialogShowing) {

                showDialog(this@DashboardActivity)
            }
        }
    }

    fun gotoHistory(view: View) {
        if (SessionManager.getBoolPref(HelperKeys.SUBSCRIPTION_ACTIVE, applicationContext)) {
            if (!SessionManager.getBoolPref(HelperKeys.GUEST_USER, applicationContext)) {
                startActivity(Intent(applicationContext, SessionsActivity::class.java))
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please subscribe to see history.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            if (!dialogShowing) {

                showDialog(this@DashboardActivity)
            }
        }
    }

    private fun gotoLogout() {
        SessionManager.clearsession(applicationContext)
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        finishAffinity()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Get the child fragment manager of the displayed fragment
            // Otherwise, handle the back press as normal
            val fm: FragmentManager = supportFragmentManager
            for (frag in fm.fragments) {
                if (frag.isVisible && frag is DashboardFragment && frag.onBackPressed()) {
                    return
                }

            }
            if (SessionManager.getBoolPref(
                    HelperKeys.STRIKER_MODE,
                    applicationContext
                ) && intent.getBooleanExtra("fromStriker", false)
            ) {
                startActivity(Intent(applicationContext, AllStrikersActivity::class.java))
                finishAffinity()
            } else {
                super.onBackPressed()
            }

        }
    }


    private fun displayScreen(id: Int) {
        binding.navView.setCheckedItem(R.id.nav_home)
        when (id) {
            R.id.nav_home -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_settings -> {
                if (SessionManager.getBoolPref(
                        HelperKeys.SUBSCRIPTION_ACTIVE,
                        applicationContext
                    )
                ) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                    startActivity(
                        Intent(
                            applicationContext,
                            GeneralSettingsActivity::class.java
                        )
                    )
                }
            }
            R.id.nav_label_striker -> {

                if (isMenuExpanded) {
                    isMenuExpanded = false
                    prevMenuItem.setGroupVisible(R.id.groupStrikers, false)

                } else {

                    isMenuExpanded = true

                    prevMenuItem.setGroupVisible(R.id.groupStrikers, true)

                }

            }
            R.id.nav_label_coach -> {

                if (isMenuExpanded) {
                    isMenuExpanded = false
                    prevMenuItem.setGroupVisible(R.id.groupCoaches, false)

                } else {
                    isMenuExpanded = true

                    prevMenuItem.setGroupVisible(R.id.groupCoaches, true)
                }

            }
            R.id.nav_label_guest -> {

                if (isMenuExpanded) {
                    isMenuExpanded = false
                    prevMenuItem.setGroupVisible(R.id.groupGuest, false)

                } else {
                    isMenuExpanded = true

                    prevMenuItem.setGroupVisible(R.id.groupGuest, true)

                }

            }
            R.id.nav_invite -> {
                if (SessionManager.getBoolPref(
                        HelperKeys.SUBSCRIPTION_ACTIVE,
                        applicationContext
                    )
                ) {

                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(
                        Intent(
                            applicationContext,
                            GuestInvitationActivity::class.java
                        )
                    )
                }
            }
            R.id.nav_connect_coach -> {
                if (SessionManager.getBoolPref(
                        HelperKeys.SUBSCRIPTION_ACTIVE,
                        applicationContext
                    )
                ) {

                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(applicationContext, InvitationActivity::class.java))
                }
            }
            R.id.nav_connect_striker -> {
                if (SessionManager.getBoolPref(
                        HelperKeys.SUBSCRIPTION_ACTIVE,
                        applicationContext
                    )
                ) {

                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(applicationContext, InvitationActivity::class.java))
                }
            }

            R.id.nav_all_striker -> {
                if (SessionManager.getBoolPref(
                        HelperKeys.SUBSCRIPTION_ACTIVE,
                        applicationContext
                    )
                ) {

                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                    startActivity(Intent(applicationContext, AllStrikersActivity::class.java))
                }


            }
            R.id.nav_all_coaches -> {
                if (SessionManager.getBoolPref(
                        HelperKeys.SUBSCRIPTION_ACTIVE,
                        applicationContext
                    )
                ) {

                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                    startActivity(Intent(applicationContext, AllCoachesActivity::class.java))
                }


            }
            R.id.nav_all_coaches_guest -> {
                if (SessionManager.getBoolPref(
                        HelperKeys.SUBSCRIPTION_ACTIVE,
                        applicationContext
                    )
                ) {

                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                    startActivity(Intent(applicationContext, AllCoachesActivity::class.java))
                }


            }
        }


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        displayScreen(item.itemId)

//        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        super.onResume()
        onNavigationItemSelected(binding.navView.menu.getItem(0))
//        SessionManager.putBoolPref(HelperKeys.SUBSCRIPTION_ACTIVE,
//            true,applicationContext)

        getProfile(SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt())
    }

    private fun showDialog(activity: Activity?) {
        dialogShowing = true
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val viewBinding = CustomDialogPremiumBinding.inflate(activity.layoutInflater)
        dialog.setContentView(viewBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (SessionManager.getStringPref(HelperKeys.ROLE_NAME, applicationContext) == "Coach") {
            viewBinding.tvEndPrice.text = "€12.00/mo"
            viewBinding.tvMidPrice.text = "€14.00/mo"
            viewBinding.tvStartPrice.text = "€15.00/mo"
            viewBinding.tvSaving.text = "SAVE 5%"
        }else{
            viewBinding.tvEndPrice.text = "€8.00/mo"
            viewBinding.tvMidPrice.text = "€9.00/mo"
            viewBinding.tvStartPrice.text = "€10.00/mo"
            viewBinding.tvSaving.text = "SAVE 20%"
        }

        viewBinding.btnPremium.setOnClickListener {
            dialog.dismiss()
            dialogShowing = false
            getSubscriptionUrl()
        }
        viewBinding.ivCross.setOnClickListener {
            dialogShowing = false
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getSubscriptionUrl() {

        progressDialog.show()
        val subscriptionUrlRequestModel = SubscriptionUrlRequestModel(
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt(),
            SessionManager.getStringPref(HelperKeys.USER_ID, applicationContext).toInt()
        )
        viewModel.getSubscriptionUrl(subscriptionUrlRequestModel)
        viewModel.response_getSubscriptionUrl.observe(this) {
            if (it != null) {
                when (it) {
                    is NetworkResult.Success -> {

                        progressDialog.dismiss()
                        // bind data to the view

                    }
                    is NetworkResult.Error -> {
                        progressDialog.dismiss()
                        when (it.statusCode) {
                            401 -> {
                                // show error message
                                Toast.makeText(
                                    applicationContext,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(applicationContext, LoginActivity::class.java))
                                finishAffinity()
                            }
                            402 -> {
                                val browserIntent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(it.jsonObject!!.getString("url"))
                                    )
                                startActivity(browserIntent)
                            }
                            else -> {
                                // show error message
                                Toast.makeText(
                                    applicationContext,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                    is NetworkResult.Loading -> {
                        // show a progress bar
                    }
                }
            }
        }

    }

    private fun getProfile(id: Int) {

        val progressDialog =
            com.techiness.progressdialoglibrary.ProgressDialog(this) // for instantiating with Determinate mode
        progressDialog.show()
        val code = "${getAppVersionName()}.${getVersionCode()}"

        val profileUserRequestModel = ProfileUserRequestModel(
            id,
            id,
            0,
            "",
            "",
            0,
            "",
            "",
            "",
            0,
            "",
            "",
            "",
            "",
            0,
            0,
            "android",
            code
        )
        viewModel.fetchProfileResponse(profileUserRequestModel)
        viewModel.response_profile.observeAsEvent(this) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    progressDialog.dismiss()
                    //bind data to the view

                    SessionManager.putStringPref(
                        HelperKeys.USER_EMAIL,
                        response.data!!.email, applicationContext
                    )
                    SessionManager.putStringPref(
                        HelperKeys.USER_FIRST,
                        response.data.firstName.toString(), applicationContext
                    )
                    SessionManager.putStringPref(
                        HelperKeys.USER_LAST,
                        response.data.lastName.toString(), applicationContext
                    )

                    SessionManager.putStringPref(
                        HelperKeys.ROLE_NAME,
                        response.data.role.toString(), applicationContext
                    )
                    SessionManager.putBoolPref(
                        HelperKeys.GUEST_USER,
                        response.data.guestUser, applicationContext
                    )
                    SessionManager.putStringPref(
                        HelperKeys.USER_IMAGE,
                        response.data.picture.toString(), applicationContext
                    )
                    SessionManager.putStringPref(
                        HelperKeys.HAND_USED,
                        response.data.playerTypeId.toString(), applicationContext
                    )
                    SessionManager.putStringPref(
                        HelperKeys.ROLE_ID,
                        response.data.roleId.toString(), applicationContext
                    )
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

                    try {
                        val date1: Date = simpleDateFormat.parse(getCurrentDateAppFormat())
                        val date2: Date = simpleDateFormat.parse(response.data.subscriptionExpiry)
                        if (printDifference12(date1, date2) != "") {
                            binding.ivTimeLeft.text = printDifference12(date1, date2)
                        } else {
                            binding.ivTimeLeft.visibility = View.GONE
                        }

                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                    try {

                        binding.ivPaymentDetail.visibility = View.VISIBLE
                        binding.ivTimeLeft.visibility = View.VISIBLE

                        SessionManager.putBoolPref(
                            HelperKeys.SUBSCRIPTION_ACTIVE,
                            response.data.isSubscriptionActive, applicationContext
                        )
                        SessionManager.putStringPref(
                            HelperKeys.SUBSCRIPTION_ID,
                            response.data.subscriptionId.toString(), applicationContext
                        )
                        if (response.data.isSubscriptionActive != true) {
                            binding.ivPaymentDetail.setImageDrawable(getDrawable(R.drawable.group_premium_end))
                            if (!dialogShowing) {
                                showDialog(this@DashboardActivity)
                            }
                        } else {
                            if (response.data.subscriptionId == 1) {
                                binding.ivPaymentDetail.setImageDrawable(getDrawable(R.drawable.freetrail_image))
                            } else {
                                binding.ivPaymentDetail.setImageDrawable(getDrawable(R.drawable.group_premium))
                            }
                        }

                        if (SessionManager.getBoolPref(
                                HelperKeys.GUEST_USER, applicationContext
                            )
                        ) {
                            binding.ivPaymentDetail.setImageDrawable(getDrawable(R.drawable.group_guest))
                            binding.ivTimeLeft.visibility = View.GONE
                        }
                    } catch (ex: Exception) {
                    }
                }
                is NetworkResult.Error -> {

                    progressDialog.dismiss()
                    if (response.statusCode == 401) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                        finishAffinity()
                    }
//                    show error message

//                    Toast.makeText(applicationContext, response.message.toString(),Toast.LENGTH_SHORT).show()


                }
                is NetworkResult.Loading -> {
                    // show a progress bar
                }
            }
        }
    }
}