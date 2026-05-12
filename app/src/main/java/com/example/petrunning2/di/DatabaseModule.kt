package com.example.petrunning2.di

import android.content.Context
import androidx.room.Room
import com.example.petrunning2.data.local.AppDatabase
import com.example.petrunning2.data.local.dao.DogDao
import com.example.petrunning2.data.local.dao.OwnedItemDao
import com.example.petrunning2.data.local.dao.RunRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "petrunning.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDogDao(db: AppDatabase): DogDao = db.dogDao()

    @Provides
    fun provideRunRecordDao(db: AppDatabase): RunRecordDao = db.runRecordDao()

    @Provides
    fun provideOwnedItemDao(db: AppDatabase): OwnedItemDao = db.ownedItemDao()
}
