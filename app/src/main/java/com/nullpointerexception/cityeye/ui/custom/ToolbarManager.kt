package com.nullpointerexception.cityeye.ui.custom

import android.util.Log
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.auth.User
import com.nullpointerexception.cityeye.databinding.CustomToolbarBinding

class ToolbarManager(toolbar:CustomToolbarBinding, user:FirebaseUser) {

    init {
        toolbar.userImage.load(user.photoUrl){
            transformations(CircleCropTransformation())
        }
        toolbar.userImage.isClickable = true
    }

}