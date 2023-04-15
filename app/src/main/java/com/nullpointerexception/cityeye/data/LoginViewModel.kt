package com.nullpointerexception.cityeye.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.entities.User
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import kotlinx.coroutines.launch
import okhttp3.internal.wait

class LoginViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    var user: LiveData<User> = _user

    private val _isDuplicateUser = MutableLiveData<Boolean>()
    var isDuplicateUser: MutableLiveData<Boolean> = _isDuplicateUser

    fun setUser(newUser: FirebaseUser) {
        val setUser = User(
            newUser.uid,
            newUser.displayName!!,
            newUser.photoUrl.toString(),
            newUser.email!!,
            newUser.phoneNumber!!
        )
        _user.value = setUser
    }

    fun getUser(): MutableLiveData<User> {
        return _user
    }

    fun isDuplicateUser(uid: String) {
        viewModelScope.launch {
            var response = FirebaseDatabase.isDuplicateUser(uid)
            response.wait()
            isDuplicateUser.value = response
        }
    }
}