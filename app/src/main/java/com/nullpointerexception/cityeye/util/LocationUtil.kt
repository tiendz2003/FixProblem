package com.nullpointerexception.cityeye.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.nullpointerexception.cityeye.R
import java.io.IOException
import java.util.*

object LocationUtil {

    fun getAddressFromCo(context: Context, latLng: LatLng): String? {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addressList: MutableList<Address>? =
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]

                return address.getAddressLine(0).toString()
            }
        } catch (e: IOException) {
            Toast.makeText(
                context,
                context.resources.getString(R.string.geocoderError),
                Toast.LENGTH_LONG
            ).show()
        }
        return null

    }


}