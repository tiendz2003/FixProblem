package com.nullpointerexception.cityeye.entities

import com.google.firebase.firestore.PropertyName

data class UserNotification(
    val problemID: String? = null,
    val problemTitle: String? = null,
    val time: String? = null,
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean? = null,
    val uid: String? = null,
    @get:PropertyName("notificationID")
    @set:PropertyName("notificationID")
    var notificationID: String? = null
)