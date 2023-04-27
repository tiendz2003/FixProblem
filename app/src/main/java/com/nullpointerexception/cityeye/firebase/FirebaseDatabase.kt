package com.nullpointerexception.cityeye.firebase

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.entities.Answer
import com.nullpointerexception.cityeye.entities.Comment
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.entities.SupportedCities
import com.nullpointerexception.cityeye.entities.User
import com.nullpointerexception.cityeye.entities.UserNotification
import com.nullpointerexception.cityeye.util.NetworkUtil
import com.nullpointerexception.cityeye.util.OtherUtilities
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirebaseDatabase {


    suspend fun addNormalProblem(
        context: Context,
        title: String,
        description: String,
        savedImageFile: File,
        location: LatLng,
        address: String
    ): Boolean {

        val database = Firebase.firestore

        val storage = Firebase.storage
        val firebase = Firebase.auth

        val problemID = OtherUtilities().getRandomString(20)

        val problem = Problem(
            firebase.uid,
            firebase.currentUser!!.displayName,
            problemID,
            title.trim(),
            description.trim(),
            null,
            savedImageFile.name,
            address,
            location.latitude.toString(),
            location.longitude.toString(),
            (System.currentTimeMillis() / 1000).toInt(),
            null,
            false
        )


        if (NetworkUtil.isNetworkAvailable(context) && !isDuplicateProblem(
                context,
                savedImageFile.name
            )
        ) {

            val imagesRef = storage.reference.child("images/${savedImageFile.name}")
            val problemRef = database.collection("problems").document(problemID)
            val userRef = database.collection("users").document(firebase.currentUser!!.uid)

            val uploadTask = suspendCoroutine<Boolean> { continuation ->
                imagesRef.putFile(Uri.fromFile(savedImageFile))
                    .addOnSuccessListener {
                        continuation.resume(true)
                    }
                    .addOnFailureListener {
                        continuation.resume(false)
                    }
            }

            if (uploadTask) {
                val batchResult = suspendCoroutine<Boolean> { continuation ->
                    database.runBatch { batch ->
                        batch.update(userRef, "problems", FieldValue.arrayUnion(problemID))
                        batch.set(problemRef, problem)
                    }.addOnSuccessListener {
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.problemUploaded),
                            Toast.LENGTH_SHORT
                        ).show()
                        continuation.resume(true)
                    }.addOnFailureListener {
                        continuation.resume(false)
                    }
                }

                return if (batchResult) {
                    true
                } else {
                    // Batch write failed
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.errorUploading),
                        Toast.LENGTH_SHORT
                    ).show()
                    removeImage(savedImageFile.name)
                    false
                }
            } else {
                // Image upload failed
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.errorUploading),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        } else {
            return false
        }
    }


    private fun isDuplicateProblem(context: Context, imageName: String): Boolean {
        val database = Firebase.firestore
        val colRef = database.collection("problems")

        var isDuplicate = false

        colRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val problem = document.toObject(Problem::class.java)

                    if (problem.imageName == imageName) {
                        isDuplicate = true
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.problemExists),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        return isDuplicate
    }

    suspend fun isDuplicateUser(uid: String): Boolean = suspendCoroutine { continuation ->
        val database = Firebase.firestore
        val colRef = database.collection("users").document(uid)

        colRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    continuation.resume(true)
                } else {
                    continuation.resume(false)
                }
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    fun addNewUser(context: Context, provider: String): Boolean {

        val loggedUser = Firebase.auth.currentUser
        val database = Firebase.firestore

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result
            val newUser = User(
                loggedUser?.uid,
                loggedUser?.displayName,
                loggedUser?.photoUrl.toString(),
                loggedUser?.email,
                loggedUser?.phoneNumber,
                provider,
                token,
                listOf(),
                listOf()
            )

            if (NetworkUtil.isNetworkAvailable(context)) {
                loggedUser?.let {
                    database.collection("users").document(it.uid)
                        .set(newUser).addOnSuccessListener {
                            Toast.makeText(
                                context,
                                context.resources.getString(R.string.welcome),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                context.resources.getString(R.string.errorLogging),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@addOnFailureListener
                        }
                }
            } else {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.errorUploading),
                    Toast.LENGTH_SHORT
                ).show()
                return@OnCompleteListener
            }
        })


        return true
    }

    fun updateFCMToken(context: Context) {

        val database = Firebase.firestore
        val loggedUser = Firebase.auth.currentUser

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result

            if (NetworkUtil.isNetworkAvailable(context)) {
                loggedUser?.let {
                    database.collection("users").document(it.uid)
                        .update("fcmToken", token).addOnSuccessListener {
                        }
                        .addOnFailureListener {
                        }
                }
            }
        })

    }

    fun removeImage(imageName: String): Boolean {
        val storageRef = Firebase.storage.reference

        val desertRef = storageRef.child("images/${imageName}")

        var isDeleted = false

        desertRef.delete().addOnSuccessListener {
            isDeleted = true
        }.addOnFailureListener {

        }

        return isDeleted
    }


    suspend fun getUser(userID: String): User? = suspendCoroutine { continuation ->
        val docRef = Firebase.firestore.collection("users").document(userID)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    continuation.resume(document.toObject(User::class.java))
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    suspend fun getProblemById(id: String): Problem? =
        suspendCoroutine { continuation ->
            val docRef = Firebase.firestore.collection("problems").whereEqualTo("problemID", id)
            docRef.get()
                .addOnSuccessListener { document ->
                    val documents = document.documents
                    for (document in documents) {
                        val problem = document.toObject(Problem::class.java)
                        if (problem != null) {
                            continuation.resume(problem)
                        }
                    }

                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        }

    suspend fun getUserProblems(problems: List<String>): List<Problem> =
        suspendCoroutine { continuation ->
            if (problems.isNotEmpty()) {
                val colRef = Firebase.firestore.collection("problems")
                colRef.whereIn(FieldPath.documentId(), problems)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val documents = querySnapshot.documents
                        val problemList = mutableListOf<Problem>()
                        for (document in documents) {
                            val problem = document.toObject(Problem::class.java)
                            if (problem != null) {
                                problemList.add(problem)
                            }
                        }
                        continuation.resume(problemList)
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                    }
            }
        }

    suspend fun getUserNotifications(notifications: List<String>): List<UserNotification> =
        suspendCoroutine { continuation ->
            if (notifications.isNotEmpty()) {
                val colRef = Firebase.firestore.collection("userNotifications")
                colRef.whereIn(FieldPath.documentId(), notifications)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val documents = querySnapshot.documents
                        val notificationsList = mutableListOf<UserNotification>()
                        for (document in documents) {
                            val notification = document.toObject(UserNotification::class.java)
                            if (notification != null) {
                                notificationsList.add(notification)
                            }
                        }
                        continuation.resume(notificationsList)
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                        continuation.resumeWithException(exception)
                    }
            } else {
                continuation.resume(emptyList())
            }
        }


    suspend fun getAllProblems(): List<Problem> =
        suspendCoroutine { continuation ->
            val colRef = Firebase.firestore.collection("problems")
            colRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val documents = querySnapshot.documents
                    val problemList = mutableListOf<Problem>()
                    for (document in documents) {
                        val problem = document.toObject(Problem::class.java)
                        if (problem != null) {
                            problemList.add(problem)
                        }
                    }
                    continuation.resume(problemList)
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }

        }

    suspend fun getAllAnswersForUser(userID: String): List<Answer> =
        suspendCoroutine { continuation ->
            val colRef = Firebase.firestore.collection("answers").whereEqualTo("userID", userID)
            colRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val documents = querySnapshot.documents
                    val answersList = mutableListOf<Answer>()
                    for (document in documents) {
                        val answer = document.toObject(Answer::class.java)
                        if (answer != null) {
                            answersList.add(answer)
                        }
                    }
                    continuation.resume(answersList)
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }

        }

    fun markNotificationAsRead(notificationID: String) {
        val database = Firebase.firestore
        database.collection("userNotifications").document(notificationID)
            .update("isRead", true).addOnSuccessListener {
            }
            .addOnFailureListener {
                Log.i("FAILURE", it.toString())
            }
    }

    suspend fun getSupportedCities() = suspendCoroutine {
        val database = Firebase.firestore
        val colRef = database.collection("supportedCities").document("cities")
        colRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    it.resume(document.toObject(SupportedCities::class.java))
                } else {
                    it.resume(null)
                }
            }
            .addOnFailureListener { e ->
                it.resumeWithException(e)
            }
    }

    @SuppressLint("SuspiciousIndentation")
    suspend fun addComment(text: String, problemID: String, user: User) = suspendCoroutine<Unit> {
        val database = Firebase.firestore
        val colRef = database.collection("problems").document(problemID).collection("comments")
        val comment =
            Comment(
                user.displayName,
                text,
                Timestamp.now(),
                user.photoUrl
            )
        colRef.add(comment).addOnSuccessListener {

        }
            .addOnFailureListener {

            }
    }


}