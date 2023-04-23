package com.nullpointerexception.cityeye

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.data.CaptureViewModel
import com.nullpointerexception.cityeye.data.MainActivityViewModel
import com.nullpointerexception.cityeye.databinding.ActivityMainBinding
import com.nullpointerexception.cityeye.ui.custom.ToolbarManager
import com.nullpointerexception.cityeye.ui.fragments.CaptureFragment
import com.nullpointerexception.cityeye.ui.fragments.ListFragment
import com.nullpointerexception.cityeye.util.PermissionUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val captureFragment = CaptureFragment()
    private val listFragment = ListFragment()
    private lateinit var viewModel: MainActivityViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i("PERMISSION", "GRANTED")
        } else {
            Toast.makeText(
                this,
                "Unable to show notifications, please change it in settings for better experience.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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


        //askNotificationPermission()
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_capture -> {
                    navController.navigate(R.id.navigation_capture)
                    true
                }

                else -> {
                    navController.navigate(R.id.navigation_list)
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
        }

        viewModel.getMessagesCount().observe(this) {
            if (it > 0) {
                val badge = BadgeDrawable.create(this)
                badge.number = it
                BadgeUtils.attachBadgeDrawable(badge, binding.mainToolbar.notificationsIcon)
            }
        }


    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {

            } else {
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }
    }


}