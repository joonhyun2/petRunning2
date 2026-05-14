package com.example.petrunning2.ui.decoration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.R
import com.example.petrunning2.analytics.AnalyticsHelper
import com.example.petrunning2.data.repository.CatalogRepository
import com.example.petrunning2.data.repository.DogRepository
import com.example.petrunning2.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClothItem(
    val id: Int,
    val drawableRes: Int,
    val price: Int,
    val name: String,
    val nameEn: String = "",
    val category: ItemCategory = ItemCategory.HAIR,
    val isOverlay: Boolean = false,
    val imageUrl: String = "", // Firebase Storage URL (서버 아이템용)
)

fun ClothItem.localizedName(): String =
    if (Locale.getDefault().language == "ko") name else nameEn.ifBlank { name }

enum class ItemCategory { FACE, HAIR, CLOTH, THEME }

internal val CLOTHES_CATALOG = listOf(
    // 얼굴 — 캐릭터에 오버레이
    ClothItem(id = 100, drawableRes = R.drawable.face_1, price = 50, name = "돼지코", nameEn = "Pig Nose", category = ItemCategory.FACE, isOverlay = true),
    // 헤어 — ribbon만 단독 소품, 나머지는 캐릭터 오버레이
    ClothItem(id = 1,   drawableRes = R.drawable.ribbon, price = 55, name = "리본",   nameEn = "Ribbon",   category = ItemCategory.HAIR, isOverlay = false),
    ClothItem(id = 201, drawableRes = R.drawable.hair_1, price = 60, name = "고양이 귀", nameEn = "Cat Ears", category = ItemCategory.HAIR, isOverlay = true),
    ClothItem(id = 202, drawableRes = R.drawable.hair_2, price = 75, name = "하트",   nameEn = "Heart",    category = ItemCategory.HAIR, isOverlay = true),
    ClothItem(id = 203, drawableRes = R.drawable.hair_3, price = 85, name = "김",     nameEn = "Seaweed",  category = ItemCategory.HAIR, isOverlay = true),
    // 옷 — 눈 아래 개별 배치
    ClothItem(id = 300, drawableRes = R.drawable.cloth_1, price = 110, name = "넥타이", nameEn = "Necktie", category = ItemCategory.CLOTH, isOverlay = false),
)

@HiltViewModel
class DecorationViewModel @Inject constructor(
    private val dogRepository: DogRepository,
    private val itemRepository: ItemRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val localItemIds = CLOTHES_CATALOG.map { it.id }.toSet()

    init {
        viewModelScope.launch { catalogRepository.fetchCatalog(localItemIds) }
        viewModelScope.launch { catalogRepository.fetchCharacterSpriteUrl() }
    }

    val characterSpriteUrl: StateFlow<String?> = catalogRepository.characterSpriteUrl
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    // CLOTHES_CATALOG(즉시) + Firestore 신규 아이템(Room DB 캐시)
    val catalog: StateFlow<List<ClothItem>> = catalogRepository.items
        .map { dbItems ->
            val newItems = dbItems.filter { it.id !in localItemIds }
            CLOTHES_CATALOG + newItems
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CLOTHES_CATALOG,
        )

    val credit: StateFlow<Int> = dogRepository.dog
        .map { it.credit }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 100,
        )

    val ownedItems: StateFlow<List<ClothItem>> = kotlinx.coroutines.flow.combine(
        catalog, itemRepository.ownedItemIds
    ) { catalogItems, ids ->
        catalogItems.filter { it.id in ids }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val unownedItems: StateFlow<List<ClothItem>> = kotlinx.coroutines.flow.combine(
        catalog, itemRepository.ownedItemIds
    ) { catalogItems, ids ->
        catalogItems.filter { it.id !in ids }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CLOTHES_CATALOG,
    )

    val equippedItemId: StateFlow<Int?> = itemRepository.equippedItemId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val equippedItemIds: StateFlow<List<Int>> = itemRepository.equippedItemIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun purchaseItem(itemId: Int, price: Int, category: ItemCategory, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = dogRepository.spendCredit(price)
            if (success) {
                itemRepository.purchaseItem(itemId)
                itemRepository.equipItem(itemId, category)
                val currentCatalog = catalog.value
                val itemName = currentCatalog.find { it.id == itemId }?.name ?: "unknown"
                analyticsHelper.logItemPurchased(itemId, itemName, price, category.name.lowercase())
            }
            onResult(success)
        }
    }

    fun equipItem(itemId: Int) {
        viewModelScope.launch {
            val currentCatalog = catalog.value
            val category = currentCatalog.find { it.id == itemId }?.category
            val itemName = currentCatalog.find { it.id == itemId }?.name ?: "unknown"
            if (category != null) {
                itemRepository.equipItem(itemId, category)
                analyticsHelper.logItemEquipped(itemId, itemName, category.name.lowercase())
            }
        }
    }

    fun unequipAll() {
        viewModelScope.launch {
            itemRepository.unequipAll()
            analyticsHelper.logDecorationReset()
        }
    }

    fun unequipCategory(category: ItemCategory) {
        viewModelScope.launch {
            itemRepository.unequipCategory(category)
            analyticsHelper.logItemUnequipped(category.name.lowercase())
        }
    }

    // ── Analytics 전용 이벤트 ────────────────────────────────────────────────

    fun logScreenView() = analyticsHelper.logScreenView("decoration")
    fun logTabDwellTime(seconds: Long) = analyticsHelper.logTabDwellTime("decoration", seconds)
    fun logItemViewed(itemId: Int, itemName: String, isLocked: Boolean) =
        analyticsHelper.logItemViewed(itemId, itemName, isLocked)

    fun logCategoryTabClicked(tabIndex: Int) {
        val category = when (tabIndex) {
            0 -> "face"; 1 -> "hair"; 2 -> "cloth"; else -> "theme"
        }
        analyticsHelper.logDecorationCategoryViewed(category)
    }

    fun logDecorationTabOpened(coinsBalance: Int) {
        analyticsHelper.logDecorationTabOpened("bottom_nav", coinsBalance)
    }

    fun logItemCancelClicked(itemId: Int) {
        val category = CLOTHES_CATALOG.find { it.id == itemId }?.category?.name?.lowercase() ?: "unknown"
        analyticsHelper.logItemUnequipped(category)
    }

    fun logPurchaseCancelled(itemId: Int, itemName: String) {
        analyticsHelper.logEvent("purchase_cancelled", itemId, itemName)
    }
}
