package com.example.ui.screens.photostudio

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FrameItem
import com.example.data.model.PhotoTemplate
import com.example.data.repository.PhotoStudioRepository
import com.example.ui.components.GlassCard
import com.example.ui.theme.SilverFrost
import java.io.File

@Composable
fun PhotoStudioScreen(
    selectedTemplate: PhotoTemplate?,
    onSelectTemplate: (PhotoTemplate) -> Unit,
    selectedFrame: FrameItem?,
    onSelectFrame: (FrameItem) -> Unit,
    selectedStyle: String,
    onSelectStyle: (String) -> Unit,
    selectedRatio: String,
    onSelectRatio: (String) -> Unit,
    customPrompt: String,
    onCustomPromptChange: (String) -> Unit,
    generationStage: String?,
    generatedResult: String?,
    generatedImagePath: String? = null,
    onGenerate: (Uri?) -> Unit,
    onClearResult: () -> Unit,
    onCopyPrompt: (String) -> Unit,
    libraryCount: Int = 0,
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uploadedPhotoUris = remember { mutableStateListOf<Uri>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uploadedPhotoUris.clear()
            uploadedPhotoUris.addAll(uris)
        }
    }

    val styles = listOf(
        "Realistic", "Cinematic", "Studio", "Luxury",
        "Vintage", "Editorial", "3D", "Fantasy", "Travel", "Wedding", "Minimal", "Professional"
    )

    val aspectRatios = listOf("1:1", "4:5", "3:4", "9:16", "16:9")
    val templateCategories = listOf("Couple", "Best Friends", "Brother & Sister", "Wedding", "Family", "Solo")
    var selectedCatFilter by remember { mutableStateOf("Couple") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Photo Generator & Frame Studio",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Upload photos to generate creative portraits, couple shots, family frames & avatars",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onNavigateToLibrary,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SilverFrost.copy(alpha = 0.35f))
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("studio_library_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Library",
                    tint = SilverFrost,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (libraryCount > 0) "Library ($libraryCount)" else "Library",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Generated Result Modal / Banner if generated
        if (generatedResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Generated Masterpiece",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            IconButton(
                                onClick = onClearResult,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val generatedBitmap = remember(generatedImagePath) {
                            if (generatedImagePath != null) {
                                val f = File(generatedImagePath)
                                if (f.exists()) {
                                    BitmapFactory.decodeFile(f.absolutePath)
                                } else null
                            } else null
                        }

                        // Composite Preview Display with Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1E2838), Color(0xFF0F1520))
                                    )
                                )
                                .border(
                                    2.dp,
                                    SilverFrost.copy(alpha = 0.5f),
                                    RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (generatedBitmap != null) {
                                Image(
                                    bitmap = generatedBitmap.asImageBitmap(),
                                    contentDescription = "Generated Photo by Gemini 2.5 Flash",
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
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Result",
                                        tint = SilverFrost,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = selectedTemplate?.title ?: "Custom Studio Creation",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Style: $selectedStyle • Frame: ${selectedFrame?.title ?: "Liquid Glass"}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFCBD5E1)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Facial structure & identity preserved with natural balance",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: View in Library, Share, Regenerate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToLibrary,
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(40.dp)
                                    .testTag("view_in_library_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Library", tint = SilverFrost, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View in Library", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Created with PromptVault AI Studio:\n${selectedTemplate?.basePrompt}")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Result"))
                                },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = { onGenerate(uploadedPhotoUris.firstOrNull()) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = SilverFrost, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section: Photo Upload Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Upload Your Photos",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Supports 1-4 people (Preserves facial features)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        if (uploadedPhotoUris.isNotEmpty()) {
                            IconButton(
                                onClick = { uploadedPhotoUris.clear() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear uploaded photos", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons: Choose from Gallery & Take Photo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("choose_from_gallery_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Gallery", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("From Gallery", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("take_photo_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Display Uploaded Photo Placeholders / Badges
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val count = if (uploadedPhotoUris.isEmpty()) 2 else uploadedPhotoUris.size
                        for (i in 0 until count) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Photo slot",
                                        tint = if (uploadedPhotoUris.isNotEmpty()) SilverFrost else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Person ${i + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Safety Note: Uploaded photos remain strictly on device for generative compositing. AI-generated results may differ from original photo.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Template Categories Tabs
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(
                text = "Select Generation Template",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templateCategories.forEach { cat ->
                    val isSelected = selectedCatFilter == cat
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
                            .clickable { selectedCatFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Templates for selected category
            val templatesForCat = PhotoStudioRepository.templates.filter { it.category == selectedCatFilter }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                templatesForCat.forEach { template ->
                    val isSelected = selectedTemplate?.id == template.id
                    GlassCard(
                        modifier = Modifier
                            .width(180.dp)
                            .height(115.dp),
                        shape = RoundedCornerShape(18.dp),
                        borderColor = if (isSelected) SilverFrost else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        onClick = { onSelectTemplate(template) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 2
                            )
                            Text(
                                text = template.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Frame Studio Selector
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(
                text = "Frame Studio Selection",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PhotoStudioRepository.frames.forEach { frame ->
                    val isSelected = selectedFrame?.id == frame.id
                    GlassCard(
                        modifier = Modifier
                            .width(160.dp)
                            .height(80.dp),
                        shape = RoundedCornerShape(16.dp),
                        borderColor = if (isSelected) SilverFrost else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        onClick = { onSelectFrame(frame) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = frame.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = frame.styleTheme,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Style and Aspect Ratio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Rendering Style",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { styleItem ->
                    val isSelected = selectedStyle == styleItem
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .border(1.dp, if (isSelected) SilverFrost.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .clickable { onSelectStyle(styleItem) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = styleItem,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Aspect Ratio",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aspectRatios.forEach { ratio ->
                    val isSelected = selectedRatio == ratio
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .border(1.dp, if (isSelected) SilverFrost.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .clickable { onSelectRatio(ratio) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ratio,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Prompt
            Text(
                text = "Custom Instruction (Optional)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = customPrompt,
                onValueChange = onCustomPromptChange,
                placeholder = {
                    Text(
                        "e.g. 'Create a cinematic sunset photo of these people standing beside a vintage car'",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_studio_prompt_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SilverFrost,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Generate Button
            Button(
                onClick = { onGenerate(uploadedPhotoUris.firstOrNull()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_image_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SilverFrost.copy(alpha = 0.5f))
                ),
                enabled = generationStage == null
            ) {
                if (generationStage != null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = SilverFrost
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = generationStage,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generate",
                        tint = SilverFrost
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Image",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}
