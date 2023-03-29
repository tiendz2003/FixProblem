package com.nullpointerexception.cityeye.util

import android.content.Context
import android.os.Environment
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.firebase.Database
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraUtil {

    private var currentPhotoPath: String? = null

    fun retrieveImage(view:View, location: LatLng) {
        val savedImageFile = currentPhotoPath?.let { File(it) }


        Database.addNormalProblem(view.context,  savedImageFile!!.name, "Some title", savedImageFile,
            location
        )
    }

    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(
            Date()
        )
        val imageFileName = "JPEG_" + timeStamp + "_" + Firebase.auth.currentUser!!.uid
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}