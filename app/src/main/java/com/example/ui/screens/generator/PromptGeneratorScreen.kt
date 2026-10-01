package com.example.ui.screens.generator

import android.content.Intent
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.SilverFrost
import java.io.File

@Composable
fun PromptGeneratorScreen(
    initialPrompt: PromptEntity? = null,
    onCopyPrompt: (String) -> Unit,
    onSaveToLibrary: (title: String, category: String, prompt: String, style: String) -> Unit,
    onGeneratePicture: (promptText: String, title: String, category: String, style: String) -> Unit = { _, _, _, _ -> },
    generatedImagePath: String? = null,
    isGeneratingPicture: Boolean = false,
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val subjects = listOf("Person / Portrait", "Supercar", "Luxury Watch", "Street Scene", "Coffee / Food", "Fashion Model", "Modern Villa", "Cybernetic Cyborg", "Mountain Peak")
    val styles = listOf("Luxury Photography", "Editorial Vogue", "Cinematic Film", "Hyper-Realistic 8k", "3D Octane Render", "Vintage 35mm", "Minimalist")
    val locations = listOf("Modern Penthouse", "Rainy Shinjuku Alley", "Big Sur Pacific Cliff", "Minimalist Concrete Studio", "Tuscan Vineyard", "Cyberpunk Den", "Parisian Balcony")
    val outfits = listOf("Tailored Cashmere Coat", "Oversized Matte Techwear", "Bespoke Silk Tuxedo", "Vintage Leather Jacket", "Minimalist Linen Robe", "High-Fashion Pleats")
    val lightings = listOf("Soft Studio Softbox", "Golden Hour Sunbeam", "Dramatic Chiaroscuro", "Neon Cyan & Amber Rim", "Overcast Window Light", "Volumetric Dust Light")
    val cameras = listOf("85mm Portrait Prime (f/1.2)", "35mm Wide Summilux (f/1.4)", "50mm Cinematic Anamorphic", "100mm Macro Telephoto", "24mm Architectural Tilt-Shift")
    val moods = listOf("Elegant & Regal", "Introspective & Moody", "High Energy & Vibrant", "Serene & Peaceful", "Mysterious & Noir", "Futuristic & Clean")
    val colorTones = listOf("Neutral Earthy with Warm Tint", "Deep Charcoal & Silver", "Teal Shadows & Warm Amber", "High-Key Crisp Monochrome", "Pastel Kodachrome 64")
    val compositions = listOf("Centered Rule-of-Thirds", "Dramatic Low-Angle Hero", "Intimate Extreme Macro", "Wide Epic Establishing Shot", "Cinematic Over-The-Shoulder")
    val backgrounds = listOf("Soft Bokeh City Lights", "Brutalist Concrete Wall", "Misty Pine Forest", "Seamless Infinity White Cyc", "Dark Marble & Burled Wood")

    var selectedSubject by remember { mutableStateOf(subjects[0]) }
    var selectedStyle by remember { mutableStateOf(styles[0]) }
    var selectedLocation by remember { mutableStateOf(locations[0]) }
    var selectedOutfit by remember { mutableStateOf(outfits[0]) }
    var selectedLighting by remember { mutableStateOf(lightings[0]) }
    var selectedCamera by remember { mutableStateOf(cameras[0]) }
    var selectedMood by remember { mutableStateOf(moods[0]) }
    var selectedColorTone by remember { mutableStateOf(colorTones[0]) }
    var selectedComposition by remember { mutableStateOf(compositions[0]) }
    var selectedBackground by remember { mutableStateOf(backgrounds[0]) }

    var generatedPromptText by remember { mutableStateOf("") }
    var generatedTitle by remember { mutableStateOf("") }

    LaunchedEffect(initialPrompt) {
        if (initialPrompt != null) {
            generatedPromptText = initialPrompt.prompt
            generatedTitle = "Remix: ${initialPrompt.title}"
        }
    }

    fun inferCategory(): String {
        return when {
            "Car" in selectedSubject -> "Cars"
            "Person" in selectedSubject -> "Portrait"
            "Fashion" in selectedSubject -> "Fashion"
            "Watch" in selectedSubject -> "Watches"
            "Food" in selectedSubject -> "Food"
            "Cyber" in selectedSubject -> "Gaming"
            "Mountain" in selectedSubject -> "Travel"
            else -> "Cinematic scenes"
        }
    }

    fun buildPrompt() {
        val subjectClean = selectedSubject.replace(" / ", " ")
        generatedTitle = "$selectedMood $subjectClean"
        generatedPromptText = (
            "A masterclass photograph of a $subjectClean in a $selectedLocation. " +
            "Style: $selectedStyle. " +
            "Wearing $selectedOutfit. " +
            "Atmospheric lighting: $selectedLighting. " +
            "Shot on $selectedCamera, composed with a $selectedComposition against $selectedBackground. " +
            "Mood: $selectedMood. " +
            "Color grade: $selectedColorTone. " +
            "Exceptional tactile details, authentic skin/material textures, award-winning cinematography, 8k resolution."
        )
    }

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
                    text = "Prompt Studio & Picture AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Craft prompts & generate stunning pictures powered by Gemini 2.5 Flash",
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
                modifier = Modifier.testTag("generator_library_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Library",
                    tint = SilverFrost,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Generated Picture Result Card (if generated)
        if (photoBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Generated Picture",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Powered by Gemini 2.5 Flash",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SilverFrost,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Saved in Library",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.5.dp, SilverFrost.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        ) {
                            Image(
                                bitmap = photoBitmap.asImageBitmap(),
                                contentDescription = "Generated Picture by Gemini 2.5 Flash",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToLibrary,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "View in Library", tint = SilverFrost, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View in Library", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Generated with Gemini 3.8 Flash on PromptVault AI:\n$generatedPromptText")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Generated Image"))
                                },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Selectors
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OptionSelectorRow("Subject", subjects, selectedSubject) { selectedSubject = it }
            OptionSelectorRow("Style", styles, selectedStyle) { selectedStyle = it }
            OptionSelectorRow("Location", locations, selectedLocation) { selectedLocation = it }
            OptionSelectorRow("Outfit / Wear", outfits, selectedOutfit) { selectedOutfit = it }
            OptionSelectorRow("Lighting", lightings, selectedLighting) { selectedLighting = it }
            OptionSelectorRow("Camera & Lens", cameras, selectedCamera) { selectedCamera = it }
            OptionSelectorRow("Mood", moods, selectedMood) { selectedMood = it }
            OptionSelectorRow("Color Tone", colorTones, selectedColorTone) { selectedColorTone = it }
            OptionSelectorRow("Composition", compositions, selectedComposition) { selectedComposition = it }
            OptionSelectorRow("Background", backgrounds, selectedBackground) { selectedBackground = it }

            Spacer(modifier = Modifier.height(4.dp))

            // Direct Custom Prompt Input Box
            Text(
                text = "Custom Prompt (Write or Edit)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            OutlinedTextField(
                value = generatedPromptText,
                onValueChange = {
                    generatedPromptText = it
                    if (generatedTitle.isBlank()) generatedTitle = "Custom Prompt"
                },
                placeholder = {
                    Text(
                        "Type any prompt (e.g. 'A futuristic supercar on a rainy neon highway, 8k cinematic lighting')",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_custom_text_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SilverFrost,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                ),
                maxLines = 4
            )

            // Buttons: Generate Prompt & Generate Picture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { buildPrompt() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("generate_prompt_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Build Prompt",
                        tint = SilverFrost
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Build Prompt",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Button(
                    onClick = {
                        val textToUse = if (generatedPromptText.isNotBlank()) generatedPromptText else {
                            buildPrompt()
                            generatedPromptText
                        }
                        val titleToUse = generatedTitle.ifBlank { "Custom Creation" }
                        val catToUse = inferCategory()
                        onGeneratePicture(textToUse, titleToUse, catToUse, selectedStyle)
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("generate_picture_button"),
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
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = SilverFrost)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    } else {
                        Text(
                            text = "🎨 Make Picture",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Generated Output Glass Card
            if (generatedPromptText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))

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
                            Text(
                                text = generatedTitle.ifEmpty { "Generated Prompt" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Ready to Copy",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SilverFrost,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = generatedPromptText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Copy, Save, Remix
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onCopyPrompt(generatedPromptText) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("generator_copy_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val cat = inferCategory()
                                    onSaveToLibrary(generatedTitle, cat, generatedPromptText, selectedStyle)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("generator_save_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    selectedLighting = lightings.random()
                                    selectedCamera = cameras.random()
                                    selectedMood = moods.random()
                                    buildPrompt()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("generator_remix_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Remix",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remix", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionSelectorRow(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
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
            options.forEach { opt ->
                val isSelected = selected == opt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) SilverFrost.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelect(opt) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}
