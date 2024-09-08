package com.nullpointerexception.cityeyev1.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointerexception.cityeyev1.entities.User
import com.nullpointerexception.cityeyev1.entities.UserNotification
import com.nullpointerexception.cityeyev1.firebase.FirebaseDatabase
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    var user: LiveData<User> = _user

    fun setUser(user: User) {
        _user.value = user
    }

    fun getUser(): MutableLiveData<User> {
        return _user
    }

    private val _notifications = MutableLiveData<List<UserNotification>>()
    var notifications: LiveData<List<UserNotification>> = _notifications

    fun setNotifications(user: List<UserNotification>) {
        _notifications.value = user
    }

    fun getNotifications(): MutableLiveData<List<UserNotification>> {
        return _notifications
    }

    fun getUserFromDatabase(uid: String) {
        viewModelScope.launch {
            val user = FirebaseDatabase.getUser(uid)
            if (user != null) {
                setUser(user)
            }
        }
    }

    fun getUserNotifications() {
        viewModelScope.launch {
            val notifications = getUser().value?.notifications?.let {
                FirebaseDatabase.getUserNotifications(
                    it
                )
            }
            notifications?.let { setNotifications(it) }
        }
    }

}