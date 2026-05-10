package org.koitharu.volumeicon

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import org.koitharu.volumeicon.config.IconTheme

class NotificationHolder(
    private val context: Context,
    var iconTheme: IconTheme,
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
                volume == 0 -> iconTheme.mutedIcon
                volume < 60 -> iconTheme.lowVolumeIcon
                else -> iconTheme.highVolumeIcon
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

    companion object {

        private const val CHANNEL_ID = "volume"
        private const val NOTIFICATION_ID = 6

        fun settingsIntent(context: Context): Intent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
    }
}