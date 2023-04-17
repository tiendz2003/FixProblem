package com.nullpointerexception.cityeye

import android.Manifest.permission.POST_NOTIFICATIONS
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.databinding.ActivityMainBinding
import com.nullpointerexception.cityeye.ui.custom.ToolbarManager
import com.nullpointerexception.cityeye.ui.fragments.CaptureFragment
import com.nullpointerexception.cityeye.ui.fragments.ListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val captureFragment = CaptureFragment()
    private val listFragment = ListFragment()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_capture -> {
                    switchToFragment(captureFragment)
                    true
                }
                else -> {
                    switchToFragment(listFragment)
                    true
                }
            }
        }

        Firebase.auth.currentUser?.let { ToolbarManager(binding.mainToolbar, it, this) }

        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            askNotificationPermission()
        }
    }



    private fun switchToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            if (!fragment.isAdded) {
                add(R.id.container, fragment)
            }
            supportFragmentManager.fragments.forEach {
                if (it != fragment) {
                    hide(it)
                }
            }
            show(fragment)
            commit()
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