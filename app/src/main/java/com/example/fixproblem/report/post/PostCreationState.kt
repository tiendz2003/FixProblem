package com.example.fixproblem.report.post

import android.net.Uri
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point

data class PostCreationState(
    val caption:String ="",
    val imageUri: Uri? = null,
    val location: GeoPoint? = null,
    val address:String= "",
    val isLoading:Boolean = false,
    val error:String?=null,
    val isSuccess:Boolean = false,
    val uploadProgress: Float = 0f
) {
}