package com.example.fixproblem.di

import android.content.Context
import androidx.room.Room
import com.example.fixproblem.data.database.AppDatabase
import com.example.fixproblem.data.repository.NotificationRepository
import com.example.fixproblem.utils.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.search.ApiType
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideFireStore():FirebaseStorage{
        return FirebaseStorage.getInstance()
    }

    @Provides
    fun provideFireBase():FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideFirebaseAuth():FirebaseAuth{
        return FirebaseAuth.getInstance()
    }
    @Provides
    fun provideContext(@ApplicationContext context:Context):Context{
        return context
    }
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context:Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    fun provideSearchEngine():SearchEngine{
        return SearchEngine.createSearchEngineWithBuiltInDataProviders(ApiType.GEOCODING,
            SearchEngineSettings()
        )
    }

    @Provides
    fun provideMapBoxRouteLine(@ApplicationContext context:Context): MapboxRouteLineViewOptions {
        return MapboxRouteLineViewOptions.Builder(context).build()
    }

    @Provides
    fun provideMapBoxNavigation(): MapboxNavigation {
        return MapboxNavigationApp.current()?:throw Exception("MapboxNavigation chưa được khởi tạo")
    }
    @Provides
    fun provideAppDatabase(@ApplicationContext context:Context):AppDatabase{
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        notificationRepository: NotificationRepository,
        firebaseAuth: FirebaseAuth
    ):NotificationHelper {
        return NotificationHelper(
            notificationRepository = notificationRepository,
            firebaseAuth =  firebaseAuth,
            context = context
        )
    }
}