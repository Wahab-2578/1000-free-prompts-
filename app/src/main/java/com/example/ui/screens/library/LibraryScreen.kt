package com.example.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.GeneratedPhotoEntity
import com.example.ui.screens.creations.MyCreationsScreen

@Composable
fun LibraryScreen(
    photos: List<GeneratedPhotoEntity>,
    onDeletePhoto: (Int) -> Unit,
    onClearAllPhotos: () -> Unit,
    onCopyPrompt: (String) -> Unit,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    MyCreationsScreen(
        photos = photos,
        onDeletePhoto = onDeletePhoto,
        onClearAllPhotos = onClearAllPhotos,
        onCopyPrompt = onCopyPrompt,
        onNavigateToStudio = onNavigateToStudio,
        modifier = modifier
    )
}
