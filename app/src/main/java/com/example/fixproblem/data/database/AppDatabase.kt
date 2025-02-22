package com.example.fixproblem.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.fixproblem.data.model.local.RemoteKey
import com.example.fixproblem.data.model.local.ReportEntity
import com.example.fixproblem.extension.GeoPointConverter

@Database(
    entities = [ReportEntity::class, RemoteKey::class],
    version = 1
)
@TypeConverters(GeoPointConverter::class)
abstract class AppDatabase :RoomDatabase() {
    abstract val reportDao : ReportDao
    abstract val remoteKeyDao : RemoteKeyDao
}