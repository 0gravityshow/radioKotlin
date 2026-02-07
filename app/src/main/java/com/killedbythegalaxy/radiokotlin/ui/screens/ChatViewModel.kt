package com.killedbythegalaxy.radiokotlin.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killedbythegalaxy.radiokotlin.data.model.ChatMessage
import com.killedbythegalaxy.radiokotlin.data.model.PaywallState
import com.killedbythegalaxy.radiokotlin.data.model.PaywallStatus
import com.killedbythegalaxy.radiokotlin.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isPremium: Boolean = false,
    val currentUserId: String? = null,
    val isSending: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val _paywallState = MutableStateFlow(PaywallState())
    val paywallState: StateFlow<PaywallState> = _paywallState.asStateFlow()
    
    init {
        initializeChat()
    }
    
    private fun initializeChat() {
        viewModelScope.launch {
            // Sign in anonymously if not authenticated
            if (chatRepository.getCurrentUserId() == null) {
                chatRepository.signInAnonymously()
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                        return@launch
                    }
            }
            
            // Check premium status
            val isPremium = chatRepository.isPremiumUser()
            _uiState.update { 
                it.copy(
                    isPremium = isPremium,
                    currentUserId = chatRepository.getCurrentUserId()
                ) 
            }
            
            if (isPremium) {
                _paywallState.update { it.copy(status = PaywallStatus.PURCHASED) }
            }
            
            // Observe messages if premium
            if (isPremium) {
                observeMessages()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { messages ->
                    _uiState.update { 
                        it.copy(
                            messages = messages,
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
        }
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        if (_uiState.value.isSending) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            
            chatRepository.sendMessage(message.trim())
                .onSuccess {
                    _uiState.update { it.copy(isSending = false) }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isSending = false,
                            error = e.message
                        ) 
                    }
                }
        }
    }
    
    fun unlockPremium() {
        viewModelScope.launch {
            _paywallState.update { it.copy(status = PaywallStatus.PROCESSING) }
            
            chatRepository.unlockPremium()
                .onSuccess {
                    _paywallState.update { it.copy(status = PaywallStatus.PURCHASED) }
                    _uiState.update { it.copy(isPremium = true) }
                    observeMessages()
                }
                .onFailure { e ->
                    _paywallState.update { 
                        it.copy(
                            status = PaywallStatus.ERROR,
                            errorMessage = e.message
                        ) 
                    }
                }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
