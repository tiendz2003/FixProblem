package com.example.fixproblem.fcm

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FCMManager @Inject constructor(
    private val firestore:FirebaseFirestore,
    private val firebaseAuth:FirebaseAuth
) {
    suspend fun updateToken(token:String){
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("Chưa đăng nhập")
            Log.d("FCM_DEBUG", "Updating token: $token for user: ${user.uid}")
            // Lấy reference tới document của user
            val userRef = firestore.collection("users").document(user.uid)

            // Kiểm tra xem document đã tồn tại chưa
            val userDoc = userRef.get().await()
            if(userDoc.exists()){
                    userRef
                    .update(
                        mapOf(
                            "fcmToken" to token,
                            "lastUpdateToken" to FieldValue.serverTimestamp()
                        )
                    )
                    .await()
                Log.d("FCM_DEBUG", "Token updated successfully")
            }else{
                // Nếu chưa tồn tại thì tạo mới
                userRef.set(
                    mapOf(
                        "userId" to user.uid,
                        "fcmToken" to token,
                        "lastUpdateToken" to FieldValue.serverTimestamp(),
                        "displayName" to (user.displayName ?: ""),
                        "email" to (user.email ?: ""),
                        "photoUrl" to (user.photoUrl?.toString() ?: ""),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Log.d("FCM_DEBUG", "Token updated successfully")
        }catch (e:FirebaseFirestoreException){
            Log.e("FCMManager",e.message.toString())
        }
    }
    suspend fun removeToken(){
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("Chưa đăng nhập")
            firestore.collection("users")
                .document(user.uid)
                .update(
                    mapOf(
                        "fcmToken" to FieldValue.delete(),
                        "lastUpdateToken" to FieldValue.serverTimestamp()
                    )
                )
                .await()
        }catch (e:FirebaseFirestoreException){
            Log.e("FCMManager",e.message.toString())
        }
    }
}