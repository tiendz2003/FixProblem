package com.example.fixproblem.fcm

import android.util.Log
import com.example.fixproblem.data.model.remote.Notification
import com.example.fixproblem.data.model.remote.NotificationType
import com.example.fixproblem.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService:FirebaseMessagingService() {
    @Inject lateinit var notificationManager:NotificationHelper
    @Inject lateinit var fcmManager:FCMManager
    override fun onNewToken(token: String) {
        Log.d("FCM_DEBUG", "New token generated: $token")
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                fcmManager.updateToken(token)
                Log.d("FCM_DEBUG", "Token update completed")
            } catch (e: Exception) {
                Log.e("FCM_DEBUG", "Error updating token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "Message received: ${message.data}")
        super.onMessageReceived(message)
        val notification = parseRemoteMessage(message)
        notification?.let {
            notificationManager.showNotification(it)
        }

    }
    private fun parseRemoteMessage(message: RemoteMessage):Notification? {
        return try {
            val data = message.data
            Notification(
                id = data["notificationId"] ?: return null,
                type = NotificationType.valueOf(data["type"] ?: return null),
                userImg = data["userImg"] ?: "",
                userName = data["userName"] ?: "",
                message = data["message"] ?: "",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                senderId = data["senderId"] ?: "",
                receiverId = data["receiverId"] ?: "",
                reportId = data["reportId"] ?: "",
                commentId = data["commentId"]?:return null
            )
        }catch (e:Exception){
            Log.e("FCM", "Error parsing message", e)
            null
        }
    }
}