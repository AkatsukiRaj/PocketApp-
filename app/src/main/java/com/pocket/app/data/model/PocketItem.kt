package com.pocket.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemType {
    PHOTO,
    DOCUMENT,
    NOTE,
    REMINDER
}

enum class ItemCategory(val displayName: String, val tamilName: String) {
    MEDICAL("Medical", "மருத்துவம்"),
    BILLS("Bills", "ரசீதுகள்"),
    ID_PROOFS("ID Proofs", "அடையாள அட்டை"),
    PERSONAL("Personal", "சொந்தவை"),
    GENERAL("General", "பொதுவானவை")
}

@Entity(tableName = "pocket_items")
data class PocketItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val itemType: ItemType,
    val category: ItemCategory = ItemCategory.GENERAL,
    val filePath: String? = null,
    val mimeType: String? = null,
    val fileExtension: String? = null,
    val fileSize: Long = 0L,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTime: Long? = null,
    val isReminderCompleted: Boolean = false,
    val isAlarmActive: Boolean = false
)
