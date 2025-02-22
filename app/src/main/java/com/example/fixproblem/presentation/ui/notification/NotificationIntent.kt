package com.example.fixproblem.presentation.ui.notification

import com.example.fixproblem.data.model.remote.Notification

sealed class NotificationIntent {
    data object GetNotifications : NotificationIntent()
    data class MarkAsRead(val notificationId: String) : NotificationIntent()
    data class DeleteNotification(val notificationId: String) : NotificationIntent()
    data class OnNotificationClick(val notification: Notification) : NotificationIntent()
}
sealed class NotificationEffect{
    data class ShowToast(val message: String) : NotificationEffect()
    data class OnNavigateToPost(val postId: String) : NotificationEffect()
}