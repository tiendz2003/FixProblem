package com.nullpointerexception.cityeye.util

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.MainActivity
import com.nullpointerexception.cityeye.LoginActivity
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase

class SessionUtil(private val activity: Activity) {


    fun autoCheckUser() {
        if (Firebase.auth.currentUser != null) {
            proceedToMainScreen()
        }
    }

    fun proceedToMainScreen() {
        activity.startActivity(Intent(activity.applicationContext, MainActivity::class.java))
        activity.finish()
    }

    fun signInWithMail(
        context: Context,
        view: View,
        auth: FirebaseAuth,
        email: String,
        password: String
    ) {

        if (checkIfUserExists(email)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        proceedToMainScreen()
                    } else {
                        Snackbar.make(
                            view, task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        FirebaseDatabase.addNewUser(context, "email")
                        proceedToMainScreen()
                    } else {
                        //if user is logged in with google
                        Snackbar.make(
                            view, task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun checkIfUserExists(email: String): Boolean {

        var doesExist = false

        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.signInMethods ?: emptyList()
                    if (result.isNotEmpty()) {
                        doesExist = true
                    }
                }
            }
        return doesExist
    }
}