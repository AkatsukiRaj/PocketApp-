package com.pocket.app.data.local

import androidx.room.*
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.data.model.ItemType
import com.pocket.app.data.model.PocketItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PocketDao {

    @Query("SELECT * FROM pocket_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<PocketItem>>

    @Query("SELECT * FROM pocket_items WHERE isPinned = 1 ORDER BY createdAt DESC")
    fun getPinnedItems(): Flow<List<PocketItem>>

    @Query("SELECT * FROM pocket_items WHERE itemType = :type ORDER BY createdAt DESC")
    fun getItemsByType(type: ItemType): Flow<List<PocketItem>>

    @Query("SELECT * FROM pocket_items WHERE itemType = :type AND folderName = :folderName ORDER BY createdAt DESC")
    fun getItemsByTypeAndFolder(type: ItemType, folderName: String): Flow<List<PocketItem>>

    @Query("SELECT DISTINCT folderName FROM pocket_items WHERE itemType = :type AND folderName IS NOT NULL AND folderName != ''")
    fun getFoldersForType(type: ItemType): Flow<List<String>>

    @Query("SELECT * FROM pocket_items WHERE itemType IN (:types) ORDER BY createdAt DESC")
    fun getItemsByTypes(types: List<ItemType>): Flow<List<PocketItem>>

    @Query("SELECT * FROM pocket_items WHERE category = :category ORDER BY createdAt DESC")
    fun getItemsByCategory(category: ItemCategory): Flow<List<PocketItem>>

    @Query("SELECT * FROM pocket_items WHERE itemType = 'REMINDER' AND isAlarmActive = 1 ORDER BY reminderTime ASC")
    fun getActiveReminders(): Flow<List<PocketItem>>

    @Query("SELECT * FROM pocket_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): PocketItem?

    @Query("SELECT * FROM pocket_items WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchItems(query: String): Flow<List<PocketItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PocketItem): Long

    @Update
    suspend fun updateItem(item: PocketItem)

    @Delete
    suspend fun deleteItem(item: PocketItem)

    @Query("DELETE FROM pocket_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pocket_items SET folderName = :newFolder WHERE id = :id")
    suspend fun updateFolder(id: Long, newFolder: String)

    @Query("UPDATE pocket_items SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: Long, isPinned: Boolean)

    @Query("UPDATE pocket_items SET isAlarmActive = :isActive WHERE id = :id")
    suspend fun setAlarmActive(id: Long, isActive: Boolean)
}
