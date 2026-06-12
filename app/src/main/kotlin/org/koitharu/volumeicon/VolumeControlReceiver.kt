package org.koitharu.volumeicon

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

class VolumeControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val targetVolume = intent?.data?.schemeSpecificPart?.toIntOrNull() ?: return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (targetVolume == SHOW_UI) {
            if (!openVolumePanel(context)) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI
                )
            }
        } else {
            val volume = if (targetVolume == UNMUTE) {
                val userVolume = VolumeWatcher.lastNonZeroVolume
                if (userVolume <= 0) {
                    (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.3).toInt()
                } else {
                    userVolume
                }
            } else {
                targetVolume
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            showToast(context, targetVolume)
        }
    }

    private fun openVolumePanel(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val panelIntent = Intent(Settings.Panel.ACTION_VOLUME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(panelIntent)
                true
            } catch (_: IllegalStateException) {
                false
            }
        } else {
            false
        }

    private fun showToast(context: Context, volume: Int) {
        if (volume == 0) {
            Toast.makeText(context, R.string.volume_is_muted, Toast.LENGTH_SHORT)
                .show()
        }
    }

    companion object {

        private const val ACTION_SET_VOLUME = "org.koitharu.volumeicon.ACTION_SET_VOLUME"
        private const val SCHEME = "volume"
        private const val MUTE = 0
        private const val UNMUTE = -1
        private const val SHOW_UI = -2

        val intentFilter: IntentFilter
            get() = IntentFilter(ACTION_SET_VOLUME).apply {
                addDataScheme(SCHEME)
            }

        fun getPendingIntent(context: Context, targetVolume: Int): PendingIntent {
            val intent = Intent(ACTION_SET_VOLUME)
            intent.setData(Uri.fromParts(SCHEME, targetVolume.toString(), null))
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        fun getUiPendingIntent(context: Context) = getPendingIntent(context, SHOW_UI)

        fun getMutePendingIntent(context: Context) = getPendingIntent(context, MUTE)

        fun getUnmutePendingIntent(context: Context) = getPendingIntent(context, UNMUTE)
    }
}