package com.example.fixproblem.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @AppDispatcher(DispatcherType.IO)
    @Provides
    fun provideDispatcher() = Dispatchers.IO
}
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDispatcher(val dispatcher: DispatcherType)

enum class DispatcherType {
    IO,Main
}