package com.example.petrunning2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.petrunning2.R
import com.example.petrunning2.ui.theme.AppTextStyle
import com.example.petrunning2.ui.theme.ColorPrimaryActive
import com.example.petrunning2.ui.theme.ColorSurface
import com.example.petrunning2.ui.theme.ColorTextDisabled
import androidx.compose.ui.text.font.FontWeight

enum class BottomNavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_home, Icons.Filled.Home),
    Decoration("decoration", R.string.nav_decoration, Icons.Filled.AutoAwesome),
    Statistics("statistics", R.string.nav_statistics, Icons.Filled.ShowChart),
    Profile("profile", R.string.nav_profile, Icons.Filled.Person);

    companion object {
        val all = entries
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = ColorTextDisabled.copy(alpha = 0.15f),
                    ambientColor = ColorTextDisabled.copy(alpha = 0.08f),
                ),
            shape = RoundedCornerShape(24.dp),
            color = ColorSurface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavDestination.all.forEach { destination ->
                    BottomNavItem(
                        destination = destination,
                        isActive = currentRoute == destination.route,
                        onClick = { onItemClick(destination.route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: BottomNavDestination,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val color = if (isActive) ColorPrimaryActive else ColorTextDisabled
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val label = stringResource(destination.labelRes)
        Icon(
            imageVector = destination.icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            style = AppTextStyle.caption.copy(fontWeight = FontWeight.Medium),
            color = color,
        )
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = ColorPrimaryActive, shape = CircleShape),
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
