package com.nullpointerexception.cityeye.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.entities.User
import com.nullpointerexception.cityeye.entities.UserNotification
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import kotlinx.coroutines.launch

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


    fun startListeningForNotifications() {
        viewModelScope.launch {
            var count = 0
            val colRef = Firebase.firestore.collection("userNotifications")
            val query = colRef.whereArrayContains("notificationID", _user.value!!.notifications!!)

            query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle any errors
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Loop through the documents in the snapshot and update your UI or perform any necessary actions
                    for (document in snapshot.documents) {
                        if (document.toObject(UserNotification::class.java)!!.isRead == true) count++
                    }
                }
            }
            setMessagesCount(count)
        }
    }


}