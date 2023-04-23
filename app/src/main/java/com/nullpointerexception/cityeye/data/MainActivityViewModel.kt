package com.nullpointerexception.cityeye.data

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.entities.User
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import kotlinx.coroutines.launch
import okhttp3.internal.wait

class MainActivityViewModel : ViewModel() {

    private val _messagesCount = MutableLiveData<Int>()
    var messagesCount: LiveData<Int> = _messagesCount

    fun setMessagesCount(count: Int) {
        _messagesCount.value = count
    }

    fun getMessagesCount(): MutableLiveData<Int> {
        return _messagesCount
    }

    private val _user = MutableLiveData<User>()
    var user: LiveData<User> = _user

    fun setUser(user: User) {
        _user.value = user
    }

    fun getUser(): MutableLiveData<User> {
        return _user
    }

    fun getUserFromDb() {
        viewModelScope.launch {
            val response = FirebaseDatabase.getUser(Firebase.auth.currentUser!!.uid)
            response?.let { setUser(it) }
        }
    }

    fun getLiveMessagesCount() {
        viewModelScope.launch {
            val response = FirebaseDatabase.getUserNotifications(_user.value!!.notifications!!)
            setMessagesCount(response.count { !it.isRead!! })
        }
    }


}