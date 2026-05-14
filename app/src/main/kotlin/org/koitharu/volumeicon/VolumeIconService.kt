package org.koitharu.volumeicon

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import org.koitharu.volumeicon.config.AppSettings
import org.koitharu.volumeicon.utils.registerReceiverCompat

class VolumeIconService : AccessibilityService() {

    private lateinit var volumeReceiver: BroadcastReceiver
    private lateinit var deviceCallback: AudioDeviceCallback
    private lateinit var notificationHolder: NotificationHolder
    private lateinit var audioManager: AudioManager
    private lateinit var settings: AppSettings
    private lateinit var preferenceListener: AutoCloseable
    private lateinit var volumeControlReceiver: VolumeControlReceiver

    override fun onCreate() {
        super.onCreate()
        settings = AppSettings(this)
        volumeControlReceiver = VolumeControlReceiver()
        notificationHolder = NotificationHolder(this, settings.iconTheme)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        volumeReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) = handleVolumeChanged()
        }
        deviceCallback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) =
                handleVolumeChanged()

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) =
                handleVolumeChanged()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        PermissionRequestActivity.startIfNeeded(this)
        registerReceiverCompat(volumeControlReceiver, VolumeControlReceiver.intentFilter, false)
        registerReceiverCompat(
            volumeReceiver,
            IntentFilter().apply {
                addAction("android.media.VOLUME_CHANGED_ACTION")
                addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            },
            true
        )
        audioManager.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
        handleVolumeChanged()
        preferenceListener = settings.doOnSettingsChanged(
            setOf(AppSettings.KEY_ICON_THEME, AppSettings.KEY_NOTIFICATION_POLICY)
        ) {
            notificationHolder.iconTheme = iconTheme
            handleVolumeChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceListener.close()
        audioManager.unregisterAudioDeviceCallback(deviceCallback)
        unregisterReceiver(volumeControlReceiver)
        unregisterReceiver(volumeReceiver)
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