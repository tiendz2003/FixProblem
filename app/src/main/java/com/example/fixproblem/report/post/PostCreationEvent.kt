package com.example.fixproblem.report.post

import android.net.Uri
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point

sealed class PostCreationEvent {
    data class UpdateCaption(val caption: String): PostCreationEvent()
    data class UpdateImage(val imgUri: Uri): PostCreationEvent()
    data class UpdateLocation(val location: GeoPoint): PostCreationEvent()
    data object SubmitPost: PostCreationEvent()
}