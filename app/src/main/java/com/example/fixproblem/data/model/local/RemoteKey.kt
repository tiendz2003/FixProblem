package com.example.fixproblem.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblRemote_keys")
data class RemoteKey(
    @PrimaryKey
    val id:String,
    val prevKey: String?,
    val nextKey: String?
)