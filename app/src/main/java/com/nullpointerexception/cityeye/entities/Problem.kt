package com.nullpointerexception.cityeye.entities

data class Problem(var uid:String? = "", val image:String? = "", val title:String? = ""){
    override fun toString(): String {
        return "Problem(uid='$uid', image='$image', title='$title')"
    }
}
