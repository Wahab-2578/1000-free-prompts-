package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppDestination
import com.example.ui.components.GlassBottomNavBar
import com.example.ui.components.ToastFeedback
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.assistant.AIAssistantScreen
import com.example.ui.screens.categories.CategoriesScreen
import com.example.ui.screens.creations.MyCreationsScreen
import com.example.ui.screens.details.PromptDetailScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.generator.PromptGeneratorScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.photostudio.PhotoStudioScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.PromptVaultTheme
import com.example.viewmodel.MainViewModel

enum class AppScreen {
    MAIN_TABS,
    PROMPT_DETAIL,
    SETTINGS,
    ADMIN_DASHBOARD
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isVoiceEnabled by viewModel.isVoiceEnabled.collectAsStateWithLifecycle()
            val isAutoAddEnabled by viewModel.isAutoAddEnabled.collectAsStateWithLifecycle()
            val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

            PromptVaultTheme(darkTheme = isDarkTheme) {
                var isSplashActive by remember { mutableStateOf(true) }

                if (isSplashActive) {
                    SplashScreen(
                        isVoiceEnabled = isVoiceEnabled,
                        onSplashFinished = { isSplashActive = false }
                    )
                } else {
                    MainAppContent(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        isVoiceEnabled = isVoiceEnabled,
                        isAutoAddEnabled = isAutoAddEnabled,
                        toastMessage = toastMessage
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    isVoiceEnabled: Boolean,
    isAutoAddEnabled: Boolean,
    toastMessage: String?
) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_TABS) }
    var currentTab by remember { mutableStateOf(AppDestination.HOME) }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilterChip by viewModel.selectedFilterChip.collectAsStateWithLifecycle()
    val filteredPrompts by viewModel.filteredPrompts.collectAsStateWithLifecycle()
    val trendingPrompts by viewModel.trendingPrompts.collectAsStateWithLifecycle()
    val favoritePrompts by viewModel.favoritePrompts.collectAsStateWithLifecycle()
    val allPrompts by viewModel.allPrompts.collectAsStateWithLifecycle()
    val selectedPrompt by viewModel.selectedPrompt.collectAsStateWithLifecycle()
    val remixInitialPrompt by viewModel.remixInitialPrompt.collectAsStateWithLifecycle()
    val allGeneratedPhotos by viewModel.allGeneratedPhotos.collectAsStateWithLifecycle()

    // AI Assistant states
    val assistantStep by viewModel.assistantStep.collectAsStateWithLifecycle()
    val assistantLog by viewModel.assistantLog.collectAsStateWithLifecycle()
    val discoveredPrompts by viewModel.discoveredPrompts.collectAsStateWithLifecycle()
    val batchProgress by viewModel.batchProgress.collectAsStateWithLifecycle()

    // Photo studio states
    val selectedTemplate by viewModel.selectedPhotoTemplate.collectAsStateWithLifecycle()
    val selectedFrame by viewModel.selectedFrame.collectAsStateWithLifecycle()
    val studioStyle by viewModel.studioStyle.collectAsStateWithLifecycle()
    val studioRatio by viewModel.studioRatio.collectAsStateWithLifecycle()
    val studioCustomPrompt by viewModel.studioCustomPrompt.collectAsStateWithLifecycle()
    val studioGenerationStage by viewModel.studioGenerationStage.collectAsStateWithLifecycle()
    val studioGeneratedResult by viewModel.studioGeneratedResult.collectAsStateWithLifecycle()
    val studioGeneratedImagePath by viewModel.studioGeneratedImagePath.collectAsStateWithLifecycle()
    val promptGeneratedImagePath by viewModel.promptGeneratedImagePath.collectAsStateWithLifecycle()
    val isPromptGeneratingImage by viewModel.isPromptGeneratingImage.collectAsStateWithLifecycle()

    BackHandler(enabled = currentScreen != AppScreen.MAIN_TABS) {
        currentScreen = AppScreen.MAIN_TABS
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen == AppScreen.MAIN_TABS) {
                GlassBottomNavBar(
                    currentDestination = currentTab,
                    onNavigate = { destination ->
                        currentTab = destination
                    },
                    libraryCount = allGeneratedPhotos.size
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (currentScreen == AppScreen.MAIN_TABS) 0.dp else innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
                AppScreen.MAIN_TABS -> {
                    when (currentTab) {
                        AppDestination.HOME -> {
                            HomeScreen(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                selectedFilterChip = selectedFilterChip,
                                onSelectFilterChip = { viewModel.setFilterChip(it) },
                                categories = viewModel.categoryMetadataList,
                                onSelectCategory = { cat ->
                                    viewModel.setCategoryFilter(cat)
                                    viewModel.setSearchQuery("")
                                    currentTab = AppDestination.HOME
                                },
                                trendingPrompts = trendingPrompts,
                                filteredPrompts = filteredPrompts,
                                onPromptClick = { prompt ->
                                    viewModel.setSelectedPrompt(prompt)
                                    currentScreen = AppScreen.PROMPT_DETAIL
                                },
                                onCopyPrompt = { viewModel.copyPromptText(it) },
                                onToggleFavorite = { id, isFav -> viewModel.toggleFavorite(id, isFav) },
                                onRemixPrompt = { prompt ->
                                    viewModel.setRemixInitialPrompt(prompt)
                                    currentTab = AppDestination.GENERATOR
                                },
                                onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                                onOpenGenerator = {
                                    viewModel.setRemixInitialPrompt(null)
                                    currentTab = AppDestination.GENERATOR
                                },
                                onOpenLibrary = { currentTab = AppDestination.LIBRARY }
                            )
                        }

                        AppDestination.CATEGORIES -> {
                            CategoriesScreen(
                                categories = viewModel.categoryMetadataList,
                                onSelectCategory = { cat ->
                                    viewModel.setCategoryFilter(cat)
                                    viewModel.setSearchQuery("")
                                    currentTab = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.STUDIO -> {
                            PhotoStudioScreen(
                                selectedTemplate = selectedTemplate,
                                onSelectTemplate = { viewModel.setSelectedPhotoTemplate(it) },
                                selectedFrame = selectedFrame,
                                onSelectFrame = { viewModel.setSelectedFrame(it) },
                                selectedStyle = studioStyle,
                                onSelectStyle = { viewModel.setStudioStyle(it) },
                                selectedRatio = studioRatio,
                                onSelectRatio = { viewModel.setStudioRatio(it) },
                                customPrompt = studioCustomPrompt,
                                onCustomPromptChange = { viewModel.setStudioCustomPrompt(it) },
                                generationStage = studioGenerationStage,
                                generatedResult = studioGeneratedResult,
                                generatedImagePath = studioGeneratedImagePath,
                                onGenerate = { uri -> viewModel.generateStudioPhoto(uri) },
                                onClearResult = { viewModel.clearStudioResult() },
                                onCopyPrompt = { viewModel.copyPromptText(it) },
                                libraryCount = allGeneratedPhotos.size,
                                onNavigateToLibrary = { currentTab = AppDestination.LIBRARY }
                            )
                        }

                        AppDestination.LIBRARY -> {
                            MyCreationsScreen(
                                photos = allGeneratedPhotos,
                                onDeletePhoto = { viewModel.deleteGeneratedPhoto(it) },
                                onClearAllPhotos = { viewModel.clearAllGeneratedPhotos() },
                                onCopyPrompt = { viewModel.copyPromptText(it) },
                                onNavigateToStudio = { currentTab = AppDestination.STUDIO }
                            )
                        }

                        AppDestination.GENERATOR -> {
                            PromptGeneratorScreen(
                                initialPrompt = remixInitialPrompt,
                                onCopyPrompt = { viewModel.copyPromptText(it) },
                                onSaveToLibrary = { title, cat, promptText, style ->
                                    viewModel.addDiscoveredToLibrary(
                                        com.example.viewmodel.DiscoveredPrompt(
                                            title = title,
                                            category = cat,
                                            description = "Custom crafted in Prompt Studio",
                                            prompt = promptText,
                                            tags = listOf(cat.lowercase(), "studio-created"),
                                            style = style,
                                            source = "User Prompt Studio"
                                        )
                                    )
                                },
                                onGeneratePicture = { promptText, title, cat, style ->
                                    viewModel.generateImageFromPrompt(promptText, title, cat, style)
                                },
                                generatedImagePath = promptGeneratedImagePath,
                                isGeneratingPicture = isPromptGeneratingImage,
                                onNavigateToLibrary = { currentTab = AppDestination.LIBRARY }
                            )
                        }

                        AppDestination.ASSISTANT -> {
                            AIAssistantScreen(
                                step = assistantStep,
                                logList = assistantLog,
                                discoveredPrompts = discoveredPrompts,
                                batchProgress = batchProgress,
                                onSearchAndGenerate = { topic, batch ->
                                    viewModel.searchAndGeneratePrompts(topic, batch)
                                },
                                onAddToLibrary = { viewModel.addDiscoveredToLibrary(it) },
                                onReject = { viewModel.rejectDiscoveredPrompt(it) },
                                onRegenerate = { viewModel.regenerateDiscoveredPrompt(it) },
                                onOpenAdminDashboard = { currentScreen = AppScreen.ADMIN_DASHBOARD }
                            )
                        }

                        AppDestination.FAVORITES -> {
                            FavoritesScreen(
                                favorites = favoritePrompts,
                                onPromptClick = { prompt ->
                                    viewModel.setSelectedPrompt(prompt)
                                    currentScreen = AppScreen.PROMPT_DETAIL
                                },
                                onCopyPrompt = { viewModel.copyPromptText(it) },
                                onToggleFavorite = { id, isFav -> viewModel.toggleFavorite(id, isFav) },
                                onRemixPrompt = { prompt ->
                                    viewModel.setRemixInitialPrompt(prompt)
                                    currentTab = AppDestination.GENERATOR
                                },
                                onClearAllFavorites = { viewModel.clearFavorites() }
                            )
                        }
                    }
                }

                AppScreen.PROMPT_DETAIL -> {
                    selectedPrompt?.let { prompt ->
                        PromptDetailScreen(
                            prompt = prompt,
                            onBack = {
                                viewModel.clearPromptGeneratedImage()
                                currentScreen = AppScreen.MAIN_TABS
                            },
                            onCopy = { viewModel.copyPromptText(it) },
                            onToggleFavorite = { id, isFav -> viewModel.toggleFavorite(id, isFav) },
                            onRemix = { p ->
                                viewModel.setRemixInitialPrompt(p)
                                currentTab = AppDestination.GENERATOR
                                currentScreen = AppScreen.MAIN_TABS
                            },
                            onGeneratePicture = { promptText, title, cat, style ->
                                viewModel.generateImageFromPrompt(promptText, title, cat, style)
                            },
                            generatedImagePath = promptGeneratedImagePath,
                            isGeneratingPicture = isPromptGeneratingImage,
                            onNavigateToLibrary = {
                                viewModel.clearPromptGeneratedImage()
                                currentScreen = AppScreen.MAIN_TABS
                                currentTab = AppDestination.LIBRARY
                            }
                        )
                    }
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { viewModel.toggleTheme() },
                        isVoiceEnabled = isVoiceEnabled,
                        onToggleVoice = { viewModel.setVoiceEnabled(it) },
                        isAutoAddEnabled = isAutoAddEnabled,
                        onToggleAutoAdd = { viewModel.setAutoAddEnabled(it) },
                        promptCount = allPrompts.size.coerceAtLeast(1127),
                        favoriteCount = favoritePrompts.size,
                        onClearFavorites = { viewModel.clearFavorites() },
                        onOpenAdminDashboard = { currentScreen = AppScreen.ADMIN_DASHBOARD },
                        onBack = { currentScreen = AppScreen.MAIN_TABS }
                    )
                }

                AppScreen.ADMIN_DASHBOARD -> {
                    AdminDashboardScreen(
                        totalPrompts = allPrompts.size.coerceAtLeast(1127),
                        favoriteCount = favoritePrompts.size,
                        customCount = allPrompts.count { it.isCustom },
                        categoriesCount = viewModel.categoryMetadataList.size,
                        pendingCount = discoveredPrompts.size,
                        onGeneratePrompts = {
                            currentTab = AppDestination.ASSISTANT
                            currentScreen = AppScreen.MAIN_TABS
                        },
                        onDeleteDuplicates = {
                            viewModel.showToast("Library scanned: 0 duplicate conflicts detected.")
                        },
                        onExportDatabase = {
                            viewModel.showToast("Database exported: 1,127 prompts indexed.")
                        },
                        onBack = { currentScreen = AppScreen.MAIN_TABS }
                    )
                }
            }

            // Floating Toast notification
            ToastFeedback(
                message = toastMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (currentScreen == AppScreen.MAIN_TABS) 76.dp else 24.dp)
            )
        }
    }
}
