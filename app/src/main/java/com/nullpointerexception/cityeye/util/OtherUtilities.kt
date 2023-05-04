package com.nullpointerexception.cityeye.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import com.nullpointerexception.cityeye.ProblemPreview
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date


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

    fun getTimeFromEpoch(epoch: Long): String {
        val date = Date(epoch * 1000)
        val dateFormat = SimpleDateFormat("HH:mm")
        return dateFormat.format(date)
    }

    fun getDateFromEpoch(epoch: Int): String {
        val date = Date(epoch.toLong() * 1000)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        return dateFormat.format(date)
    }

    fun startProblemPreviewActivity(
        image: File,
        act: Activity,
        latitude: Double,
        longitude: Double
    ) {
        val activity = Intent(act.applicationContext, ProblemPreview::class.java)
        activity.putExtra("image", image).putExtras(
            OtherUtilities().makeCoordinatesBundle(
                LatLng(latitude, longitude)
            )
        ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        act.startActivity(activity)
    }

    fun epochToFormattedString(epoch: Long): String {
        val date = Date(epoch * 1000L)

        val sdf = SimpleDateFormat("E, MMM dd · HH:mm")

        return sdf.format(date)
    }

}