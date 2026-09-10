package com.pocket.app

import android.app.Application
import com.pocket.app.data.local.PocketDatabase
import com.pocket.app.data.preferences.AppPreferences
import com.pocket.app.data.repository.PocketRepository
import com.pocket.app.reminder.NotificationHelper

class PocketApplication : Application() {

    val database by lazy { PocketDatabase.getDatabase(this) }
    val repository by lazy { PocketRepository(database.pocketDao(), this) }
    val preferences by lazy { AppPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
