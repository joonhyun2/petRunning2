package com.example.petrunning2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petrunning2.data.local.entity.OwnedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnedItemDao {
    @Query("SELECT itemId FROM owned_items")
    fun getOwnedItemIds(): Flow<List<Int>>

    // 하위 호환: 기존 단일 장착 아이템 (equipped = 1인 첫 번째)
    @Query("SELECT itemId FROM owned_items WHERE equipped = 1 LIMIT 1")
    fun getEquippedItemId(): Flow<Int?>

    // 카테고리별 장착 아이템 ID 목록 (FACE, HAIR, CLOTH, THEME 각 1개씩)
    @Query("SELECT itemId FROM owned_items WHERE equippedCategory IS NOT NULL")
    fun getEquippedItemIds(): Flow<List<Int>>

    // 특정 카테고리의 장착 아이템
    @Query("SELECT itemId FROM owned_items WHERE equippedCategory = :category LIMIT 1")
    fun getEquippedItemIdByCategory(category: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOwnedItem(item: OwnedItemEntity)

    // 전체 해제
    @Query("UPDATE owned_items SET equipped = 0, equippedCategory = NULL")
    suspend fun unequipAll()

    // 특정 카테고리 해제 후 새 아이템 장착
    @Query("UPDATE owned_items SET equipped = 0, equippedCategory = NULL WHERE equippedCategory = :category")
    suspend fun unequipCategory(category: String)

    @Query("UPDATE owned_items SET equipped = 1, equippedCategory = :category WHERE itemId = :itemId")
    suspend fun equipItem(itemId: Int, category: String)
}
