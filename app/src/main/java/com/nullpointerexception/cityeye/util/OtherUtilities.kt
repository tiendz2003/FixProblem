package com.nullpointerexception.cityeye.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.view.View

class OtherUtilities {

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(view: View): Location? {
        val locationManager =
            view.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

}