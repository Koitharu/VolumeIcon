package org.koitharu.volumeicon.config

enum class NotificationPolicy {

    ALWAYS, MUTED, NOT_MUTED, NEVER;

    fun shouldShow(isMuted: Boolean) = when (this) {
        ALWAYS -> true
        MUTED -> isMuted
        NOT_MUTED -> !isMuted
        NEVER -> false
    }
}