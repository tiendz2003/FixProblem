package com.nullpointerexception.cityeye

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.data.MainActivityViewModel
import com.nullpointerexception.cityeye.databinding.ActivityMainBinding
import com.nullpointerexception.cityeye.ui.custom.ToolbarManager
import com.nullpointerexception.cityeye.util.PermissionUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        PermissionUtils.createNotificationChannel(this)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_capture -> {
                    navController.navigate(R.id.navigation_capture)
                    true
                }

                R.id.navigation_list -> {
                    navController.navigate(R.id.navigation_list)
                    true
                }

                else -> {
                    navController.navigate(R.id.navigation_places)
                    true
                }
            }
        }

        Firebase.auth.currentUser?.let { ToolbarManager(binding.mainToolbar, it, this) }

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
                val badge = BadgeDrawable.create(this)
                badge.number = it
                BadgeUtils.attachBadgeDrawable(badge, binding.mainToolbar.notificationsIcon)
            } else {
                val badge = BadgeDrawable.create(this)
                BadgeUtils.detachBadgeDrawable(badge, binding.mainToolbar.notificationsIcon)
            }
        }
    }
}