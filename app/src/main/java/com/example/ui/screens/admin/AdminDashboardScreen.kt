package com.example.ui.screens.admin

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.SilverFrost

@Composable
fun AdminDashboardScreen(
    totalPrompts: Int,
    favoriteCount: Int,
    customCount: Int,
    categoriesCount: Int,
    pendingCount: Int,
    onGeneratePrompts: () -> Unit,
    onDeleteDuplicates: () -> Unit,
    onExportDatabase: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .testTag("admin_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Admin & Creator Dashboard",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "System telemetry, database operations & scaling",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stats Matrix
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "Total Prompts",
                    value = "$totalPrompts",
                    icon = Icons.Default.LibraryBooks,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "AI Generated",
                    value = "$customCount",
                    icon = Icons.Default.Psychology,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "Categories",
                    value = "$categoriesCount",
                    icon = Icons.Default.Category,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "User Favorites",
                    value = "$favoriteCount",
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "Pending Review",
                    value = "$pendingCount",
                    icon = Icons.Default.HourglassTop,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Duplicates Cleaned",
                    value = "0",
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Controls
            Text(
                text = "Database Management Controls",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SilverFrost
                )
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onGeneratePrompts,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_generate_prompts_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Generate", tint = SilverFrost)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Launch AI Research Agent", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onExportDatabase,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_export_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Database (JSON Backup)", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onDeleteDuplicates,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_delete_duplicates_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clean Duplicates")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detect & Delete Redundant Prompts", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Architecture Note
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PromptVault AI Scalable Architecture",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Mobile App: Jetpack Compose Liquid Glass UI\n• Prompt Engine: Indexed Room SQLite supporting 10,000+ entries\n• Assistant Agent: Duplicate screening & semantic normalization\n• Privacy: Zero external telemetry, encrypted sandbox storage",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = SilverFrost,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
