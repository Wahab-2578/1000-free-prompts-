package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SilverFrost

enum class AppDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showInBottomBar: Boolean = true
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    CATEGORIES("categories", "Categories", Icons.Filled.Category, Icons.Outlined.Category),
    STUDIO("studio", "Studio", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
    LIBRARY("library", "Creations", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary),
    ASSISTANT("assistant", "AI Bot", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    FAVORITES("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    GENERATOR("generator", "Remix", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, false)
}

@Composable
fun GlassBottomNavBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    libraryCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestination.entries.filter { it.showInBottomBar }.forEach { destination ->
                val isSelected = currentDestination == destination

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) SilverFrost else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    label = "iconColor"
                )

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else Color.Transparent,
                    label = "pillBgColor"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(pillBgColor)
                        .clickable { onNavigate(destination) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("nav_item_${destination.route}")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.label,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )

                        if (destination == AppDestination.LIBRARY && libraryCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SilverFrost)
                            )
                        }
                    }

                    Text(
                        text = if (destination == AppDestination.LIBRARY && libraryCount > 0) "Creations ($libraryCount)" else destination.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = iconColor
                        )
                    )
                }
            }
        }
    }
}
