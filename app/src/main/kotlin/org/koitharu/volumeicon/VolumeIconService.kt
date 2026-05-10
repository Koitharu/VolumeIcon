package org.koitharu.volumeicon

import android.accessibilityservice.AccessibilityService
import android.database.ContentObserver
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import org.koitharu.volumeicon.config.AppSettings

class VolumeIconService : AccessibilityService() {

    private lateinit var volumeObserver: ContentObserver
    private lateinit var notificationHolder: NotificationHolder
    private lateinit var audioManager: AudioManager
    private lateinit var settings: AppSettings
    private lateinit var preferenceListener: AutoCloseable

    override fun onCreate() {
        super.onCreate()
        settings = AppSettings(this)
        notificationHolder = NotificationHolder(this)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                handleVolumeChanged()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        PermissionRequestActivity.startIfNeeded(this)
        contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            volumeObserver
        )
        handleVolumeChanged()
        preferenceListener = settings.doOnNotificationPolicyChanged {
            handleVolumeChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceListener.close()
        contentResolver.unregisterContentObserver(volumeObserver)
        notificationHolder.clear()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    private fun handleVolumeChanged() {
        val currentVolume = audioManager.getStreamVolume(STREAM_MUSIC)
        val policy = settings.notificationPolicy
        if (policy.shouldShow(isMuted = currentVolume == 0)) {
            val maxVolume = audioManager.getStreamMaxVolume(STREAM_MUSIC)
            val volumePercent = currentVolume * 100 / maxVolume
            notificationHolder.showNotification(volumePercent)
        } else {
            notificationHolder.clear()
        }
    }
}