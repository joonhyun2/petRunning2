package com.example.petrunning2.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.petrunning2.R
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorBorder
import com.example.petrunning2.ui.theme.ColorPrimary
import com.example.petrunning2.ui.theme.ColorPrimaryLight
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorTextDisabled
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary
import com.example.petrunning2.ui.theme.PetRunning2Theme

@Composable
fun EditNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    val canConfirm = name.isNotBlank() && name != currentName

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
            // 제목
            Text(
                text = stringResource(R.string.edit_name_title),
                style = AppTextStyle.titleSm,
                color = ColorTextPrimary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 입력 필드
            val borderColor = if (name.isNotBlank()) ColorPrimaryLight else ColorBorder
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = AppTextStyle.bodyLg.copy(color = ColorTextPrimary),
                cursorBrush = SolidColor(ColorPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .background(ColorBg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (name.isEmpty()) {
                            Text(
                                text = stringResource(R.string.edit_name_placeholder),
                                style = AppTextStyle.bodyLg,
                                color = ColorTextDisabled,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 버튼 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 취소
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.edit_name_cancel),
                        style = AppTextStyle.bodyLg.copy(fontWeight = FontWeight.Medium),
                        color = ColorTextSecondary,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 확인
                TextButton(
                    onClick = { onConfirm(name) },
                    enabled = canConfirm,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.edit_name_confirm),
                        style = AppTextStyle.bodyLg.copy(fontWeight = FontWeight.Medium),
                        color = if (canConfirm) ColorPrimary else ColorTextDisabled,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditNameDialogPreview() {
    PetRunning2Theme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(32.dp),
        ) {
            EditNameDialog(
                currentName = "모모",
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
