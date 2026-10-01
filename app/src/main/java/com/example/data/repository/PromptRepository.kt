package com.example.data.repository

import android.content.Context
import com.example.data.local.GeneratedPhotoDao
import com.example.data.local.PromptDao
import com.example.data.model.CategoryItem
import com.example.data.model.GeneratedPhotoEntity
import com.example.data.model.PromptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class PromptRepository(
    private val promptDao: PromptDao,
    private val generatedPhotoDao: GeneratedPhotoDao,
    private val context: Context
) {
    val allPrompts: Flow<List<PromptEntity>> = promptDao.getAllPrompts()
    val trendingPrompts: Flow<List<PromptEntity>> = promptDao.getTrendingPrompts()
    val favoritePrompts: Flow<List<PromptEntity>> = promptDao.getFavoritePrompts()
    val categories: Flow<List<String>> = promptDao.getCategories()
    val favoriteCount: Flow<Int> = promptDao.getFavoriteCount()
    val customCount: Flow<Int> = promptDao.getCustomCount()
    val allGeneratedPhotos: Flow<List<GeneratedPhotoEntity>> = generatedPhotoDao.getAllGeneratedPhotos()
    val generatedPhotoCount: Flow<Int> = generatedPhotoDao.getPhotoCount()

    suspend fun saveGeneratedPhoto(photo: GeneratedPhotoEntity): Long = withContext(Dispatchers.IO) {
        generatedPhotoDao.insertPhoto(photo)
    }

    suspend fun deleteGeneratedPhoto(id: Int) = withContext(Dispatchers.IO) {
        val photo = generatedPhotoDao.getPhotoById(id)
        if (photo?.imagePath != null) {
            try {
                val file = java.io.File(photo.imagePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        generatedPhotoDao.deletePhotoById(id)
    }

    suspend fun clearAllGeneratedPhotos() = withContext(Dispatchers.IO) {
        try {
            val dir = java.io.File(context.filesDir, "generated_photos")
            if (dir.exists()) {
                dir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        generatedPhotoDao.clearAllPhotos()
    }

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val count = promptDao.getTotalCount()
        if (count == 0) {
            seedFromAssets()
        }
    }

    private suspend fun seedFromAssets() = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("prompts.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<PromptEntity>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val tagsArr = obj.optJSONArray("tags")
                val tagsStr = if (tagsArr != null) {
                    (0 until tagsArr.length()).joinToString(", ") { tagsArr.getString(it) }
                } else {
                    obj.optString("tags", "")
                }

                list.add(
                    PromptEntity(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        category = obj.getString("category"),
                        description = obj.optString("description", ""),
                        prompt = obj.getString("prompt"),
                        tags = tagsStr,
                        style = obj.optString("style", "Photorealistic"),
                        aspectRatio = obj.optString("aspectRatio", "16:9"),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        isTrending = obj.optBoolean("isTrending", false),
                        isCustom = false,
                        source = obj.optString("source", "PromptVault Curated Library"),
                        timestamp = System.currentTimeMillis() - (jsonArray.length() - i) * 60000L
                    )
                )
            }

            if (list.isNotEmpty()) {
                promptDao.insertAll(list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun search(query: String): Flow<List<PromptEntity>> {
        return promptDao.searchPrompts(query)
    }

    fun getByCategory(category: String): Flow<List<PromptEntity>> {
        return promptDao.getPromptsByCategory(category)
    }

    fun getPromptById(id: Int): Flow<PromptEntity?> {
        return promptDao.getPromptById(id)
    }

    suspend fun setFavorite(id: Int, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        promptDao.setFavorite(id, isFavorite)
    }

    suspend fun clearAllFavorites() = withContext(Dispatchers.IO) {
        promptDao.clearAllFavorites()
    }

    suspend fun deletePrompt(id: Int) = withContext(Dispatchers.IO) {
        promptDao.deleteById(id)
    }

    suspend fun getTotalCount(): Int = withContext(Dispatchers.IO) {
        promptDao.getTotalCount()
    }

    suspend fun addPrompt(
        title: String,
        category: String,
        description: String,
        prompt: String,
        tags: List<String>,
        style: String,
        source: String = "Vault AI Assistant",
        aspectRatio: String = "16:9"
    ): PromptEntity = withContext(Dispatchers.IO) {
        val maxId = promptDao.getMaxId() ?: 1000
        val newEntity = PromptEntity(
            id = maxId + 1,
            title = title,
            category = category,
            description = description,
            prompt = prompt,
            tags = tags.joinToString(", "),
            style = style,
            aspectRatio = aspectRatio,
            isFavorite = false,
            isTrending = false,
            isCustom = true,
            source = source,
            timestamp = System.currentTimeMillis()
        )
        promptDao.insertPrompt(newEntity)
        newEntity
    }

    suspend fun checkSimilarity(candidatePrompt: String): PromptEntity? = withContext(Dispatchers.IO) {
        val words = candidatePrompt.lowercase()
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length > 3 }
            .toSet()

        if (words.isEmpty()) return@withContext null

        val all = promptDao.getAllPromptsSync()
        var highestMatch: PromptEntity? = null
        var maxJaccard = 0.0

        for (item in all) {
            val itemWords = item.prompt.lowercase()
                .split(Regex("[^a-zA-Z0-9]+"))
                .filter { it.length > 3 }
                .toSet()

            val intersection = words.intersect(itemWords).size
            val union = words.union(itemWords).size
            if (union > 0) {
                val jaccard = intersection.toDouble() / union
                if (jaccard > 0.65 && jaccard > maxJaccard) {
                    maxJaccard = jaccard
                    highestMatch = item
                }
            }
        }
        highestMatch
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val list = promptDao.getAllPromptsSync()
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("category", item.category)
                put("description", item.description)
                put("prompt", item.prompt)
                put("tags", item.tags)
                put("style", item.style)
                put("source", item.source)
            }
            jsonArray.put(obj)
        }
        jsonArray.toString(2)
    }

    fun getCategoryMetadataList(): List<CategoryItem> {
        return listOf(
            CategoryItem("Selfie", 84, "ic_selfie", "Photorealistic", "Golden hour, candid portraits, mirror snaps & creative angles"),
            CategoryItem("Portrait", 112, "ic_portrait", "Photorealistic", "Editorial, studio lighting, fine art & expressive close-ups"),
            CategoryItem("Fashion", 95, "ic_fashion", "Editorial", "Haute couture, streetwear, avant-garde & runway styles"),
            CategoryItem("Luxury", 78, "ic_luxury", "Cinematic", "Superyachts, penthouses, private jets & high-end lifestyle"),
            CategoryItem("Cars", 90, "ic_cars", "Photorealistic", "Hypercars, vintage classics, track racing & midnight drifts"),
            CategoryItem("Motorcycles", 56, "ic_motorcycles", "Cinematic", "Custom cafe racers, scramblers, track bikes & desert enduros"),
            CategoryItem("Gaming", 68, "ic_gaming", "3D Render", "Hacker setups, futuristic battlestations & mecha hangars"),
            CategoryItem("Technology", 65, "ic_tech", "3D Render", "Quantum computing, holographic interfaces & futuristic gadgets"),
            CategoryItem("Smartphones", 54, "ic_smartphones", "Studio", "Commercial hero shots, exploded views & glass floating angles"),
            CategoryItem("Product photography", 92, "ic_product", "Studio", "Perfume flacons, cosmetics, travertine styling & softbox light"),
            CategoryItem("Brand/product advertisements", 72, "ic_ads", "Editorial", "High-impact commercial campaigns & brand hero stills"),
            CategoryItem("Shoes", 64, "ic_shoes", "Studio", "Sneakers floating, leather craft & athletic deconstructed views"),
            CategoryItem("Watches", 62, "ic_watches", "Studio", "Skeleton movements, sapphire crystal clarity & tool chronographs"),
            CategoryItem("Food", 88, "ic_food", "Photorealistic", "Gourmet dishes, artisan bakery, pasta twirls & morning brunch"),
            CategoryItem("Restaurants", 58, "ic_restaurants", "Cinematic", "Michelin star counters, romantic bistros & speakeasy bars"),
            CategoryItem("Travel", 96, "ic_travel", "Photorealistic", "Alpine lakes, misty temples, Mediterranean coasts & safari"),
            CategoryItem("Nature", 82, "ic_nature", "Photorealistic", "Redwood sunbeams, thundering waterfalls & aurora skies"),
            CategoryItem("Architecture", 84, "ic_architecture", "Photorealistic", "Brutalist villas, parametric glass pavilions & modern zen lofts"),
            CategoryItem("Street photography", 76, "ic_street", "Photorealistic", "Rainy neon crosswalks, golden piazza shadows & candid moments"),
            CategoryItem("Cinematic scenes", 94, "ic_cinematic", "Cinematic", "Neo-noir atmospheres, desert monoliths & anamorphic flares"),
            CategoryItem("Studio photography", 70, "ic_studio", "Studio", "Dual-color gel lighting, prism refractions & clean backdrops"),
            CategoryItem("3D renders", 75, "ic_3d", "3D Render", "Fluid iridescent ribbons, isometric rooms & clay miniatures"),
            CategoryItem("Toy photography", 48, "ic_toy", "Photorealistic", "Miniature astronaut dioramas & weathered die-cast cars"),
            CategoryItem("Anime-inspired original characters", 74, "ic_anime", "Anime", "Makoto Shinkai aesthetics, cyber samurai & fantasy scholars"),
            CategoryItem("Fantasy", 82, "ic_fantasy", "Cinematic", "Elven citadels, sleeping dragons, enchanted forest groves"),
            CategoryItem("Sci-fi", 80, "ic_scifi", "Cinematic", "Orbital stations, cybernetic laboratories & deep space vessels"),
            CategoryItem("Horror", 46, "ic_horror", "Cinematic", "Gothic mansions in fog, submerged subway tunnels & dark woods"),
            CategoryItem("Wedding", 72, "ic_wedding", "Photorealistic", "Tuscan villa vows, modern minimalist gowns & emotional moments"),
            CategoryItem("Business", 68, "ic_business", "Editorial", "Scandinavian conference rooms, keynote stages & team collabs"),
            CategoryItem("Social media", 64, "ic_social", "Photorealistic", "Aesthetic desk flatlays, iced matcha pours & aesthetic reels"),
            CategoryItem("YouTube thumbnails", 55, "ic_yt", "Cinematic", "High-contrast creator setups, extreme solo challenges & gear"),
            CategoryItem("Profile pictures", 70, "ic_pfp", "Studio", "Professional avatars, friendly executive headshots & soft light"),
            CategoryItem("Wallpapers", 66, "ic_wallpaper", "3D Render", "Liquid velvet dunes, floating twilight orbs & 4k phone art"),
            CategoryItem("Professional headshots", 74, "ic_headshots", "Studio", "Corporate leaders, medical researchers & creative founders"),
            CategoryItem("Sports", 58, "ic_sports", "Cinematic", "Sprinters bursting from blocks, big wave surfing & basketball"),
            CategoryItem("Fitness", 58, "ic_fitness", "Photorealistic", "Chalk clouds before heavy lifts, cliffside sunrise yoga"),
            CategoryItem("Lifestyle", 72, "ic_lifestyle", "Photorealistic", "Slow morning baking, beach bonfires & serene living moments"),
            CategoryItem("Minimal photography", 66, "ic_minimal", "Minimal", "Lone cypress trees, geometric concrete shadows & clean voids"),
            CategoryItem("Vintage photography", 60, "ic_vintage", "Vintage", "1970s road trip diners, 1950s Havana pastel streetscapes"),
            CategoryItem("Retro aesthetics", 58, "ic_retro", "Vintage", "80s city pop lounges, chrome boomboxes & nostalgic analog tape"),
            CategoryItem("Editorial photography", 70, "ic_editorial", "Editorial", "Sculptural garments in raw desert, high-fashion art covers"),
            CategoryItem("Creative advertising", 60, "ic_advertising", "Studio", "Liquid coffee crown splashes, exploded sneaker anatomy"),
            CategoryItem("E-commerce product images", 72, "ic_ecommerce", "Studio", "Ceramic tableware sets, full-grain leather wallets & catalog"),
            CategoryItem("Outfit", 62, "ic_outfit", "Editorial", "Old money quiet luxury, waterproof techwear & tailoring")
        )
    }
}
