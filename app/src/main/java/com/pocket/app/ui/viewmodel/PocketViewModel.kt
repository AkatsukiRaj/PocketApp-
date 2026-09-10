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

    val notes: StateFlow<List<PocketItem>> = repository.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<PocketItem>> = repository.getReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLanguage(newLanguage: AppLanguage) {
        preferences.setLanguage(newLanguage)
    }

    fun setThemeMode(newThemeMode: AppThemeMode) {
        preferences.setThemeMode(newThemeMode)
    }

    fun saveIncomingFile(uri: Uri, title: String, category: ItemCategory, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveIncomingUri(uri, title, category)
            onComplete()
        }
    }

    fun addNote(title: String, content: String, category: ItemCategory = ItemCategory.GENERAL) {
        viewModelScope.launch {
            repository.createNote(title, content, category)
        }
    }

    fun addReminder(title: String, desc: String, timeMillis: Long, category: ItemCategory = ItemCategory.MEDICAL) {
        viewModelScope.launch {
            repository.createReminder(title, desc, timeMillis, category)
        }
    }

    fun togglePin(item: PocketItem) {
        viewModelScope.launch {
            repository.togglePin(item)
        }
    }

    fun toggleAlarm(item: PocketItem) {
        viewModelScope.launch {
            repository.toggleAlarm(item)
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
