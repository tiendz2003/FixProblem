package com.nullpointerexception.cityeye.firebase

import android.content.ContentValues.TAG
import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.data.ListViewModel
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.util.OtherUtilities
import java.io.File

object Database {

    fun addNormalProblem(context: Context, imageName:String, title:String, savedImageFile: File, location:Location){

        val database = Firebase.firestore
        val storage = Firebase.storage
        val firebase = Firebase.auth

        val problem = hashMapOf(
            "uid" to firebase.uid,
            "image" to imageName,
            "title" to title,
            "location_lat" to location.latitude.toString(),
            "location_lon" to location.longitude.toString()
        )


        database.collection("problems").document(OtherUtilities().getRandomString(15))
            .set(problem).addOnSuccessListener {
                Toast.makeText(context, "Succesfully uploaded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(context, "Failed uploaded!", Toast.LENGTH_SHORT).show()
            }

        val imagesRef = storage.reference.child("images/${savedImageFile.name}")
        val uploadTask = imagesRef.putFile(Uri.fromFile(savedImageFile))
        uploadTask.addOnSuccessListener {
            Toast.makeText(context,"Image uploaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context,"Error uploading image", Toast.LENGTH_SHORT).show()
            Log.e("ERROR", it.stackTraceToString())
        }

    }
}