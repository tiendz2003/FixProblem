package com.example.fixproblem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ImageCompressor @Inject constructor(
    private val context:Context
) {
    suspend fun compressImage(
        imageUri:Uri,
        maxWidth:Int = 1080,
        maxHeight:Int = 1080,
        quality:Int = 80
    ):ByteArray{
        return withContext(Dispatchers.Default) {
            //Đọc orientation của ảnh
            val orientation  = context.contentResolver.openInputStream(imageUri)?.use {input->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }?:ExifInterface.ORIENTATION_NORMAL
            //Đọc kích thước ảnh gốc
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true// Chỉ đọc kích thước ảnh mà không tải ảnh vào bộ nhớ
            }
            context.contentResolver.openInputStream(imageUri)?.use {input->
                BitmapFactory.decodeStream(input, null, options)
            }
            //Tính toán tỉ lệ scale
            val scale = calculateScale(
                originalWidth = options.outWidth,
                originalHeight = options.outHeight,
                maxWidth = maxWidth,
                maxHeight = maxHeight
            )
            // Đọc và resize bitmap
            options.apply {
                inJustDecodeBounds = false//lần này sẽ lưu ảnh vào bộ nhớ
                inSampleSize = scale//giảm kích thước ảnh theo scale
            }

            val bitmap = context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: throw IllegalStateException("Không thể đọc ảnh")
            // Xoay ảnh nếu cần
            val rotatedBitmap = rotateBitmap(bitmap, orientation)
            // Nén ảnh thành ByteArray
            ByteArrayOutputStream().use { output ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)//nén ảnh thành jpeg
                rotatedBitmap.recycle()//
                output.toByteArray()
            }
    }
}
    private fun calculateScale(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ):Int{
        var scale = 1
        while (originalWidth / (scale * 2) >= maxWidth &&
            originalHeight / (scale * 2) >= maxHeight) {
            scale *= 2
        }
        return scale
    }
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }
}