package com.example.ui.screens.creations

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.model.GeneratedPhotoEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.SilverFrost
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyCreationsScreen(
    photos: List<GeneratedPhotoEntity>,
    onDeletePhoto: (Int) -> Unit,
    onClearAllPhotos: () -> Unit,
    onCopyPrompt: (String) -> Unit,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("All") }
    var selectedPhotoForDetail by remember { mutableStateOf<GeneratedPhotoEntity?>(null) }
    var showClearConfirmationDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Portrait", "Couple", "Solo", "Supercar", "Luxury", "Fashion", "Custom")

    // Filter by category and search query
    val filteredList = photos.filter { photo ->
        val matchesCategory = if (selectedFilterCategory == "All") true else photo.category.contains(selectedFilterCategory, ignoreCase = true)
        val matchesSearch = if (searchQuery.isBlank()) true else {
            photo.title.contains(searchQuery, ignoreCase = true) ||
            photo.promptUsed.contains(searchQuery, ignoreCase = true) ||
            photo.category.contains(searchQuery, ignoreCase = true) ||
            photo.style.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    // Compute device storage usage for all creations
    val totalStorageBytes = remember(photos) {
        photos.sumOf { photo ->
            photo.imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) file.length() else 0L
            } ?: 0L
        }
    }

    val formattedStorageSize = remember(totalStorageBytes) {
        val mb = totalStorageBytes / (1024.0 * 1024.0)
        if (mb >= 1.0) String.format(Locale.getDefault(), "%.1f MB", mb)
        else String.format(Locale.getDefault(), "%.0f KB", totalStorageBytes / 1024.0)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("my_creations_screen"),
        contentPadding = PaddingValues(bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Section
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Creations",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Locally stored AI creations • Room DB & Storage",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    if (photos.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmationDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                                .testTag("clear_creations_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All Creations",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Storage and Database Stats Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = "Database", tint = SilverFrost, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${photos.size} Images",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                )
                                Text(
                                    text = "Room Database",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                )
                            }
                        }

                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = "Storage", tint = SilverFrost, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = formattedStorageSize,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                )
                                Text(
                                    text = "Device Storage",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search creations by title or prompt...", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)))
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = SilverFrost, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("creations_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SilverFrost,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedFilterCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) SilverFrost.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedFilterCategory = category }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("creations_category_$category")
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // Empty State
        if (filteredList.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .border(1.5.dp, SilverFrost.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "No Creations",
                                    tint = SilverFrost,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = if (searchQuery.isNotEmpty() || selectedFilterCategory != "All") "No Matching Creations" else "No Creations Yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (searchQuery.isNotEmpty() || selectedFilterCategory != "All") {
                                    "No locally saved images matched '$searchQuery'. Try adjusting filters."
                                } else {
                                    "Generate custom images with Gemini 2.5 Flash in Photo Studio or Prompt Remix to view them stored locally in your database."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onNavigateToStudio,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("empty_creations_studio_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(SilverFrost.copy(alpha = 0.4f))
                                )
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Photo Studio", tint = SilverFrost, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Create in Studio",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Creations Grid Cards
            items(filteredList, key = { it.id }) { photo ->
                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    CreationGridCard(
                        photo = photo,
                        onClick = { selectedPhotoForDetail = photo },
                        onDelete = { onDeletePhoto(photo.id) },
                        onShare = { sharePhoto(context, photo) }
                    )
                }
            }
        }
    }

    // Detail Dialog
    selectedPhotoForDetail?.let { photo ->
        CreationDetailDialog(
            photo = photo,
            onDismiss = { selectedPhotoForDetail = null },
            onCopyPrompt = onCopyPrompt,
            onDelete = {
                onDeletePhoto(photo.id)
                selectedPhotoForDetail = null
            },
            onShare = { sharePhoto(context, photo) }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            title = {
                Text("Clear All Creations?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will permanently remove all stored metadata from the Room database and delete the generated picture files from your device storage.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllPhotos()
                        showClearConfirmationDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CreationGridCard(
    photo: GeneratedPhotoEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val dateStr = remember(photo.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(photo.timestamp))
    }

    // Decode image from local file storage
    val photoBitmap = remember(photo.imagePath) {
        photo.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        }
    }

    val fileSizeStr = remember(photo.imagePath) {
        photo.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val kb = file.length() / 1024
                if (kb > 1024) String.format(Locale.getDefault(), "%.1f MB", kb / 1024.0) else "$kb KB"
            } else null
        }
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("creation_card_${photo.id}"),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Visual Preview Box loaded from storage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(photo.primaryColorHex),
                                Color(photo.secondaryColorHex)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        SilverFrost.copy(alpha = 0.35f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (photoBitmap != null) {
                    Image(
                        bitmap = photoBitmap.asImageBitmap(),
                        contentDescription = photo.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Photo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = photo.style,
                            style = MaterialTheme.typography.labelSmall.copy(color = SilverFrost, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                    }
                }

                // Aspect ratio & file size pill at top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xAA000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (fileSizeStr != null) "${photo.aspectRatio} • $fileSizeStr" else photo.aspectRatio,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = photo.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = photo.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun CreationDetailDialog(
    photo: GeneratedPhotoEntity,
    onDismiss: () -> Unit,
    onCopyPrompt: (String) -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val dateStr = remember(photo.timestamp) {
        val sdf = SimpleDateFormat("MMMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(photo.timestamp))
    }

    val photoBitmap = remember(photo.imagePath) {
        photo.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        }
    }

    val fileInfo = remember(photo.imagePath) {
        photo.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val sizeKb = file.length() / 1024
                val sizeStr = if (sizeKb > 1024) String.format(Locale.getDefault(), "%.1f MB", sizeKb / 1024.0) else "$sizeKb KB"
                Pair(file.name, sizeStr)
            } else Pair("Not found on disk", "0 KB")
        } ?: Pair("Unknown", "0 KB")
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with title & close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = photo.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High Resolution Image Preview from Local Device Storage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(photo.primaryColorHex), Color(photo.secondaryColorHex))
                            )
                        )
                        .border(2.dp, SilverFrost.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap.asImageBitmap(),
                            contentDescription = photo.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = photo.title, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Metadata Overlay pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xDD000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${fileInfo.second} • ${photo.aspectRatio} • Gemini 2.5 Flash",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SilverFrost,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prompt Section
                Text(
                    text = "Prompt Instruction",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = photo.promptUsed,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        ),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Copy Prompt, Share, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCopyPrompt(photo.promptUsed) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

private fun sharePhoto(context: Context, photo: GeneratedPhotoEntity) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, photo.title)
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "✨ Created with Gemini 2.5 Flash on PromptVault AI\n\nPrompt:\n${photo.promptUsed}"
        )

        photo.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val uri: Uri = try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (e: Exception) {
                    Uri.fromFile(file)
                }
                shareIntent.type = "image/png"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share AI Creation"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
