package com.nullpointerexception.cityeyev1.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import cityeyev1.R
import com.google.android.gms.maps.model.LatLng
import com.nullpointerexception.cityeyev1.entities.SupportedCities
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object LocationUtil {

    suspend fun getAddressFromCo(context: Context, latLng: LatLng): String? =
        suspendCoroutine { continuation ->
            val geocoder = Geocoder(context, Locale.getDefault())

            try {
                val addressList: MutableList<Address>? =
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    continuation.resume(address.getAddressLine(0))
                } else {
                    continuation.resume(null)
                }
            } catch (e: IOException) {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.geocoderError),
                    Toast.LENGTH_LONG
                ).show()
                continuation.resume(null)
            }
        }


    suspend fun checkIfInSupportedCity(
        context: Context,
        latLng: LatLng,
        supportedCities: SupportedCities
    ): Boolean {
        val address = getAddressFromCo(context, latLng)
        var validCity = false

        for (city in supportedCities.cities!!) {
            if (address?.contains(city) == true) {
                validCity = true
                break
            }
        }
        return validCity
    }


}