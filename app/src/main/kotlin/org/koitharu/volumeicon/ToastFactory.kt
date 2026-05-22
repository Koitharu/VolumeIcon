package org.koitharu.volumeicon

import android.content.Context
import android.view.Gravity
import android.widget.Toast

class ToastFactory(
    private val context: Context,
) {

    fun createDeviceToast(device: OutputDevice): Toast {
        val toast = Toast.makeText(context, device.getLabel(context.resources), Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP, 0, 0)
        return toast
    }
}