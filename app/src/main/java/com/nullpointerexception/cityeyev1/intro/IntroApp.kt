package com.nullpointerexception.cityeyev1.intro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import cityeyev1.R
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.github.appintro.AppIntroPageTransformerType
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeyev1.LoginActivity
import com.nullpointerexception.cityeyev1.util.SessionUtil


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

        SessionUtil(this).autoCheckUser()

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