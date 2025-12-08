package com.newzura.tiktokbluetoothswiper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Path
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat

class TikTokSwipeService : AccessibilityService() {
    private val TAG = "TikTokSwipeService"
    private var isTikTokInForeground = false
    private var mediaSession: MediaSession? = null
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        Log.d(TAG, "Audio focus change: $focusChange")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (isTikTokInForeground) {
                    Log.d(TAG, "Regaining audio focus")
                    requestAudioFocus()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate called")
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected called")
        setupMediaSessionAndNotification()
        requestAudioFocus()
    }

    private fun requestAudioFocus() {
        Log.d(TAG, "Requesting audio focus")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(
                    AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_MEDIA)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    }
                )
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(audioFocusListener)
                build()
            }
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        Log.d(TAG, "Abandoning audio focus")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusListener)
        }
    }

    private fun setupMediaSessionAndNotification() {
        Log.d(TAG, "Setting up MediaSession and notification")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.NOTIFICATION_CHANNEL_ID,
                "Contrôleur TikTok",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val playbackState = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_PLAY_PAUSE
            )
            .setState(PlaybackState.STATE_PLAYING, 0, 1f)
            .build()

        mediaSession = MediaSession(this, "TiktokSwipeServiceMediaSession").apply {
            setPlaybackState(playbackState)

            val mediaButtonIntent = Intent(
                Intent.ACTION_MEDIA_BUTTON,
                null,
                this@TikTokSwipeService,
                MediaButtonReceiver::class.java
            )
            val pendingIntent = PendingIntent.getBroadcast(
                this@TikTokSwipeService,
                0,
                mediaButtonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setMediaButtonReceiver(pendingIntent)
            Log.d(TAG, "MediaButtonReceiver enregistré")

            setCallback(object : MediaSession.Callback() {
                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                    Log.d(TAG, "onMediaButtonEvent appelé")
                    val keyEvent = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                    if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                        val keyCode = keyEvent.keyCode
                        val keyName = KeyEvent.keyCodeToString(keyCode)
                        Log.d(TAG, "KeyEvent détecté dans callback : $keyName ($keyCode)")
                        sendKeyEventToActivity(keyCode, keyName)
                        handleSwipeLogic(keyCode)
                        return true
                    }
                    return false
                }

                override fun onSkipToNext() {
                    Log.d(TAG, "onSkipToNext appelé")
                    val keyCode = KeyEvent.KEYCODE_MEDIA_NEXT
                    val keyName = KeyEvent.keyCodeToString(keyCode)
                    sendKeyEventToActivity(keyCode, keyName)
                    handleSwipeLogic(keyCode)
                }

                override fun onSkipToPrevious() {
                    Log.d(TAG, "onSkipToPrevious appelé")
                    val keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS
                    val keyName = KeyEvent.keyCodeToString(keyCode)
                    sendKeyEventToActivity(keyCode, keyName)
                    handleSwipeLogic(keyCode)
                }
            })

            isActive = true
            Log.d(TAG, "MediaSession activée")
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, AppConstants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Contrôleur TikTok Actif")
            .setContentText("Prêt à swiper les vidéos.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                AppConstants.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            @Suppress("DEPRECATION")
            startForeground(AppConstants.NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")

        if (intent?.action == AppConstants.ACTION_MEDIA_BUTTON) {
            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                val keyCode = keyEvent.keyCode
                val keyName = KeyEvent.keyCodeToString(keyCode)
                Log.d(TAG, "onStartCommand : KeyEvent détecté : $keyName ($keyCode)")
                sendKeyEventToActivity(keyCode, keyName)
                handleSwipeLogic(keyCode)
            }
        }

        return START_STICKY
    }

    private fun handleSwipeLogic(keyCode: Int) {
        Log.d(TAG, "handleSwipeLogic appelé avec keyCode: $keyCode, TikTok en foreground: $isTikTokInForeground")

        if (isTikTokInForeground) {
            when {
                keyCode in AppConfig.SWIPE_UP_KEYS -> {
                    Log.d(TAG, "Swipe UP")
                    swipeUp()
                }
                keyCode in AppConfig.SWIPE_DOWN_KEYS -> {
                    Log.d(TAG, "Swipe DOWN")
                    swipeDown()
                }
                keyCode in AppConfig.PLAY_PAUSE_KEYS -> {
                    Log.d(TAG, "Pause/Play détecté")
                    pauseOrPlayVideo()
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            val wasInForeground = isTikTokInForeground

            Log.d(TAG, "DEBUG - Package detected: $packageName")

            isTikTokInForeground = packageName == "com.zhiliaoapp.musically" ||
                    packageName == "com.ss.android.ugc.trill" ||
                    packageName == "com.tiktok" ||
                    packageName?.contains("tiktok", ignoreCase = true) == true

            Log.d(TAG, "Package actuel: $packageName, TikTok en foreground: $isTikTokInForeground")

            if (!wasInForeground && isTikTokInForeground) {
                Log.d(TAG, "TikTok est passé au foreground, demande du focus audio")
                requestAudioFocus()
            }
        }
    }

    private fun sendKeyEventToActivity(keyCode: Int, keyName: String) {
        Log.d(TAG, "Envoi de l'événement clé: $keyName ($keyCode)")
        val intent = Intent(AppConstants.ACTION_KEY_EVENT).apply {
            setPackage(packageName)
            putExtra(AppConstants.EXTRA_KEY_CODE, keyCode)
            putExtra(AppConstants.EXTRA_KEY_NAME, keyName)
        }
        sendBroadcast(intent)
    }

    private fun swipeUp() {
        Log.d(TAG, "Execution du swipe UP")
        val metrics = resources.displayMetrics
        val path = Path().apply {
            moveTo(metrics.widthPixels / 2f, metrics.heightPixels * 0.75f)
            lineTo(metrics.widthPixels / 2f, metrics.heightPixels * 0.25f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 200))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun swipeDown() {
        Log.d(TAG, "Execution du swipe DOWN")
        val metrics = resources.displayMetrics
        val path = Path().apply {
            moveTo(metrics.widthPixels / 2f, metrics.heightPixels * 0.25f)
            lineTo(metrics.widthPixels / 2f, metrics.heightPixels * 0.75f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 200))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun pauseOrPlayVideo() {
        Log.d(TAG, "Execution du pause/play - Tap au centre de l'écran")
        val metrics = resources.displayMetrics
        val centerX = metrics.widthPixels / 2f
        val centerY = metrics.heightPixels / 2f

        val path = Path().apply {
            moveTo(centerX, centerY)
            lineTo(centerX, centerY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt called")
        abandonAudioFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        mediaSession?.release()
        abandonAudioFocus()
    }
}
