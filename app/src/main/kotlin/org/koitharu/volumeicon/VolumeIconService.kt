package org.koitharu.volumeicon

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import org.koitharu.volumeicon.config.AppSettings
import org.koitharu.volumeicon.config.BeepPolicy
import org.koitharu.volumeicon.utils.registerReceiverCompat

class VolumeIconService : AccessibilityService() {

    private lateinit var volumeReceiver: BroadcastReceiver
    private lateinit var deviceCallback: AudioDeviceCallback
    private lateinit var notificationHolder: NotificationHolder
    private lateinit var audioManager: AudioManager
    private lateinit var settings: AppSettings
    private lateinit var preferenceListener: AutoCloseable
    private lateinit var volumeControlReceiver: VolumeControlReceiver
    private lateinit var toneGenerator: ToneGenerator

    override fun onCreate() {
        super.onCreate()
        settings = AppSettings(this)
        volumeControlReceiver = VolumeControlReceiver()
        toneGenerator = ToneGenerator(STREAM_MUSIC, 100)
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
        toneGenerator.release()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) {
            return super.onKeyEvent(event)
        }
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return handleVolumeButton(event.action, keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        }
        return super.onKeyEvent(event)
    }

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

    @SuppressLint("WrongConstant")
    private fun handleVolumeButton(action: Int, isUp: Boolean): Boolean {
        if (audioManager.mode != AudioManager.MODE_NORMAL) {
            return false
        }
        val beepPolicy = settings.beepPolicy
        val hideUi = settings.hideVolumeUi
        if (beepPolicy == BeepPolicy.SYSTEM && !hideUi) {
            // fallback to system behavior
            return false
        }
        if (action == KeyEvent.ACTION_DOWN) {
            var flags = 1 shl 12 // FLAG_FROM_KEY
            if (!settings.hideVolumeUi) {
                flags = flags or AudioManager.FLAG_SHOW_UI
            }
            when (settings.beepPolicy) {
                BeepPolicy.SYSTEM -> Unit
                BeepPolicy.ENABLED -> playBeep()
                BeepPolicy.DISABLED -> flags = flags or AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            }
            audioManager.adjustVolume(
                if (isUp) {
                    AudioManager.ADJUST_RAISE
                } else {
                    AudioManager.ADJUST_LOWER
                },
                flags
            )
        }
        return true
    }

    private fun playBeep() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    companion object {

        fun isActive(context: Context): Boolean {
            val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = am.getEnabledAccessibilityServiceList(FEEDBACK_GENERIC)
            return enabledServices.any { service ->
                val serviceInfo = service.resolveInfo.serviceInfo
                serviceInfo.packageName == context.packageName && serviceInfo.name == VolumeIconService::class.java.name
            }
        }
    }
}