package com.nullpointerexception.cityeye.intro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.github.appintro.AppIntroPageTransformerType
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.LoginActivity
import com.nullpointerexception.cityeye.R


class IntroApp : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_main_fragment))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.first_intro_fragment))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.second_intro_fragment))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.third_intro_fragment))

        setTransformer(AppIntroPageTransformerType.Fade)
        isIndicatorEnabled = true
        isSystemBackButtonLocked = true
        isWizardMode = true
        setImmersiveMode()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        startLoginActivity()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        startLoginActivity()
    }

    fun startLoginActivity() {
        Firebase.auth.signOut()
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        finish()
    }

}