package com.nullpointerexception.cityeye.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraUtil {

    private var currentPhotoPath: String? = null
    private lateinit var storage: FirebaseStorage

    fun retrieveImage(context: Context) {
        val savedImageFile = currentPhotoPath?.let { File(it) }
        Database.addNormalProblem(context,  savedImageFile!!.name, "Some title", savedImageFile)
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