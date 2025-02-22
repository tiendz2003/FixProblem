package com.example.fixproblem.presentation.ui.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixproblem.data.model.remote.Notification
import com.example.fixproblem.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val auth:FirebaseAuth
):ViewModel() {
    private val _uiState= MutableStateFlow(NotificationState())
    val uiState get() = _uiState.asStateFlow()
    private val _effect = Channel<NotificationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    init {
        getNotifications()
    }
     fun processIntent(intent:NotificationIntent){
        when(intent){
            is NotificationIntent.DeleteNotification -> deleteNotification(intent.notificationId)
            is NotificationIntent.GetNotifications -> getNotifications()
            is NotificationIntent.MarkAsRead -> markAsRead(intent.notificationId)
            is NotificationIntent.OnNotificationClick -> handleNotificationClick(intent.notification)
        }
    }
    fun updateState(update:(NotificationState)->NotificationState){
        _uiState.update(update)
    }
    private fun getNotifications(){
        viewModelScope.launch {
         try {
             _uiState.update { it.copy(isLoading = true) }
             auth.currentUser?.uid?.let { userId->
                notificationRepository.getNotifications(userId)
                    .catch { error->
                        Log.d("NotificationViewModel", "getNotifications: ${error.message}")
                        _uiState.update { it.copy(isLoading = false,error = error.message) }
                    }.collect{notifications->
                        Log.d("NotificationViewModel", "getNotifications: ${notifications.size}")
                        _uiState.update { it.copy(isLoading = false,notifications = notifications) }
                    }
             }
         }catch (e:Exception){
            _uiState.update { it.copy(isLoading = false,error = e.message) }
             _effect.send(NotificationEffect.ShowToast("Lỗi khi tải thông báo :(("))
         }
        }
    }
    private fun markAsRead(notificationId:String){
        viewModelScope.launch {
            try {
                //Khi bấm chuyển sang isRead sang true
                notificationRepository.markAsRead(notificationId)
                _effect.send(NotificationEffect.ShowToast("Đã đọc"))
            }catch (e:Exception){
                _effect.send(NotificationEffect.ShowToast("Lỗi :(("))
            }
        }
    }
    private fun deleteNotification(notificationId: String){
        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(notificationId)
                _effect.send(NotificationEffect.ShowToast("Đã xóa thông báo"))
            }catch (e:Exception){
                _effect.send(NotificationEffect.ShowToast("Lỗi :(("))
            }
        }
    }
    private fun handleNotificationClick(notification: Notification){
        viewModelScope.launch {
            try{
                markAsRead(notification.id)
                _effect.send(NotificationEffect.OnNavigateToPost(notification.id))
            }catch (e:Exception){
                _effect.send(NotificationEffect.ShowToast("Có lỗi xảy ra:${e.message}"))
            }
        }
    }
}