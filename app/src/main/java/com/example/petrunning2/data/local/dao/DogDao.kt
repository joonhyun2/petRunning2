package com.example.petrunning2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petrunning2.data.local.entity.DogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DogDao {
    @Query("SELECT * FROM dog WHERE id = 0")
    fun getDog(): Flow<DogEntity?>

    @Query("SELECT * FROM dog WHERE id = 0")
    suspend fun getDogOnce(): DogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDog(dog: DogEntity)
}
