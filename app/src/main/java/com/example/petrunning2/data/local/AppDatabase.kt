package com.example.petrunning2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.petrunning2.data.local.dao.DogDao
import com.example.petrunning2.data.local.dao.OwnedItemDao
import com.example.petrunning2.data.local.dao.RunRecordDao
import com.example.petrunning2.data.local.entity.DogEntity
import com.example.petrunning2.data.local.entity.OwnedItemEntity
import com.example.petrunning2.data.local.entity.RunRecordEntity

@Database(
    entities = [DogEntity::class, RunRecordEntity::class, OwnedItemEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dogDao(): DogDao
    abstract fun runRecordDao(): RunRecordDao
    abstract fun ownedItemDao(): OwnedItemDao
}
