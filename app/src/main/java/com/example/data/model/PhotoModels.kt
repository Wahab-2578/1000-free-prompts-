package com.example.data.model

data class PhotoTemplate(
    val id: String,
    val title: String,
    val category: String, // Couple, Best Friends, Brother & Sister, Wedding, Family, Solo
    val description: String,
    val basePrompt: String,
    val style: String,
    val recommendedRatio: String,
    val minPhotos: Int = 1
)

data class FrameItem(
    val id: String,
    val title: String,
    val category: String, // Couple, Wedding, Birthday, Eid, Luxury, Friendship, Graduation, etc.
    val styleTheme: String,
    val borderDescription: String
)
