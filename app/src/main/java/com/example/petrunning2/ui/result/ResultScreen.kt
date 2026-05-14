package com.example.petrunning2.ui.result

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petrunning2.R
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorBorder
import com.example.petrunning2.ui.theme.ColorBorderSubtle
import com.example.petrunning2.ui.theme.ColorCredit
import com.example.petrunning2.ui.theme.ColorCreditBg
import com.example.petrunning2.ui.theme.ColorExp
import com.example.petrunning2.ui.theme.ColorExpBg
import com.example.petrunning2.ui.theme.ColorPrimary
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorSurfaceSoft
import com.example.petrunning2.ui.theme.ColorTextDisabled
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary
import com.example.petrunning2.ui.theme.PetRunning2Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.remember
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions

@Composable
fun ResultScreen(
    distanceKm: Double,
    elapsedSeconds: Long,
    paceSecPerKm: Long,
    routePoints: String = "",
    isFromHistory: Boolean = false,
    onNavigateToHome: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // 데이터 세팅 (최초 1회)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.setRunData(distanceKm, elapsedSeconds, paceSecPerKm, routePoints)
    }

    // 뒤로가기 시에도 결과 저장 (history 조회는 저장 불필요)
    BackHandler(enabled = !isFromHistory) {
        viewModel.saveResult(onComplete = onNavigateToHome)
    }

    ResultScreenContent(
        uiState = uiState,
        isFromHistory = isFromHistory,
        onDone = {
            if (isFromHistory) {
                onNavigateToHome()
            } else {
                viewModel.saveResult(onComplete = onNavigateToHome)
            }
        },
        onShare = { /* share intent handled inside */ },
    )
}

@Composable
private fun ResultScreenContent(
    uiState: ResultUiState,
    isFromHistory: Boolean = false,
    onDone: () -> Unit,
    onShare: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Summary Card ──
        SummaryCard(
            distanceKm = uiState.distanceKm,
            elapsedSeconds = uiState.elapsedSeconds,
            paceSecPerKm = uiState.paceSecPerKm,
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ── Map Card ──
        MapCard(routePoints = uiState.routePoints, locationLabel = uiState.locationLabel)

        Spacer(modifier = Modifier.height(48.dp))

        // ── Earned Grid ──
        EarnedGrid(
            earnedXp = uiState.earnedXp,
            earnedCredit = uiState.earnedCredit,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Actions ──
        EndActions(
            isSaving = uiState.isSaving,
            isFromHistory = isFromHistory,
            onDone = onDone,
            onShare = {
                val shareText = context.getString(
                    R.string.result_share_text,
                    "%.2f".format(uiState.distanceKm),
                    formatTime(uiState.elapsedSeconds),
                    formatPace(uiState.paceSecPerKm),
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                com.example.petrunning2.analytics.AnalyticsHelper().logResultShared()
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.result_share_chooser)))
            },
        )
    }
}

// ── Summary Card ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    distanceKm: Double,
    elapsedSeconds: Long,
    paceSecPerKm: Long,
) {
    val dateStr = SimpleDateFormat("yyyy.MM.dd(E)", Locale.KOREAN).format(Date())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.03f),
            )
            .background(ColorSurface, RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 날짜
        Text(
            text = dateStr,
            style = AppTextStyle.titleMd.copy(fontWeight = FontWeight.Bold),
            color = ColorTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 15.dp),
        )

        // 구분선: 3dp border-bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(ColorBorderSubtle),
        )

        // 거리 대형 숫자: height 121dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(121.dp)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "%.2f".format(distanceKm),
                style = AppTextStyle.displayLg.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-4.4).sp,
                ),
                color = ColorTextPrimary,
            )
            Spacer(modifier = Modifier.width(27.dp))
            Text(
                text = "km",
                style = AppTextStyle.titleLg.copy(fontWeight = FontWeight.Bold),
                color = ColorTextSecondary,
                modifier = Modifier.padding(top = 9.dp),
            )
        }

        // 요약 행: 뛴 시간 · 평균 페이스
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryMetric(value = formatTime(elapsedSeconds), label = stringResource(R.string.result_time_label))
            DotSeparator()
            SummaryMetric(value = formatPace(paceSecPerKm), label = stringResource(R.string.result_pace_label))
        }
    }
}

@Composable
private fun SummaryMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = AppTextStyle.titleMd.copy(fontWeight = FontWeight.Bold),
            color = ColorTextPrimary,
        )
        Text(
            text = label,
            style = AppTextStyle.bodySm,
            color = ColorTextSecondary,
        )
    }
}

@Composable
private fun DotSeparator() {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .size(4.dp)
            .clip(CircleShape)
            .background(ColorTextDisabled),
    )
}

// ── Map Card ─────────────────────────────────────────────────────────────────

@Composable
private fun MapCard(routePoints: String, locationLabel: String = "") {
    // 경로 좌표 파싱
    val points = remember(routePoints) {
        if (routePoints.isBlank()) emptyList()
        else routePoints.split("|").mapNotNull { segment ->
            val parts = segment.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) com.google.android.gms.maps.model.LatLng(lat, lng)
                else null
            } else null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .aspectRatio(342f / 280f)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.02f),
            )
            .clip(RoundedCornerShape(24.dp)),
    ) {
        if (points.size >= 2) {
            // 실제 Google Maps + 초록색 경로
            val context = LocalContext.current
            val mapStyle = remember {
                try {
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                } catch (e: Exception) {
                    null
                }
            }

            val cameraPositionState = rememberCameraPositionState {
                val mid = points[points.size / 2]
                position = CameraPosition.fromLatLngZoom(mid, 15f)
            }

            // 지도가 로드되면 경로에 맞게 카메라 이동
            LaunchedEffect(points) {
                if (points.size >= 2) {
                    val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                    points.forEach { bounds.include(it) }
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(bounds.build(), 56)
                    )
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = mapStyle,
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                ),
            ) {
                // 초록색 경로 폴리라인
                Polyline(
                    points = points,
                    color = ColorPrimary,
                    width = 14f,
                )
                // 시작점 마커 — 초록색 꽉찬 원 (선 두께와 비슷한 크기)
                Marker(
                    state = MarkerState(position = points.first()),
                    title = "시작",
                    icon = createCircleMarker(ColorPrimary, 10f),
                )
                // 도착점 마커 — 초록색 꽉찬 원
                Marker(
                    state = MarkerState(position = points.last()),
                    title = "도착",
                    icon = createCircleMarker(ColorPrimary, 10f),
                )
            }
        } else {
            // 경로 데이터 없을 때: 빈 지도 배경
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorSurfaceSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.result_no_route),
                    style = AppTextStyle.bodySm,
                    color = ColorTextDisabled,
                )
            }
        }

        // 위치 pill — 지도 오른쪽 위 코너 오버레이
        if (locationLabel.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(
                        Color.White.copy(alpha = 0.88f),
                        CircleShape,
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = locationLabel,
                    style = AppTextStyle.bodySm.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    ),
                    color = ColorPrimary,
                )
            }
        }
    }
}

// ── Earned Grid ──────────────────────────────────────────────────────────────

@Composable
private fun EarnedGrid(earnedXp: Int, earnedCredit: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EarnedCard(
            iconBg = null,
            iconColor = ColorExp,
            iconText = null,
            label = stringResource(R.string.result_xp_label),
            value = "+$earnedXp",
            unit = stringResource(R.string.result_xp_unit),
            modifier = Modifier.weight(1f),
        )
        EarnedCard(
            iconBg = ColorCreditBg,
            iconColor = ColorCredit,
            iconText = "★",
            label = stringResource(R.string.result_credit_label),
            value = "+$earnedCredit",
            unit = stringResource(R.string.result_credit_unit),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EarnedCard(
    iconBg: Color?,
    iconColor: Color,
    iconText: String?,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(128.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.03f),
            )
            .background(ColorSurface, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 아이콘 원형 (iconText가 있을 때만 표시)
        if (iconBg != null && iconText != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = iconText,
                    style = AppTextStyle.bodyMd.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                    ),
                    color = iconColor,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = label,
            style = AppTextStyle.bodySm,
            color = ColorTextSecondary,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.Bold),
            color = ColorTextPrimary,
        )

        Text(
            text = unit,
            style = AppTextStyle.micro,
            color = ColorTextDisabled,
        )
    }
}

// ── End Actions ──────────────────────────────────────────────────────────────

@Composable
private fun EndActions(
    isSaving: Boolean,
    isFromHistory: Boolean = false,
    onDone: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 완료/확인 버튼: height 60dp, pill, ColorPrimary
        Button(
            onClick = onDone,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.1f),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                ),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorPrimary,
                contentColor = ColorSurface,
                disabledContainerColor = ColorPrimary.copy(alpha = 0.5f),
                disabledContentColor = ColorSurface.copy(alpha = 0.5f),
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 체크 아이콘: 20dp 원 + 체크마크
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(ColorSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCheckIcon(ColorPrimary)
                    }
                }
                Text(
                    text = when {
                        isSaving -> stringResource(R.string.result_saving)
                        isFromHistory -> stringResource(R.string.result_confirm)
                        else -> stringResource(R.string.result_done)
                    },
                    style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        // 공유 버튼: height 60dp, outlined
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.05f),
                ),
            shape = CircleShape,
            border = BorderStroke(1.dp, ColorBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = ColorSurface,
                contentColor = ColorPrimary,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 공유 아이콘
                Canvas(modifier = Modifier.size(14.dp)) {
                    drawShareIcon(ColorPrimary)
                }
                Text(
                    text = stringResource(R.string.result_share),
                    style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}


// ── DrawScope Helpers ────────────────────────────────────────────────────────

private fun DrawScope.drawCheckIcon(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.2f, h * 0.5f)
        lineTo(w * 0.4f, h * 0.75f)
        lineTo(w * 0.8f, h * 0.25f)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

private fun DrawScope.drawShareIcon(color: Color) {
    val w = size.width
    val h = size.height
    val r = 2.5.dp.toPx()

    // 3개의 원 (노드)
    drawCircle(color, radius = r, center = Offset(w * 0.2f, h * 0.5f))
    drawCircle(color, radius = r, center = Offset(w * 0.8f, h * 0.2f))
    drawCircle(color, radius = r, center = Offset(w * 0.8f, h * 0.8f))

    // 연결선
    val strokeW = 1.5.dp.toPx()
    drawLine(color, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.2f), strokeW)
    drawLine(color, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.8f), strokeW)
}

// ── Map Helpers ──────────────────────────────────────────────────────────────

private fun createCircleMarker(color: androidx.compose.ui.graphics.Color, radiusDp: Float): com.google.android.gms.maps.model.BitmapDescriptor {
    val radiusPx = (radiusDp * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
    val size = radiusPx * 2
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt(),
        )
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(radiusPx.toFloat(), radiusPx.toFloat(), radiusPx.toFloat(), paint)
    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}

// ── Format Utilities ─────────────────────────────────────────────────────────

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

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 979)
@Composable
private fun ResultScreenPreview() {
    PetRunning2Theme {
        ResultScreenContent(
            uiState = ResultUiState(
                distanceKm = 3.82,
                elapsedSeconds = 1456L,
                paceSecPerKm = 380L,
                earnedXp = 48,
                earnedCredit = 12,
            ),
            onDone = {},
            onShare = {},
        )
    }
}
