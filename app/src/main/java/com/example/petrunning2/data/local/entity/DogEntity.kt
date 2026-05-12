package com.example.petrunning2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dog")
data class DogEntity(
    @PrimaryKey val id: Int = 0,
    val name: String = "Pix",
    val level: Int = 1,
    val currentXp: Int = 0,
    val maxXp: Int = 100,
    val credit: Int = 100,
)
