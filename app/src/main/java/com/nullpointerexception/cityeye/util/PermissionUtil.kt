package com.nullpointerexception.cityeye.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object PermissionUtils {

    fun requestPermission(context: Context, permissions: Array<String>, callback: (Boolean) -> Unit) {
        val permissionResults = arrayListOf<Boolean>()
        for (permission in permissions) {
            permissionResults.add(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
        }
        if (!permissionResults.contains(false)) {
            callback(true)
            return
        }
        ActivityCompat.requestPermissions(context as Activity, permissions, 100)
    }

}