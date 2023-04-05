package com.nullpointerexception.cityeye.entities

data class Problem(
    var uid: String? = null,
    val title: String? = null,
    val description: String? = null,
    val imageName: String? = null,
    val address: String? = null,
    val location_lat: String? = null,
    val location_lon: String? = null,
    val epoch: Int? = null
) {

    override fun toString(): String {
        return "Problem(uid=$uid, title=$title, description=$description, imageName=$imageName, address=$address, location_lat=$location_lat, location_lon=$location_lon)"
    }
}
