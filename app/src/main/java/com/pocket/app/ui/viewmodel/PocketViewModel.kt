package com.pocket.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocket.app.PocketApplication
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.data.model.PocketItem
import com.pocket.app.data.preferences.AppLanguage
import com.pocket.app.data.preferences.AppThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PocketViewModel(application: Application) : AndroidViewModel(application) {

    private val pocketApp = application as PocketApplication
    private val repository = pocketApp.repository
    private val preferences = pocketApp.preferences

    val language: StateFlow<AppLanguage> = preferences.language
    val themeMode: StateFlow<AppThemeMode> = preferences.themeMode

    val pinnedItems: StateFlow<List<PocketItem>> = repository.getPinnedItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<PocketItem>> = repository.getPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<PocketItem>> = repository.getDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photoFolders: StateFlow<List<String>> = repository.getFoldersForType(com.pocket.app.data.model.ItemType.PHOTO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val docFolders: StateFlow<List<String>> = repository.getFoldersForType(com.pocket.app.data.model.ItemType.DOCUMENT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<PocketItem>> = repository.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLanguage(newLanguage: AppLanguage) {
        preferences.setLanguage(newLanguage)
    }

    fun setThemeMode(newThemeMode: AppThemeMode) {
        preferences.setThemeMode(newThemeMode)
    }

    fun saveIncomingFile(
        uri: Uri,
        title: String,
        category: ItemCategory,
        folderName: String = "General",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveIncomingUri(uri, title, category, folderName)
            onComplete()
        }
    }

    fun updateFolder(item: PocketItem, newFolder: String) {
        viewModelScope.launch {
            repository.updateItemFolder(item.id, newFolder)
        }
    }

    fun renameItem(item: PocketItem, newTitle: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.renameItem(item.id, newTitle)
            onComplete()
        }
    }

    fun addNote(title: String, content: String, category: ItemCategory = ItemCategory.GENERAL) {
        viewModelScope.launch {
            repository.createNote(title, content, category)
        }
    }

    fun togglePin(item: PocketItem) {
        viewModelScope.launch {
            repository.togglePin(item)
        }
    }

    fun deleteItem(item: PocketItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun shareItem(item: PocketItem) {
        repository.shareItem(item)
    }
}
