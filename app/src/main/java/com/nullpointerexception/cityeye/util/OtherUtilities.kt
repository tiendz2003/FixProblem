package com.nullpointerexception.cityeye.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OtherUtilities {

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val randomString = (1..length)
            .map { allowedChars.random() }
            .joinToString("")
        val currentDateTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val hash = "$randomString$currentDateTime"
        return hash.toList().shuffled().joinToString("")
    }

    fun makeCoordinatesBundle(coordinates: LatLng): Bundle {
        val args = Bundle()
        args.putParcelable("coordinates", coordinates)
        return args
    }

}