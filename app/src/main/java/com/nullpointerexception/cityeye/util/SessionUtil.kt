package com.nullpointerexception.cityeye.util

import android.app.Activity
import android.content.Intent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.MainActivity
import com.nullpointerexception.cityeye.LoginActivity

class SessionUtil {

    fun logOut(activity: Activity){
        Firebase.auth.signOut()
        activity.startActivity(Intent(activity.applicationContext, LoginActivity::class.java))
        activity.finish()
    }

    fun autoCheckUser(activity: Activity){
        if(Firebase.auth.currentUser != null){
            proceedToMainScreen(activity)
        }
    }

    fun proceedToMainScreen(activity: Activity) {
        activity.startActivity(Intent(activity.applicationContext, MainActivity::class.java))
        activity.finish()
    }

}