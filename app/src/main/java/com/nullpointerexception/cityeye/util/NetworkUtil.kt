package com.nullpointerexception.cityeye.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast
import com.nullpointerexception.cityeye.R


object NetworkUtil {

    fun isNetworkAvailable(context: Context): Boolean {
        val conManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = conManager.activeNetworkInfo

        val result = activeNetworkInfo != null && activeNetworkInfo.isConnected

        if (!result) {
            Toast.makeText(
                context,
                context.resources.getString(R.string.noInternet),
                Toast.LENGTH_SHORT
            ).show()
        }

        return result
    }

}