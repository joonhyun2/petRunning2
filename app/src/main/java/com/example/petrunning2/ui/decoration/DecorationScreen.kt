package com.example.petrunning2.ui.decoration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petrunning2.R
import com.example.petrunning2.ui.components.AppLogo
import com.example.petrunning2.ui.components.BottomNavBar
import com.example.petrunning2.ui.components.BottomNavDestination
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorBorder
import com.example.petrunning2.ui.theme.ColorBorderSubtle
import com.example.petrunning2.ui.theme.ColorCredit
import com.example.petrunning2.ui.theme.ColorCreditBg
import com.example.petrunning2.ui.theme.ColorExp
import com.example.petrunning2.ui.theme.ColorPrimaryChart
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorSurfaceSoft
import com.example.petrunning2.ui.theme.ColorTextDisabled
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.PetRunning2Theme

data class DecorationItem(
    val id: Int,
    val isLocked: Boolean = false,
    val price: Int? = null,
    val drawableRes: Int? = null,
)

private val tabs = listOf("얼굴", "헤어", "옷", "테마")

@Composable
fun DecorationScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: DecorationViewModel = hiltViewModel(),
) {
    val credit by viewModel.credit.collectAsState()
    val ownedItems by viewModel.ownedItems.collectAsState()
    val unownedItems by viewModel.unownedItems.collectAsState()
    val equippedItemIds by viewModel.equippedItemIds.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedItemId by remember { mutableIntStateOf(-1) }

    // 탭이나 장착 아이템 변경 시 selectedItemId 동기화
    val currentTabCategory = when (selectedTab) {
        0 -> ItemCategory.FACE; 1 -> ItemCategory.HAIR; 2 -> ItemCategory.CLOTH; else -> ItemCategory.THEME
    }
    val equippedInCurrentTab = equippedItemIds.firstOrNull { id ->
        CLOTHES_CATALOG.find { it.id == id }?.category == currentTabCategory
    }

    // 탭 전환 시 해당 탭의 장착 아이템으로 자동 동기화
    LaunchedEffect(selectedTab, equippedItemIds) {
        selectedItemId = equippedInCurrentTab ?: -1
    }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showInsufficientDialog by remember { mutableStateOf(false) }

    // 탭별 카테고리 매핑
    val tabCategory = when (selectedTab) {
        0 -> ItemCategory.FACE
        1 -> ItemCategory.HAIR
        2 -> ItemCategory.CLOTH
        else -> ItemCategory.THEME
    }

    // 현재 탭의 카탈로그 아이템
    val tabCatalogItems = CLOTHES_CATALOG.filter { it.category == tabCategory }

    // 현재 탭 아이템을 DecorationItem으로 변환 (소유 여부 반영)
    val currentItems = tabCatalogItems.map { item ->
        val isOwned = item.id in ownedItems.map { it.id }
        DecorationItem(
            id = item.id,
            isLocked = !isOwned,
            price = if (!isOwned) item.price else null,
            drawableRes = item.drawableRes,
        )
    }

    val selectedItem = currentItems.find { it.id == selectedItemId }
    // 선택된 아이템이 잠금 상태면 구매 버튼 표시
    val showPurchaseButton = selectedItem?.isLocked == true
    // 선택한 아이템이 이미 장착된 아이템이면 취소 버튼 표시 (잠금 아이템 제외)
    val showCancelButton = selectedItemId > 0 && equippedItemIds.contains(selectedItemId) && !showPurchaseButton

    // 프리뷰 로직:
    // - 락된 아이템 선택 → 해당 아이템 미리보기 (구매 전 미리보기)
    // - 구매한 아이템(락 안됨) 선택 → 장착된 아이템 표시 (선택만 한 것, 아직 적용 안 됨)
    // - 아무것도 선택 안 함 → 장착된 아이템 표시
    val previewItemId: Int? = when {
        selectedItemId > 0 && selectedItem?.isLocked == true -> selectedItemId  // 락 아이템 미리보기
        else -> equippedInCurrentTab  // 장착된 아이템 표시
    }

    // 캐릭터 프리뷰: 현재 탭 카테고리는 previewItemId로, 나머지 카테고리는 equippedItemIds 그대로
    val previewItemIds = equippedItemIds.filter { id ->
        CLOTHES_CATALOG.find { it.id == id }?.category != currentTabCategory
    } + listOfNotNull(previewItemId)

    // 다이얼로그에 표시할 ClothItem 정보
    val selectedClothItem = CLOTHES_CATALOG.find { it.id == selectedItemId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
            .statusBarsPadding(),
    ) {
        val bottomNavBarHeight = 110.dp
        val systemNavBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = bottomNavBarHeight + systemNavBarInset + 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── 헤더: 로고 + 코인 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppLogo()
                CoinDisplay(credit = credit)
            }

            // ── 캐릭터 프리뷰 ── (모든 카테고리 장착 아이템 표시)
            CharacterPreview(
                equippedItemIds = previewItemIds,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── 커스터마이즈 패널 ──
            CustomizePanel(
                selectedTab = selectedTab,
                onTabSelected = { newTab ->
                    selectedTab = newTab
                    viewModel.logCategoryTabClicked(newTab)
                    // 탭 전환 시 해당 탭의 장착 아이템으로 초기화 (없으면 -1)
                    val newCategory = when (newTab) {
                        0 -> ItemCategory.FACE; 1 -> ItemCategory.HAIR; 2 -> ItemCategory.CLOTH; else -> ItemCategory.THEME
                    }
                    selectedItemId = equippedItemIds.firstOrNull { id ->
                        CLOTHES_CATALOG.find { it.id == id }?.category == newCategory
                    } ?: -1
                },
                currentItems = currentItems,
                selectedItemId = selectedItemId,
                equippedItemId = equippedInCurrentTab,
                onItemSelected = { id ->
                    selectedItemId = id
                },
                onReset = {
                    selectedItemId = -1
                    viewModel.unequipAll()
                },
                onApply = {
                    if (selectedItemId > 0) {
                        viewModel.equipItem(selectedItemId)
                    }
                },
                showPurchaseButton = showPurchaseButton,
                onPurchaseClick = { showPurchaseDialog = true },
                showCancelButton = showCancelButton,
                onCancel = {
                    selectedItemId = -1
                    viewModel.unequipCategory(currentTabCategory)  // 현재 탭 카테고리만 해제
                },
            )
        }

        BottomNavBar(
            currentRoute = BottomNavDestination.Decoration.route,
            onItemClick = { route ->
                when (route) {
                    BottomNavDestination.Home.route -> onNavigateToHome()
                    BottomNavDestination.Statistics.route -> onNavigateToStats()
                    BottomNavDestination.Profile.route -> onNavigateToProfile()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    // ── Frame 1500: 구매 확인 다이얼로그 ──
    if (showPurchaseDialog && selectedClothItem != null) {
        PurchaseConfirmDialog(
            itemName = selectedClothItem.name,
            itemDrawableRes = selectedClothItem.drawableRes,
            onConfirm = {
                showPurchaseDialog = false
                viewModel.purchaseItem(selectedClothItem.id, selectedClothItem.price) { success ->
                    if (success) showSuccessDialog = true
                    else showInsufficientDialog = true
                }
            },
            onDismiss = {
                viewModel.logPurchaseCancelled(selectedClothItem?.id ?: -1, selectedClothItem?.name ?: "unknown")
                showPurchaseDialog = false
            },
        )
    }

    // ── Frame 1501: 구매 완료 다이얼로그 ──
    if (showSuccessDialog && selectedClothItem != null) {
        PurchaseSuccessDialog(
            itemDrawableRes = selectedClothItem.drawableRes,
            onConfirm = {
                showSuccessDialog = false
                // 탭 유지, 선택만 해제
                selectedItemId = -1
            },
        )
    }

    // ── 크레딧 부족 다이얼로그 ──
    if (showInsufficientDialog) {
        InsufficientCreditDialog(
            onConfirm = { showInsufficientDialog = false },
        )
    }
}

// ── 코인 표시 ────────────────────────────────────────────────────────────────

@Composable
private fun CoinDisplay(credit: Int) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .background(ColorCreditBg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        com.example.petrunning2.ui.components.CreditIcon(size = 18.dp)
        Text(
            text = "$credit",
            style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
            color = ColorCredit,
        )
    }
}

// ── 캐릭터 프리뷰 ─────────────────────────────────────────────────────────────

@Composable
private fun CharacterPreview(equippedItemId: Int? = null, equippedItemIds: List<Int> = emptyList()) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ColorSurfaceSoft),
        contentAlignment = Alignment.Center,
    ) {
        com.example.petrunning2.ui.components.PetCharacter(
            size = 114.dp,
            equippedItemId = equippedItemId,
            equippedItemIds = equippedItemIds,
        )
    }
}

// ── 커스터마이즈 패널 ──────────────────────────────────────────────────────────

@Composable
private fun CustomizePanel(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    currentItems: List<DecorationItem>,
    selectedItemId: Int,
    equippedItemId: Int?,
    onItemSelected: (Int) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    showPurchaseButton: Boolean,
    onPurchaseClick: () -> Unit,
    showCancelButton: Boolean,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
            )
            .background(ColorSurface, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 19.dp),
    ) {
        DecorationTabs(selectedTab = selectedTab, onTabSelected = onTabSelected)
        Spacer(modifier = Modifier.height(21.dp))
        if (selectedTab == 3) {
            // 테마 탭: Coming Soon — 다른 탭의 아이템 그리드와 동일한 높이 유지
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cellSize = (maxWidth - 12.dp * 3) / 4
                val fixedHeight = cellSize * 3 + 12.dp * 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fixedHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Coming Soon!",
                        style = AppTextStyle.titleMd,
                        color = ColorTextDisabled,
                    )
                }
            }
        } else {
            DecorationItemGrid(
                items = currentItems,
                selectedItemId = selectedItemId,
                equippedItemId = equippedItemId,
                onItemSelected = onItemSelected,
            )
        }
        Spacer(modifier = Modifier.height(23.dp))
        DecorationActions(
            onReset = onReset,
            onApply = onApply,
            showPurchaseButton = showPurchaseButton,
            onPurchaseClick = onPurchaseClick,
            showCancelButton = showCancelButton,
            onCancel = onCancel,
        )
    }
}

// ── 탭 ───────────────────────────────────────────────────────────────────────

@Composable
private fun DecorationTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ColorBorderSubtle)
            .border(1.dp, ColorBorderSubtle, RoundedCornerShape(14.dp)),
    ) {
        tabs.forEachIndexed { index, label ->
            val isActive = index == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(51.dp)
                    .then(
                        if (isActive) Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorPrimaryChart)
                        else Modifier
                    )
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = AppTextStyle.bodyLg.copy(
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isActive) ColorSurface else ColorTextDisabled,
                )
            }
        }
    }
}

// ── 아이템 그리드 ─────────────────────────────────────────────────────────────

@Composable
private fun DecorationItemGrid(
    items: List<DecorationItem>,
    selectedItemId: Int,
    equippedItemId: Int?,
    onItemSelected: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellSize = (maxWidth - 12.dp * 3) / 4
        val fixedHeight = cellSize * 3 + 12.dp * 2
        val rows = items.chunked(4)
        Column(
            modifier = Modifier.height(fixedHeight),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { item ->
                        DecorationItemCell(
                            item = item,
                            isSelected = item.id == selectedItemId,
                            isEquipped = item.id == equippedItemId,
                            onClick = { onItemSelected(item.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(4 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun DecorationItemCell(
    item: DecorationItem,
    isSelected: Boolean,
    isEquipped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 선택됐으면 항상 초록 테두리, 아니면 기본 테두리
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (isSelected) Modifier.border(2.dp, ColorPrimaryChart, RoundedCornerShape(14.dp))
                else Modifier.border(1.dp, ColorBorderSubtle, RoundedCornerShape(14.dp))
            )
            .background(ColorSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (item.drawableRes != null) {
            Image(
                painter = painterResource(item.drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit,
            )
        } else {
            FaceIcon(item.id)
        }

        // 장착 중인 아이템: 체크 표시 (선택 여부와 무관하게 항상 표시)
        if (isEquipped) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-7).dp, y = 7.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(ColorPrimaryChart),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.5f)
                        lineTo(size.width * 0.4f, size.height * 0.75f)
                        lineTo(size.width * 0.85f, size.height * 0.2f)
                    }
                    drawPath(path, color = ColorSurface, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                }
            }
        }

        if (item.isLocked) {
            LockIcon(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp))
        }

        if (item.isLocked && item.price != null) {
            PriceBadge(
                price = item.price,
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-5).dp, y = (-7).dp),
            )
        }
    }
}

// ── 아이콘 ────────────────────────────────────────────────────────────────────

@Composable
private fun FaceIcon(id: Int) {
    Canvas(modifier = Modifier.size(width = 46.dp, height = 28.dp)) {
        val w = size.width
        val eyeRadius = 2.5.dp.toPx()
        val blushRadius = 3.dp.toPx()
        drawCircle(color = ColorTextPrimary, radius = eyeRadius, center = Offset(9.dp.toPx(), 9.dp.toPx()))
        drawCircle(color = ColorTextPrimary, radius = eyeRadius, center = Offset(w - 9.dp.toPx(), 9.dp.toPx()))
        drawCircle(color = ColorExp.copy(alpha = 0.6f), radius = blushRadius, center = Offset(9.dp.toPx(), 19.dp.toPx()))
        drawCircle(color = ColorExp.copy(alpha = 0.6f), radius = blushRadius, center = Offset(w - 9.dp.toPx(), 19.dp.toPx()))
    }
}

@Composable
private fun LockIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 10.dp, height = 15.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = ColorTextDisabled,
            topLeft = Offset(0f, h * 0.4f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
        )
        drawArc(
            color = ColorTextDisabled,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, 0f),
            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.55f),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
private fun PriceBadge(price: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(22.dp)
            .border(1.dp, ColorBorder, CircleShape)
            .background(ColorSurface, CircleShape)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        com.example.petrunning2.ui.components.CreditIcon(size = 10.dp)
        Text(text = "$price", style = AppTextStyle.caption.copy(fontSize = 10.sp), color = ColorCredit)
    }
}

// ── 액션 버튼 ─────────────────────────────────────────────────────────────────

@Composable
private fun DecorationActions(
    onReset: () -> Unit,
    onApply: () -> Unit,
    showPurchaseButton: Boolean = false,
    onPurchaseClick: () -> Unit = {},
    showCancelButton: Boolean = false,
    onCancel: () -> Unit = {},
) {
    val buttonAction = when {
        showCancelButton -> onCancel
        showPurchaseButton -> onPurchaseClick
        else -> onApply
    }
    val buttonText = when {
        showCancelButton -> "취소"
        showPurchaseButton -> "구매"
        else -> "적용"
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .weight(0.45f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ColorBorder),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = ColorSurface, contentColor = ColorPrimaryChart),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawArc(color = ColorPrimaryChart, startAngle = -90f, sweepAngle = 270f, useCenter = false, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    val arrowPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width * 0.35f, 0f)
                        lineTo(size.width * 0.5f, size.height * 0.15f)
                        lineTo(size.width * 0.65f, 0f)
                    }
                    drawPath(arrowPath, color = ColorPrimaryChart, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                }
                Text(text = "초기화", style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold), maxLines = 1)
            }
        }

        Button(
            onClick = buttonAction,
            modifier = Modifier
                .weight(0.55f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryChart, contentColor = ColorSurface),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Canvas(modifier = Modifier.size(width = 8.dp, height = 12.dp)) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height * 0.5f)
                        lineTo(size.width * 0.4f, size.height)
                        lineTo(size.width, 0f)
                    }
                    drawPath(path, color = ColorSurface, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                }
                Text(
                    text = buttonText,
                    style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
            }
        }
    }
}

// ── Frame 1500: 구매 확인 다이얼로그 ──────────────────────────────────────────

@Composable
private fun PurchaseConfirmDialog(
    itemName: String,
    itemDrawableRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xE8F5F5F5), RoundedCornerShape(20.dp))
                .padding(vertical = 40.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = "${itemName}을 구매하시겠습니까?",
                    style = AppTextStyle.titleMd,
                    color = ColorTextPrimary,
                    textAlign = TextAlign.Center,
                )
                Image(
                    painter = painterResource(itemDrawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.size(width = 108.dp, height = 47.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryChart),
                    ) {
                        Text("아니오", style = AppTextStyle.titleMd, color = Color.White)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.size(width = 108.dp, height = 47.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryChart),
                    ) {
                        Text("예", style = AppTextStyle.titleMd, color = Color.White)
                    }

                }
            }
        }
    }
}

// ── Frame 1501: 구매 완료 다이얼로그 ──────────────────────────────────────────

@Composable
private fun PurchaseSuccessDialog(
    itemDrawableRes: Int,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xE8F5F5F5), RoundedCornerShape(20.dp))
                .padding(vertical = 40.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = "구매를 완료하였습니다!",
                    style = AppTextStyle.titleMd,
                    color = ColorTextPrimary,
                    textAlign = TextAlign.Center,
                )
                Image(
                    painter = painterResource(itemDrawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit,
                )
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.size(width = 108.dp, height = 47.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryChart),
                ) {
                    Text("확인", style = AppTextStyle.titleMd, color = Color.White)
                }
            }
        }
    }
}

// ── 크레딧 부족 다이얼로그 ────────────────────────────────────────────────────

@Composable
private fun InsufficientCreditDialog(
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onConfirm) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xE8F5F5F5), RoundedCornerShape(20.dp))
                .padding(vertical = 40.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = "크레딧이 부족해요!",
                    style = AppTextStyle.titleMd,
                    color = ColorTextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "달리기를 해서 크레딧을 모아보세요",
                    style = AppTextStyle.bodyMd,
                    color = ColorTextPrimary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.size(width = 108.dp, height = 47.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryChart),
                ) {
                    Text("확인", style = AppTextStyle.titleMd, color = Color.White)
                }
            }
        }
    }
}

// ── 프리뷰 ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 394, heightDp = 926)
@Composable
private fun DecorationScreenPreview() {
    PetRunning2Theme {
        DecorationScreen(
            onNavigateToHome = {},
            onNavigateToStats = {},
            onNavigateToProfile = {},
        )
    }
}
