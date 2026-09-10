package com.pocket.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(ReminderScheduler.EXTRA_ITEM_ID, 0L)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Pocket Reminder"
        val desc = intent.getStringExtra(ReminderScheduler.EXTRA_DESC) ?: ""

        NotificationHelper.showReminderNotification(
            context = context,
            notificationId = itemId.toInt(),
            title = title,
            description = desc
        )
    }
}
