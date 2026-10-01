package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GeneratedPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedPhotoDao {
    @Query("SELECT * FROM generated_photos ORDER BY timestamp DESC")
    fun getAllGeneratedPhotos(): Flow<List<GeneratedPhotoEntity>>

    @Query("SELECT * FROM generated_photos WHERE category = :category ORDER BY timestamp DESC")
    fun getGeneratedPhotosByCategory(category: String): Flow<List<GeneratedPhotoEntity>>

    @Query("SELECT * FROM generated_photos WHERE id = :id LIMIT 1")
    suspend fun getPhotoById(id: Int): GeneratedPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: GeneratedPhotoEntity): Long

    @Query("DELETE FROM generated_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)

    @Query("DELETE FROM generated_photos")
    suspend fun clearAllPhotos()

    @Query("SELECT COUNT(*) FROM generated_photos")
    fun getPhotoCount(): Flow<Int>
}
