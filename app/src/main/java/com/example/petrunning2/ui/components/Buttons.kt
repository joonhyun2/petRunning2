package com.example.petrunning2.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBorder
import com.example.petrunning2.ui.theme.ColorPrimary
import com.example.petrunning2.ui.theme.ColorPrimaryLight
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorTextDisabled

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = ColorPrimary.copy(alpha = 0.25f),
            ),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPrimaryLight,
            contentColor = ColorSurface,
            disabledContainerColor = ColorTextDisabled,
            disabledContentColor = ColorSurface,
        ),
    ) {
        Text(text = text, style = AppTextStyle.titleSm)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ColorBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ColorPrimary,
        ),
    ) {
        Text(
            text = text,
            style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Bold),
        )
    }
}
