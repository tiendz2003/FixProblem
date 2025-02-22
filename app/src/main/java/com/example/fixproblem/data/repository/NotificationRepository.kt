package com.example.fixproblem.data.repository

import com.example.fixproblem.data.model.remote.Notification
import com.example.fixproblem.di.AppDispatcher
import com.example.fixproblem.di.DispatcherType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    val firestore: FirebaseFirestore,
    @AppDispatcher(DispatcherType.IO) val ioDispatcher:CoroutineDispatcher
) {
    private val notificationRefs = firestore.collection("notifications")
    fun getNotifications(userId:String) = callbackFlow{
        //Truy vấn phức hợp cần tạo index trong firestore
        val listener = notificationRefs.whereEqualTo("receiverId",userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener{snapshot,error->
                if(error != null){
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.toObjects(Notification::class.java)?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }.retry(3) { e->
        //Khi lỗi kết nối thử lại
        e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.UNAVAILABLE
    }.flowOn(ioDispatcher)

    suspend fun markAsRead(notificationId:String){
        notificationRefs.document(notificationId).update("read",true).await()
    }
    suspend fun deleteNotification(notificationId: String){
        notificationRefs.document(notificationId).delete().await()
    }
    suspend fun createNotification(notification: Notification){
        notificationRefs.document().set(notification).await()
    }
}