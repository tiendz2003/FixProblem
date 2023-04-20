package com.nullpointerexception.cityeye.entities

data class User(
    val uid: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val provider: String? = null,
    val fcmToken: String? = null,
    val problems: List<String>? = listOf(),
    val notifications: List<String>? = listOf()
)
