package com.nullpointerexception.cityeyev1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cityeyev1.R
import cityeyev1.databinding.ActivityMainBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeyev1.data.MainActivityViewModel
import com.nullpointerexception.cityeyev1.ui.custom.ToolbarManager
import com.nullpointerexception.cityeyev1.util.PermissionUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel


    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
       WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        PermissionUtils.createNotificationChannel(this)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        supportActionBar?.hide()

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_capture,
                R.id.navigation_list,
                R.id.navigation_events,
                R.id.navigation_places
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        Firebase.auth.currentUser?.let { ToolbarManager(binding.mainToolbar, it, this) }
        setNavFont()
    }
    //Thay đổi font menu
    private fun setNavFont() {
        val navView = binding.navView
        val font = resources.getFont(R.font.pro_rounded_medium) // Tải font tùy chỉnh từ thư mục res/font
        for (i in 0 until navView.menu.size()) {
            val menuItem = navView.menu.getItem(i)
            val spannableTitle = SpannableString(menuItem.title)
            spannableTitle.setSpan(
                TypefaceSpan(font), // Áp dụng font tùy chỉnh
                0,
                spannableTitle.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            menuItem.title = spannableTitle
        }
    }


    @SuppressLint("UnsafeOptInUsageError")
    override fun onStart() {
        super.onStart()

        viewModel.getUserFromDb()

        viewModel.getUser().observe(this) {
            viewModel.getLiveMessagesCount()
            viewModel.startListeningForNotifications()
        }

        viewModel.getMessagesCount().observe(this) {
            if (it > 0) {
                viewModel.badge = BadgeDrawable.create(this)
                viewModel.badge!!.number = it
                BadgeUtils.attachBadgeDrawable(
                    viewModel.badge!!,
                    binding.mainToolbar.notificationsIcon
                )
                binding.mainToolbar.notificationsIcon.visibility = View.VISIBLE
                binding.mainToolbar.notificationsIconEmpty.visibility = View.GONE
            } else {
                binding.mainToolbar.notificationsIcon.visibility = View.GONE
                binding.mainToolbar.notificationsIconEmpty.visibility = View.VISIBLE
            }
        }

    }
}