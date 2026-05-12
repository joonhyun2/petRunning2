package com.example.petrunning2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owned_items")
data class OwnedItemEntity(
    @PrimaryKey val itemId: Int,
    val equipped: Boolean = false,
    // 카테고리별 장착 지원: "FACE", "HAIR", "CLOTH", "THEME", null(미장착)
    val equippedCategory: String? = null,
)
