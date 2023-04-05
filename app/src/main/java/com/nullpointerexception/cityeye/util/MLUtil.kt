package com.nullpointerexception.cityeye.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.gson.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.ByteArrayOutputStream
import java.io.File


class MLUtil(val context: Context, image: File) {

    private var imageLabeler: ImageLabeler
    var bitmap: Bitmap

    init {
        val imageLabelerOptions = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.10f)
            .build()

        bitmap = BitmapFactory.decodeFile(image.absolutePath)

        imageLabeler = ImageLabeling.getClient(imageLabelerOptions)
        labelImage(bitmap)
    }

    private fun labelImage(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        imageLabeler.process(inputImage)
            .addOnSuccessListener {
                for (label in it) {
                    Log.i("LABEL", "${label.text} ${label.confidence} ${label.index}")
                }
            }
            .addOnFailureListener { e ->
                Log.i("LABEL", "Failed due to ${e.message}")
                Toast.makeText(context, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

}