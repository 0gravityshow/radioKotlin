package com.killedbythegalaxy.radiokotlin.ui.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killedbythegalaxy.radiokotlin.ui.theme.*
import kotlin.random.Random

@Composable
fun RadioScreen(onChatClick: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(0.8f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientStart, GradientMiddle, GradientEnd)))
    ) {
        StarfieldBackground()
        
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KILLED BY THE GALAXY",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Surface(
                    shape = CircleShape,
                    color = StatusLive.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "live")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 1f, targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                            label = "pulse"
                        )
                        Box(modifier = Modifier.size(8.dp).background(StatusLive.copy(alpha = alpha), CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text("LIVE", style = MaterialTheme.typography.labelSmall, color = StatusLive, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Visualizer
            CircularVisualizer(isPlaying = isPlaying, modifier = Modifier.size(280.dp))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Now Playing Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CosmicGray.copy(alpha = 0.8f)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isPlaying) "ON AIR" else "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPlaying) StatusLive else CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Killed by the Galaxy Radio",
                        style = MaterialTheme.typography.headlineSmall,
                        color = StarWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Cosmic Beats • 24/7 Stream",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberCyanDark
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Play Button
            FilledIconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.size(80.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CyberCyan,
                    contentColor = VoidBlack
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Volume
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VolumeDown, null, tint = CyberCyanDark, modifier = Modifier.size(24.dp))
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan, inactiveTrackColor = CyberBlue)
                )
                Icon(Icons.Default.VolumeUp, null, tint = CyberCyan, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Chat Button
            Button(
                onClick = onChatClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple, contentColor = StarWhite),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Chat, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("EGO CHAT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CircularVisualizer(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            
            for (i in 0..5) {
                drawCircle(color = CyberCyan.copy(alpha = 0.1f - i * 0.015f), radius = radius + i * 10, center = center)
            }
            drawCircle(
                color = CyberCyan.copy(alpha = if (isPlaying) 0.8f else 0.4f),
                radius = radius * pulse, center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
            drawCircle(color = NebulaPurple.copy(alpha = 0.5f), radius = radius * 0.7f, center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
        }
        
        Surface(modifier = Modifier.size(200.dp), shape = CircleShape, color = DeepSpace.copy(alpha = 0.9f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isPlaying) {
                    EqualizerBars()
                } else {
                    Icon(Icons.Default.Radio, null, tint = CyberCyan.copy(alpha = 0.7f), modifier = Modifier.size(64.dp))
                }
            }
        }
    }
}

@Composable
private fun EqualizerBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(12) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = 20f, targetValue = 80f,
                animationSpec = infiniteRepeatable(tween(300 + index * 50, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "bar$index"
            )
            Box(
                modifier = Modifier.width(8.dp).height(height.dp)
                    .background(Brush.verticalGradient(listOf(CyberCyan, NebulaPurple)), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun StarfieldBackground() {
    val stars = remember { List(100) { Offset(Random.nextFloat(), Random.nextFloat()) to Random.nextFloat() * 2 + 1 } }
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "twinkle")
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEachIndexed { index, (pos, size) ->
            drawCircle(StarWhite.copy(alpha = if (index % 3 == 0) twinkle else 0.7f), size, Offset(pos.x * this.size.width, pos.y * this.size.height))
        }
    }
}
