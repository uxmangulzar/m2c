package com.fenris.motion2coach.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.Helper.addFragment
import com.fenris.motion2coach.view.activity.history.SessionsActivity
import com.fenris.motion2coach.view.activity.settings.GeneralSettingsActivity
import com.fenris.motion2coach.view.fragment.DashboardFragment
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OldDashboardActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawer_layout: DrawerLayout
    lateinit var nav_view: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        drawer_layout = findViewById(R.id.drawer_layout)
        nav_view = findViewById(R.id.nav_view)
        val iv_profile = findViewById<ImageView>(R.id.iv_profile)
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.itemIconTintList = null
        val header=nav_view.getHeaderView(0)

//        val tv_version=header.findViewById<TextView>(R.id.tv_version)
//        val code="${getVersionCode()}.${getAppVersionName()}"
//        tv_version.text = "Version $code"
//        val menu: Menu = nav_view.getMenu()
//        val menuItem = menu.getItem(1)
//        val spanString = SpannableString(menuItem.title.toString())
//        spanString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.red)), 0, spanString.length, 0)
//        menuItem.title = spanString

        val  ham_button=findViewById<ImageView>(R.id.ham_button)
        iv_profile.setOnClickListener {
            startActivity(Intent(applicationContext, GeneralSettingsActivity::class.java))
//            replaceFromActivity(R.id.frame, ProfileSettingsActivity())

        }
        ham_button.setOnClickListener {
            if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.openDrawer(GravityCompat.START)
            } else drawer_layout.closeDrawer(GravityCompat.END)
        }
        addFragment(R.id.frame, DashboardFragment())
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            // Get the child fragment manager of the displayed fragment

                // Otherwise, handle the back press as normal
            val fm: FragmentManager = supportFragmentManager
            for (frag in fm.fragments) {
                if (frag.isVisible && frag is DashboardFragment && frag.onBackPressed()) {
                    return
                }

            }
            super.onBackPressed()

        }
    }


    private fun displayScreen(id: Int) {


        when (id) {
            R.id.nav_home -> {
                startActivity(Intent(applicationContext, SessionsActivity::class.java))
            }

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        displayScreen(item.itemId)

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
    fun showHideMenuView(visible: Boolean) {

        findViewById<LinearLayout>(R.id.layid).isVisible = visible
    }


}
