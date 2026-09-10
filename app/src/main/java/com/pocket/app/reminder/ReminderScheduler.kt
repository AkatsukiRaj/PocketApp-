package com.pocket.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pocket.app.data.model.PocketItem

object ReminderScheduler {

    const val EXTRA_ITEM_ID = "extra_item_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_DESC = "extra_desc"

    fun scheduleReminder(context: Context, item: PocketItem) {
        val triggerTime = item.reminderTime ?: return
        if (triggerTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ITEM_ID, item.id)
            putExtra(EXTRA_TITLE, item.title)
            putExtra(EXTRA_DESC, item.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, itemId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itemId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
