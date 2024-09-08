package com.nullpointerexception.cityeyev1.entities

import com.google.firebase.Timestamp


data class Problem(
    var uid: String? = null,
    var userName: String? = null,
    var problemID: String? = null,
    val title: String? = null,
    val description: String? = null,
    val comments: List<Comment>? = listOf(),
    val imageName: String? = null,
    val address: String? = null,
    val location_lat: String? = null,
    val location_lon: String? = null,
    val epoch: Int? = null,
    val answerID: String? = null,
    val solved: Boolean? = null,
    val timestamp: Timestamp? = null
) {

    override fun toString(): String {
        return "Problem(uid=$uid, title=$title, description=$description, imageName=$imageName, address=$address, location_lat=$location_lat, location_lon=$location_lon)"
    }
}
