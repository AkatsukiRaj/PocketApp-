package com.pocket.app.data.repository

import android.content.Context
import android.net.Uri
import com.pocket.app.data.local.PocketDao
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.data.model.ItemType
import com.pocket.app.data.model.PocketItem
import com.pocket.app.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class PocketRepository(
    private val dao: PocketDao,
    private val context: Context
) {

    fun getAllItems(): Flow<List<PocketItem>> = dao.getAllItems()

    fun getPinnedItems(): Flow<List<PocketItem>> = dao.getPinnedItems()

    fun getPhotos(): Flow<List<PocketItem>> = dao.getItemsByType(ItemType.PHOTO)

    fun getDocuments(): Flow<List<PocketItem>> = dao.getItemsByType(ItemType.DOCUMENT)

    fun getFoldersForType(type: ItemType): Flow<List<String>> = dao.getFoldersForType(type)

    fun getNotes(): Flow<List<PocketItem>> = dao.getItemsByType(ItemType.NOTE)

    fun searchItems(query: String): Flow<List<PocketItem>> = dao.searchItems(query)

    suspend fun saveIncomingUri(
        uri: Uri,
        title: String,
        category: ItemCategory = ItemCategory.GENERAL,
        folderName: String = "General"
    ): PocketItem = withContext(Dispatchers.IO) {
        val (savedFile, originalName) = FileUtils.saveUriToVault(context, uri)
        val ext = FileUtils.getExtension(originalName)
        val resolverMime = try { context.contentResolver.getType(uri) } catch (e: Exception) { null }
        val fileMime = FileUtils.getMimeType(context, savedFile)
        val mime = resolverMime ?: fileMime
        val type = FileUtils.determineItemType(ext, mime)

        val item = PocketItem(
            title = if (title.isNotBlank()) title else originalName,
            description = originalName,
            itemType = type,
            category = category,
            folderName = if (folderName.isNotBlank()) folderName else "General",
            filePath = savedFile.absolutePath,
            mimeType = mime,
            fileExtension = ext,
            fileSize = savedFile.length(),
            isPinned = false
        )

        val id = dao.insertItem(item)
        item.copy(id = id)
    }

    suspend fun updateItemFolder(id: Long, newFolder: String) = withContext(Dispatchers.IO) {
        dao.updateFolder(id, newFolder)
    }

    suspend fun renameItem(id: Long, newTitle: String) = withContext(Dispatchers.IO) {
        dao.updateTitle(id, newTitle)
    }

    suspend fun createNote(
        title: String,
        content: String,
        category: ItemCategory = ItemCategory.GENERAL
    ): Long = withContext(Dispatchers.IO) {
        val note = PocketItem(
            title = title,
            description = content,
            itemType = ItemType.NOTE,
            category = category,
            isPinned = false
        )
        dao.insertItem(note)
    }

    suspend fun togglePin(item: PocketItem) = withContext(Dispatchers.IO) {
        dao.togglePin(item.id, !item.isPinned)
    }

    suspend fun deleteItem(item: PocketItem) = withContext(Dispatchers.IO) {
        item.filePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        dao.deleteItem(item)
    }

    fun shareItem(item: PocketItem) {
        item.filePath?.let {
            FileUtils.shareFile(context, it, item.title)
        }
    }
}
