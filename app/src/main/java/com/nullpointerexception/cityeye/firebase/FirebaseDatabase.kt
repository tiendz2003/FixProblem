package com.nullpointerexception.cityeye.firebase

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.entities.User
import com.nullpointerexception.cityeye.util.LocationUtil
import com.nullpointerexception.cityeye.util.NetworkUtil
import com.nullpointerexception.cityeye.util.OtherUtilities
import java.io.File

object FirebaseDatabase {


    fun addNormalProblem(
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

        val problem = Problem(
            firebase.uid,
            title,
            description,
            savedImageFile.name,
            address,
            location.latitude.toString(),
            location.longitude.toString()
        )


        if (NetworkUtil.isNetworkAvailable(context) && !isDuplicateProblem(
                context,
                savedImageFile.name
            )
        ) {
            val problemID = OtherUtilities().getRandomString(20)

            val imagesRef = storage.reference.child("images/${savedImageFile.name}")
            val problemRef = database.collection("problems").document(problemID)
            val userRef = database.collection("users").document(firebase.currentUser!!.uid)
            val uploadTask = imagesRef.putFile(Uri.fromFile(savedImageFile))

            uploadTask.addOnSuccessListener {

                database.runBatch { batch ->
                    batch.update(userRef, "problems", FieldValue.arrayUnion(problemID))

                    batch.set(problemRef, problem)
                }.addOnSuccessListener {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.problemUploaded),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.errorUploading),
                            Toast.LENGTH_SHORT
                        ).show()
                        removeImage(savedImageFile.name)
                    }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.errorUploading),
                    Toast.LENGTH_SHORT
                ).show()
                return@addOnFailureListener
            }
        } else {
            return false
        }
        return true
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

    fun addNewUser(context: Context, provider: String): Boolean {

        val loggedUser = Firebase.auth.currentUser
        val database = Firebase.firestore

        val newUser = User(
            loggedUser!!.uid,
            loggedUser.displayName,
            loggedUser.photoUrl.toString(),
            loggedUser.email,
            loggedUser.phoneNumber,
            provider
        )

        if (NetworkUtil.isNetworkAvailable(context)
        ) {
            database.collection("users").document(loggedUser.uid)
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
        } else {
            Toast.makeText(
                context,
                context.resources.getString(R.string.errorUploading),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
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


}