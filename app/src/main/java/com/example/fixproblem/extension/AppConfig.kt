package com.example.fixproblem.extension

import android.content.Context
import androidx.room.TypeConverter
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.firestore.GeoPoint

object Config {
    fun createImageLoader(context:Context):ImageLoader {
        return ImageLoader.Builder(context)
        //Cấu hình disk cache
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.25)// Sử dụng 25% dung lượng cache có sẵn
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)// Sử dụng 25% dung lượng bộ nhớ  có sẵn
                    .build()
            }
            .crossfade(true)// Animation khi load ảnh
            .crossfade(300)// // Thời gian animation (ms)
            .respectCacheHeaders(false)// Bỏ qua cache headers từ network
            .build()
    }
}
//Chuyển đổi GeoPoint sang String-> lưu dữ liệu dạng nguyên thủy
 class GeoPointConverter {
    @TypeConverter
    fun fromGeoPoint(geoPoint: GeoPoint): String {
        return "${geoPoint.latitude},${geoPoint.longitude}"
    }

    @TypeConverter
    fun toGeoPoint(data: String): GeoPoint {
        val parts = data.split(",")
        return GeoPoint(parts[0].toDouble(), parts[1].toDouble())
    }
}