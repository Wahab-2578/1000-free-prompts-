package com.example.ui.screens.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.SilverFrost
import com.example.viewmodel.AssistantStep
import com.example.viewmodel.DiscoveredPrompt

@Composable
fun AIAssistantScreen(
    step: AssistantStep,
    logList: List<String>,
    discoveredPrompts: List<DiscoveredPrompt>,
    batchProgress: String,
    onSearchAndGenerate: (topic: String, batch: Int) -> Unit,
    onAddToLibrary: (DiscoveredPrompt) -> Unit,
    onReject: (DiscoveredPrompt) -> Unit,
    onRegenerate: (DiscoveredPrompt) -> Unit,
    onOpenAdminDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputTopic by remember { mutableStateOf("") }
    var selectedBatchCount by remember { mutableStateOf(1) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Prompt Assistant",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Professional AI research & prompt generation agent",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                IconButton(
                    onClick = onOpenAdminDashboard,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("admin_dashboard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Dashboard",
                        tint = SilverFrost,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Input Card: "What should I find for you?"
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "What should I find for you?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "e.g., 'Find new cinematic car photography prompt ideas' or 'Trending AI selfie prompts'",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputTopic,
                        onValueChange = { inputTopic = it },
                        placeholder = {
                            Text(
                                "Enter topic or creative concept...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("assistant_input_topic"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SilverFrost,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (inputTopic.isNotBlank()) onSearchAndGenerate(inputTopic, selectedBatchCount)
                        })
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Batch Mode Selector
                    Text(
                        text = "AI Batch Size",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1 to "Single (1)", 10 to "Batch 10", 50 to "Batch 50", 100 to "Batch 100").forEach { (count, label) ->
                            val isSelected = selectedBatchCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) SilverFrost.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedBatchCount = count }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search & Generate Button
                    Button(
                        onClick = {
                            val topic = inputTopic.ifBlank { "Cinematic luxury automotive photography" }
                            onSearchAndGenerate(topic, selectedBatchCount)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("search_and_generate_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(SilverFrost.copy(alpha = 0.4f))
                        ),
                        enabled = step == AssistantStep.IDLE || step == AssistantStep.COMPLETED
                    ) {
                        if (step != AssistantStep.IDLE && step != AssistantStep.COMPLETED) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = SilverFrost
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(step.label, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SilverFrost,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Search & Generate",
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

        // Live Timeline Progress
        if (step != AssistantStep.IDLE) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Assistant Workflow Timeline",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            if (batchProgress.isNotEmpty()) {
                                Text(
                                    text = batchProgress,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SilverFrost,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val stepsList = listOf(
                            AssistantStep.SEARCHING to "Searching web sources",
                            AssistantStep.ANALYZING to "Analyzing results",
                            AssistantStep.CREATING to "Creating original prompts",
                            AssistantStep.CHECKING_DUPLICATES to "Checking duplicates",
                            AssistantStep.CATEGORIZING to "Categorizing prompts",
                            AssistantStep.COMPLETED to "Ready to add"
                        )

                        stepsList.forEach { (s, title) ->
                            val isPast = step.ordinal > s.ordinal
                            val isCurrent = step == s

                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPast) Color(0xFF388E8E)
                                            else if (isCurrent) SilverFrost
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPast) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Done",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    } else if (isCurrent) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp,
                                            color = Color.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Review Discovered Prompts Section
        if (discoveredPrompts.isNotEmpty()) {
            item {
                Text(
                    text = "Discovered & Transformed Prompts (${discoveredPrompts.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            items(discoveredPrompts) { promptItem ->
                DiscoveredPromptReviewCard(
                    item = promptItem,
                    onAdd = { onAddToLibrary(promptItem) },
                    onReject = { onReject(promptItem) },
                    onRegenerate = { onRegenerate(promptItem) }
                )
            }
        }
    }
}

@Composable
fun DiscoveredPromptReviewCard(
    item: DiscoveredPrompt,
    onAdd: () -> Unit,
    onReject: () -> Unit,
    onRegenerate: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discovered_prompt_card"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW PROMPT FOUND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SilverFrost,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            // Duplicate notification banner if detected
            if (item.isDuplicate) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33B45309))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Duplicate warning",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Similar prompt exists: '${item.similarExisting?.title}'",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFDE68A),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prompt content container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
                    .padding(12.dp)
            ) {
                Text(
                    text = item.prompt,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Source: ${item.source}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Add to Library, Regenerate, Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("add_to_library_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Add to Library", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRegenerate,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag("regenerate_discovered_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Regenerate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = onReject,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .testTag("reject_discovered_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
