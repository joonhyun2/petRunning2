package com.example.petrunning2.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.example.petrunning2.ui.theme.PetRunning2Theme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petrunning2.R
import com.example.petrunning2.data.Dog
import com.example.petrunning2.ui.components.AppLogo
import com.example.petrunning2.ui.components.BottomNavBar
import com.example.petrunning2.ui.components.BottomNavDestination
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorBorderSubtle
import com.example.petrunning2.ui.theme.ColorCredit
import com.example.petrunning2.ui.theme.ColorCreditBg
import com.example.petrunning2.ui.theme.ColorLocation
import com.example.petrunning2.ui.theme.ColorLocationBg
import com.example.petrunning2.ui.theme.ColorPace
import com.example.petrunning2.ui.theme.ColorPaceBg
import com.example.petrunning2.ui.theme.ColorPrimaryLight
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorSurfaceSoft
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary

@Composable
fun HomeScreen(
    onNavigateToRunning: () -> Unit,
    onNavigateToDecoration: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val dog by viewModel.dog.collectAsState()
    val stats by viewModel.todayStats.collectAsState()
    val equippedItemId by viewModel.equippedItemId.collectAsState()
    val equippedItemIds by viewModel.equippedItemIds.collectAsState()
    HomeScreenContent(
        dog = dog,
        stats = stats,
        equippedItemId = equippedItemId,
        equippedItemIds = equippedItemIds,
        onNavigateToRunning = onNavigateToRunning,
        onNavigateToDecoration = onNavigateToDecoration,
        onNavigateToStats = onNavigateToStats,
        onNavigateToProfile = onNavigateToProfile,
    )
}

@Composable
private fun HomeScreenContent(
    dog: Dog,
    stats: TodayStats,
    equippedItemId: Int?,
    equippedItemIds: List<Int> = emptyList(),
    onNavigateToRunning: () -> Unit,
    onNavigateToDecoration: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
            .statusBarsPadding(),
    ) {
        val systemNavBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomNavBarHeight = 110.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 8.dp,
                    bottom = bottomNavBarHeight + systemNavBarInset,
                    start = 26.dp,
                    end = 26.dp,
                ),
        ) {
            // Header: 56dp, 좌측 로고 + 우측 코인
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppLogo()
                CoinDisplay(credit = dog.credit)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 캐릭터 카드 — 남은 공간의 절반 차지
            PetGrowthCard(
                dog = dog,
                equippedItemId = equippedItemId,
                equippedItemIds = equippedItemIds,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 오늘 통계 카드
            TodayStatsCard(stats = stats)

            Spacer(modifier = Modifier.height(16.dp))

            // 달리기 시작 버튼
            HomeStartButton(onClick = onNavigateToRunning)

            Spacer(modifier = Modifier.height(8.dp))
        }

        BottomNavBar(
            currentRoute = BottomNavDestination.Home.route,
            onItemClick = { route ->
                when (route) {
                    BottomNavDestination.Decoration.route -> onNavigateToDecoration()
                    BottomNavDestination.Statistics.route -> onNavigateToStats()
                    BottomNavDestination.Profile.route -> onNavigateToProfile()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Coin Display ─────────────────────────────────────────────────────────────

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

// ── Pet Growth Card ──────────────────────────────────────────────────────────

@Composable
private fun PetGrowthCard(dog: Dog, equippedItemId: Int?, equippedItemIds: List<Int> = emptyList(), modifier: Modifier = Modifier) {
    val progress = if (dog.maxXp > 0) dog.currentXp.toFloat() / dog.maxXp else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.04f),
            )
            .background(ColorSurfaceSoft, RoundedCornerShape(32.dp))
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Pet stage: 남은 공간 채우기
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            com.example.petrunning2.ui.components.PetCharacter(
                size = 114.dp,
                equippedItemIds = equippedItemIds,
            )
        }

        Spacer(modifier = Modifier.height(9.dp))

        // Progress panel: max 290dp wide
        PetProgressPanel(
            level = dog.level,
            progress = progress,
            modifier = Modifier.widthIn(max = 290.dp),
        )
    }
}

@Composable
private fun PetProgressPanel(
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "레벨 $level",
            style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
            color = ColorPrimaryLight,
            maxLines = 1,
        )
        // Track
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .background(Color(0xFFE1E5D9), RoundedCornerShape(50)),
        ) {
            // Fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(ColorPrimaryLight, RoundedCornerShape(50)),
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
            color = ColorPrimaryLight,
        )
    }
}

// ── Today Stats Card ─────────────────────────────────────────────────────────

@Composable
private fun TodayStatsCard(stats: TodayStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.04f),
            )
            .background(ColorSurface, RoundedCornerShape(32.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "오늘",
            style = AppTextStyle.titleSm,
            color = ColorPrimaryLight,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TodayStatItem(
                iconType = StatIconType.Time,
                value = formatTime(stats.totalTimeSeconds),
                label = "뛴 시간",
                modifier = Modifier.weight(1f),
            )
            TodayStatItem(
                iconType = StatIconType.Distance,
                value = "%.1f".format(stats.totalDistanceKm),
                label = "뛴 거리",
                modifier = Modifier.weight(1f),
            )
            TodayStatItem(
                iconType = StatIconType.Pace,
                value = formatPace(stats.avgPaceSecPerKm),
                label = "평균 페이스",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private enum class StatIconType { Time, Distance, Pace }

@Composable
private fun TodayStatItem(
    iconType: StatIconType,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val iconBg = when (iconType) {
        StatIconType.Time -> ColorSurfaceSoft
        StatIconType.Distance -> ColorLocationBg
        StatIconType.Pace -> ColorPaceBg
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        border = BorderStroke(1.dp, ColorBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 17.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            // Icon circle (40x38dp, pill background)
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 38.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(width = 40.dp, height = 38.dp)) {
                    when (iconType) {
                        StatIconType.Time -> drawClockIcon(ColorPrimaryLight)
                        StatIconType.Distance -> drawPinIcon(ColorLocation)
                        StatIconType.Pace -> drawSpeedIcon(ColorPace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = AppTextStyle.titleMd.copy(fontWeight = FontWeight.ExtraBold),
                color = ColorTextPrimary,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = AppTextStyle.bodySm,
                color = ColorTextSecondary,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Icon Canvas ───────────────────────────────────────────────────────────────
// CSS 기준으로 변환 (container: 40×38dp)

private fun DrawScope.drawClockIcon(color: Color) {
    val sw = 2.dp.toPx()
    // ::before  inset: 10px → oval at (10,10)-(30,28)
    drawOval(
        color = color,
        topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
        size = Size(20.dp.toPx(), 18.dp.toPx()),
        style = Stroke(sw),
    )
    val cx = 20.dp.toPx()
    val cy = 19.dp.toPx()
    // ::after  border-left + border-bottom → vertical then horizontal hand
    drawLine(color, Offset(cx, 13.dp.toPx()), Offset(cx, cy), sw, StrokeCap.Round)
    drawLine(color, Offset(cx, cy), Offset(26.dp.toPx(), cy), sw, StrokeCap.Round)
}

private fun DrawScope.drawPinIcon(color: Color) {
    val sw = 2.dp.toPx()
    val cx = 20.dp.toPx()
    val pinCy = 15.dp.toPx()
    val r = 7.dp.toPx()
    // ::before  border-radius: 50% 50% 50% 0 rotate(-45deg) → teardrop circle
    drawCircle(color = color, radius = r, center = Offset(cx, pinCy), style = Stroke(sw))
    // Pin point (two lines converging downward)
    drawLine(color, Offset(cx - r * 0.65f, pinCy + r * 0.65f), Offset(cx, pinCy + r * 1.55f), sw, StrokeCap.Round)
    drawLine(color, Offset(cx + r * 0.65f, pinCy + r * 0.65f), Offset(cx, pinCy + r * 1.55f), sw, StrokeCap.Round)
    // ::after  inner dot
    drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(cx, pinCy))
}

private fun DrawScope.drawSpeedIcon(color: Color) {
    val sw = 2.dp.toPx()
    val cx = 20.dp.toPx()
    // ::before  left:10, top:13, width:20, height:13, border-bottom:0 → top-half arc
    val arcTop = 13.dp.toPx()
    val arcH = 13.dp.toPx()
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(10.dp.toPx(), arcTop),
        size = Size(20.dp.toPx(), arcH * 2f),
        style = Stroke(sw, cap = StrokeCap.Round),
    )
    // ::after  left:21, top:15, width:9, rotate(-35deg) → needle
    // 회전 후 좌표: start ≈ (21.8, 18.6), end ≈ (29.2, 13.4)
    drawLine(
        color = color,
        start = Offset(21.8.dp.toPx(), 18.6.dp.toPx()),
        end = Offset(29.2.dp.toPx(), 13.4.dp.toPx()),
        strokeWidth = sw,
        cap = StrokeCap.Round,
    )
}

// ── Start Button ──────────────────────────────────────────────────────────────

@Composable
private fun HomeStartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.Black.copy(alpha = 0.05f),
            ),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPrimaryLight,
            contentColor = ColorSurface,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "달리기 시작",
                style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.ExtraBold),
            )
            // CSS: border-right + border-bottom → skewed chevron
            Canvas(modifier = Modifier.size(width = 10.dp, height = 20.dp)) {
                val sw = 3.dp.toPx()
                val midY = size.height / 2f
                drawLine(ColorSurface, Offset(0f, 4.dp.toPx()), Offset(size.width, midY), sw, StrokeCap.Round)
                drawLine(ColorSurface, Offset(size.width, midY), Offset(0f, size.height - 4.dp.toPx()), sw, StrokeCap.Round)
            }
        }
    }
}

// ── Format Utilities ──────────────────────────────────────────────────────────

private fun formatTime(totalSeconds: Long): String {
    if (totalSeconds <= 0L) return "0:00"
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun formatPace(secPerKm: Long): String {
    if (secPerKm <= 0L) return "0'00\""
    val minutes = secPerKm / 60
    val seconds = secPerKm % 60
    return "%d'%02d\"".format(minutes, seconds)
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    PetRunning2Theme {
        HomeScreenContent(
            dog = Dog(name = "Pix", level = 3, currentXp = 68, maxXp = 100, credit = 240),
            stats = TodayStats(
                totalTimeSeconds = 1456L,
                totalDistanceKm = 3.82,
                avgPaceSecPerKm = 342L,
            ),
            equippedItemId = null,
            onNavigateToRunning = {},
            onNavigateToDecoration = {},
            onNavigateToStats = {},
            onNavigateToProfile = {},
        )
    }
}
