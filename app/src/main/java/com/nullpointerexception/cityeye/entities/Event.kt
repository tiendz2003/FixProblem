package com.nullpointerexception.cityeye.entities

import java.io.Serializable

data class Event(
    val title: String? = null,
    val description: String? = null,
    val city: String? = null,
    val location: String? = null,
    val epochStart: Long? = null,
    val epochEnd: Long? = null,
    val price: Int? = null,
    val imageUrl: String? = null
) : Serializable