package com.nullpointerexception.cityeye.ui.custom

import android.app.Activity
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseUser
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.CustomToolbarBinding
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