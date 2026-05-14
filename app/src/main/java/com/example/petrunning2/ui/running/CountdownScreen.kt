package com.example.petrunning2.ui.running

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.PetRunning2Theme
import kotlinx.coroutines.delay

@Composable
fun CountdownScreen(onCountdownFinished: () -> Unit) {
    var count by remember { mutableIntStateOf(3) }
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(count) {
        scale.snapTo(0.4f)
        alpha.snapTo(0f)
        scale.animateTo(1f, animationSpec = tween(300))
        alpha.animateTo(1f, animationSpec = tween(200))
        delay(700)
        alpha.animateTo(0f, animationSpec = tween(200))
        if (count > 1) {
            count -= 1
        } else {
            onCountdownFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$count",
            style = AppTextStyle.displayXl.copy(
                fontWeight = FontWeight.ExtraBold,
                color = ColorTextPrimary,
            ),
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CountdownScreenPreview() {
    PetRunning2Theme {
        CountdownScreen(onCountdownFinished = {})
    }
}
