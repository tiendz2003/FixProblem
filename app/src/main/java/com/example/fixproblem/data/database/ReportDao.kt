package com.example.fixproblem.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.fixproblem.data.model.local.ReportEntity
import com.example.fixproblem.data.model.remote.Report

@Dao
interface ReportDao {
    @Upsert
    suspend fun upsertReport(report: List<ReportEntity>)

    @Query("SELECT * FROM tblReport")
    fun getAllReports(): PagingSource<Int,ReportEntity>

    @Query("DELETE FROM tblReport")
    suspend fun deleteAllReports()
}