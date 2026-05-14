package com.example.petrunning2.ui.statics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petrunning2.R
import com.example.petrunning2.data.local.entity.RunRecordEntity
import com.example.petrunning2.ui.components.BottomNavBar
import com.example.petrunning2.ui.components.BottomNavDestination
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorBorder
import com.example.petrunning2.ui.theme.ColorBorderSubtle
import com.example.petrunning2.ui.theme.ColorPrimaryChart
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorSurfaceSubtle
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary
import com.example.petrunning2.ui.theme.PetRunning2Theme
import java.util.Calendar


@Composable
fun StaticsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToDecoration: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRecordDetail: ((distanceKm: Double, elapsedSeconds: Long, paceSecPerKm: Long, routePoints: String) -> Unit)? = null,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.logScreenView() }
    DisposableEffect(Unit) {
        val enterTime = System.currentTimeMillis()
        onDispose { viewModel.logTabDwellTime((System.currentTimeMillis() - enterTime) / 1000) }
    }

    val records by viewModel.records.collectAsState()
    var selectedPeriod by remember { mutableIntStateOf(0) }

    val now = Calendar.getInstance()

    // 주간: 어떤 주를 보고 있는지 (< > 로 년/월 이동, 그 안에서 해당 주의 월~일)
    var weeklyYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var weeklyMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }
    var weeklyWeekOfMonth by remember { mutableIntStateOf(now.get(Calendar.WEEK_OF_MONTH)) }

    // 월간: 어떤 월을 보고 있는지 (막대그래프 = 1주~N주)
    var monthlyYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var monthlyMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }

    // 년간: 어떤 년도를 보고 있는지 (막대그래프 = 1~12월)
    var yearlyYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }

    StaticsScreenContent(
        records = records,
        selectedPeriod = selectedPeriod,
        onPeriodSelected = {
            selectedPeriod = it
            viewModel.logStatsPeriodChanged(it)
        },
        weeklyYear = weeklyYear,
        weeklyMonth = weeklyMonth,
        weeklyWeekOfMonth = weeklyWeekOfMonth,
        onWeeklyMonthChange = { delta ->
            var m = weeklyMonth + delta
            var y = weeklyYear
            if (m < 0) { m = 11; y-- }
            if (m > 11) { m = 0; y++ }
            weeklyYear = y
            weeklyMonth = m
            // 이전 달로 갈 때는 마지막 주, 다음 달로 갈 때는 1주
            weeklyWeekOfMonth = if (delta < 0) getWeeksInMonth(y, m) else 1
        },
        onWeeklyWeekChange = { week ->
            if (week == -1) {
                // 이전 달 마지막 주로 세팅 (이미 onMonthChange에서 처리됨)
                weeklyWeekOfMonth = getWeeksInMonth(weeklyYear, weeklyMonth)
            } else {
                weeklyWeekOfMonth = week
            }
        },
        monthlyYear = monthlyYear,
        monthlyMonth = monthlyMonth,
        onMonthlyChange = { delta ->
            var m = monthlyMonth + delta
            var y = monthlyYear
            if (m < 0) { m = 11; y-- }
            if (m > 11) { m = 0; y++ }
            monthlyYear = y
            monthlyMonth = m
        },
        yearlyYear = yearlyYear,
        onYearlyChange = { delta -> yearlyYear += delta },
        onNavigateToHome = onNavigateToHome,
        onNavigateToDecoration = onNavigateToDecoration,
        onNavigateToProfile = onNavigateToProfile,
        onRecordClick = { distanceKm, elapsedSeconds, paceSecPerKm, routePoints ->
            viewModel.logRecentActivityClicked()
            onNavigateToRecordDetail?.invoke(distanceKm, elapsedSeconds, paceSecPerKm, routePoints)
        },
    )
}

@Composable
private fun StaticsScreenContent(
    records: List<RunRecordEntity>,
    selectedPeriod: Int,
    onPeriodSelected: (Int) -> Unit,
    weeklyYear: Int,
    weeklyMonth: Int,
    weeklyWeekOfMonth: Int,
    onWeeklyMonthChange: (Int) -> Unit,
    onWeeklyWeekChange: (Int) -> Unit,
    monthlyYear: Int,
    monthlyMonth: Int,
    onMonthlyChange: (Int) -> Unit,
    yearlyYear: Int,
    onYearlyChange: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToDecoration: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onRecordClick: ((Double, Long, Long, String) -> Unit)? = null,
) {
    // 바 데이터 + 라벨 계산
    val barData by remember(records, selectedPeriod, weeklyYear, weeklyMonth, weeklyWeekOfMonth, monthlyYear, monthlyMonth, yearlyYear) {
        derivedStateOf {
            computeBarData(records, selectedPeriod, weeklyYear, weeklyMonth, weeklyWeekOfMonth, monthlyYear, monthlyMonth, yearlyYear)
        }
    }
    val dayLabels = listOf(
        stringResource(R.string.stats_day_mon),
        stringResource(R.string.stats_day_tue),
        stringResource(R.string.stats_day_wed),
        stringResource(R.string.stats_day_thu),
        stringResource(R.string.stats_day_fri),
        stringResource(R.string.stats_day_sat),
        stringResource(R.string.stats_day_sun),
    )
    val weekSuffix = stringResource(R.string.stats_week_unit)
    val barLabels = getBarLabels(selectedPeriod, monthlyYear, monthlyMonth, dayLabels, weekSuffix)
    var selectedBarIndex by remember(selectedPeriod) { mutableStateOf<Int?>(null) }

    val displayRecords by remember(records, selectedPeriod, weeklyYear, weeklyMonth, weeklyWeekOfMonth, monthlyYear, monthlyMonth, yearlyYear) {
        derivedStateOf {
            val idx = selectedBarIndex
            if (idx != null) {
                computeBarFilteredRecords(records, selectedPeriod, idx, weeklyYear, weeklyMonth, weeklyWeekOfMonth, monthlyYear, monthlyMonth, yearlyYear)
            } else {
                filterRecords(records, selectedPeriod, weeklyYear, weeklyMonth, weeklyWeekOfMonth, monthlyYear, monthlyMonth, yearlyYear)
            }
        }
    }

    val totalDistance = displayRecords.sumOf { it.distanceKm }
    val totalTime = displayRecords.sumOf { it.elapsedSeconds }
    val avgPace = if (totalDistance > 0) (totalTime / totalDistance).toLong() else 0L
    val totalCoins = displayRecords.sumOf { it.distanceKm.toInt() }

    Box(
        modifier = Modifier.fillMaxSize().background(ColorBg).statusBarsPadding(),
    ) {
        val systemNavBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 26.dp, end = 26.dp, top = 16.dp, bottom = 110.dp + systemNavBarInset),
        ) {
            // Header
            StaticsHeader(selectedPeriod = selectedPeriod, onPeriodSelected = onPeriodSelected)
            Spacer(modifier = Modifier.height(26.dp))

            // Period Selector
            when (selectedPeriod) {
                0 -> WeeklySelector(
                    year = weeklyYear,
                    month = weeklyMonth,
                    weekOfMonth = weeklyWeekOfMonth,
                    maxWeeks = getWeeksInMonth(weeklyYear, weeklyMonth),
                    onMonthChange = onWeeklyMonthChange,
                    onWeekChange = onWeeklyWeekChange,
                )
                1 -> MonthlySelector(year = monthlyYear, month = monthlyMonth, onMonthChange = onMonthlyChange)
                2 -> YearlySelector(year = yearlyYear, onYearChange = onYearlyChange)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Chart Card
            ChartCard(
                totalDistance = totalDistance,
                totalTime = totalTime,
                avgPace = avgPace,
                totalCoins = totalCoins,
                barData = barData,
                barLabels = barLabels,
                selectedBarIndex = selectedBarIndex,
                onBarSelected = { selectedBarIndex = it },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 최근 활동 ──
            RecentActivitySection(
                records = records.sortedByDescending { it.timestamp }.take(10),
                onRecordClick = onRecordClick,
            )
        }

        BottomNavBar(
            currentRoute = BottomNavDestination.Statistics.route,
            onItemClick = { route ->
                when (route) {
                    BottomNavDestination.Home.route -> onNavigateToHome()
                    BottomNavDestination.Decoration.route -> onNavigateToDecoration()
                    BottomNavDestination.Profile.route -> onNavigateToProfile()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun StaticsHeader(selectedPeriod: Int, onPeriodSelected: (Int) -> Unit) {
    val periodTabs = listOf(
        stringResource(R.string.stats_tab_weekly),
        stringResource(R.string.stats_tab_monthly),
        stringResource(R.string.stats_tab_yearly),
    )
    Row(
        modifier = Modifier.fillMaxWidth().height(46.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.stats_title),
            style = AppTextStyle.titleMd.copy(fontWeight = FontWeight.ExtraBold),
            color = ColorPrimaryChart,
        )
        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(CircleShape)
                .background(ColorBorderSubtle)
                .padding(4.dp),
        ) {
            periodTabs.forEachIndexed { index, label ->
                val isActive = index == selectedPeriod
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .then(
                            if (isActive) Modifier
                                .shadow(1.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.05f))
                                .background(ColorSurface)
                            else Modifier
                        )
                        .clickable { onPeriodSelected(index) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = AppTextStyle.bodyMd,
                        color = if (isActive) ColorTextPrimary else ColorTextSecondary,
                    )
                }
            }
        }
    }
}

// ── Weekly Selector: < 2026년 5월 1주차 > ────────────────────────────────────

@Composable
private fun WeeklySelector(
    year: Int,
    month: Int,
    weekOfMonth: Int,
    maxWeeks: Int,
    onMonthChange: (Int) -> Unit,
    onWeekChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.stats_prev_week),
            tint = ColorTextSecondary,
            modifier = Modifier.size(28.dp).clickable {
                if (weekOfMonth > 1) {
                    onWeekChange(weekOfMonth - 1)
                } else {
                    // 이전 달 마지막 주로
                    onMonthChange(-1)
                    // onWeekChange will be called with maxWeeks of new month via recomposition
                    onWeekChange(-1) // signal to set to last week
                }
            },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.stats_week_label, year, month + 1, weekOfMonth),
            style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.Bold),
            color = ColorTextPrimary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.stats_next_week),
            tint = ColorTextSecondary,
            modifier = Modifier.size(28.dp).clickable {
                if (weekOfMonth < maxWeeks) {
                    onWeekChange(weekOfMonth + 1)
                } else {
                    // 다음 달 1주차로
                    onMonthChange(1)
                    onWeekChange(1)
                }
            },
        )
    }
}

// ── Monthly Selector: < 2026년 5월 > (막대 = 1주~N주) ───────────────────────

@Composable
private fun MonthlySelector(year: Int, month: Int, onMonthChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.stats_prev_month),
            tint = ColorTextSecondary,
            modifier = Modifier.size(28.dp).clickable { onMonthChange(-1) },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.stats_month_label, year, month + 1),
            style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.Bold),
            color = ColorTextPrimary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.stats_next_month),
            tint = ColorTextSecondary,
            modifier = Modifier.size(28.dp).clickable { onMonthChange(1) },
        )
    }
}

// ── Yearly Selector: < 2026년 > (막대 = 1~12월) ─────────────────────────────

@Composable
private fun YearlySelector(year: Int, onYearChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.stats_prev_year),
            tint = ColorTextSecondary,
            modifier = Modifier.size(28.dp).clickable { onYearChange(-1) },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.stats_year_label, year),
            style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.Bold),
            color = ColorTextPrimary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.stats_next_year),
            tint = ColorTextSecondary,
            modifier = Modifier.size(28.dp).clickable { onYearChange(1) },
        )
    }
}

// ── Chart Card ───────────────────────────────────────────────────────────────

@Composable
private fun ChartCard(
    totalDistance: Double,
    totalTime: Long,
    avgPace: Long,
    totalCoins: Int,
    barData: List<Float>,
    barLabels: List<String>,
    selectedBarIndex: Int?,
    onBarSelected: (Int?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(30.dp), spotColor = Color.Black.copy(alpha = 0.06f))
            .border(1.dp, ColorBorder, RoundedCornerShape(30.dp))
            .background(ColorSurface, RoundedCornerShape(30.dp))
            .padding(24.dp),
    ) {
        // 총 달린 거리 (큰 글씨, 초록색)
        Text(
            text = "${"%.2f".format(totalDistance)} km",
            style = AppTextStyle.metricLg.copy(fontWeight = FontWeight.ExtraBold),
            color = ColorPrimaryChart,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 메트릭 행: 시간 / 페이스 / 획득 코인
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MetricPill(label = stringResource(R.string.stats_metric_time), value = formatTime(totalTime))
            MetricPill(label = stringResource(R.string.stats_metric_pace), value = formatPace(avgPace))
            MetricPill(label = stringResource(R.string.stats_metric_earned), value = "+$totalCoins", showCreditIcon = true)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 막대 그래프
        BarChart(barData = barData, barLabels = barLabels, selectedBarIndex = selectedBarIndex, onBarSelected = onBarSelected)
    }
}

@Composable
private fun MetricPill(label: String, value: String, showCreditIcon: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (showCreditIcon) {
                com.example.petrunning2.ui.components.CreditIcon(size = 14.dp)
            }
            Text(
                text = value,
                style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.ExtraBold),
                color = ColorTextPrimary,
            )

        }
        Text(text = label, style = AppTextStyle.caption, color = ColorTextSecondary)
    }
}

// ── Bar Chart ────────────────────────────────────────────────────────────────

@Composable
private fun BarChart(
    barData: List<Float>,
    barLabels: List<String>,
    selectedBarIndex: Int?,
    onBarSelected: (Int?) -> Unit,
) {
    val maxVal = barData.maxOrNull()?.coerceAtLeast(0.1f) ?: 0.1f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(130.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            barData.forEachIndexed { index, value ->
                val fraction = (value / maxVal).coerceIn(0f, 1f)
                val isHighlight = if (selectedBarIndex != null) index == selectedBarIndex else index == barData.lastIndex
                val barColor = if (isHighlight) ColorPrimaryChart else ColorSurfaceSubtle

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).height(130.dp).clickable {
                        onBarSelected(if (selectedBarIndex == index) null else index)
                    },
                ) {
                    if (value > 0f) {
                        Text(
                            text = "%.1f".format(value),
                            style = AppTextStyle.caption.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = if (isHighlight) ColorPrimaryChart else ColorTextSecondary,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val barHeight = (fraction * 90).dp.coerceAtLeast(4.dp)
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            barLabels.forEachIndexed { index, label ->
                val isHighlight = if (selectedBarIndex != null) index == selectedBarIndex else index == barLabels.lastIndex
                Text(
                    text = label,
                    style = AppTextStyle.caption.copy(fontSize = 11.sp),
                    color = if (isHighlight) ColorPrimaryChart else ColorTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Recent Activity Section ──────────────────────────────────────────────────

@Composable
private fun RecentActivitySection(
    records: List<RunRecordEntity>,
    onRecordClick: ((Double, Long, Long, String) -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.stats_recent_activity),
            style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.Bold),
            color = ColorTextPrimary,
        )

        if (records.isEmpty()) {
            Text(
                text = stringResource(R.string.stats_no_activity),
                style = AppTextStyle.bodyMd,
                color = ColorTextSecondary,
            )
        } else {
            records.forEach { record ->
                RecentActivityCard(
                    record = record,
                    onClick = { onRecordClick?.invoke(record.distanceKm, record.elapsedSeconds, record.paceSecPerKm, record.routePoints) },
                )
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    record: RunRecordEntity,
    onClick: () -> Unit,
) {
    val cal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
    val dateStr = "${cal.get(Calendar.MONTH) + 1}월 ${cal.get(Calendar.DAY_OF_MONTH)}일"
    val coins = (record.distanceKm * 20).toInt()

    // 경로 좌표 파싱 (썸네일용)
    val routeLatLngs = remember(record.routePoints) {
        if (record.routePoints.isBlank()) emptyList()
        else record.routePoints.split("|").mapNotNull { seg ->
            val parts = seg.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) Pair(lat, lng) else null
            } else null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .border(1.dp, ColorBorder, RoundedCornerShape(16.dp))
            .background(ColorSurface, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 왼쪽: 경로 썸네일 (Canvas로 실제 좌표 선 그리기)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ColorSurfaceSubtle),
            contentAlignment = Alignment.Center,
        ) {
            if (routeLatLngs.size >= 2) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(52.dp).padding(4.dp)) {
                    // 좌표를 0~1로 정규화 후 Canvas 크기에 맞게 변환
                    val lats = routeLatLngs.map { it.first }
                    val lngs = routeLatLngs.map { it.second }
                    val minLat = lats.min()
                    val maxLat = lats.max()
                    val minLng = lngs.min()
                    val maxLng = lngs.max()
                    val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
                    val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

                    val points = routeLatLngs.map { (lat, lng) ->
                        val x = ((lng - minLng) / lngRange * size.width).toFloat()
                        val y = ((maxLat - lat) / latRange * size.height).toFloat() // y 뒤집기
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = ColorPrimaryChart,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        )
                    }
                    // 시작점
                    drawCircle(ColorPrimaryChart, radius = 3.dp.toPx(), center = points.first())
                    // 끝점
                    drawCircle(ColorPrimaryChart, radius = 3.dp.toPx(), center = points.last())
                }
            } else {
                // 경로 없을 때 기본 아이콘
                androidx.compose.foundation.Canvas(modifier = Modifier.size(40.dp)) {
                    val w = size.width
                    val h = size.height
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.1f, h * 0.8f)
                        cubicTo(w * 0.3f, h * 0.2f, w * 0.7f, h * 0.6f, w * 0.9f, h * 0.2f)
                    }
                    drawPath(
                        path = path,
                        color = ColorPrimaryChart,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        ),
                    )
                    drawCircle(ColorPrimaryChart, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.8f))
                    drawCircle(ColorPrimaryChart, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.2f))
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 오른쪽: 날짜 + 위치 + 거리/시간/코인
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = dateStr,
                style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
                color = ColorTextPrimary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${"%.2f".format(record.distanceKm)}km",
                    style = AppTextStyle.bodySm,
                    color = ColorTextSecondary,
                )
                Text(
                    text = formatTime(record.elapsedSeconds),
                    style = AppTextStyle.bodySm,
                    color = ColorTextSecondary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    com.example.petrunning2.ui.components.CreditIcon(size = 12.dp)
                    Text(
                        text = "+$coins",
                        style = AppTextStyle.bodySm.copy(fontWeight = FontWeight.Bold),
                        color = ColorTextPrimary,
                    )
                }
            }
        }

        // 화살표
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = stringResource(R.string.stats_detail_view),
            tint = ColorTextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ── Data Helpers ─────────────────────────────────────────────────────────────

private fun filterRecords(
    records: List<RunRecordEntity>,
    period: Int,
    weeklyYear: Int, weeklyMonth: Int, weeklyWeekOfMonth: Int,
    monthlyYear: Int, monthlyMonth: Int,
    yearlyYear: Int,
): List<RunRecordEntity> {
    return when (period) {
        0 -> { // 주간: 해당 주의 월요일 기준으로 연/월/주차 필터
            records.filter { record ->
                val (y, m, w) = getWeekIndexInMonth(record.timestamp)
                y == weeklyYear && m == weeklyMonth && w == weeklyWeekOfMonth
            }
        }
        1 -> { // 월간: 특정 년/월 전체
            val cal = Calendar.getInstance()
            records.filter { record ->
                cal.timeInMillis = record.timestamp
                cal.get(Calendar.YEAR) == monthlyYear && cal.get(Calendar.MONTH) == monthlyMonth
            }
        }
        2 -> { // 년간: 특정 년도 전체
            val cal = Calendar.getInstance()
            records.filter { record ->
                cal.timeInMillis = record.timestamp
                cal.get(Calendar.YEAR) == yearlyYear
            }
        }
        else -> records
    }
}

private fun computeBarData(
    records: List<RunRecordEntity>,
    period: Int,
    weeklyYear: Int, weeklyMonth: Int, weeklyWeekOfMonth: Int,
    monthlyYear: Int, monthlyMonth: Int,
    yearlyYear: Int,
): List<Float> {
    val cal = Calendar.getInstance()
    return when (period) {
        0 -> { // 주간: 7일 (월~일)
            val result = MutableList(7) { 0f }
            records.filter { record ->
                val (y, m, w) = getWeekIndexInMonth(record.timestamp)
                y == weeklyYear && m == weeklyMonth && w == weeklyWeekOfMonth
            }.forEach { record ->
                cal.timeInMillis = record.timestamp
                val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 월=0
                result[dayIndex] += record.distanceKm.toFloat()
            }
            result
        }
        1 -> { // 월간: 주차별 (1주~N주)
            val maxWeeks = getWeeksInMonth(monthlyYear, monthlyMonth)
            val result = MutableList(maxWeeks) { 0f }
            records.filter { record ->
                cal.timeInMillis = record.timestamp
                cal.get(Calendar.YEAR) == monthlyYear && cal.get(Calendar.MONTH) == monthlyMonth
            }.forEach { record ->
                cal.timeInMillis = record.timestamp
                val week = weekOfMonth(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH),
                ).coerceIn(1, maxWeeks)
                result[week - 1] += record.distanceKm.toFloat()
            }
            result
        }
        2 -> { // 년간: 12개월
            val result = MutableList(12) { 0f }
            records.filter { record ->
                cal.timeInMillis = record.timestamp
                cal.get(Calendar.YEAR) == yearlyYear
            }.forEach { record ->
                cal.timeInMillis = record.timestamp
                result[cal.get(Calendar.MONTH)] += record.distanceKm.toFloat()
            }
            result
        }
        else -> emptyList()
    }
}

private fun computeBarFilteredRecords(
    records: List<RunRecordEntity>,
    period: Int,
    barIndex: Int,
    weeklyYear: Int, weeklyMonth: Int, weeklyWeekOfMonth: Int,
    monthlyYear: Int, monthlyMonth: Int,
    yearlyYear: Int,
): List<RunRecordEntity> {
    val cal = Calendar.getInstance()
    return when (period) {
        0 -> records.filter { record ->
            val (y, m, w) = getWeekIndexInMonth(record.timestamp)
            val dayIndex = Calendar.getInstance().apply { timeInMillis = record.timestamp }
                .let { (it.get(Calendar.DAY_OF_WEEK) + 5) % 7 }
            y == weeklyYear && m == weeklyMonth && w == weeklyWeekOfMonth && dayIndex == barIndex
        }
        1 -> records.filter { record ->
            cal.timeInMillis = record.timestamp
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            y == monthlyYear && m == monthlyMonth && weekOfMonth(y, m, d) == barIndex + 1
        }
        2 -> records.filter { record ->
            cal.timeInMillis = record.timestamp
            cal.get(Calendar.YEAR) == yearlyYear &&
                    cal.get(Calendar.MONTH) == barIndex
        }
        else -> emptyList()
    }
}

private fun getBarLabels(
    period: Int,
    monthlyYear: Int,
    monthlyMonth: Int,
    dayLabels: List<String>,
    weekSuffix: String,
): List<String> {
    return when (period) {
        0 -> dayLabels
        1 -> {
            val maxWeeks = getWeeksInMonth(monthlyYear, monthlyMonth)
            (1..maxWeeks).map { "$it$weekSuffix" }
        }
        2 -> (1..12).map { "$it" }
        else -> emptyList()
    }
}

// 일요일 시작 기준 (일=0, 월=1, ... 토=6) — 한국 달력 표준
private fun firstDayOffset(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    return cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY  // 0~6
}

private fun weekOfMonth(year: Int, month: Int, dayOfMonth: Int): Int {
    val offset = firstDayOffset(year, month)
    return (dayOfMonth + offset - 1) / 7 + 1
}

private fun getWeeksInMonth(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val offset = firstDayOffset(year, month)
    return (daysInMonth + offset - 1) / 7 + 1
}

// 타임스탬프 → (year, month, weekIndex)
private fun getWeekIndexInMonth(timeMillis: Long): Triple<Int, Int, Int> {
    val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val day = cal.get(Calendar.DAY_OF_MONTH)
    return Triple(year, month, weekOfMonth(year, month, day))
}

private fun formatTime(totalSeconds: Long): String {
    if (totalSeconds <= 0L) return "0m"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"
}

private fun formatPace(secPerKm: Long): String {
    if (secPerKm <= 0L) return "-'--\""
    val minutes = secPerKm / 60
    val seconds = secPerKm % 60
    return "%d'%02d\"".format(minutes, seconds)
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 394, heightDp = 926)
@Composable
private fun StaticsScreenPreview() {
    PetRunning2Theme {
        StaticsScreenContent(
            records = listOf(
                RunRecordEntity(distanceKm = 3.2, elapsedSeconds = 1200, paceSecPerKm = 375, xpGained = 32, timestamp = System.currentTimeMillis() - 86400000 * 6),
                RunRecordEntity(distanceKm = 5.6, elapsedSeconds = 2100, paceSecPerKm = 375, xpGained = 56, timestamp = System.currentTimeMillis() - 86400000 * 5),
                RunRecordEntity(distanceKm = 2.1, elapsedSeconds = 800, paceSecPerKm = 381, xpGained = 21, timestamp = System.currentTimeMillis() - 86400000 * 4),
                RunRecordEntity(distanceKm = 4.7, elapsedSeconds = 1750, paceSecPerKm = 372, xpGained = 47, timestamp = System.currentTimeMillis() - 86400000 * 3),
                RunRecordEntity(distanceKm = 3.8, elapsedSeconds = 1400, paceSecPerKm = 368, xpGained = 38, timestamp = System.currentTimeMillis() - 86400000 * 2),
                RunRecordEntity(distanceKm = 5.4, elapsedSeconds = 2000, paceSecPerKm = 370, xpGained = 54, timestamp = System.currentTimeMillis() - 86400000),
            ),
            selectedPeriod = 0,
            onPeriodSelected = {},
            weeklyYear = 2026, weeklyMonth = 4, weeklyWeekOfMonth = 1,
            onWeeklyMonthChange = {}, onWeeklyWeekChange = {},
            monthlyYear = 2026, monthlyMonth = 4, onMonthlyChange = {},
            yearlyYear = 2026, onYearlyChange = {},
            onNavigateToHome = {}, onNavigateToDecoration = {}, onNavigateToProfile = {},
        )
    }
}
