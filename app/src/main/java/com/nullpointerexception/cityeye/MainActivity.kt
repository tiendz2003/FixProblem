package com.nullpointerexception.cityeye

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.databinding.ActivityMainBinding
import com.nullpointerexception.cityeye.ui.custom.ToolbarManager

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

        ToolbarManager(binding.mainToolbar, Firebase.auth.currentUser!!, this)



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