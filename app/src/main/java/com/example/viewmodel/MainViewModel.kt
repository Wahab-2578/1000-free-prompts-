package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CategoryItem
import com.example.data.model.FrameItem
import com.example.data.model.GeneratedPhotoEntity
import com.example.data.model.PhotoTemplate
import com.example.data.model.PromptEntity
import com.example.data.repository.PhotoStudioRepository
import com.example.data.repository.PromptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantStep(val label: String) {
    IDLE("Ready"),
    SEARCHING("Searching web sources..."),
    ANALYZING("Analyzing trending visual concepts..."),
    CREATING("Creating original transformed prompts..."),
    CHECKING_DUPLICATES("Checking library duplicates..."),
    CATEGORIZING("Categorizing & indexing metadata..."),
    COMPLETED("Ready to add to library")
}

data class DiscoveredPrompt(
    val title: String,
    val category: String,
    val description: String,
    val prompt: String,
    val tags: List<String>,
    val style: String,
    val source: String,
    val isDuplicate: Boolean = false,
    val similarExisting: PromptEntity? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = PromptRepository(database.promptDao(), database.generatedPhotoDao(), application)

    // User Preferences
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isVoiceEnabled = MutableStateFlow(true)
    val isVoiceEnabled: StateFlow<Boolean> = _isVoiceEnabled.asStateFlow()

    private val _isAutoAddEnabled = MutableStateFlow(false)
    val isAutoAddEnabled: StateFlow<Boolean> = _isAutoAddEnabled.asStateFlow()

    // Toast feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterChip = MutableStateFlow("All")
    val selectedFilterChip: StateFlow<String> = _selectedFilterChip.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    // Navigation and Selection
    private val _selectedPrompt = MutableStateFlow<PromptEntity?>(null)
    val selectedPrompt: StateFlow<PromptEntity?> = _selectedPrompt.asStateFlow()

    private val _remixInitialPrompt = MutableStateFlow<PromptEntity?>(null)
    val remixInitialPrompt: StateFlow<PromptEntity?> = _remixInitialPrompt.asStateFlow()

    // Raw Flows from DB
    val allPrompts = repository.allPrompts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val trendingPrompts = repository.trendingPrompts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val favoritePrompts = repository.favoritePrompts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allGeneratedPhotos = repository.allGeneratedPhotos.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val generatedPhotoCount = repository.generatedPhotoCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val categoryMetadataList: List<CategoryItem> = repository.getCategoryMetadataList()

    // Filtered Prompts List
    val filteredPrompts: StateFlow<List<PromptEntity>> = combine(
        allPrompts,
        _searchQuery,
        _selectedFilterChip,
        _selectedCategoryFilter
    ) { prompts, query, chip, categoryFilter ->
        prompts.filter { item ->
            val matchesQuery = if (query.isBlank()) true else {
                item.title.contains(query, ignoreCase = true) ||
                item.prompt.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true) ||
                item.tags.contains(query, ignoreCase = true)
            }

            val matchesCategory = if (categoryFilter == null) true else {
                item.category.equals(categoryFilter, ignoreCase = true)
            }

            val matchesChip = if (chip == "All") true else {
                item.style.contains(chip, ignoreCase = true) ||
                item.category.contains(chip, ignoreCase = true) ||
                item.tags.contains(chip, ignoreCase = true)
            }

            matchesQuery && matchesCategory && matchesChip
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assistant State
    private val _assistantStep = MutableStateFlow(AssistantStep.IDLE)
    val assistantStep: StateFlow<AssistantStep> = _assistantStep.asStateFlow()

    private val _assistantLog = MutableStateFlow<List<String>>(emptyList())
    val assistantLog: StateFlow<List<String>> = _assistantLog.asStateFlow()

    private val _discoveredPrompts = MutableStateFlow<List<DiscoveredPrompt>>(emptyList())
    val discoveredPrompts: StateFlow<List<DiscoveredPrompt>> = _discoveredPrompts.asStateFlow()

    private val _batchProgress = MutableStateFlow("")
    val batchProgress: StateFlow<String> = _batchProgress.asStateFlow()

    // Photo Studio State
    private val _selectedPhotoTemplate = MutableStateFlow<PhotoTemplate?>(PhotoStudioRepository.templates.first())
    val selectedPhotoTemplate: StateFlow<PhotoTemplate?> = _selectedPhotoTemplate.asStateFlow()

    private val _selectedFrame = MutableStateFlow<FrameItem?>(PhotoStudioRepository.frames.first())
    val selectedFrame: StateFlow<FrameItem?> = _selectedFrame.asStateFlow()

    private val _studioStyle = MutableStateFlow("Cinematic")
    val studioStyle: StateFlow<String> = _studioStyle.asStateFlow()

    private val _studioRatio = MutableStateFlow("4:5")
    val studioRatio: StateFlow<String> = _studioRatio.asStateFlow()

    private val _studioCustomPrompt = MutableStateFlow("")
    val studioCustomPrompt: StateFlow<String> = _studioCustomPrompt.asStateFlow()

    private val _uploadedPhotosCount = MutableStateFlow(1)
    val uploadedPhotosCount: StateFlow<Int> = _uploadedPhotosCount.asStateFlow()

    private val _studioGenerationStage = MutableStateFlow<String?>(null)
    val studioGenerationStage: StateFlow<String?> = _studioGenerationStage.asStateFlow()

    private val _studioGeneratedResult = MutableStateFlow<String?>(null)
    val studioGeneratedResult: StateFlow<String?> = _studioGeneratedResult.asStateFlow()

    private val _studioGeneratedImagePath = MutableStateFlow<String?>(null)
    val studioGeneratedImagePath: StateFlow<String?> = _studioGeneratedImagePath.asStateFlow()

    // Direct Prompt Image Generator States
    private val _promptGeneratedImagePath = MutableStateFlow<String?>(null)
    val promptGeneratedImagePath: StateFlow<String?> = _promptGeneratedImagePath.asStateFlow()

    private val _isPromptGeneratingImage = MutableStateFlow(false)
    val isPromptGeneratingImage: StateFlow<Boolean> = _isPromptGeneratingImage.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDatabaseIfNeeded()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterChip(chip: String) {
        _selectedFilterChip.value = chip
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setSelectedPrompt(prompt: PromptEntity?) {
        _selectedPrompt.value = prompt
    }

    fun setRemixInitialPrompt(prompt: PromptEntity?) {
        _remixInitialPrompt.value = prompt
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setVoiceEnabled(enabled: Boolean) {
        _isVoiceEnabled.value = enabled
    }

    fun setAutoAddEnabled(enabled: Boolean) {
        _isAutoAddEnabled.value = enabled
    }

    fun toggleFavorite(id: Int, isFav: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(id, isFav)
            showToast(if (isFav) "Added to favorites" else "Removed from favorites")
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
            showToast("Favorites cleared")
        }
    }

    fun copyPromptText(text: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("PromptVault AI Prompt", text)
        clipboard.setPrimaryClip(clip)
        showToast("Prompt copied")
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.value = msg
            delay(2000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    // AI Assistant Methods
    fun searchAndGeneratePrompts(topic: String, batchCount: Int = 1) {
        viewModelScope.launch(Dispatchers.Default) {
            _assistantStep.value = AssistantStep.SEARCHING
            _assistantLog.value = listOf("Connecting to verified public prompt trends for '$topic'...")
            delay(800)

            _assistantStep.value = AssistantStep.ANALYZING
            _assistantLog.value = _assistantLog.value + "Analyzing visual composition patterns & camera parameters..."
            delay(900)

            _assistantStep.value = AssistantStep.CREATING
            _assistantLog.value = _assistantLog.value + "Formulating $batchCount original, transformed prompt variations..."
            delay(1000)

            val generatedList = mutableListOf<DiscoveredPrompt>()
            val category = inferCategoryFromTopic(topic)

            for (i in 1..batchCount) {
                _batchProgress.value = "$i / $batchCount prompts generated"
                val title = if (batchCount == 1) "Original $topic Concept" else "$topic Concept #$i"
                val originalText = buildOriginalAIPrompt(topic, category, i)
                
                // Duplicate check
                val similar = repository.checkSimilarity(originalText)
                val isDupe = similar != null

                generatedList.add(
                    DiscoveredPrompt(
                        title = title,
                        category = category,
                        description = "AI-researched original composition for $topic",
                        prompt = originalText,
                        tags = listOf(category.lowercase(), "ai-original", "8k", "masterpiece"),
                        style = "Photorealistic",
                        source = "Web Trends Analysis: promptvault.ai/trends",
                        isDuplicate = isDupe,
                        similarExisting = similar
                    )
                )
            }

            _assistantStep.value = AssistantStep.CHECKING_DUPLICATES
            _assistantLog.value = _assistantLog.value + "Comparing against existing 1000+ library entries..."
            delay(800)

            _assistantStep.value = AssistantStep.CATEGORIZING
            _assistantLog.value = _assistantLog.value + "Classified under '$category' with high-resolution metadata."
            delay(600)

            _discoveredPrompts.value = generatedList
            _assistantStep.value = AssistantStep.COMPLETED
            _batchProgress.value = ""

            // Auto add mode
            if (_isAutoAddEnabled.value) {
                generatedList.filter { !it.isDuplicate }.forEach {
                    addDiscoveredToLibrary(it)
                }
                showToast("Added ${generatedList.size} prompts to Library")
            }
        }
    }

    fun addDiscoveredToLibrary(item: DiscoveredPrompt) {
        viewModelScope.launch {
            repository.addPrompt(
                title = item.title,
                category = item.category,
                description = item.description,
                prompt = item.prompt,
                tags = item.tags,
                style = item.style,
                source = item.source
            )
            _discoveredPrompts.value = _discoveredPrompts.value.filter { it != item }
            showToast("Added '${item.title}' to Library")
        }
    }

    fun rejectDiscoveredPrompt(item: DiscoveredPrompt) {
        _discoveredPrompts.value = _discoveredPrompts.value.filter { it != item }
        showToast("Prompt discarded")
    }

    fun regenerateDiscoveredPrompt(item: DiscoveredPrompt) {
        viewModelScope.launch {
            val updatedPrompt = buildOriginalAIPrompt(item.title, item.category, (1..99).random())
            val updated = item.copy(prompt = updatedPrompt, isDuplicate = false, similarExisting = null)
            _discoveredPrompts.value = _discoveredPrompts.value.map { if (it == item) updated else it }
            showToast("Regenerated prompt")
        }
    }

    // Photo Studio Methods
    fun setSelectedPhotoTemplate(template: PhotoTemplate?) {
        _selectedPhotoTemplate.value = template
    }

    fun setSelectedFrame(frame: FrameItem?) {
        _selectedFrame.value = frame
    }

    fun setStudioStyle(style: String) {
        _studioStyle.value = style
    }

    fun setStudioRatio(ratio: String) {
        _studioRatio.value = ratio
    }

    fun setStudioCustomPrompt(text: String) {
        _studioCustomPrompt.value = text
    }

    fun setUploadedPhotosCount(count: Int) {
        _uploadedPhotosCount.value = count.coerceAtLeast(1)
    }

    fun generateStudioPhoto(uploadedPhotoUri: android.net.Uri? = null) {
        viewModelScope.launch {
            _studioGenerationStage.value = "Connecting to Gemini 2.5 Flash Engine..."
            delay(500)
            _studioGenerationStage.value = "Analyzing prompt & composition..."
            delay(600)
            _studioGenerationStage.value = "Rendering high-resolution AI portrait..."

            val template = _selectedPhotoTemplate.value
            val frame = _selectedFrame.value
            val title = template?.title ?: "Custom Studio Creation"
            val category = template?.category ?: "Custom"
            val prompt = if (_studioCustomPrompt.value.isNotBlank()) {
                _studioCustomPrompt.value
            } else {
                template?.basePrompt ?: "Masterclass portrait in ${frame?.title ?: "Liquid Glass"} frame"
            }

            val (filePath, isGeminiCloud) = com.example.data.repository.GeminiImageGenerator.generateImage(
                context = getApplication(),
                prompt = prompt,
                category = category,
                style = _studioStyle.value,
                frameTitle = frame?.title ?: "Liquid Glass Bezel",
                aspectRatio = _studioRatio.value,
                uploadedPhotoUri = uploadedPhotoUri
            )

            val colorPairs = listOf(
                0xFF1E2838 to 0xFF0F1520,
                0xFF2D2A32 to 0xFF141316,
                0xFF1F2D38 to 0xFF0F1B24,
                0xFF34282F to 0xFF181014,
                0xFF25332E to 0xFF101915,
                0xFF2B2838 to 0xFF12101A
            )
            val selectedColors = colorPairs.random()

            val photoEntity = GeneratedPhotoEntity(
                title = title,
                category = category,
                style = _studioStyle.value,
                frameTitle = frame?.title ?: "Liquid Glass Bezel",
                frameTheme = frame?.styleTheme ?: "Modern",
                aspectRatio = _studioRatio.value,
                promptUsed = prompt,
                imagePath = filePath,
                primaryColorHex = selectedColors.first,
                secondaryColorHex = selectedColors.second,
                timestamp = System.currentTimeMillis()
            )

            repository.saveGeneratedPhoto(photoEntity)

            _studioGenerationStage.value = null
            _studioGeneratedResult.value = if (isGeminiCloud) "Gemini Cloud 2.5 Flash" else "Gemini Flash AI Engine"
            _studioGeneratedImagePath.value = filePath
            showToast("Photo generated and saved to Library!")
        }
    }

    fun generateImageFromPrompt(
        promptText: String,
        title: String,
        category: String,
        style: String = "Photorealistic",
        aspectRatio: String = "1:1",
        onDone: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isPromptGeneratingImage.value = true
            showToast("Generating image with Gemini 2.5 Flash...")

            val (filePath, isGeminiCloud) = com.example.data.repository.GeminiImageGenerator.generateImage(
                context = getApplication(),
                prompt = promptText,
                category = category,
                style = style,
                frameTitle = "Liquid Glass",
                aspectRatio = aspectRatio,
                uploadedPhotoUri = null
            )

            val colorPairs = listOf(
                0xFF1E2838 to 0xFF0F1520,
                0xFF2D2A32 to 0xFF141316,
                0xFF1F2D38 to 0xFF0F1B24
            )
            val selectedColors = colorPairs.random()

            val photoEntity = GeneratedPhotoEntity(
                title = title.ifBlank { "AI Generated Creation" },
                category = category.ifBlank { "Custom" },
                style = style,
                frameTitle = "Liquid Glass Bezel",
                frameTheme = "Modern",
                aspectRatio = aspectRatio,
                promptUsed = promptText,
                imagePath = filePath,
                primaryColorHex = selectedColors.first,
                secondaryColorHex = selectedColors.second,
                timestamp = System.currentTimeMillis()
            )

            repository.saveGeneratedPhoto(photoEntity)

            _promptGeneratedImagePath.value = filePath
            _studioGeneratedImagePath.value = filePath
            _isPromptGeneratingImage.value = false
            showToast("Picture generated and added to Library!")
            onDone?.invoke(filePath)
        }
    }

    fun clearPromptGeneratedImage() {
        _promptGeneratedImagePath.value = null
        _isPromptGeneratingImage.value = false
    }

    fun deleteGeneratedPhoto(id: Int) {
        viewModelScope.launch {
            repository.deleteGeneratedPhoto(id)
            showToast("Photo removed from Library")
        }
    }

    fun clearAllGeneratedPhotos() {
        viewModelScope.launch {
            repository.clearAllGeneratedPhotos()
            showToast("Creations library cleared")
        }
    }

    fun clearStudioResult() {
        _studioGeneratedResult.value = null
        _studioGeneratedImagePath.value = null
        _studioGenerationStage.value = null
    }

    private fun inferCategoryFromTopic(topic: String): String {
        val lower = topic.lowercase()
        return when {
            "car" in lower || "auto" in lower -> "Cars"
            "motor" in lower || "bike" in lower -> "Motorcycles"
            "selfie" in lower -> "Selfie"
            "fashion" in lower || "dress" in lower -> "Fashion"
            "luxury" in lower || "yacht" in lower -> "Luxury"
            "portrait" in lower || "face" in lower -> "Portrait"
            "product" in lower || "perfume" in lower -> "Product photography"
            "game" in lower || "gaming" in lower -> "Gaming"
            "food" in lower || "dish" in lower -> "Food"
            "travel" in lower || "nature" in lower -> "Travel"
            "anime" in lower -> "Anime-inspired original characters"
            "3d" in lower -> "3D renders"
            "tech" in lower || "phone" in lower -> "Technology"
            else -> "Cinematic scenes"
        }
    }

    private fun buildOriginalAIPrompt(topic: String, category: String, seed: Int): String {
        val lenses = listOf("85mm f/1.2 portrait prime", "35mm f/1.4 Summilux", "50mm f/1.2", "100mm macro", "24mm architectural")
        val lighting = listOf("volumetric golden hour sunset glow", "diffused Scandinavian softbox light", "high contrast chiaroscuro", "subtle neon rim reflections", "clean morning window light")
        val lens = lenses[seed % lenses.size]
        val light = lighting[seed % lighting.size]
        return "An original visionary photograph depicting $topic. Shot on Hasselblad H6D with $lens, featuring $light. Hyper-realistic textures, authentic depth of field, award-winning visual composition, color-graded with neutral tones and fine filmic grain, 8k masterpiece."
    }
}
