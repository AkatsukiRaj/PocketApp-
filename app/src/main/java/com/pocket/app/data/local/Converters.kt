package com.pocket.app.data.local

import androidx.room.TypeConverter
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.data.model.ItemType

class Converters {
    @TypeConverter
    fun fromItemType(value: ItemType): String = value.name

    @TypeConverter
    fun toItemType(value: String): ItemType = runCatching { ItemType.valueOf(value) }.getOrDefault(ItemType.DOCUMENT)

    @TypeConverter
    fun fromItemCategory(value: ItemCategory): String = value.name

    @TypeConverter
    fun toItemCategory(value: String): ItemCategory = runCatching { ItemCategory.valueOf(value) }.getOrDefault(ItemCategory.GENERAL)
}
