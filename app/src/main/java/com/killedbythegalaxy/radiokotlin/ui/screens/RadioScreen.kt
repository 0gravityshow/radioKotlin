package com.killedbythegalaxy.radiokotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.killedbythegalaxy.radiokotlin.data.model.PlayerStatus
import com.killedbythegalaxy.radiokotlin.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun RadioScreen(
    viewModel: RadioViewModel = hiltViewModel(),
    onChatClick: () -> Unit
) {
    val radioState by viewModel.radioState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            )
    ) {
        // Animated star background
        StarfieldBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            RadioHeader(listeners = radioState.listeners, isLive = radioState.isLive)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Circular Ad Display / Visualizer
            CircularVisualizer(
                isPlaying = radioState.status == PlayerStatus.PLAYING,
                modifier = Modifier.size(280.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Now Playing Info
            NowPlayingCard(
                title = radioState.currentTrack?.title ?: "Connecting...",
                artist = radioState.currentTrack?.artist ?: "Killed by the Galaxy Radio",
                status = radioState.status
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Controls
            PlayerControls(
                isPlaying = radioState.status == PlayerStatus.PLAYING,
                isBuffering = radioState.status == PlayerStatus.BUFFERING,
                volume = radioState.volume,
                onPlayPause = { viewModel.playPause() },
                onVolumeChange = { viewModel.setVolume(it) }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Chat Button
            EgoChatButton(onClick = onChatClick)
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RadioHeader(listeners: Int, isLive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Station Name
        Text(
            text = "KILLED BY THE GALAXY",
            style = MaterialTheme.typography.titleMedium,
            color = CyberCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        
        // Live indicator and listeners
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Listeners count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$listeners",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyan
                )
            }
            
            // Live indicator
            if (isLive) {
                Surface(
                    shape = CircleShape,
                    color = StatusLive.copy(alpha = 0.2f),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Pulsing dot
                        val infiniteTransition = rememberInfiniteTransition(label = "live")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(StatusLive.copy(alpha = alpha), CircleShape)
                        )
                        
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusLive,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    
    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulse animation
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            
            // Glow effect
            for (i in 0..5) {
                drawCircle(
                    color = CyberCyan.copy(alpha = 0.1f - i * 0.015f),
                    radius = radius + i * 10,
                    center = center
                )
            }
            
            // Main ring
            drawCircle(
                color = CyberCyan.copy(alpha = if (isPlaying) 0.8f else 0.4f),
                radius = radius * pulse,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
            
            // Inner decorative ring
            drawCircle(
                color = NebulaPurple.copy(alpha = 0.5f),
                radius = radius * 0.7f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
        
        // Center content
        Surface(
            modifier = Modifier.size(200.dp),
            shape = CircleShape,
            color = DeepSpace.copy(alpha = 0.9f),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Visualizer bars (when playing)
                if (isPlaying) {
                    EqualizerBars()
                } else {
                    // Logo/Station icon when paused
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = CyberCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EqualizerBars() {
    val barCount = 12
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    
    Row(
        modifier = Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = 20f,
                targetValue = 80f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 300 + index * 50,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$index"
            )
            
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CyberCyan, NebulaPurple)
                        )
                    )
            )
        }
    }
}

@Composable
private fun NowPlayingCard(
    title: String,
    artist: String,
    status: PlayerStatus
) {
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            PlayerStatus.PLAYING -> StatusLive
            PlayerStatus.BUFFERING, PlayerStatus.CONNECTING -> StatusConnecting
            PlayerStatus.RECONNECTING -> StatusReconnecting
            PlayerStatus.ERROR -> StatusOffline
            else -> CyberCyan
        },
        label = "statusColor"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CosmicGray.copy(alpha = 0.8f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status indicator
            Text(
                text = status.name.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Track title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = StarWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (artist.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberCyanDark,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    volume: Float,
    onPlayPause: () -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Play/Pause Button
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(80.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = CyberCyan,
                contentColor = VoidBlack
            ),
            enabled = !isBuffering
        ) {
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = VoidBlack,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Volume Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeDown,
                contentDescription = null,
                tint = CyberCyanDark,
                modifier = Modifier.size(24.dp)
            )
            
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = CyberCyan,
                    activeTrackColor = CyberCyan,
                    inactiveTrackColor = CyberBlue
                )
            )
            
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EgoChatButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NebulaPurple,
            contentColor = StarWhite
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "EGO CHAT",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun StarfieldBackground() {
    val stars = remember {
        List(100) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat()
            ) to Random.nextFloat() * 2 + 1
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEachIndexed { index, (position, size) ->
            val alpha = if (index % 3 == 0) twinkle else 0.7f
            drawCircle(
                color = StarWhite.copy(alpha = alpha),
                radius = size,
                center = Offset(position.x * this.size.width, position.y * this.size.height)
            )
        }
    }
}
