package org.koitharu.volumeicon.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
import android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE
import android.media.AudioManager
import android.os.Build

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerReceiverCompat(
    receiver: BroadcastReceiver,
    intentFilter: IntentFilter,
    exported: Boolean,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(
            receiver, intentFilter, if (exported) {
                Context.RECEIVER_EXPORTED
            } else {
                Context.RECEIVER_NOT_EXPORTED
            }
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        registerReceiver(receiver, intentFilter, 0)
    } else {
        registerReceiver(receiver, intentFilter)
    }
}

@Suppress("DEPRECATION")
fun AudioManager.isBuiltInSpeakerInUse(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val type = communicationDevice?.type ?: return isSpeakerphoneOn
        type == TYPE_BUILTIN_SPEAKER || type == TYPE_BUILTIN_SPEAKER_SAFE
    } else {
        isSpeakerphoneOn
    }
}