package org.koitharu.volumeicon

import android.os.Handler
import android.os.Looper

object VolumeWatcher {

    private val handler = Handler(Looper.getMainLooper())
    private var lastVolume: Int = 0
    private val commitRunnable = Runnable {
        if (lastVolume != 0) {
            lastNonZeroVolume = lastVolume
        }
    }
    private const val TIMEOUT = 5_000L

    var lastNonZeroVolume: Int = 0
        private set

    fun onVolumeChanged(volume: Int) {
        if (volume != lastVolume) {
            lastVolume = volume
            handler.removeCallbacks(commitRunnable)
            handler.postDelayed(commitRunnable, TIMEOUT)
        }
    }
}
