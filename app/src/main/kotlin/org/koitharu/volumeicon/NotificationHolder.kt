package org.koitharu.volumeicon

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class NotificationHolder(
    private val context: Context,
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.volume_icon_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                channel.isBlockable = false
            }
            channel.enableVibration(false)
            channel.enableLights(false)
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(volume: Int) {
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
        }
        notification.setSmallIcon(
            when {
                volume == 0 -> R.drawable.ic_volume_muted
                volume < 30 -> R.drawable.ic_volume_low
                volume < 60 -> R.drawable.ic_volume_medium
                else -> R.drawable.ic_volume_high
            }
        )
        notification.setContentTitle(
            if (volume == 0) {
                context.getString(R.string.volume_is_muted)
            } else {
                context.getString(R.string.volume_level, volume)
            }
        )
        notification.setProgress(100, volume, false)
        notification.setOngoing(true)
        notification.setCategory(Notification.CATEGORY_STATUS)
        notification.setSound(null, null)
        notification.setVibrate(longArrayOf())
        notification.setOnlyAlertOnce(true)
        notification.setPriority(Notification.PRIORITY_DEFAULT)
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    fun clear() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private companion object {

        const val CHANNEL_ID = "volume"
        const val NOTIFICATION_ID = 6
    }
}