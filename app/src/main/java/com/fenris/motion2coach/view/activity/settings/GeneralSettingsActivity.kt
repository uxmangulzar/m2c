package com.fenris.motion2coach.view.activity.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.fenris.motion2coach.databinding.ActivityGeneralSettingsBinding
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.invite.InvitationActivity
import com.fenris.motion2coach.view.activity.account.DeleteAccountActivity
import com.fenris.motion2coach.view.activity.contactus.ContactUsActivity
import com.fenris.motion2coach.view.activity.invite.GuestInvitationActivity
import com.fenris.motion2coach.view.activity.profile.ProfileActivity

class GeneralSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGeneralSettingsBinding

    override fun onResume() {
        super.onResume()
        binding.btnRoleName.text = SessionManager.getStringPref(HelperKeys.ROLE_NAME,applicationContext)
        binding.tvUserName.text = SessionManager.getStringPref(HelperKeys.USER_FIRST,applicationContext)+ " "+SessionManager.getStringPref(HelperKeys.USER_LAST,applicationContext)
        val options = RequestOptions()
            .centerCrop()
            .placeholder(android.R.drawable.stat_notify_error)
            .error(android.R.drawable.stat_notify_error)
        Glide.with(applicationContext)
            .load(SessionManager.getStringPref(HelperKeys.USER_IMAGE,applicationContext))
            .apply(options)
            .transform(CenterCrop(), RoundedCorners(24))
            .into(binding.ivProfile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this fragment
        binding = ActivityGeneralSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preventSleep()

        if (SessionManager.getBoolPref(HelperKeys.GUEST_USER,applicationContext)){
            binding.linConnect.visibility = View.GONE
        }
        val code="${getAppVersionName()}.${getVersionCode()}"
        binding.tvVersion.text = "Version $code"


        binding.linPrivacy.setOnClickListener {
            startActivity(Intent(applicationContext, PrivacySettingsActivity::class.java))
        }
        binding.linContact.setOnClickListener {
            startActivity(Intent(applicationContext, ContactUsActivity::class.java))
        }
        binding.linDelete.setOnClickListener {
            startActivity(Intent(applicationContext, DeleteAccountActivity::class.java))
        }

        binding.linConnect.setOnClickListener {
            startActivity(Intent(applicationContext, InvitationActivity::class.java))
        }

        binding.linProfile.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
        }
        binding.linConnectGuest.setOnClickListener {
            startActivity(Intent(applicationContext, GuestInvitationActivity::class.java))
        }

    }
    fun onBackPressed(view: View) {
        finish()
    }
}