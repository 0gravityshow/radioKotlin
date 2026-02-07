package com.killedbythegalaxy.radiokotlin.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.killedbythegalaxy.radiokotlin.MainActivity
import com.killedbythegalaxy.radiokotlin.data.remote.AzuraCastApi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RadioPlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        
        // Create ExoPlayer with audio-focused settings
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // handleAudioFocus
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
        
        // Create pending intent for notification tap
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Create MediaSession
        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(pendingIntent)
            .build()
        
        // Set initial media item (stream URL)
        val mediaItem = MediaItem.Builder()
            .setUri(AzuraCastApi.STREAM_URL)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Killed by the Galaxy Radio")
                    .setArtist("Live Stream")
                    .setIsPlayable(true)
                    .build()
            )
            .setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setMaxPlaybackSpeed(1.02f)
                    .build()
            )
            .build()
        
        player?.setMediaItem(mediaItem)
        player?.prepare()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player?.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady != true || player.mediaItemCount == 0) {
            stopSelf()
        }
    }
    
    companion object {
        const val ACTION_PLAY = "com.killedbythegalaxy.radiokotlin.PLAY"
        const val ACTION_PAUSE = "com.killedbythegalaxy.radiokotlin.PAUSE"
        const val ACTION_STOP = "com.killedbythegalaxy.radiokotlin.STOP"
    }
}
