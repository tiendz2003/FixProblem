package com.nullpointerexception.cityeyev1.entities

import com.google.firebase.firestore.PropertyName

data class MapItem(
    val type: String? = null,
    val lat: Double? = null,
    @get:PropertyName("lng")
    @set:PropertyName("lng")
    var lng: Double? = null,
    val address: String? = null
)