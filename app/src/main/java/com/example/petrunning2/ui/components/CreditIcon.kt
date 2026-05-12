package com.example.petrunning2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petrunning2.ui.theme.ColorCredit
import com.example.petrunning2.ui.theme.ColorSurface

/**
 * 앱 전체에서 사용하는 크레딧(코인) 아이콘.
 * 노란 원 + 흰색 ★
 */
@Composable
fun CreditIcon(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ColorCredit),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "★",
            style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                color = ColorSurface,
                fontSize = (size.value * 0.5f).sp,
                lineHeight = (size.value * 0.5f).sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            modifier = Modifier.offset(y = -(size * 0.04f)),
        )
    }
}
