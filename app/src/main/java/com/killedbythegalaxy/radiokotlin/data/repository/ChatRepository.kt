package com.killedbythegalaxy.radiokotlin.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.killedbythegalaxy.radiokotlin.data.model.ChatMessage
import com.killedbythegalaxy.radiokotlin.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val messagesCollection = firestore.collection("ego_chat_messages")
    private val usersCollection = firestore.collection("users")
    
    /**
     * Observe real-time chat messages
     */
    fun observeMessages(limit: Int = 50): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = messagesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)
                } ?: emptyList()
                
                trySend(messages.reversed())
            }
        
        awaitClose { subscription.remove() }
    }
    
    /**
     * Send a message to the chat
     */
    suspend fun sendMessage(message: String): Result<ChatMessage> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            // Check if user is blacklisted
            val userProfile = getUserProfile(currentUser.uid)
            if (userProfile?.isBlacklisted == true) {
                val blockedUntil = userProfile.blacklistedUntil?.toDate()
                if (blockedUntil != null && blockedUntil.after(Date())) {
                    return Result.failure(Exception("You are temporarily blocked. Reason: ${userProfile.blacklistReason}"))
                }
            }
            
            // Check rate limit (max 1 message per 3 seconds)
            val lastMessageTime = userProfile?.lastMessageTime?.toDate()?.time ?: 0
            if (System.currentTimeMillis() - lastMessageTime < 3000) {
                return Result.failure(Exception("Please wait before sending another message"))
            }
            
            val chatMessage = ChatMessage(
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Anonymous",
                userAvatar = currentUser.photoUrl?.toString(),
                message = message.take(500), // Limit to 500 characters
                timestamp = Timestamp.now()
            )
            
            val docRef = messagesCollection.add(chatMessage).await()
            
            // Update user's last message time
            usersCollection.document(currentUser.uid).update(
                mapOf(
                    "lastMessageTime" to Timestamp.now(),
                    "messageCount" to (userProfile?.messageCount ?: 0) + 1
                )
            ).await()
            
            Result.success(chatMessage.copy(id = docRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile
     */
    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            usersCollection.document(userId).get().await()
                .toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Create or update user profile
     */
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(profile.id).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if user has premium access
     */
    suspend fun isPremiumUser(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return getUserProfile(userId)?.isPremium ?: false
    }
    
    /**
     * Unlock premium (after Stripe payment)
     */
    suspend fun unlockPremium(): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            usersCollection.document(userId).update(
                mapOf(
                    "isPremium" to true,
                    "premiumPurchaseDate" to Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    /**
     * Sign in anonymously
     */
    suspend fun signInAnonymously(): Result<String> {
        return try {
            val result = auth.signInAnonymously().await()
            val userId = result.user?.uid ?: throw Exception("Failed to get user ID")
            
            // Create initial user profile
            val profile = UserProfile(
                id = userId,
                displayName = "Galaxy Traveler #${userId.takeLast(4)}",
                createdAt = Timestamp.now()
            )
            usersCollection.document(userId).set(profile).await()
            
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
