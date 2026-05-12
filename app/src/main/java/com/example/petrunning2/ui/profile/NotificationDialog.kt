package com.example.petrunning2.ui.profile

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBorderSubtle
import com.example.petrunning2.ui.theme.ColorPrimary
import com.example.petrunning2.ui.theme.ColorPrimaryChart
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorTextDisabled
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary

@Composable
fun NotificationDialog(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onEnabledChange(true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f),
                )
                .background(ColorSurface, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                text = "앱 알림",
                style = AppTextStyle.titleSm,
                color = ColorTextPrimary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "알림",
                    style = AppTextStyle.bodyLg.copy(fontWeight = FontWeight.Medium),
                    color = ColorTextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { wantEnable ->
                        if (wantEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onEnabledChange(wantEnable)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ColorSurface,
                        checkedTrackColor = ColorPrimaryChart,
                        uncheckedThumbColor = ColorSurface,
                        uncheckedTrackColor = ColorBorderSubtle,
                        uncheckedBorderColor = ColorBorderSubtle,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "활동 알림, 동기 부여 관련 정보를 받겠습니다.",
                style = AppTextStyle.bodySm.copy(fontSize = 12.sp),
                color = ColorTextDisabled,
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onConfirm,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = "확인",
                    style = AppTextStyle.bodyLg.copy(fontWeight = FontWeight.Medium),
                    color = ColorPrimary,
                )
            }
        }
    }
}
