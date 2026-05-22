package org.koitharu.volumeicon.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.media.AudioDeviceInfo.TYPE_BLE_HEADSET
import android.media.AudioDeviceInfo.TYPE_BLE_SPEAKER
import android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
import android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO
import android.media.AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
import android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
import android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE
import android.media.AudioDeviceInfo.TYPE_USB_HEADSET
import android.media.AudioDeviceInfo.TYPE_WIRED_HEADPHONES
import android.media.AudioDeviceInfo.TYPE_WIRED_HEADSET
import android.media.AudioManager
import android.os.Build
import org.koitharu.volumeicon.OutputDevice
import org.koitharu.volumeicon.OutputDevice.Type

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

fun AudioManager.getOutputDevice(): OutputDevice {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val device = communicationDevice ?: return OutputDevice(Type.OTHER, null)
        val type = when (device.type) {
            TYPE_BUILTIN_EARPIECE,
            TYPE_BUILTIN_SPEAKER,
            TYPE_BUILTIN_SPEAKER_SAFE -> Type.BUILTIN_SPEAKER

            TYPE_BLE_HEADSET,
            TYPE_BLE_SPEAKER,
            TYPE_BLUETOOTH_SCO,
            TYPE_BLUETOOTH_A2DP -> Type.BLUETOOTH

            TYPE_WIRED_HEADSET,
            TYPE_USB_HEADSET,
            TYPE_WIRED_HEADPHONES -> Type.HEADPHONES

            else -> Type.OTHER
        }
        OutputDevice(
            type = type,
            name = device.productName?.takeUnless {
                it.isBlank() || it == Build.MODEL
            }
        )
    } else {
        val type = when {
            isSpeakerphoneOn -> Type.BUILTIN_SPEAKER
            isBluetoothScoOn || isBluetoothA2dpOn -> Type.BLUETOOTH
            isWiredHeadsetOn -> Type.HEADPHONES
            else -> Type.BUILTIN_SPEAKER // earpiece
        }
        OutputDevice(type, null)
    }
}