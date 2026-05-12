package com.example.petrunning2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorTextDisabled
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary

// 홈 오늘 카드, end_run 보상 카드, 통계 하단 카드에서 사용하는 세로형 지표 항목
@Composable
fun StatMetricItem(
    value: String,
    label: String,
    unit: String = "",
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        icon?.invoke()
        Text(
            text = value,
            style = AppTextStyle.titleMd,
            color = ColorTextPrimary,
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = AppTextStyle.micro,
                color = ColorTextDisabled,
            )
        }
        Text(
            text = label,
            style = AppTextStyle.bodySm,
            color = ColorTextSecondary,
        )
    }
}

// 기본 카드 컨테이너 - 화면별 카드에 사용
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.06f),
                ambientColor = Color.Black.copy(alpha = 0.04f),
            )
            .background(color = ColorSurface, shape = RoundedCornerShape(20.dp))
            .padding(20.dp),
        content = content,
    )
}
