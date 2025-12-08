package com.newzura.tiktokbluetoothswiper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent

class MediaButtonReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "MediaButtonReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            Log.d(TAG, "Intent ou Context null")
            return
        }

        Log.d(TAG, "Intent reçu : ${intent.action}")

        val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        Log.d(TAG, "KeyEvent : $keyEvent")

        if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "KeyEvent DOWN détecté : ${KeyEvent.keyCodeToString(keyEvent.keyCode)}")

            // Transférer au service
            val serviceIntent = Intent(context, TikTokSwipeService::class.java)
            serviceIntent.action = AppConstants.ACTION_MEDIA_BUTTON
            serviceIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)

            context.startService(serviceIntent)
        }
    }
}