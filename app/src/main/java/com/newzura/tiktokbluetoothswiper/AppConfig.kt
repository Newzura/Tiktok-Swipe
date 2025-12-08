package com.newzura.tiktokbluetoothswiper

import android.view.KeyEvent

object AppConfig {
    // Boutons pour swipe UP
    val SWIPE_UP_KEYS = setOf(
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_MEDIA_NEXT
    )

    // Boutons pour swipe DOWN
    val SWIPE_DOWN_KEYS = setOf(
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_MEDIA_PREVIOUS
    )

    // Boutons pour PAUSE/PLAY
    val PLAY_PAUSE_KEYS = setOf(
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY,
        KeyEvent.KEYCODE_MEDIA_PAUSE
    )
}
