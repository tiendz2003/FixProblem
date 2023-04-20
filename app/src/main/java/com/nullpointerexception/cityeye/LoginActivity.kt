package com.nullpointerexception.cityeye

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_NOTIFICATION_POLICY
import android.Manifest.permission.CAMERA
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.databinding.ActivityLoginBinding
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import com.nullpointerexception.cityeye.util.PermissionUtils
import com.nullpointerexception.cityeye.util.SessionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val REQ_ONE_TAP = 2

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth



        SessionUtil(this).autoCheckUser()
        requestPermissions()
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.client_ID))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()

        binding.googleButton.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0, null
                        )

                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    Log.d(TAG, e.localizedMessage!!)
                }
        }

        binding.loginButton.setOnClickListener {
            SessionUtil(this).signInWithMail(
                this,
                binding.root,
                auth,
                binding.emailField.getText(),
                binding.passwordField.getText()
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == -1){
            val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = googleCredential.googleIdToken

            when {
                idToken != null -> {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (!FirebaseDatabase.isDuplicateUser(Firebase.auth.currentUser!!.uid)) {
                                        FirebaseDatabase.addNewUser(applicationContext, "google")
                                    }
                                    FirebaseDatabase.updateFCMToken(applicationContext)
                                }
                                SessionUtil(this).proceedToMainScreen()
                            } else {
                                Log.w(TAG, "signInWithCredential:failure", task.exception)
                            }
                        }
                }

                else -> {
                    Log.d(TAG, "No ID token!")
                }
            }
        }
    }

    fun TextInputLayout.getText(): String {
        return this.editText!!.text.toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (arePermissionsGranted()) {
                Log.i("PERMISSIONS", "All accepted.")
            } else {
                PermissionUtils.requestNotificationPermission(this)
                //Snackbar.make(findViewById(android.R.id.content), "Please enable, notifications, location and camera permissions in settings.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun arePermissionsGranted(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this, CAMERA
        )
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this, ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this, ACCESS_COARSE_LOCATION
        )
        val notificationPolicyPermission = ContextCompat.checkSelfPermission(
            this, ACCESS_NOTIFICATION_POLICY
        )
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                notificationPolicyPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                CAMERA,
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
                ACCESS_NOTIFICATION_POLICY
            ),
            100
        )
    }


}