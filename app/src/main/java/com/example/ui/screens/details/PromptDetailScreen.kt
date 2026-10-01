package com.example.ui.screens.details

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PromptEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.HeartRedSubtle
import com.example.ui.theme.SilverFrost
import java.io.File

@Composable
fun PromptDetailScreen(
    prompt: PromptEntity,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onToggleFavorite: (Int, Boolean) -> Unit,
    onRemix: (PromptEntity) -> Unit,
    onGeneratePicture: ((promptText: String, title: String, category: String, style: String) -> Unit)? = null,
    generatedImagePath: String? = null,
    isGeneratingPicture: Boolean = false,
    onNavigateToLibrary: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val formattedId = String.format("Prompt #%03d", prompt.id)

    val photoBitmap = remember(generatedImagePath) {
        generatedImagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .testTag("detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { onToggleFavorite(prompt.id, !prompt.isFavorite) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("detail_favorite_button")
                ) {
                    Icon(
                        imageVector = if (prompt.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (prompt.isFavorite) HeartRedSubtle else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${prompt.title}\n\n${prompt.prompt}\n\nShared via PromptVault AI")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Prompt"))
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("detail_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Large Preview Banner / Generated Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E2838),
                            Color(0xFF0F141E)
                        )
                    )
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (photoBitmap != null) {
                Image(
                    bitmap = photoBitmap.asImageBitmap(),
                    contentDescription = "Generated Picture by Gemini 2.5 Flash",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                // Overlay pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Gemini 2.5 Flash",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SilverFrost,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FFFFFF))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SilverFrost,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = prompt.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ratio: ${prompt.aspectRatio} • Style: ${prompt.style}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
        }

        // If generated picture exists, show a "View in Library" button right here
        if (photoBitmap != null && onNavigateToLibrary != null) {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Button(
                    onClick = onNavigateToLibrary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "View in Library", tint = SilverFrost, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Picture saved in Library! Tap to view", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Full Prompt Large Rounded Glass Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Full Prompt Specification",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedId,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SilverFrost
                            )
                        )

                        Text(
                            text = prompt.source,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = prompt.prompt,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags
            Text(
                text = "Tags & Characteristics",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prompt.tagList.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Generate Picture Button with Gemini 3.8 Flash
            if (onGeneratePicture != null) {
                Button(
                    onClick = {
                        onGeneratePicture(prompt.prompt, prompt.title, prompt.category, prompt.style)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("detail_generate_picture_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SilverFrost.copy(alpha = 0.5f))
                    ),
                    enabled = !isGeneratingPicture
                ) {
                    if (isGeneratingPicture) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = SilverFrost
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering with Gemini 2.5 Flash...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Make Picture",
                            tint = SilverFrost
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎨 Make Picture with Gemini 2.5 Flash",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Primary Action Buttons
            Button(
                onClick = { onCopy(prompt.prompt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_copy_prompt_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SilverFrost.copy(alpha = 0.3f))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy prompt",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy Full Prompt",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { onRemix(prompt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_remix_prompt_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Remix prompt",
                    tint = SilverFrost,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Remix in Prompt Studio",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
