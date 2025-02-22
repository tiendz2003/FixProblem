package com.example.fixproblem.presentation.ui.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixproblem.R
import com.example.fixproblem.di.AppDispatcher
import com.example.fixproblem.di.DispatcherType
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
   @AppDispatcher(DispatcherType.IO) val ioDispatcher: CoroutineDispatcher
):ViewModel() {
    private val _uiState = MutableStateFlow(LoginAuthState())
    val uiState get() = _uiState.asStateFlow()

    init {
        //checkAuthState()
    }
    private fun updateState(update:(LoginAuthState)->LoginAuthState) {
        _uiState.update(update)
    }
    fun onEvent(event:LoginIntent) {
        when(event){
            is LoginIntent.HandleSignInResult -> handleSignInResult(event.result)
            is LoginIntent.LogOut -> signOut()
            is LoginIntent.SignInWithEmailPassword -> signInWithEmailPassword(event.email,event.password)
            is LoginIntent.SignInWithGoogle -> signInWithGoogle(event.context)
         }
    }
    private fun checkAuthState() {
        Firebase.auth.currentUser?.let { user ->
            if (user.email.isNullOrBlank()) {
                // Nếu không có email, coi như chưa xác thực
                updateState {
                    it.copy(
                        isAuthenticated = false,
                        isLoading = false,
                        error = "Người dùng chưa xác thực email"
                    )
                }
            } else {
                // Người dùng đã xác thực
                Log.d("Auth", "User already signed in: ${user.displayName}")
                updateState {
                    it.copy(
                        alreadySignUp = true,
                        isLoading = false,
                        user = user,
                        isAnonymous = user.isAnonymous,
                        isAuthenticated = true
                    )
                }
            }
        } ?: run {
            // Người dùng chưa đăng nhập
            updateState {
                it.copy(
                    isAuthenticated = false,
                    isLoading = false
                )
            }
        }
    }

    private fun signInWithGoogle(context:Context) {
        viewModelScope.launch(ioDispatcher) {
            updateState { it.copy(isLoading = true,error = null) }
            try {
                val credentialManager = CredentialManager.create(context = context)
                //Tạo nonce để bảo mật
                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashNonce = digest.fold(""){ str,byte->str + "%02x".format(byte)}

                val signInOptions = GetSignInWithGoogleOption.Builder(
                    context.getString(R.string.web_client_id),
                ).setNonce(hashNonce).build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOptions)
                    .build()
                val result = credentialManager.getCredential(context,request)
                handleSignInResult(result)
            }catch (e:NoCredentialException){
                Log.e("SignIn", "signInWithGoogle: ${e.message}", )
                updateState {
                    it.copy(
                        isLoading = false,
                        error = "Không xác thực nào của Google"
                    )
                }
            }
            catch (e: GetCredentialException){
                Log.e("Auth", "Failed to get credentials", e)
                updateState {
                    it.copy(
                        isLoading = false,
                        error ="Failed to get credentials"
                    )
                }
            }
        }
    }
    private fun signInWithEmailPassword(email:String, pass:String){
        viewModelScope.launch(ioDispatcher) {
            updateState { it.copy(isLoading = true,error = null) }
            try {
                Firebase.auth.signInWithEmailAndPassword(email,pass)
                    .await()
                    .user?.let {user->
                        updateState {
                            it.copy(
                                alreadySignUp = true,
                                isLoading = false,
                                user = user,
                                isAnonymous = user.isAnonymous,
                                isAuthenticated = true
                            )
                        }
                    }
                Log.d("Signin","Đăng nhập thành công")
            }catch (e:Exception){
                updateState {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Authentication failed"
                    )
                }
            }
        }
    }
    private fun handleSignInResult(result: GetCredentialResponse) {
        viewModelScope.launch(ioDispatcher) {
            updateState { it.copy(isLoading = true, error = null) }
            try {
                when(val credential = result.credential){
                    is CustomCredential->{
                        if(credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential = GoogleIdTokenCredential
                                    .createFrom(credential.data)
                                val googleIdToken = googleIdTokenCredential.idToken
                                val authCredential = GoogleAuthProvider.getCredential(
                                    googleIdToken,null
                                )
                                val user = Firebase.auth.signInWithCredential(authCredential).await().user
                                user.run {
                                    updateState {
                                        it.copy(
                                            alreadySignUp = true,
                                            isLoading = false,
                                            user = user,
                                            isAnonymous = user!!.isAnonymous,
                                            isAuthenticated = true
                                        )
                                    }
                                }
                            }catch (e:GoogleIdTokenParsingException){
                                updateState {
                                    it.copy(
                                        isLoading = false,
                                        error = "Lỗi khi xác thực ${e.message}"
                                    )
                                }
                            }
                        }else{
                            updateState {
                                it.copy(
                                    isLoading = false,
                                    error = "Lỗi khi xác thực"
                                )
                            }
                        }
                    }
                }
            }catch (e:Exception){
                updateState {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    private fun signOut() {
        viewModelScope.launch(ioDispatcher) {
            updateState { it.copy(isLoading = true,error = null) }
            try {
                Firebase.auth.signOut()
                updateState {
                    LoginAuthState()
                }

            }catch (e:Exception){
                updateState {
                    it.copy(
                        isLoading = false,
                        error = e.message?:"Đăng xuất không thành công :(("
                    )
                }
            }
        }
    }
}