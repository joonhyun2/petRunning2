package com.example.petrunning2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_record")
data class RunRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val distanceKm: Double,
    val elapsedSeconds: Long,
    val paceSecPerKm: Long,
    val xpGained: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val routePoints: String = "" // "lat,lng|lat,lng|..." 형태로 저장
)
