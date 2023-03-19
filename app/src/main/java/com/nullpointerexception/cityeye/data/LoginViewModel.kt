package com.nullpointerexception.cityeye.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    var user : LiveData<User> = _user

    fun setUser(newUser: FirebaseUser){
        val setUser = User(newUser.uid, newUser.displayName!!, newUser.photoUrl.toString(), newUser.email!!, newUser.phoneNumber!!)
        _user.value = setUser
    }

    fun getUser():MutableLiveData<User>{
        return _user
    }

}