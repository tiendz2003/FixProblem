package com.example.fixproblem.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fixproblem.data.model.remote.Comment
import com.example.fixproblem.data.model.remote.Report
import com.google.firebase.firestore.GeoPoint

@Entity(
    tableName = "tblReport"
)
data class ReportEntity(
    @PrimaryKey
    val id: String = "", // Unique ID cho mỗi báo cáo
    val userName:String?="",
    val userImage:String="",
    val title: String = "",
    val description: String = "",
    val location: GeoPoint= GeoPoint(0.0, 0.0),
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "", // ID của người đăng báo cáo
    val status: String = "pending", // pending, reviewing, resolved
    val category: String = "", // Loại vấn đề môi trường
    val isLiked:Boolean = false,
    val like: Int = 0,
    val shares: Int = 0,
)
fun Report.toReportEntity(): ReportEntity {
    return ReportEntity(
        id = id,
        userName = userName,
        userImage = userImage,
        title = title,
        description = description,
        location = location,
        imageUrl = imageUrl,
        timestamp = timestamp,
        userId = userId,
        status = status,
        category = category,
        isLiked = isLiked,
        like = like,
        shares = shares,
    )
}
    fun ReportEntity.toReport(): Report {
        return Report(
            id = id,
            userName = userName,
            userImage = userImage,
            title = title,
            description = description,
            location = location,
            imageUrl = imageUrl,
            timestamp = timestamp,
            userId = userId,
            status = status,
            category = category,
            isLiked = isLiked,
            like = like,
            shares = shares,
        )
}