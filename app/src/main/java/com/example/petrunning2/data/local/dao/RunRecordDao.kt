package com.example.petrunning2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petrunning2.data.local.entity.RunRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunRecordDao {
    @Query("SELECT * FROM run_record ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<RunRecordEntity>>

    @Query("SELECT * FROM run_record WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayRecords(startOfDay: Long): Flow<List<RunRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RunRecordEntity)
}
