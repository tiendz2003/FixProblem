package com.nullpointerexception.cityeye.ui.custom

import android.content.Intent
import android.widget.PopupMenu
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.LoginActivity
import com.nullpointerexception.cityeye.MainActivity
import com.nullpointerexception.cityeye.NotificationsActivity
import com.nullpointerexception.cityeye.ProfileActivity
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.CustomToolbarBinding

class ToolbarManager(
    val toolbar: CustomToolbarBinding,
    user: FirebaseUser,
    val activity: MainActivity
) {

    val listPopupWindow = PopupMenu(activity.applicationContext, toolbar.userImage)

    init {
        if (user.photoUrl.toString() != "null") {
            Glide.with(activity.applicationContext).load(user.photoUrl).transform(CircleCrop())
                .into(toolbar.userImage)
        }


        toolbar.userImage.isClickable = true

        listPopupWindow.inflate(R.menu.profile_dropdown_menu)
        listPopupWindow.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    startProfileActivity()
                    true
                }
                R.id.sign_out -> {
                    logOut()
                    true
                }

                else -> {
                    true
                }
            }
        }

        toolbar.userImage.setOnClickListener {
            listPopupWindow.show()
        }

        toolbar.notificationsIcon.setOnClickListener {
            startNotificationsActivity()
        }
    }

    fun startProfileActivity() {
        val intent = Intent(activity, ProfileActivity::class.java)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            toolbar.userImage,
            "userImage"
        )
        activity.startActivity(intent, options.toBundle())
    }

    fun startNotificationsActivity() {
        activity.startActivity(Intent(activity, NotificationsActivity::class.java))
    }


    fun logOut() {
        Firebase.auth.signOut()
        activity.startActivity(Intent(activity.applicationContext, LoginActivity::class.java))
        activity.finish()
    }

}