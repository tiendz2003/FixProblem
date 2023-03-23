package com.nullpointerexception.cityeye.ui.custom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.MainActivity
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.CustomToolbarBinding
import com.nullpointerexception.cityeye.ui.LoginActivity
import com.nullpointerexception.cityeye.util.SessionUtil

class ToolbarManager(toolbar:CustomToolbarBinding, user:FirebaseUser, activity: Activity) {

    val listPopupWindow = PopupMenu(activity.applicationContext, toolbar.userImage)

    init {
        Glide.with(activity.applicationContext).load(user.photoUrl).transform(CircleCrop()).into(toolbar.userImage)
        toolbar.userImage.isClickable = true

        listPopupWindow.inflate(R.menu.profile_dropdown_menu)
        listPopupWindow.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.profile -> {true}
                R.id.sign_out -> {
                    SessionUtil().logOut(activity)
                    true
                }
                else -> {true}
            }
        }

        toolbar.userImage.setOnClickListener {
            listPopupWindow.show()
        }


    }




}