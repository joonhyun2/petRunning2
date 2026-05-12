package com.example.petrunning2.data.repository

import com.example.petrunning2.data.local.dao.OwnedItemDao
import com.example.petrunning2.data.local.entity.OwnedItemEntity
import com.example.petrunning2.ui.decoration.ItemCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val ownedItemDao: OwnedItemDao
) {
    val ownedItemIds: Flow<List<Int>> = ownedItemDao.getOwnedItemIds()

    // 하위 호환용 (단일 장착)
    val equippedItemId: Flow<Int?> = ownedItemDao.getEquippedItemId()

    // 카테고리별 장착 아이템 ID 목록
    val equippedItemIds: Flow<List<Int>> = ownedItemDao.getEquippedItemIds()

    suspend fun purchaseItem(itemId: Int) {
        ownedItemDao.insertOwnedItem(OwnedItemEntity(itemId = itemId))
    }

    // 카테고리별 장착 (같은 카테고리 기존 아이템 해제 후 새 아이템 장착)
    suspend fun equipItem(itemId: Int, category: ItemCategory) {
        val categoryStr = category.name
        ownedItemDao.unequipCategory(categoryStr)
        ownedItemDao.equipItem(itemId, categoryStr)
    }

    // 특정 카테고리 해제
    suspend fun unequipCategory(category: ItemCategory) {
        ownedItemDao.unequipCategory(category.name)
    }

    suspend fun unequipAll() {
        ownedItemDao.unequipAll()
    }
}
