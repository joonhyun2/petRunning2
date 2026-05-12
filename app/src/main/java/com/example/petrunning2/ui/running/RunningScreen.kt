package com.example.petrunning2.ui.running

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.petrunning2.R
import com.example.petrunning2.ui.components.AppLogo
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorBorder
import com.example.petrunning2.ui.theme.ColorBorderSubtle
import com.example.petrunning2.ui.theme.ColorCredit
import com.example.petrunning2.ui.theme.ColorPrimary
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorSurfaceSoft
import com.example.petrunning2.ui.theme.ColorSurfaceSubtle
import com.example.petrunning2.ui.theme.ColorTextDisabled
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary

@Composable
fun RunningScreen(
    onNavigateToEndRun: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    viewModel: RunningViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val equippedItemId by viewModel.equippedItemId.collectAsState()

    val activeRewards = remember { mutableStateListOf<FloatingReward>() }
    LaunchedEffect(Unit) {
        viewModel.rewardEvents.collect { reward ->
            activeRewards.add(reward)
        }
    }

    var showTooShortDialog by remember { mutableStateOf(false) }

    RunningScreenContent(
        uiState = uiState,
        equippedItemId = equippedItemId,
        activeRewards = activeRewards,
        onRewardFinished = { id -> activeRewards.removeAll { it.id == id } },
        onPause = {
            if (uiState.status == RunStatus.RUNNING) viewModel.pauseRun()
            else viewModel.resumeRun()
        },
        onStop = {
            viewModel.stopRun()
            if (uiState.distanceKm * 1000 < 5.0) {
                // 5m 미만 → 팝업 표시
                showTooShortDialog = true
            } else {
                onNavigateToEndRun()
            }
        },
    )

    if (showTooShortDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Color(0xE8F5F5F5),
                        androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                    .padding(32.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(24.dp),
                ) {
                    androidx.compose.material3.Text(
                        text = "달린거리가 너무 짧아\n기록을 저장할 수 없어요!",
                        style = AppTextStyle.titleSm,
                        color = ColorTextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    androidx.compose.material3.Button(
                        onClick = {
                            viewModel.logRunCancelled(
                                elapsedSeconds = uiState.elapsedSeconds,
                                distanceKm = uiState.distanceKm,
                                reason = "too_short"
                            )
                            showTooShortDialog = false
                            onNavigateToHome()
                        },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary,
                            contentColor = ColorSurface,
                        ),
                    ) {
                        androidx.compose.material3.Text(
                            text = "확인",
                            style = AppTextStyle.bodyMd.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RunningScreenContent(
        uiState: RunningUiState,
        equippedItemId: Int?,
        activeRewards: List<FloatingReward>,
        onRewardFinished: (Long) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBg),
        ) {
            // 배경 스파클 장식
            SparkleDecorations()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                // 헤더: 로고 + GPS 배지
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AppLogo()
                    GpsBadge(connected = uiState.status == RunStatus.RUNNING)
                }

                // 페이스 섹션
                PaceSection(
                    paceString = uiState.paceSecPerKm.toPaceString(),
                    modifier = Modifier.weight(0.35f),
                )

                // 거리/시간 2열
                RunStatsRow(
                    distanceKm = uiState.distanceKm,
                    elapsedString = uiState.elapsedSeconds.toTimeString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 캐릭터 영역
                RunnerArea(
                    equippedItemId = equippedItemId,
                    activeRewards = activeRewards,
                    onRewardFinished = onRewardFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 컨트롤: pause + stop 버튼
                RunningControls(
                    isPaused = uiState.status == RunStatus.PAUSED,
                    onPause = onPause,
                    onStop = onStop,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

// ── GPS 배지 ──────────────────────────────────────────────────────────────────

    @Composable
    private fun GpsBadge(connected: Boolean) {
        Row(
            modifier = Modifier
                .background(ColorSurfaceSubtle, CircleShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "위치",
                style = AppTextStyle.bodySm.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                ),
                color = ColorPrimary,
            )
            Spacer(modifier = Modifier.width(6.dp))
            // 신호 막대 3개: 6px, 8px, 12px 높이
            val barColor = if (connected) ColorPrimary else ColorTextDisabled
            GpsSignalBars(color = barColor)
        }
    }

    @Composable
    private fun GpsSignalBars(color: Color) {
        val heights = listOf(6.dp, 8.dp, 12.dp)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(h)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color),
                )
            }
        }
    }

// ── 페이스 섹션 ───────────────────────────────────────────────────────────────

    @Composable
    private fun PaceSection(paceString: String, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // "페이스" 라벨 pill
            Box(
                modifier = Modifier
                    .background(ColorSurfaceSubtle, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "페이스",
                    style = AppTextStyle.bodySm.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    ),
                    color = ColorPrimary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 88sp 대형 페이스 숫자
            Text(
                text = paceString,
                style = AppTextStyle.displayXl.copy(
                    letterSpacing = (-4.4).sp,
                ),
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "현재 페이스",
                style = AppTextStyle.bodyLg,
                color = ColorTextSecondary,
            )
        }
    }

// ── 거리/시간 2열 ─────────────────────────────────────────────────────────────

    @Composable
    private fun RunStatsRow(
        distanceKm: Double,
        elapsedString: String,
        modifier: Modifier = Modifier,
    ) {
        Row(modifier = modifier) {
            // 거리
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "%.2f".format(distanceKm),
                    style = AppTextStyle.metricLg,
                    color = ColorTextPrimary,
                )
                Text(
                    text = "km",
                    style = AppTextStyle.bodyMd,
                    color = ColorTextSecondary,
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(52.dp)
                    .align(Alignment.CenterVertically)
                    .background(ColorBorderSubtle),
            )

            // 시간
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = elapsedString,
                    style = AppTextStyle.metricLg,
                    color = ColorTextPrimary,
                )
                Text(
                    text = "시간",
                    style = AppTextStyle.bodyMd,
                    color = ColorTextSecondary,
                )
            }
        }
    }

// ── 캐릭터 + 플로팅 보상 영역 ─────────────────────────────────────────────────

    @Composable
    private fun RunnerArea(
        equippedItemId: Int?,
        activeRewards: List<FloatingReward>,
        onRewardFinished: (Long) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Box(modifier = modifier) {
            // 속도선 (캐릭터 왼쪽)
            SpeedLines(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 134.dp, y = 104.dp)
                    .alpha(0.35f),
            )

            // 그림자 (캐릭터 아래)
            Box(
                modifier = Modifier
                    .size(width = 147.dp, height = 11.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 143.dp)
                    .clip(RoundedCornerShape(108.dp))
                    .background(ColorBorder)
                    .alpha(0.8f),
            )

            // 캐릭터 이미지 (top: 59dp, center)
            com.example.petrunning2.ui.components.PetCharacter(
                size = 114.dp,
                equippedItemId = equippedItemId,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 59.dp),
            )

            // 머리 위 플로팅 보상 텍스트 (0.01km마다 등장 → 위로 떠오르며 페이드아웃)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 30.dp),
                contentAlignment = Alignment.Center,
            ) {
                activeRewards.forEach { reward ->
                    key(reward.id) {
                        FloatingRewardText(
                            reward = reward,
                            onFinished = { onRewardFinished(reward.id) },
                        )
                    }
                }
            }
        }
    }

    /** 캐릭터 머리 위에서 떠오르며 페이드아웃되는 보상 텍스트 */
    @Composable
    private fun FloatingRewardText(
        reward: FloatingReward,
        onFinished: () -> Unit,
    ) {
        val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }
        val alpha = remember { androidx.compose.animation.core.Animatable(1f) }

        LaunchedEffect(Unit) {
            launch {
                offsetY.animateTo(
                    targetValue = -60f,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 1500,
                        easing = androidx.compose.animation.core.LinearOutSlowInEasing,
                    ),
                )
            }
            launch {
                delay(600)
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 900),
                )
                onFinished()
            }
        }

        Text(
            text = reward.text,
            modifier = Modifier
                .offset(y = offsetY.value.dp)
                .alpha(alpha.value),
            style = AppTextStyle.bodySm.copy(fontWeight = FontWeight.Bold),
            color = if (reward.isCredit) ColorCredit else ColorPrimary,
        )
    }

    @Composable
    private fun SpeedLines(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ColorTextDisabled),
            )
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ColorTextDisabled),
            )
        }
    }

// ── 컨트롤 버튼 ───────────────────────────────────────────────────────────────

    @Composable
    private fun RunningControls(
        isPaused: Boolean,
        onPause: () -> Unit,
        onStop: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 일시정지/재개 버튼
            androidx.compose.material3.Button(
                onClick = onPause,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color.Black.copy(alpha = 0.1f),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorSurface,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Canvas(modifier = Modifier.size(width = 16.dp, height = 20.dp)) {
                        if (isPaused) drawPlayIcon(ColorSurface) else drawPauseIcon(ColorSurface)
                    }
                    Text(
                        text = if (isPaused) "재개" else "일시정지",
                        style = AppTextStyle.titleSm.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.9.sp
                        ),
                    )
                }
            }

            // 종료 버튼: 꾹 1.5초 눌러야 종료, 가운데서 초록색 채워지는 애니메이션
            HoldToStopButton(
                onStop = onStop,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Composable
    private fun HoldToStopButton(
        onStop: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val HOLD_DURATION = 1500L
        var isHolding by remember { mutableStateOf(false) }
        val progress = remember { androidx.compose.animation.core.Animatable(0f) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(isHolding) {
            if (isHolding) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = HOLD_DURATION.toInt(),
                        easing = androidx.compose.animation.core.LinearEasing,
                    ),
                )
                if (progress.value >= 1f) {
                    onStop()
                }
            } else {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                )
            }
        }

        Box(
            modifier = modifier
                .height(68.dp)
                .shadow(
                    1.dp,
                    RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(ColorSurfaceSoft)
                .border(1.dp, ColorBorderSubtle, RoundedCornerShape(32.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            tryAwaitRelease()
                            isHolding = false
                        }
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            // 정중앙에서 양쪽으로 퍼지는 채우기 애니메이션
            if (progress.value > 0f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val fillWidth = size.width * progress.value
                    val fillLeft = (size.width - fillWidth) / 2f
                    drawRoundRect(
                        color = ColorPrimary.copy(alpha = 0.22f),
                        topLeft = Offset(fillLeft, 0f),
                        size = Size(fillWidth, size.height),
                        cornerRadius = CornerRadius(32.dp.toPx()),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ColorPrimary),
                )
                Text(
                    text = "종료",
                    style = AppTextStyle.titleSm.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9.sp,
                    ),
                    color = ColorPrimary,
                )
            }
        }
    }

// ── 배경 스파클 ───────────────────────────────────────────────────────────────

    @Composable
    private fun SparkleDecorations() {
        // CSS 절대 좌표 4개 점을 Box로 표현
        Box(modifier = Modifier.fillMaxSize().alpha(0.5f)) {
            listOf(
                Triple(78.dp, 354.dp, 8.dp),
                Triple((-98).dp, 398.dp, 12.dp), // right: 98 → 별도 처리 불가, 근사 생략
                Triple(59.dp, 530.dp, 8.dp),
                Triple((-59).dp, 575.dp, 8.dp),  // right: 59 → 근사 생략
            ).take(2).forEach { (x, y, s) ->
                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(s)
                        .clip(CircleShape)
                        .background(ColorSurface),
                )
            }
        }
    }

// ── DrawScope 아이콘 ──────────────────────────────────────────────────────────

    private fun DrawScope.drawPauseIcon(color: Color) {
        // 두 개의 수직 막대 (6×20dp, radius 2dp)
        val w = 6.dp.toPx()
        val h = 20.dp.toPx()
        val r = 2.dp.toPx()
        drawRoundRect(
            color,
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(r)
        )
        drawRoundRect(
            color,
            topLeft = Offset(10.dp.toPx(), 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(r)
        )
    }

    private fun DrawScope.drawPlayIcon(color: Color) {
        // 삼각형 재생 버튼
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, size.height / 2f)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, color)
    }

// ── Preview ───────────────────────────────────────────────────────────────────

    @Preview(showBackground = true, widthDp = 390, heightDp = 844)
    @Composable
    private fun RunningScreenPreview() {
        com.example.petrunning2.ui.theme.PetRunning2Theme {
            RunningScreenContent(
                uiState = RunningUiState(
                    distanceKm = 3.82,
                    elapsedSeconds = 1456L,
                    paceSecPerKm = 342L,
                    status = RunStatus.RUNNING,
                ),
                equippedItemId = null,
                activeRewards = emptyList(),
                onRewardFinished = {},
                onPause = {},
                onStop = {},
            )
        }
    }

    @Preview(showBackground = true, widthDp = 390, heightDp = 844)
    @Composable
    private fun RunningScreenPausedPreview() {
        com.example.petrunning2.ui.theme.PetRunning2Theme {
            RunningScreenContent(
                uiState = RunningUiState(
                    distanceKm = 3.82,
                    elapsedSeconds = 1456L,
                    paceSecPerKm = 342L,
                    status = RunStatus.PAUSED,
                ),
                equippedItemId = null,
                activeRewards = emptyList(),
                onRewardFinished = {},
                onPause = {},
                onStop = {},
            )
        }

    }
