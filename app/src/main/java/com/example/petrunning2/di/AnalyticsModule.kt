package com.example.petrunning2.di

import com.example.petrunning2.analytics.AnalyticsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsHelper(): AnalyticsHelper = AnalyticsHelper()
}
