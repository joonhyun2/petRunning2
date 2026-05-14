package com.example.petrunning2.data.repository

import com.example.petrunning2.data.local.dao.CatalogItemDao
import com.example.petrunning2.data.local.entity.CatalogItemEntity
import com.example.petrunning2.ui.decoration.ClothItem
import com.example.petrunning2.ui.decoration.ItemCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    private val catalogItemDao: CatalogItemDao,
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _characterSpriteUrl = MutableStateFlow<String?>(null)
    val characterSpriteUrl: Flow<String?> = _characterSpriteUrl.asStateFlow()

    // Room DB에서 즉시 제공 (앱 시작 시 끊김 없음)
    val items: Flow<List<ClothItem>> = catalogItemDao.getAll().map { entities ->
        entities.mapNotNull { it.toClothItem() }
    }

    suspend fun fetchCatalog(localItemIds: Set<Int>) {
        try {
            val snapshot = firestore.collection("items").get().await()
            val cachedItems = catalogItemDao.getAllSync().associateBy { it.id }

            val fetchedEntities = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                if (id in localItemIds) return@mapNotNull null  // CLOTHES_CATALOG 아이템은 스킵

                val name = doc.getString("name") ?: return@mapNotNull null
                val nameEn = doc.getString("nameEn") ?: ""
                val price = doc.getLong("price")?.toInt() ?: 0
                val categoryStr = doc.getString("category") ?: return@mapNotNull null
                val imagePath = doc.getString("imageUrl") ?: ""
                val isOverlay = doc.getBoolean("isOverlay") ?: true

                val validCategory = when (categoryStr.lowercase()) {
                    "face", "hair", "cloth", "theme" -> categoryStr.lowercase()
                    else -> return@mapNotNull null
                }

                val imageUrl = when {
                    cachedItems[id]?.imageUrl?.isNotBlank() == true -> cachedItems[id]!!.imageUrl
                    imagePath.startsWith("https://") || imagePath.startsWith("http://") -> imagePath
                    imagePath.isNotBlank() -> getImageUrl(imagePath) ?: ""
                    else -> ""
                }

                CatalogItemEntity(id, name, nameEn, price, validCategory, imageUrl, isOverlay)
            }

            val changed = fetchedEntities.filter { it != cachedItems[it.id] }
            if (changed.isNotEmpty()) {
                catalogItemDao.upsertAll(changed)
            }
        } catch (_: Exception) {}
    }

    suspend fun fetchCharacterSpriteUrl() {
        try {
            val ref = storage.reference.child("characters/pet_idle.png")
            _characterSpriteUrl.value = ref.downloadUrl.await().toString()
        } catch (_: Exception) {}
    }

    private suspend fun getImageUrl(path: String): String? {
        return try {
            storage.reference.child(path).downloadUrl.await().toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun CatalogItemEntity.toClothItem(): ClothItem? {
        val cat = when (category) {
            "face" -> ItemCategory.FACE
            "hair" -> ItemCategory.HAIR
            "cloth" -> ItemCategory.CLOTH
            "theme" -> ItemCategory.THEME
            else -> return null
        }
        return ClothItem(
            id = id,
            drawableRes = 0,
            price = price,
            name = name,
            nameEn = nameEn,
            category = cat,
            isOverlay = isOverlay,
            imageUrl = imageUrl,
        )
    }
}
