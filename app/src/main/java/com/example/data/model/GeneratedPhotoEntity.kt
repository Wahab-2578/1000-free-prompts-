package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_photos")
data class GeneratedPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String, // Couple, Best Friends, Brother & Sister, Wedding, Family, Solo, Custom
    val style: String,
    val frameTitle: String,
    val frameTheme: String,
    val aspectRatio: String = "4:5",
    val promptUsed: String,
    val imagePath: String? = null,
    val primaryColorHex: Long = 0xFF1E2838,
    val secondaryColorHex: Long = 0xFF0F1520,
    val timestamp: Long = System.currentTimeMillis()
)
