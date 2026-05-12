package com.example.petrunning2.data.repository

import com.example.petrunning2.ui.decoration.ClothItem
import com.example.petrunning2.ui.decoration.ItemCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _items = MutableStateFlow<List<ClothItem>>(emptyList())
    val items: Flow<List<ClothItem>> = _items.asStateFlow()

    private val _characterSpriteUrl = MutableStateFlow<String?>(null)
    val characterSpriteUrl: Flow<String?> = _characterSpriteUrl.asStateFlow()

    suspend fun fetchCatalog() {
        try {
            val snapshot = firestore.collection("items").get().await()
            val catalogItems = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                val name = doc.getString("name") ?: return@mapNotNull null
                val price = doc.getLong("price")?.toInt() ?: 0
                val categoryStr = doc.getString("category") ?: return@mapNotNull null
                val imageUrl = doc.getString("imageUrl") ?: ""
                val isOverlay = doc.getBoolean("isOverlay") ?: true

                val category = when (categoryStr.lowercase()) {
                    "face" -> ItemCategory.FACE
                    "hair" -> ItemCategory.HAIR
                    "cloth" -> ItemCategory.CLOTH
                    "theme" -> ItemCategory.THEME
                    else -> return@mapNotNull null
                }

                ClothItem(
                    id = id,
                    drawableRes = 0, // 서버 아이템은 drawableRes 사용 안 함
                    price = price,
                    name = name,
                    category = category,
                    isOverlay = isOverlay,
                    imageUrl = imageUrl,
                )
            }
            _items.value = catalogItems
        } catch (e: Exception) {
            // 네트워크 실패 시 빈 리스트 유지 (오프라인 캐시는 Firestore가 자동 처리)
        }
    }

    suspend fun fetchCharacterSpriteUrl() {
        try {
            val ref = storage.reference.child("characters/pet_idle.png")
            val url = ref.downloadUrl.await().toString()
            _characterSpriteUrl.value = url
        } catch (e: Exception) {
            // 실패 시 null 유지 → 로컬 fallback 사용
        }
    }

    // Storage URL 가져오기 (아이템 이미지)
    suspend fun getImageUrl(path: String): String? {
        return try {
            storage.reference.child(path).downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }
}
