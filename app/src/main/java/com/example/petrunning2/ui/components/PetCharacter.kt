package com.example.petrunning2.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.petrunning2.R
import com.example.petrunning2.ui.decoration.CLOTHES_CATALOG
import com.example.petrunning2.ui.decoration.ItemCategory
import kotlinx.coroutines.delay

// pet_idle.png 가 4프레임 2x2 그리드 스프라이트 시트
private const val FRAME_COUNT = 4
private const val FRAME_DELAY_MS = 150L

// 각 프레임에서 캐릭터 머리가 움직이는 만큼 아이템도 따라가도록 하는 오프셋 (sizePx 비율)
private val ITEM_DY = floatArrayOf(0.00f, 0.025f, 0.00f, -0.015f)
private val ITEM_DX = floatArrayOf(0.00f, 0.005f, 0.00f, -0.005f)

@Composable
fun PetCharacter(
    modifier: Modifier = Modifier,
    size: Dp = 114.dp,
    equippedItemId: Int? = null,
    equippedItemIds: List<Int> = emptyList(),
    isAnimating: Boolean = true,
) {
    // 단일 ID 또는 복수 ID 모두 지원
    val allEquippedIds = (equippedItemIds + listOfNotNull(equippedItemId)).distinct()

    val equippedItems = allEquippedIds.mapNotNull { id ->
        CLOTHES_CATALOG.find { it.id == id }
    }

    val context = LocalContext.current
    val density = LocalDensity.current

    val spriteSheet = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.pet_idle)
    }
    val frameW = spriteSheet.width / 2
    val frameH = spriteSheet.height / 2

    // 모든 장착 아이템 비트맵 로드
    val itemBitmaps = remember(allEquippedIds) {
        equippedItems.mapNotNull { item ->
            item.drawableRes?.let { res ->
                item to BitmapFactory.decodeResource(context.resources, res)
            }
        }
    }

    var frameIndex by remember { mutableStateOf(0) }

    if (isAnimating) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(FRAME_DELAY_MS)
                frameIndex = (frameIndex + 1) % FRAME_COUNT
            }
        }
    }

    val sizePx = with(density) { size.toPx().toInt() }

    Canvas(modifier = modifier.size(size)) {
        // 1) 캐릭터 스프라이트
        val col = frameIndex % 2
        val row = frameIndex / 2
        drawImage(
            image = spriteSheet.asImageBitmap(),
            srcOffset = IntOffset(col * frameW, row * frameH),
            srcSize = IntSize(frameW, frameH),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(sizePx, sizePx),
        )

        // 2) 모든 장착 아이템을 순서대로 오버레이
        val dx = (sizePx * ITEM_DX[frameIndex]).toInt()
        val dy = (sizePx * ITEM_DY[frameIndex]).toInt()
        itemBitmaps.forEach { (item, bitmap) ->
            if (item.isOverlay) {
                drawImage(
                    image = bitmap.asImageBitmap(),
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(bitmap.width, bitmap.height),
                    dstOffset = IntOffset(dx, dy),
                    dstSize = IntSize(sizePx, sizePx),
                )
            } else {
                val itemSize = when (item.category) {
                    ItemCategory.HAIR  -> (sizePx * 0.42f).toInt()
                    ItemCategory.FACE  -> (sizePx * 0.30f).toInt()
                    ItemCategory.CLOTH -> (sizePx * 0.38f).toInt()
                    ItemCategory.THEME -> (sizePx * 0.46f).toInt()
                }
                val baseX = (sizePx - itemSize) / 2f
                val baseY = when (item.category) {
                    ItemCategory.HAIR  -> sizePx * 0.20f
                    ItemCategory.FACE  -> sizePx * 0.38f
                    ItemCategory.CLOTH -> sizePx * 0.68f
                    ItemCategory.THEME -> sizePx * 0.04f
                }
                drawImage(
                    image = bitmap.asImageBitmap(),
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(bitmap.width, bitmap.height),
                    dstOffset = IntOffset((baseX + dx).toInt(), (baseY + dy).toInt()),
                    dstSize = IntSize(itemSize, itemSize),
                )
            }
        }
    }
}
