package com.example.fixproblem.data.model.remote

data class Notification @JvmOverloads constructor(
    val id: String ="",
    val type: NotificationType = NotificationType.LIKE, // Thêm trường type
    val userImg: String="",
    val userName: String="",
    val message: String="",
    val timestamp: Long=System.currentTimeMillis(),
    val isRead: Boolean = false,
    val senderId: String ="",
    val receiverId: String="",
    val reportId: String="",
    val commentId: String="",
)
