package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryItem
import com.example.data.model.PromptEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.PromptCard
import com.example.ui.theme.SilverFrost

@Composable
fun HomeScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilterChip: String,
    onSelectFilterChip: (String) -> Unit,
    categories: List<CategoryItem>,
    onSelectCategory: (String) -> Unit,
    trendingPrompts: List<PromptEntity>,
    filteredPrompts: List<PromptEntity>,
    onPromptClick: (PromptEntity) -> Unit,
    onCopyPrompt: (String) -> Unit,
    onToggleFavorite: (Int, Boolean) -> Unit,
    onRemixPrompt: (PromptEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGenerator: () -> Unit,
    onOpenLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filterChips = listOf(
        "All", "Realistic", "Cinematic", "3D", "Portrait",
        "Product", "Fashion", "Anime", "Photography", "Luxury"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PromptVault AI",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "1000+ AI Image Prompts",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.3.sp
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onOpenLibrary,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                                .testTag("quick_library_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Creations Library",
                                tint = SilverFrost,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenGenerator,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                                .testTag("quick_remix_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Quick Remix Generator",
                                tint = SilverFrost,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                                .testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = SilverFrost,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                GlassSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterChips.forEach { chip ->
                        val isSelected = selectedFilterChip == chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) SilverFrost.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectFilterChip(chip) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("filter_chip_$chip")
                        ) {
                            Text(
                                text = chip,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // Featured Categories Section (when not actively searching)
        if (searchQuery.isEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Featured Categories",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    val featuredCats = categories.take(12)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(featuredCats) { cat ->
                            CategoryGlassPill(
                                item = cat,
                                onClick = { onSelectCategory(cat.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Trending Prompts Section
            if (trendingPrompts.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Trending",
                                tint = SilverFrost,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trending Prompts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }

                        TextButton(onClick = { onSelectFilterChip("All") }) {
                            Text(
                                text = "View All (${filteredPrompts.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = SilverFrost,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Prompts List
        items(filteredPrompts, key = { it.id }) { prompt ->
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                PromptCard(
                    prompt = prompt,
                    onCopy = onCopyPrompt,
                    onToggleFavorite = onToggleFavorite,
                    onRemix = onRemixPrompt,
                    onClick = { onPromptClick(prompt) }
                )
            }
        }
    }
}

@Composable
fun CategoryGlassPill(
    item: CategoryItem,
    onClick: () -> Unit
) {
    val icon = when (item.name) {
        "Selfie" -> Icons.Default.Face
        "Portrait" -> Icons.Default.Face
        "Fashion" -> Icons.Default.LocalMall
        "Luxury" -> Icons.Default.Star
        "Cars" -> Icons.Default.DirectionsCar
        "Products", "Product photography" -> Icons.Default.CameraAlt
        "Cinematic", "Cinematic scenes" -> Icons.Default.Movie
        "Gaming" -> Icons.Default.SportsEsports
        "Travel" -> Icons.Default.Flight
        "Food" -> Icons.Default.Restaurant
        "3D", "3D renders" -> Icons.Default.ViewInAr
        else -> Icons.Default.AutoAwesome
    }

    GlassCard(
        modifier = Modifier
            .width(136.dp)
            .height(84.dp),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.name,
                    tint = SilverFrost,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "${item.count}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}
