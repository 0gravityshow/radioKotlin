package com.killedbythegalaxy.radiokotlin.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killedbythegalaxy.radiokotlin.ui.theme.*

data class DemoMessage(val id: Int, val user: String, val text: String, val isBot: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBackClick: () -> Unit) {
    var isPremium by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        DemoMessage(1, "Galaxy Traveler", "Hey everyone! What's playing?"),
        DemoMessage(2, "EgoBot", "Don't Panic! The answer to your musical question is, as always, 42 beats per minute. 🌌", true),
        DemoMessage(3, "Cosmic DJ", "Love this station! 🚀")
    )) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("EGO CHAT", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        if (isPremium) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Star, null, tint = PaywallGold, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoidBlack, titleContentColor = CyberCyan)
            )
        },
        containerColor = VoidBlack
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Brush.verticalGradient(listOf(VoidBlack, DeepSpace, VoidBlack)))) {
            if (!isPremium) {
                PaywallContent(onUnlock = { isPremium = true })
            } else {
                ChatContent(
                    messages = messages,
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            messages = messages + DemoMessage(messages.size + 1, "You", messageText)
                            messageText = ""
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PaywallContent(onUnlock: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "paywall")
    val glowAlpha by infiniteTransition.animateFloat(0.5f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "glow")
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(120.dp).background(
                Brush.radialGradient(listOf(PaywallGold.copy(alpha = glowAlpha * 0.3f), Color.Transparent)), CircleShape))
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = CosmicGray) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, null, tint = PaywallGold, modifier = Modifier.size(40.dp))
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        Text("UNLOCK EGO CHAT", style = MaterialTheme.typography.headlineMedium, color = PaywallGold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(16.dp))
        Text("Join the galactic conversation!\nChat with fellow travelers.", style = MaterialTheme.typography.bodyLarge, color = StarWhite.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        
        Spacer(Modifier.height(24.dp))
        listOf("Real-time chat with listeners", "Talk to @egobot", "Lifetime access", "Don't Panic!").forEach { feature ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Default.Check, null, tint = StatusLive, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(feature, color = StarWhite.copy(alpha = 0.9f))
            }
        }
        
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onUnlock,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PaywallGold, contentColor = VoidBlack),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Default.LockOpen, null)
            Spacer(Modifier.width(8.dp))
            Text("UNLOCK FOR \$1.99", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ChatContent(messages: List<DemoMessage>, messageText: String, onMessageChange: (String) -> Unit, onSend: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (msg.isBot) "@egobot" else msg.user,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (msg.isBot) EgoBotColor else CyberCyanDark,
                        fontWeight = if (msg.isBot) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                        color = if (msg.isBot) EgoBotColor.copy(alpha = 0.2f) else ChatBubbleOther
                    ) {
                        Text(msg.text, modifier = Modifier.padding(12.dp), color = StarWhite)
                    }
                }
            }
        }
        
        Surface(modifier = Modifier.fillMaxWidth(), color = CosmicGray) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...", color = StarWhite.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberBlue,
                        cursorColor = CyberCyan, focusedTextColor = StarWhite, unfocusedTextColor = StarWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 2
                )
                Spacer(Modifier.width(12.dp))
                FilledIconButton(
                    onClick = onSend,
                    enabled = messageText.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = CyberCyan, contentColor = VoidBlack)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Send")
                }
            }
        }
    }
}
