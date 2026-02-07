package com.killedbythegalaxy.radiokotlin.ui.screens

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.killedbythegalaxy.radiokotlin.data.model.PlayerStatus
import com.killedbythegalaxy.radiokotlin.data.model.RadioState
import com.killedbythegalaxy.radiokotlin.data.model.Song
import com.killedbythegalaxy.radiokotlin.data.repository.RadioRepository
import com.killedbythegalaxy.radiokotlin.service.RadioPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RadioViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val radioRepository: RadioRepository
) : ViewModel() {
    
    private val _radioState = MutableStateFlow(RadioState())
    val radioState: StateFlow<RadioState> = _radioState.asStateFlow()
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    init {
        initializeMediaController()
        observeNowPlaying()
    }
    
    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, RadioPlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupPlayerListener()
            
            // Auto-start playback
            mediaController?.play()
        }, MoreExecutors.directExecutor())
    }
    
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val status = when (playbackState) {
                    Player.STATE_IDLE -> PlayerStatus.IDLE
                    Player.STATE_BUFFERING -> PlayerStatus.BUFFERING
                    Player.STATE_READY -> if (mediaController?.isPlaying == true) PlayerStatus.PLAYING else PlayerStatus.PAUSED
                    Player.STATE_ENDED -> PlayerStatus.IDLE
                    else -> PlayerStatus.IDLE
                }
                _radioState.update { it.copy(status = status) }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _radioState.update { 
                    it.copy(status = if (isPlaying) PlayerStatus.PLAYING else PlayerStatus.PAUSED) 
                }
            }
            
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val title = mediaMetadata.title?.toString()
                val artist = mediaMetadata.artist?.toString()
                if (title != null || artist != null) {
                    _radioState.update {
                        it.copy(
                            currentTrack = Song(
                                id = "",
                                text = "$artist - $title",
                                artist = artist ?: "",
                                title = title ?: "",
                                album = null,
                                genre = null,
                                lyrics = null,
                                art = null,
                                custom_fields = null
                            )
                        )
                    }
                }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _radioState.update { 
                    it.copy(
                        status = PlayerStatus.ERROR,
                        errorMessage = error.message
                    ) 
                }
            }
        })
    }
    
    private fun observeNowPlaying() {
        viewModelScope.launch {
            radioRepository.observeNowPlaying(intervalMs = 10_000)
                .catch { e ->
                    _radioState.update { it.copy(errorMessage = e.message) }
                }
                .collect { result ->
                    result.onSuccess { response ->
                        _radioState.update { state ->
                            state.copy(
                                currentTrack = response.now_playing?.song,
                                nextTrack = response.playing_next?.song,
                                listeners = response.listeners.current,
                                isLive = response.live.is_live,
                                streamerName = response.live.streamer_name,
                                elapsed = response.now_playing?.elapsed ?: 0,
                                duration = response.now_playing?.duration ?: 0,
                                errorMessage = null
                            )
                        }
                    }.onFailure { e ->
                        _radioState.update { it.copy(errorMessage = e.message) }
                    }
                }
        }
    }
    
    fun playPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }
    
    fun play() {
        mediaController?.play()
    }
    
    fun pause() {
        mediaController?.pause()
    }
    
    fun stop() {
        mediaController?.stop()
    }
    
    fun setVolume(volume: Float) {
        mediaController?.volume = volume.coerceIn(0f, 1f)
        _radioState.update { it.copy(volume = volume) }
    }
    
    override fun onCleared() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
