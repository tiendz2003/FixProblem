package com.nullpointerexception.cityeye

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.databinding.ActivityMainBinding
import com.nullpointerexception.cityeye.ui.custom.ToolbarManager
import com.nullpointerexception.cityeye.util.LocationUtil
import com.nullpointerexception.cityeye.util.PermissionUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val captureFragment = CaptureFragment()
    private val listFragment = ListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.setOnItemSelectedListener {
            when(it.itemId){
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



}