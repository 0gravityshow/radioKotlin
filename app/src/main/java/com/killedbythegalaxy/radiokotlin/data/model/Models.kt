package com.killedbythegalaxy.radiokotlin.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// AzuraCast API Response Models
data class NowPlayingResponse(
    val station: Station,
    val listeners: Listeners,
    val now_playing: NowPlaying?,
    val playing_next: PlayingNext?,
    val live: LiveStatus
)

data class Station(
    val id: Int,
    val name: String,
    val shortcode: String,
    val description: String,
    val listen_url: String,
    val url: String?,
    val public_player_url: String?
)

data class Listeners(
    val total: Int,
    val unique: Int,
    val current: Int
)

data class NowPlaying(
    val sh_id: Int,
    val played_at: Long,
    val duration: Int,
    val playlist: String?,
    val streamer: String?,
    val is_request: Boolean,
    val song: Song,
    val elapsed: Int,
    val remaining: Int
)

data class Song(
    val id: String,
    val text: String,
    val artist: String,
    val title: String,
    val album: String?,
    val genre: String?,
    val lyrics: String?,
    val art: String?,
    val custom_fields: Map<String, String>?
)

data class PlayingNext(
    val cued_at: Long,
    val played_at: Long,
    val duration: Int,
    val playlist: String?,
    val is_request: Boolean,
    val song: Song
)

data class LiveStatus(
    val is_live: Boolean,
    val streamer_name: String?,
    val broadcast_start: Long?
)

// Firestore Chat Models
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val message: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null,
    val isEgoBot: Boolean = false,
    val isModerated: Boolean = false,
    val moderationReason: String? = null
)

data class UserProfile(
    @DocumentId val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val isPremium: Boolean = false,
    val premiumPurchaseDate: Timestamp? = null,
    val isBlacklisted: Boolean = false,
    val blacklistedUntil: Timestamp? = null,
    val blacklistReason: String? = null,
    val messageCount: Int = 0,
    val lastMessageTime: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

// Player State
enum class PlayerStatus {
    IDLE,
    CONNECTING,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR,
    RECONNECTING
}

data class RadioState(
    val status: PlayerStatus = PlayerStatus.IDLE,
    val currentTrack: Song? = null,
    val nextTrack: Song? = null,
    val listeners: Int = 0,
    val isLive: Boolean = false,
    val streamerName: String? = null,
    val errorMessage: String? = null,
    val volume: Float = 1f,
    val elapsed: Int = 0,
    val duration: Int = 0
)

// Ad Model
data class Advertisement(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val actionUrl: String?,
    val durationSeconds: Int = 30
)

// Paywall State
enum class PaywallStatus {
    NOT_PURCHASED,
    PROCESSING,
    PURCHASED,
    ERROR
}

data class PaywallState(
    val status: PaywallStatus = PaywallStatus.NOT_PURCHASED,
    val errorMessage: String? = null
)
