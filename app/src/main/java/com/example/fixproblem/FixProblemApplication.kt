package com.example.fixproblem

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.example.fixproblem.extension.Config
import com.google.firebase.FirebaseApp
import com.mapbox.common.MapboxOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.search.autocomplete.PlaceAutocomplete
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FixProblemApplication:Application() {
    private lateinit var imageLoader: ImageLoader
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        imageLoader = Config.createImageLoader(this)
        //Đặt làm default ImageLoader cho toàn bộ ứng dụng
        Coil.setImageLoader(imageLoader)
        //Setup NavigationSDK
        MapboxOptions.accessToken = getString(R.string.mapbox_access_token)
        val navigationOptions =  NavigationOptions.Builder(this)
            .build()
        //Khởi tạo instance cho mapBox navigation
        if(!MapboxNavigationApp.isSetup()){
            MapboxNavigationApp.setup(
                navigationOptions
            ).attachAllActivities(this)//Được cập nhật cho tất cả activity
        }

    }
}
