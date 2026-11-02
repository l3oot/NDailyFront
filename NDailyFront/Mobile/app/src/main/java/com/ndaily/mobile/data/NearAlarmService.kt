package com.ndaily.mobile.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.ndaily.mobile.MainActivity
import com.ndaily.mobile.R

class NearAlarmService : Service() {
    private var player: MediaPlayer? = null
    private var allowVibration = true
    private var allowSound = true
    private var title = ""
    private var body = ""

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= 31) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                tearDown()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_CONFIG -> {
                allowVibration = intent.getBooleanExtra(EXTRA_VIBRATION, allowVibration)
                allowSound = intent.getBooleanExtra(EXTRA_SOUND, allowSound)
                applyOutputs()
                return START_STICKY
            }
            else -> {
                title = intent?.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { title }
                body = intent?.getStringExtra(EXTRA_BODY).orEmpty().ifBlank { body }
                allowVibration = intent?.getBooleanExtra(EXTRA_VIBRATION, allowVibration) ?: allowVibration
                allowSound = intent?.getBooleanExtra(EXTRA_SOUND, allowSound) ?: allowSound
                ensureChannel()
                startAsForeground(buildNotification(title, body))
                applyOutputs()
                return START_STICKY
            }
        }
    }

    override fun onDestroy() {
        tearDown()
        super.onDestroy()
    }

    private fun startAsForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                NOTIFY_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFY_ID, notification)
        }
    }

    private fun applyOutputs() {
        if (allowVibration) startVibration() else stopVibration()
        if (allowSound) startSound() else stopSound()
    }

    private fun startVibration() {
        val vibe = vibrator ?: return
        if (!vibe.hasVibrator()) return
        // System-managed repeating waveform keeps pulsing while the FGS is alive.
        val pattern = longArrayOf(0L, 400L, 500L)
        if (Build.VERSION.SDK_INT >= 26) {
            vibe.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibe.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    private fun startSound() {
        if (player != null) return
        try {
            val media = MediaPlayer()
            media.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            resources.openRawResourceFd(R.raw.alarm).use { afd ->
                media.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            media.isLooping = true
            media.setVolume(1f, 1f)
            media.prepare()
            media.start()
            player = media
        } catch (_: Exception) {
            player = null
        }
    }

    private fun stopSound() {
        val media = player ?: return
        player = null
        try {
            if (media.isPlaying) media.stop()
        } catch (_: IllegalStateException) {
        }
        media.release()
    }

    private fun tearDown() {
        stopVibration()
        stopSound()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Nearwake alarm",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Alerts when you enter or leave a Nearwake area"
                enableVibration(false)
                setSound(null, null)
            },
        )
    }

    private fun buildNotification(title: String, body: String): Notification {
        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_notify)
            .setContentTitle(title.ifBlank { getString(R.string.app_name) })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(pending)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "nearwake_alarm"
        const val NOTIFY_ID = 4201
        private const val ACTION_START = "com.ndaily.mobile.action.NEAR_ALARM_START"
        private const val ACTION_STOP = "com.ndaily.mobile.action.NEAR_ALARM_STOP"
        private const val ACTION_CONFIG = "com.ndaily.mobile.action.NEAR_ALARM_CONFIG"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_BODY = "body"
        private const val EXTRA_VIBRATION = "vibration"
        private const val EXTRA_SOUND = "sound"

        fun start(context: Context, title: String, body: String, vibration: Boolean, sound: Boolean) {
            val intent = Intent(context, NearAlarmService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                putExtra(EXTRA_VIBRATION, vibration)
                putExtra(EXTRA_SOUND, sound)
            }
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, NearAlarmService::class.java).apply { action = ACTION_STOP },
            )
        }

        fun configure(context: Context, vibration: Boolean, sound: Boolean) {
            context.startService(
                Intent(context, NearAlarmService::class.java).apply {
                    action = ACTION_CONFIG
                    putExtra(EXTRA_VIBRATION, vibration)
                    putExtra(EXTRA_SOUND, sound)
                },
            )
        }
    }
}
