package com.killedbythegalaxy.radiokotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.killedbythegalaxy.radiokotlin.data.model.ChatMessage
import com.killedbythegalaxy.radiokotlin.data.model.PaywallStatus
import com.killedbythegalaxy.radiokotlin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val paywallState by viewModel.paywallState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "EGO CHAT",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        if (uiState.isPremium) {
                            Surface(
                                shape = CircleShape,
                                color = PaywallGold.copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Premium",
                                    tint = PaywallGold,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VoidBlack,
                    titleContentColor = CyberCyan
                )
            )
        },
        containerColor = VoidBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(VoidBlack, DeepSpace, VoidBlack)
                        )
                    )
            )
            
            when {
                uiState.isLoading -> LoadingState()
                !uiState.isPremium -> PaywallScreen(
                    status = paywallState.status,
                    error = paywallState.errorMessage,
                    onUnlock = { viewModel.unlockPremium() }
                )
                else -> ChatContent(
                    messages = uiState.messages,
                    currentUserId = uiState.currentUserId,
                    isSending = uiState.isSending,
                    error = uiState.error,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onClearError = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connecting to the galaxy...",
                color = CyberCyanDark,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PaywallScreen(
    status: PaywallStatus,
    error: String?,
    onUnlock: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "paywall")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lock Icon with glow
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PaywallGold.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = CosmicGray,
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = PaywallGold,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "UNLOCK EGO CHAT",
            style = MaterialTheme.typography.headlineMedium,
            color = PaywallGold,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Join the galactic conversation!\nChat with fellow travelers across the cosmos.",
            style = MaterialTheme.typography.bodyLarge,
            color = StarWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Features list
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureItem("Real-time chat with listeners")
            FeatureItem("Talk to @egobot (Hitchhiker's Guide style)")
            FeatureItem("Lifetime access - pay once")
            FeatureItem("Don't Panic!")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Error message
        AnimatedVisibility(visible = error != null) {
            Text(
                text = error ?: "",
                color = StatusOffline,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Unlock button
        Button(
            onClick = onUnlock,
            enabled = status != PaywallStatus.PROCESSING,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PaywallGold,
                contentColor = VoidBlack
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (status == PaywallStatus.PROCESSING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = VoidBlack,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UNLOCK FOR \$1.99",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Secure payment via Stripe",
            style = MaterialTheme.typography.bodySmall,
            color = StarWhite.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = StatusLive,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = StarWhite.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun ChatContent(
    messages: List<ChatMessage>,
    currentUserId: String?,
    isSending: Boolean,
    error: String?,
    onSendMessage: (String) -> Unit,
    onClearError: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Error snackbar
        AnimatedVisibility(visible = error != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = StatusOffline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error ?: "",
                        color = StatusOffline,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClearError) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = StatusOffline
                        )
                    }
                }
            }
        }
        
        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatBubble(
                    message = message,
                    isCurrentUser = message.userId == currentUserId
                )
            }
        }
        
        // Input field
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CosmicGray,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { if (it.length <= 500) messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Type a message... (or @egobot)",
                            color = StarWhite.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBlue,
                        cursorColor = CyberCyan,
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank() && !isSending) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        }
                    )
                )
                
                FilledIconButton(
                    onClick = {
                        if (messageText.isNotBlank() && !isSending) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank() && !isSending,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CyberCyan,
                        contentColor = VoidBlack,
                        disabledContainerColor = CyberBlue,
                        disabledContentColor = StarWhite.copy(alpha = 0.5f)
                    )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = VoidBlack,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    val bubbleColor = when {
        message.isEgoBot -> EgoBotColor.copy(alpha = 0.2f)
        isCurrentUser -> ChatBubbleUser
        else -> ChatBubbleOther
    }
    
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Username (not for current user)
        if (!isCurrentUser) {
            Text(
                text = if (message.isEgoBot) "@egobot" else message.userName,
                style = MaterialTheme.typography.labelSmall,
                color = if (message.isEgoBot) EgoBotColor else CyberCyanDark,
                fontWeight = if (message.isEgoBot) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }
        
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            color = bubbleColor,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarWhite
                )
                
                // Timestamp
                Text(
                    text = formatTimestamp(message.timestamp?.toDate()?.time ?: 0),
                    style = MaterialTheme.typography.labelSmall,
                    color = StarWhite.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - millis
    return when {
        diff < 60_000 -> "now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}
