package com.fenris.motion2coach.view.activity.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.Helper.getAppVersionName
import com.fenris.motion2coach.util.Helper.getVersionCode
import com.fenris.motion2coach.util.Helper.preventSleep
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.dashboard.DashboardActivity
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        preventSleep()

        FirebaseApp.initializeApp(applicationContext)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        val code="${getAppVersionName()}.${getVersionCode()}"
        SessionManager.putStringPref(HelperKeys.APP_VERSION,code,applicationContext)
        val uId=SessionManager.getStringPref(HelperKeys.USER_ID,applicationContext)

        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        Handler(Looper.myLooper()!!).postDelayed({
            // code
            if (uId.equals("")){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }


        }, 3000)

    }
}