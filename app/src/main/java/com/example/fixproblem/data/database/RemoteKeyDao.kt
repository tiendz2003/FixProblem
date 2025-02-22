package com.example.fixproblem.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fixproblem.data.model.local.RemoteKey

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKey>)
    @Query("SELECT * FROM tblRemote_keys WHERE id = :id" )
    suspend fun getRemoteKey(id: String): RemoteKey?
    @Query("DELETE FROM tblRemote_keys")
    suspend fun deleteAllRemoteKeys()
}