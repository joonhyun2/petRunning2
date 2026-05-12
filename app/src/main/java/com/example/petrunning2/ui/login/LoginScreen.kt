package com.example.petrunning2.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petrunning2.R
import com.example.petrunning2.ui.components.AppLogo
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorBg
import com.example.petrunning2.ui.theme.ColorPrimaryLight
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorSurfaceSoft
import com.example.petrunning2.ui.theme.ColorTextPrimary
import com.example.petrunning2.ui.theme.ColorTextSecondary
import com.example.petrunning2.ui.theme.PetRunning2Theme

@Composable
fun LoginScreen(
    onGuestLogin: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // 로고
            AppLogo(modifier = Modifier.size(48.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // 앱 이름
            Text(
                text = "Pet Running",
                style = AppTextStyle.titleLg.copy(fontWeight = FontWeight.ExtraBold),
                color = ColorPrimaryLight,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 서브 텍스트
            Text(
                text = "달리면서 캐릭터를 키워보세요",
                style = AppTextStyle.bodyMd,
                color = ColorTextSecondary,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 캐릭터 이미지
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(ColorSurfaceSoft, RoundedCornerShape(100.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.pet),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // 게스트 로그인 버튼
            Button(
                onClick = onGuestLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = ColorPrimaryLight.copy(alpha = 0.25f),
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimaryLight,
                    contentColor = ColorSurface,
                ),
            ) {
                Text(
                    text = "게스트로 시작하기",
                    style = AppTextStyle.titleSm.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 안내 텍스트
            Text(
                text = "로그인 없이 바로 시작할 수 있어요",
                style = AppTextStyle.bodySm,
                color = ColorTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 394, heightDp = 844)
@Composable
private fun LoginScreenPreview() {
    PetRunning2Theme {
        LoginScreen(onGuestLogin = {})
    }
}
