package com.nullpointerexception.cityeyev1.intro

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import cityeyev1.R
import cityeyev1.databinding.FragmentIntroLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeyev1.firebase.FirebaseDatabase
import com.nullpointerexception.cityeyev1.util.SessionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntroLoginFragment : Fragment() {

    private lateinit var binding: FragmentIntroLoginBinding

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val REQ_ONE_TAP = 2

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentIntroLoginBinding.inflate(layoutInflater)

        auth = Firebase.auth

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            100
        )

        SessionUtil(requireActivity()).autoCheckUser()

        oneTapClient = Identity.getSignInClient(requireContext())
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
                .addOnSuccessListener(requireActivity()) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0, null
                        )

                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(ContentValues.TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(requireActivity()) { e ->
                    Log.d(ContentValues.TAG, e.localizedMessage!!)
                }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == -1) {
            val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = googleCredential.googleIdToken

            when {
                idToken != null -> {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (!FirebaseDatabase.isDuplicateUser(Firebase.auth.currentUser!!.uid)) {
                                        FirebaseDatabase.addNewUser(requireContext(), "google")
                                    }
                                    FirebaseDatabase.updateFCMToken(requireContext())
                                }
                                SessionUtil(requireActivity()).proceedToMainScreen()
                            } else {
                                Log.w(
                                    ContentValues.TAG,
                                    "signInWithCredential:failure",
                                    task.exception
                                )
                            }
                        }
                }

                else -> {
                    Log.d(ContentValues.TAG, "No ID token!")
                }
            }
        }
    }
}