package com.example.sheandsoul_nick

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sheandsoul_nick.features.auth.presentation.MusicTrack // Assuming this is your data class

class MusicPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val NOTIFICATION_ID = 123
    private val CHANNEL_ID = "sheandsoul_music_channel"

    // The MediaSession interface is how other components will interact with our service
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()

        // TODO: You'll need to set up a listener to handle player state changes
        // and update the notification accordingly.
    }

    // This is called when the service is destroyed. We must release the player resources.
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}