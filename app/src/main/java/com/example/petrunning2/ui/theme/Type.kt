package com.example.petrunning2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: 배포 전 Noto Sans KR 폰트 파일을 res/font/에 추가하고 FontFamily로 교체
private val appFontFamily = FontFamily.Default

object AppTextStyle {
    val displayXl = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 88.sp,
        lineHeight = 88.sp,
    )
    val displayLg = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 54.sp,
        lineHeight = 88.sp,
    )
    val metricLg = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
    )
    val metricMd = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 32.sp,
    )
    val titleLg = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    )
    val titleMd = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    )
    val titleSm = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    )
    val bodyLg = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val bodyMd = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val bodySm = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val caption = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )
    val micro = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        lineHeight = 12.sp,
    )
}

val Typography = Typography(
    displayLarge = AppTextStyle.displayXl,
    displayMedium = AppTextStyle.displayLg,
    titleLarge = AppTextStyle.titleLg,
    titleMedium = AppTextStyle.titleMd,
    titleSmall = AppTextStyle.titleSm,
    bodyLarge = AppTextStyle.bodyLg,
    bodyMedium = AppTextStyle.bodyMd,
    bodySmall = AppTextStyle.bodySm,
    labelSmall = AppTextStyle.caption,
)
