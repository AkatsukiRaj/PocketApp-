package com.pocket.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pocket.app.data.model.PocketItem

@Database(entities = [PocketItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PocketDatabase : RoomDatabase() {

    abstract fun pocketDao(): PocketDao

    companion object {
        @Volatile
        private var INSTANCE: PocketDatabase? = null

        fun getDatabase(context: Context): PocketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketDatabase::class.java,
                    "pocket_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
