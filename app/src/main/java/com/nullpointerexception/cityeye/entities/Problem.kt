package com.nullpointerexception.cityeye.entities

data class Problem(var uid:String? = "", val image:String? = "", val title:String? = "", val location_lat:String? = "", val location_lon:String? = ""){
    override fun toString(): String {
        return "Problem(uid=$uid, image=$image, title=$title, latitude=$location_lat, longitude=$location_lon)"
    }
}
