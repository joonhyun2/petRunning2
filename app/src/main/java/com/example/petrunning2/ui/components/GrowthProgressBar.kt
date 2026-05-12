package com.example.petrunning2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorPrimaryLight

@Composable
fun GrowthProgressBar(
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp)
            .heightIn(min = 21.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Lv.$level",
            style = AppTextStyle.bodyMd.copy(
                fontWeight = FontWeight.Bold,
                color = ColorPrimaryLight,
            ),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE1E5D9)),
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
            style = AppTextStyle.bodyMd.copy(
                fontWeight = FontWeight.Bold,
                color = ColorPrimaryLight,
            ),
        )
    }
}
