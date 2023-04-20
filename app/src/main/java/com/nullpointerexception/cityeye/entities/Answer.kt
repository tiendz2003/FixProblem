package com.nullpointerexception.cityeye.entities

data class Answer(
    var problemID: String? = null,
    val response: String? = null,
    val timeOfResponse: String? = null,
    val userID: String? = null,
    val read: Boolean? = null
)