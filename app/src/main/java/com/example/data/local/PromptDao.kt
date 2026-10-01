package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PromptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY id ASC")
    fun getAllPrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE isTrending = 1 ORDER BY id ASC LIMIT 50")
    fun getTrendingPrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE category = :category ORDER BY id ASC")
    fun getPromptsByCategory(category: String): Flow<List<PromptEntity>>

    @Query("""
        SELECT * FROM prompts 
        WHERE title LIKE '%' || :query || '%' 
           OR prompt LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
        ORDER BY id ASC
    """)
    fun searchPrompts(query: String): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id LIMIT 1")
    fun getPromptById(id: Int): Flow<PromptEntity?>

    @Query("SELECT * FROM prompts WHERE id = :id LIMIT 1")
    suspend fun getPromptByIdSync(id: Int): PromptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(prompts: List<PromptEntity>)

    @Query("UPDATE prompts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE prompts SET isFavorite = 0")
    suspend fun clearAllFavorites()

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM prompts WHERE isCustom = 1")
    fun getCustomCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM prompts WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT DISTINCT category FROM prompts ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM prompts")
    suspend fun getAllPromptsSync(): List<PromptEntity>

    @Query("SELECT MAX(id) FROM prompts")
    suspend fun getMaxId(): Int?
}
