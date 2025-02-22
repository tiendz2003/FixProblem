package com.example.fixproblem.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fixproblem.MainActivity
import com.example.fixproblem.R
import com.example.fixproblem.data.model.remote.Notification
import com.example.fixproblem.data.model.remote.NotificationType
import com.example.fixproblem.data.repository.NotificationRepository
import com.example.fixproblem.presentation.theme.CustomColor
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val firebaseAuth: FirebaseAuth,
    private val context:Context
){
    companion object {
        const val LIKE_CHANNEL_ID = "like_notifications"
        const val COMMENT_CHANNEL_ID = "comment_notifications"
        const val GROUP_LIKE = "group_like"
        const val GROUP_COMMENT = "group_comment"
    }
    init {
        createNotificationChannel()
    }
    private val notificationManager by lazy { NotificationManagerCompat.from(context) }
    private fun createNotificationChannel(){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
           NotificationChannel(
               LIKE_CHANNEL_ID,
               "Lượt thích",
               NotificationManager.IMPORTANCE_DEFAULT
           ).apply {
               description = "Thông báo khi có người thích bài viết"
               enableLights(true)
               lightColor = Color.BLUE
               enableVibration(true)
           },
           NotificationChannel(
               COMMENT_CHANNEL_ID,
               "Bình luan",
               NotificationManager.IMPORTANCE_DEFAULT
           ).apply {
               description = "Thông báo khi người dùng bình luân"
               enableLights(true)
               lightColor = Color.BLUE
               enableVibration(true)
           }
       )
        notificationManager.createNotificationChannels(channels)
    }
    @SuppressLint("MissingPermission")
    fun showNotification(notification:Notification){
        Log.d("FCM_DEBUG", "Token: ${firebaseAuth.currentUser?.uid}")
        Log.d("FCM_DEBUG", "Notification received: $notification")
        val (chanelId,groupId) = when(notification.type){
            NotificationType.LIKE -> LIKE_CHANNEL_ID to GROUP_LIKE
            NotificationType.COMMENT -> COMMENT_CHANNEL_ID to GROUP_COMMENT
            NotificationType.REPLY_COMMENT -> COMMENT_CHANNEL_ID to GROUP_COMMENT
        }
        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reportId",notification.reportId)
            putExtra("notification_type",notification.type)
            putExtra("notification_id",notification.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(context, chanelId)
            .setSmallIcon(
                when(notification.type){
                    NotificationType.LIKE -> R.drawable.comment
                    NotificationType.COMMENT -> R.drawable.comment
                    NotificationType.REPLY_COMMENT -> R.drawable.nav_icon
                }
            )
            .setContentTitle(
                when (notification.type) {
                    NotificationType.LIKE -> "Lượt thích mới"
                    NotificationType.COMMENT -> "Bình luận mới"
                    NotificationType.REPLY_COMMENT -> "Trả lời mới"
                }
            )
            .setContentText(notification.message)
            .setPriority(
                when (notification.type) {
                NotificationType.LIKE -> NotificationCompat.PRIORITY_DEFAULT
                else -> NotificationCompat.PRIORITY_HIGH
            }
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(groupId)

        notificationManager.notify(notification.id.hashCode(), notificationBuilder.build())
        showGroupSummary(groupId,chanelId)
    }

    @SuppressLint("MissingPermission")
    private fun showGroupSummary(groupId:String,channelId:String){
        val summaryNotification = NotificationCompat.Builder(context,channelId)
            .setSmallIcon(
                when(groupId){
                    GROUP_LIKE -> R.drawable.comment
                    else ->R.drawable.nav_icon
                }
            )
            .setGroup(groupId)
            .setGroupSummary(true)
            .build()

        notificationManager.notify(groupId.hashCode(),summaryNotification)

    }
    @SuppressLint("MissingPermission")
    suspend fun createNotification(
        type:NotificationType,
        receiverId: String,
        reportId: String,
        reportTitle:String,
        commentId: String? = null,
        commentContent: String?=null
    ){
        if(receiverId == firebaseAuth.currentUser?.uid) return

        val message = when(type){
            NotificationType.LIKE -> "${firebaseAuth.currentUser?.displayName} thích bài viết: $reportTitle"
            NotificationType.COMMENT -> "${firebaseAuth.currentUser?.displayName} đã bình luận về  bài viết: $reportTitle"
            NotificationType.REPLY_COMMENT -> "${firebaseAuth.currentUser?.displayName} đã trả lời bình luận của bạn: ${commentContent?.take(50)}"
        }
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            type = type,
            userImg = firebaseAuth.currentUser?.photoUrl.toString(),
            userName = firebaseAuth.currentUser?.displayName ?: "Người dùng",
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            senderId = firebaseAuth.currentUser?.uid ?: "",
            receiverId = receiverId,
            reportId = reportId,
            commentId = commentId?:""
        )
            notificationRepository.createNotification(notification)
    }


}