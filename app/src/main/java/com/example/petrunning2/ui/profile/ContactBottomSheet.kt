package com.example.petrunning2.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petrunning2.R
import com.example.petrunning2.ui.components.PrimaryButton
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorBorder
import com.example.petrunning2.ui.theme.ColorBorderSubtle
import com.example.petrunning2.ui.theme.ColorPrimary
import com.example.petrunning2.ui.theme.ColorPrimaryActive
import com.example.petrunning2.ui.theme.ColorPrimaryLight
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorSurfaceSoft
import com.example.petrunning2.ui.theme.ColorTextDisabled
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary
import com.example.petrunning2.ui.theme.PetRunning2Theme

private enum class ContactCategory(@StringRes val labelRes: Int) {
    Bug(R.string.contact_category_bug),
    Feature(R.string.contact_category_feature),
    Other(R.string.contact_category_other),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var selectedCategory by remember { mutableStateOf(ContactCategory.Bug) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val canSend = subject.isNotBlank() && message.isNotBlank()
    val context = LocalContext.current
    val categoryLabel = stringResource(selectedCategory.labelRes)
    val emailChooserTitle = stringResource(R.string.contact_email_chooser)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(ColorBorder),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            // ── Header ──
            Text(
                text = stringResource(R.string.contact_title),
                style = AppTextStyle.titleMd,
                color = ColorTextPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.contact_subtitle),
                style = AppTextStyle.bodyMd,
                color = ColorTextSecondary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Category Chips ──
            Text(
                text = stringResource(R.string.contact_category_label),
                style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Medium),
                color = ColorTextSecondary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ContactCategory.entries.forEach { category ->
                    CategoryChip(
                        label = stringResource(category.labelRes),
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Subject Field ──
            Text(
                text = stringResource(R.string.contact_subject_label),
                style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Medium),
                color = ColorTextSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ContactTextField(
                value = subject,
                onValueChange = { subject = it },
                placeholder = stringResource(R.string.contact_subject_placeholder),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Message Field ──
            Text(
                text = stringResource(R.string.contact_message_label),
                style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Medium),
                color = ColorTextSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ContactTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = stringResource(R.string.contact_message_placeholder),
                singleLine = false,
                minLines = 5,
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.contact_email_notice),
                style = AppTextStyle.bodySm,
                color = ColorTextDisabled,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Send Button ──
            PrimaryButton(
                text = stringResource(R.string.contact_send_button),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("mharuki527@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "[$categoryLabel] $subject")
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, emailChooserTitle))
                    } catch (e: ActivityNotFoundException) {
                        // no email app installed
                    }
                    onDismiss()
                },
                enabled = canSend,
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) ColorSurfaceSoft else ColorSurface,
        label = "chip_bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) ColorPrimaryLight else ColorBorder,
        label = "chip_border",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) ColorPrimaryActive else ColorTextSecondary,
        label = "chip_text",
    )

    Box(
        modifier = Modifier
            .height(34.dp)
            .border(1.dp, borderColor, CircleShape)
            .background(bgColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = AppTextStyle.bodyMd.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            ),
            color = textColor,
        )
    }
}

@Composable
private fun ContactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    minLines: Int = 1,
) {
    val borderColor = if (value.isNotBlank()) ColorPrimaryLight else ColorBorder

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = AppTextStyle.bodyLg.copy(color = ColorTextPrimary),
        cursorBrush = SolidColor(ColorPrimary),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(ColorBg, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = AppTextStyle.bodyLg,
                        color = ColorTextDisabled,
                    )
                }
                innerTextField()
            }
        },
    )
}

// ── Preview ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 414, heightDp = 883)
@Composable
private fun ContactBottomSheetPreview() {
    PetRunning2Theme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF000000).copy(alpha = 0.3f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(ColorSurface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                var selectedCategory by remember { mutableStateOf(ContactCategory.Bug) }
                var subject by remember { mutableStateOf("") }
                var message by remember { mutableStateOf("") }
                val canSend = subject.isNotBlank() && message.isNotBlank()

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(ColorBorder),
                )

                Text(text = stringResource(R.string.contact_title), style = AppTextStyle.titleMd, color = ColorTextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(text = stringResource(R.string.contact_subtitle), style = AppTextStyle.bodyMd, color = ColorTextSecondary)
                Spacer(Modifier.height(24.dp))
                Text(text = stringResource(R.string.contact_category_label), style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Medium), color = ColorTextSecondary)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContactCategory.entries.forEach { cat ->
                        CategoryChip(label = stringResource(cat.labelRes), selected = selectedCategory == cat, onClick = { selectedCategory = cat })
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(text = stringResource(R.string.contact_subject_label), style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Medium), color = ColorTextSecondary)
                Spacer(Modifier.height(8.dp))
                ContactTextField(value = subject, onValueChange = { subject = it }, placeholder = stringResource(R.string.contact_subject_placeholder), singleLine = true)
                Spacer(Modifier.height(16.dp))
                Text(text = stringResource(R.string.contact_message_label), style = AppTextStyle.bodyMd.copy(fontWeight = FontWeight.Medium), color = ColorTextSecondary)
                Spacer(Modifier.height(8.dp))
                ContactTextField(value = message, onValueChange = { message = it }, placeholder = stringResource(R.string.contact_message_placeholder), singleLine = false, minLines = 5)
                Spacer(Modifier.height(8.dp))
                Text(text = stringResource(R.string.contact_email_notice), style = AppTextStyle.bodySm, color = ColorTextDisabled)
                Spacer(Modifier.height(24.dp))
                PrimaryButton(text = stringResource(R.string.contact_send_button), onClick = {}, enabled = canSend)
            }
        }
    }
}
