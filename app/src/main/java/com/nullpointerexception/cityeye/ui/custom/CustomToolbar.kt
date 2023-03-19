package com.nullpointerexception.cityeye.ui.custom

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.CustomToolbarBinding

class CustomToolbar(context:Context, attrs:AttributeSet) : ConstraintLayout(context, attrs) {

    private var binding:CustomToolbarBinding

    init{
        inflate(context, R.layout.custom_toolbar,null)
        binding = CustomToolbarBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)

        val user = Firebase.auth.currentUser

        binding.userIcon.load(user?.photoUrl){
            transformations(CircleCropTransformation())
        }
    }

}