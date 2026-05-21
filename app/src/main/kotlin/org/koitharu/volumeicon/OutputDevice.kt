package org.koitharu.volumeicon

import android.content.res.Resources

data class OutputDevice(
    val type: Type,
    val name: CharSequence?,
) {

    enum class Type(
        val titleResId: Int,
    ) {

        BUILTIN_SPEAKER(R.string.builtin_speaker),
        HEADPHONES(R.string.headphones),
        BLUETOOTH(R.string.bluetooth_device),
        OTHER(R.string.other_device),
    }

    fun getLabel(resources: Resources): CharSequence {
        val typeLabel = resources.getString(type.titleResId)
        return when {
            name == null -> typeLabel
            type == Type.OTHER -> name
            else -> resources.getString(R.string.output_label_template, typeLabel, name)
        }
    }
}