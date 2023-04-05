package com.nullpointerexception.cityeye.entities

data class Problem(
    var uid: String? = null,
    val title: String? = null,
    val description: String? = null,
    val imageName: String? = null,
    val address: String? = null,
    val location_lat: String? = null,
    val location_lon: String? = null
) {
}
