package com.example.fixproblem.presentation.ui.notification

import com.example.fixproblem.data.model.remote.Notification

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false
)