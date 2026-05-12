package com.example.petrunning2.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.example.petrunning2.R
import com.example.petrunning2.ui.components.AppLogo
import com.example.petrunning2.ui.components.BottomNavBar
import com.example.petrunning2.ui.components.BottomNavDestination
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorBorder
import com.example.petrunning2.ui.theme.ColorBorderSubtle
import com.example.petrunning2.ui.theme.ColorCredit
import com.example.petrunning2.ui.theme.ColorPace
import com.example.petrunning2.ui.theme.ColorPaceBg
import com.example.petrunning2.ui.theme.ColorPrimary
import com.example.petrunning2.ui.theme.ColorPrimaryActive
import com.example.petrunning2.ui.theme.ColorPrimaryChart
import com.example.petrunning2.ui.theme.ColorPrimaryLight
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorSurfaceSoft
import com.example.petrunning2.ui.theme.ColorTextDisabled
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary
import com.example.petrunning2.ui.theme.PetRunning2Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToDecoration: () -> Unit,
    onNavigateToStats: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val dog by viewModel.dog.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()

    var showContactSheet by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    // 다이얼로그 내 임시 상태 (확인 누르기 전까지 반영 안 함)
    var pendingNotificationEnabled by remember(showNotificationDialog) { mutableStateOf(notificationEnabled) }
    val contactSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showNotificationDialog) {
        NotificationDialog(
            enabled = pendingNotificationEnabled,
            onEnabledChange = { pendingNotificationEnabled = it },
            onConfirm = {
                viewModel.setNotificationEnabled(pendingNotificationEnabled)
                showNotificationDialog = false
            },
            onDismiss = { showNotificationDialog = false },
        )
    }

    if (showContactSheet) {
        ContactBottomSheet(
            onDismiss = { showContactSheet = false },
            sheetState = contactSheetState,
        )
    }

    if (showEditNameDialog) {
        EditNameDialog(
            currentName = dog.name,
            onConfirm = { newName ->
                viewModel.updateName(newName)
                showEditNameDialog = false
            },
            onDismiss = { showEditNameDialog = false },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Header: 56dp ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppLogo()
            }

            // ── Content ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                ProfileCard(
                    name = dog.name,
                    statusText = "함께 달리며 성장 중",
                    level = dog.level,
                    progress = if (dog.maxXp > 0) dog.currentXp.toFloat() / dog.maxXp else 0f,
                    onEditProfile = { showEditNameDialog = true },
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProfileMenuCard(
                    onMyItems = onNavigateToDecoration,
                    onNotificationSettings = { showNotificationDialog = true },
                    onContact = { showContactSheet = true },
                    onLogout = onLogout,
                )
            }
        }

        // ── Bottom Navigation ──
        BottomNavBar(
            currentRoute = BottomNavDestination.Profile.route,
            onItemClick = { route ->
                when (route) {
                    BottomNavDestination.Home.route -> onNavigateToHome()
                    BottomNavDestination.Decoration.route -> onNavigateToDecoration()
                    BottomNavDestination.Statistics.route -> onNavigateToStats()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}


// ── ProfileCard ──────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    name: String,
    statusText: String,
    level: Int,
    progress: Float,
    onEditProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ColorBorder, RoundedCornerShape(24.dp))
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.06f),
            )
            .background(ColorSurfaceSoft, RoundedCornerShape(24.dp))
            .padding(start = 20.dp, end = 20.dp, top = 23.dp, bottom = 20.dp),
    ) {
        // 프로필 메인: 아바타 + 텍스트
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .border(1.dp, ColorSurface, CircleShape)
                    .clip(CircleShape)
                    .background(ColorSurface),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.pet),
                    contentDescription = null,
                    modifier = Modifier.size(58.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Profile copy
            Column {
                Text(
                    text = name,
                    style = AppTextStyle.titleLg,
                    color = ColorTextPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = statusText,
                    style = AppTextStyle.bodyMd,
                    color = ColorTextSecondary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onEditProfile,
                    modifier = Modifier.height(36.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, ColorPrimaryLight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ColorPrimaryActive,
                    ),
                    contentPadding = ButtonDefaults.ContentPadding.let {
                        androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 0.dp)
                    },
                ) {
                    Text(
                        text = "프로필 수정",
                        style = AppTextStyle.bodyMd,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(21.dp))

        // Growth Progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp)
                .background(
                    color = ColorSurface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "레벨 $level",
                style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
                color = ColorPrimaryLight,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ColorBorder),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(50))
                        .background(ColorPrimaryLight),
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
                color = ColorPrimaryLight,
            )
        }
    }
}

// ── BadgeSectionCard ─────────────────────────────────────────────────────────

@Composable
private fun BadgeSectionCard(
    onViewAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(197.dp)
            .border(1.dp, ColorBorder, RoundedCornerShape(24.dp))
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.06f),
            )
            .background(ColorSurface, RoundedCornerShape(24.dp))
            .padding(start = 20.dp, end = 20.dp, top = 19.dp, bottom = 22.dp),
    ) {
        // Section head
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "획득 배지",
                style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.Medium),
                color = ColorPrimary,
            )
            // 전체 보기 버튼
            Row(
                modifier = Modifier
                    .height(28.dp)
                    .border(1.dp, ColorBorderSubtle, CircleShape)
                    .background(ColorSurface, CircleShape)
                    .clickable(onClick = onViewAll)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "전체 보기",
                    style = AppTextStyle.bodySm,
                    color = ColorTextDisabled,
                )
                // Chevron
                Canvas(modifier = Modifier.size(8.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.1f)
                        lineTo(size.width * 0.8f, size.height * 0.5f)
                        lineTo(size.width * 0.2f, size.height * 0.9f)
                    }
                    drawPath(
                        path,
                        color = ColorTextDisabled,
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Badge list: 3열
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BadgeItem(
                type = BadgeType.Plant,
                label = "새싹 러너",
            )
            BadgeItem(
                type = BadgeType.Cone,
                label = "연속 챌린저",
            )
            BadgeItem(
                type = BadgeType.Shoe,
                label = "꾸준한 발걸음",
            )
        }
    }
}

private enum class BadgeType { Plant, Cone, Shoe }

@Composable
private fun BadgeItem(
    type: BadgeType,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Badge icon circle: 80dp
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(
                    width = 2.dp,
                    color = when (type) {
                        BadgeType.Plant -> ColorBorder
                        BadgeType.Cone -> ColorPaceBg
                        BadgeType.Shoe -> ColorBorder
                    },
                    shape = CircleShape,
                )
                .clip(CircleShape)
                .background(
                    when (type) {
                        BadgeType.Plant -> ColorSurfaceSoft
                        BadgeType.Cone -> ColorPaceBg
                        BadgeType.Shoe -> ColorSurfaceSoft
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(40.dp)) {
                when (type) {
                    BadgeType.Plant -> drawPlantBadge()
                    BadgeType.Cone -> drawConeBadge()
                    BadgeType.Shoe -> drawShoeBadge()
                }
            }
            // + 장식
            Text(
                text = "+",
                style = AppTextStyle.bodySm.copy(fontWeight = FontWeight.Bold),
                color = ColorCredit,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = 12.dp),
            )
            Text(
                text = "+",
                style = AppTextStyle.bodySm.copy(fontWeight = FontWeight.Bold),
                color = ColorCredit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 9.dp, bottom = 14.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = label,
            style = AppTextStyle.bodySm,
            color = ColorTextSecondary,
        )
    }
}

// ── Badge DrawScope Icons ────────────────────────────────────────────────────

private fun DrawScope.drawPlantBadge() {
    val cx = size.width / 2f
    val leafSize = 8.dp.toPx()
    val stemWidth = 3.dp.toPx()

    // 줄기
    drawLine(
        color = ColorPrimaryActive,
        start = Offset(cx, size.height * 0.8f),
        end = Offset(cx, size.height * 0.35f),
        strokeWidth = stemWidth,
        cap = StrokeCap.Round,
    )
    // 잎 (좌)
    val leftLeaf = Path().apply {
        moveTo(cx, size.height * 0.5f)
        cubicTo(cx - leafSize * 1.5f, size.height * 0.35f, cx - leafSize * 2f, size.height * 0.2f, cx - leafSize, size.height * 0.15f)
        cubicTo(cx - leafSize * 0.5f, size.height * 0.25f, cx, size.height * 0.4f, cx, size.height * 0.5f)
    }
    drawPath(leftLeaf, color = ColorPrimaryActive)
    // 잎 (우)
    val rightLeaf = Path().apply {
        moveTo(cx, size.height * 0.45f)
        cubicTo(cx + leafSize * 1.5f, size.height * 0.3f, cx + leafSize * 2f, size.height * 0.15f, cx + leafSize, size.height * 0.1f)
        cubicTo(cx + leafSize * 0.5f, size.height * 0.2f, cx, size.height * 0.35f, cx, size.height * 0.45f)
    }
    drawPath(rightLeaf, color = ColorPrimaryActive)
}

private fun DrawScope.drawConeBadge() {
    val cx = size.width / 2f
    val triW = 14.dp.toPx()
    val triH = 16.dp.toPx()
    val topY = size.height * 0.25f

    // 삼각형 (콘)
    val triangle = Path().apply {
        moveTo(cx, topY)
        lineTo(cx + triW, topY + triH)
        lineTo(cx - triW, topY + triH)
        close()
    }
    drawPath(triangle, color = ColorPace)

    // 내부 장식
    drawRoundRect(
        color = ColorCredit,
        topLeft = Offset(cx - 3.dp.toPx(), topY + 6.dp.toPx()),
        size = Size(6.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(1.dp.toPx()),
    )
}

private fun DrawScope.drawShoeBadge() {
    val cx = size.width / 2f
    val cy = size.height * 0.5f

    // 신발 몸통
    drawRoundRect(
        color = ColorTextDisabled,
        topLeft = Offset(cx - 12.dp.toPx(), cy),
        size = Size(24.dp.toPx(), 10.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx()),
    )
    // 밑창
    drawRoundRect(
        color = ColorSurface,
        topLeft = Offset(cx - 10.dp.toPx(), cy + 10.dp.toPx()),
        size = Size(22.dp.toPx(), 5.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx()),
        style = Stroke(width = 1.dp.toPx()),
    )
    // 장식 점
    drawCircle(
        color = ColorPrimaryChart,
        radius = 3.dp.toPx(),
        center = Offset(cx + 4.dp.toPx(), cy - 4.dp.toPx()),
    )
    drawCircle(
        color = ColorPrimaryLight,
        radius = 2.5.dp.toPx(),
        center = Offset(cx + 10.dp.toPx(), cy + 2.dp.toPx()),
    )
}

// ── ProfileMenuCard ──────────────────────────────────────────────────────────

@Composable
private fun ProfileMenuCard(
    onMyItems: () -> Unit,
    onNotificationSettings: () -> Unit,
    onContact: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(241.dp)
            .border(1.dp, ColorBorder, RoundedCornerShape(24.dp))
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.06f),
            )
            .background(ColorSurface, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 4.dp),
    ) {
        ProfileMenuItem(
            iconType = MenuIconType.Bag,
            label = "꾸미기",
            onClick = onMyItems,
            showTopBorder = false,
        )
        ProfileMenuItem(
            iconType = MenuIconType.Bell,
            label = "알림 설정",
            onClick = onNotificationSettings,
            showTopBorder = true,
        )
        ProfileMenuItem(
            iconType = MenuIconType.Chat,
            label = "문의하기",
            onClick = onContact,
            showTopBorder = true,
        )
        ProfileMenuItem(
            iconType = MenuIconType.Logout,
            label = "로그아웃",
            onClick = onLogout,
            showTopBorder = true,
        )
    }
}

private enum class MenuIconType { Bag, Bell, Chat, Logout }

@Composable
private fun ProfileMenuItem(
    iconType: MenuIconType,
    label: String,
    onClick: () -> Unit,
    showTopBorder: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .then(
                if (showTopBorder) Modifier.drawDashedTopBorder()
                else Modifier
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Menu icon
        Canvas(modifier = Modifier.size(22.dp)) {
            when (iconType) {
                MenuIconType.Bag -> drawBagIcon()
                MenuIconType.Bell -> drawBellIcon()
                MenuIconType.Chat -> drawChatIcon()
                MenuIconType.Logout -> drawLogoutIcon()
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = label,
            style = AppTextStyle.bodyLg,
            color = ColorTextSecondary,
            modifier = Modifier.weight(1f),
        )

        // Chevron arrow
        Canvas(modifier = Modifier.size(8.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.1f)
                lineTo(size.width * 0.8f, size.height * 0.5f)
                lineTo(size.width * 0.2f, size.height * 0.9f)
            }
            drawPath(
                path,
                color = ColorTextDisabled,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

// ── Dashed border modifier ───────────────────────────────────────────────────

private fun Modifier.drawDashedTopBorder(): Modifier = this.then(
    Modifier.drawBehind {
        val dashWidth = 6.dp.toPx()
        val gapWidth = 4.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = ColorBorderSubtle,
                start = Offset(x, 0f),
                end = Offset((x + dashWidth).coerceAtMost(size.width), 0f),
                strokeWidth = 1.dp.toPx(),
            )
            x += dashWidth + gapWidth
        }
    }
)

// ── Menu Icon DrawScope ──────────────────────────────────────────────────────

private fun DrawScope.drawBagIcon() {
    val color = ColorPrimaryLight
    val sw = 2.dp.toPx()
    // 몸통
    drawRoundRect(
        color = color,
        topLeft = Offset(3.dp.toPx(), 5.dp.toPx()),
        size = Size(16.dp.toPx(), 15.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx()),
        style = Stroke(sw),
    )
    // 손잡이
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(7.dp.toPx(), 1.dp.toPx()),
        size = Size(8.dp.toPx(), 8.dp.toPx()),
        style = Stroke(sw),
    )
}

private fun DrawScope.drawBellIcon() {
    val color = ColorPrimaryLight
    val sw = 2.dp.toPx()
    // 종 몸통
    drawRoundRect(
        color = color,
        topLeft = Offset(5.dp.toPx(), 4.dp.toPx()),
        size = Size(12.dp.toPx(), 13.dp.toPx()),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(sw),
    )
    // 추
    drawRoundRect(
        color = color,
        topLeft = Offset(9.dp.toPx(), 19.dp.toPx()),
        size = Size(4.dp.toPx(), 2.dp.toPx()),
        cornerRadius = CornerRadius(9999f),
    )
}

private fun DrawScope.drawChatIcon() {
    val color = ColorPrimaryLight
    val sw = 2.dp.toPx()
    // 말풍선 원
    drawCircle(
        color = color,
        radius = 7.dp.toPx(),
        center = Offset(11.dp.toPx(), 11.dp.toPx()),
        style = Stroke(sw),
    )
    // 꼬리
    val tail = Path().apply {
        moveTo(14.dp.toPx(), 16.dp.toPx())
        lineTo(18.dp.toPx(), 19.dp.toPx())
        lineTo(15.dp.toPx(), 14.dp.toPx())
    }
    drawPath(tail, color = color, style = Stroke(sw, cap = StrokeCap.Round))
}

private fun DrawScope.drawLogoutIcon() {
    val color = ColorPrimaryLight
    val sw = 2.dp.toPx()
    // 문 프레임 (왼쪽 열린 사각형)
    val frame = Path().apply {
        moveTo(12.dp.toPx(), 3.dp.toPx())
        lineTo(3.dp.toPx(), 3.dp.toPx())
        lineTo(3.dp.toPx(), 19.dp.toPx())
        lineTo(12.dp.toPx(), 19.dp.toPx())
    }
    drawPath(frame, color = color, style = Stroke(sw, cap = StrokeCap.Round))
    // 화살표 (오른쪽으로)
    drawLine(
        color = color,
        start = Offset(9.dp.toPx(), 11.dp.toPx()),
        end = Offset(19.dp.toPx(), 11.dp.toPx()),
        strokeWidth = sw,
        cap = StrokeCap.Round,
    )
    // 화살표 머리
    val arrowHead = Path().apply {
        moveTo(16.dp.toPx(), 7.dp.toPx())
        lineTo(20.dp.toPx(), 11.dp.toPx())
        lineTo(16.dp.toPx(), 15.dp.toPx())
    }
    drawPath(arrowHead, color = color, style = Stroke(sw, cap = StrokeCap.Round))
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 414, heightDp = 883)
@Composable
private fun ProfileScreenPreview() {
    PetRunning2Theme {
        ProfileScreen(
            onNavigateToHome = {},
            onNavigateToDecoration = {},
            onNavigateToStats = {},
        )
    }
}
