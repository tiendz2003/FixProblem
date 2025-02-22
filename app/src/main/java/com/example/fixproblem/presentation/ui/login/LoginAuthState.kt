package com.example.fixproblem.presentation.ui.login

import android.content.Context
import android.credentials.CredentialManager
import androidx.credentials.GetCredentialResponse
import com.google.firebase.auth.FirebaseUser

data class LoginAuthState(
    val isLoading:Boolean = false,
    val user:FirebaseUser?=null,
    val isAnonymous:Boolean = false,
    val isAuthenticated:Boolean = false,
    val alreadySignUp:Boolean = false,
    val error:String? = null
)
sealed interface LoginIntent{
    data class HandleSignInResult(val result:GetCredentialResponse):LoginIntent
    data class SignInWithEmailPassword(val email:String,val password:String):LoginIntent
    data class SignInWithGoogle(val context:Context):LoginIntent
    data object LogOut:LoginIntent
}
