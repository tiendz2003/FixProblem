package com.nullpointerexception.cityeyev1.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraUtil {

    private var currentPhotoPath: String? = null

    fun retrieveImage(): File? {
        Log.d("CameraUtil", "Current photo path: $currentPhotoPath")
        val file = currentPhotoPath?.let { File(it) }
        Log.d("CameraUtil", "File exists: ${file?.exists()}")
        return file?.takeIf { it.exists() }?.let { compressImageFile(it, 80) }
    }

    fun createImageFile(context: Context): File? {
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_${Firebase.auth.currentUser?.uid ?: "unknown"}"
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    private fun compressImageFile(file: File, quality: Int): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val matrix = Matrix()
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            val compressedFile = File(file.absolutePath)
            FileOutputStream(compressedFile).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}