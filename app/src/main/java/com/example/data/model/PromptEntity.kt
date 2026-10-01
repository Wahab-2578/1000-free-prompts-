package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val prompt: String,
    val tags: String, // Comma-separated tags
    val style: String,
    val aspectRatio: String = "16:9",
    val isFavorite: Boolean = false,
    val isTrending: Boolean = false,
    val isCustom: Boolean = false,
    val source: String = "PromptVault Curated Library",
    val timestamp: Long = System.currentTimeMillis()
) {
    val tagList: List<String>
        get() = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
