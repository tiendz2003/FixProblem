package com.nullpointerexception.cityeye.util

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

object Database {

    fun addNormalProblem(context: Context, imageName:String, title:String, savedImageFile: File){

        val database = Firebase.firestore
        val storage = Firebase.storage
        val firebase = Firebase.auth

        val problem = hashMapOf(
            "uid" to firebase.uid,
            "image" to imageName,
            "title" to title
        )


        database.collection("problems").document("normalProblems")
            .set(problem).addOnSuccessListener {
                Toast.makeText(context, "Succesfully uploaded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(context, "Failed uploaded!", Toast.LENGTH_SHORT).show()
            }

        val imagesRef = storage.reference.child("images/${savedImageFile?.name}")
        val uploadTask = imagesRef.putFile(Uri.fromFile(savedImageFile))
        uploadTask.addOnSuccessListener {
            Toast.makeText(context,"Image uploaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context,"Error uploading image", Toast.LENGTH_SHORT).show()
            Log.e("ERROR", it.stackTraceToString())
        }

    }

}