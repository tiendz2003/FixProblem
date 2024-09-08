package com.nullpointerexception.cityeyev1.entities

import com.google.firebase.Timestamp

data class Comment(
    val userName: String? = null,
    val commentText: String? = null,
    val timestamp: Timestamp? = null,
    val userImage: String? = null
)
