package org.koitharu.volumeicon.config

import org.koitharu.volumeicon.R

enum class IconTheme(
    val mutedIcon: Int,
    val lowVolumeIcon: Int,
    val highVolumeIcon: Int,
) {
    SOLID(
        mutedIcon = R.drawable.ic_volume_muted_solid,
        lowVolumeIcon = R.drawable.ic_volume_low_solid,
        highVolumeIcon = R.drawable.ic_volume_high_solid,
    ),
    ROUNDED(
        mutedIcon = R.drawable.ic_volume_muted_rounded,
        lowVolumeIcon = R.drawable.ic_volume_low_rounded,
        highVolumeIcon = R.drawable.ic_volume_high_rounded,
    ),
    LIGHT(
        mutedIcon = R.drawable.ic_volume_muted_light,
        lowVolumeIcon = R.drawable.ic_volume_low_light,
        highVolumeIcon = R.drawable.ic_volume_high_light,
    ),
}