/**
 * Cloud Functions for radioKotlin
 * - EgoBot: AI chatbot with Hitchhiker's Guide personality
 * - Message Moderation: AI-powered content filtering
 */

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");
const { GoogleGenerativeAI } = require("@google/generative-ai");

admin.initializeApp();

// Set global options
setGlobalOptions({ region: "us-central1" });

const db = admin.firestore();

// Initialize Gemini AI (set GEMINI_API_KEY in Firebase config)
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

/**
 * EgoBot - Responds when users mention @egobot
 * Personality: Hitchhiker's Guide to the Galaxy style humor
 */
exports.egoBot = onDocumentCreated("ego_chat_messages/{messageId}", async (event) => {
  const message = event.data.data();
  const messageId = event.params.messageId;
  
  // Check if message mentions @egobot
  if (!message.message.toLowerCase().includes("@egobot")) {
    return null;
  }
  
  // Don't respond to own messages
  if (message.isEgoBot) {
    return null;
  }
  
  try {
    const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
    
    const prompt = `You are EgoBot, a witty AI assistant inspired by the Hitchhiker's Guide to the Galaxy. 
Your personality traits:
- Dry British humor
- References to "42", "Don't Panic", "Infinite Improbability Drive", "Vogon poetry"
- Slightly sarcastic but helpful
- Cosmic perspective on everything
- Occasionally mentions towels

User message: "${message.message}"

Respond in 1-2 sentences with your characteristic wit. Keep it brief and entertaining.`;

    const result = await model.generateContent(prompt);
    const response = result.response.text();
    
    // Add EgoBot response to chat
    await db.collection("ego_chat_messages").add({
      userId: "egobot",
      userName: "EgoBot",
      message: response,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      isEgoBot: true,
      replyTo: messageId
    });
    
    console.log(`EgoBot responded to message ${messageId}`);
    return null;
    
  } catch (error) {
    console.error("EgoBot error:", error);
    return null;
  }
});

/**
 * Message Moderation - AI-powered content filtering
 * Actions:
 * - Mild violation: 24-hour ban
 * - Severe violation: Permanent blacklist (Black Hole)
 */
exports.moderateMessage = onDocumentCreated("ego_chat_messages/{messageId}", async (event) => {
  const message = event.data.data();
  const messageId = event.params.messageId;
  
  // Skip EgoBot messages
  if (message.isEgoBot) {
    return null;
  }
  
  try {
    const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
    
    const prompt = `You are a content moderator. Analyze this message for harmful content.
Categories to check:
- Hate speech or discrimination
- Explicit violence or threats
- Sexual content
- Harassment or bullying
- Spam or excessive profanity

Message: "${message.message}"

Respond ONLY with one of these exact words:
- CLEAN (no issues)
- MILD (minor profanity or rudeness - 24h ban)
- SEVERE (hate speech, threats, explicit content - permanent ban)`;

    const result = await model.generateContent(prompt);
    const verdict = result.response.text().trim().toUpperCase();
    
    if (verdict === "CLEAN") {
      return null;
    }
    
    const userId = message.userId;
    const userRef = db.collection("users").doc(userId);
    
    if (verdict === "MILD") {
      // 24-hour temporary ban
      const blockedUntil = new Date();
      blockedUntil.setHours(blockedUntil.getHours() + 24);
      
      await userRef.update({
        blacklistedUntil: admin.firestore.Timestamp.fromDate(blockedUntil),
        blacklistReason: "Temporary ban for minor violation"
      });
      
      // Mark message as moderated
      await event.data.ref.update({
        isModerated: true,
        moderationReason: "MILD_VIOLATION"
      });
      
      console.log(`User ${userId} temporarily banned for 24 hours`);
      
    } else if (verdict === "SEVERE") {
      // Permanent blacklist (Black Hole)
      await userRef.update({
        isBlacklisted: true,
        blacklistReason: "Permanent ban for severe violation - Event Horizon"
      });
      
      // Mark message as moderated
      await event.data.ref.update({
        isModerated: true,
        moderationReason: "SEVERE_VIOLATION"
      });
      
      // Delete the offensive message
      await event.data.ref.delete();
      
      console.log(`User ${userId} permanently blacklisted (Black Hole)`);
    }
    
    return null;
    
  } catch (error) {
    console.error("Moderation error:", error);
    return null;
  }
});

/**
 * Cleanup old messages (optional - run periodically)
 * Keeps only last 1000 messages to control Firestore costs
 */
exports.cleanupOldMessages = require("firebase-functions/v2/scheduler")
  .onSchedule("every 24 hours", async (event) => {
    const messagesRef = db.collection("ego_chat_messages");
    const snapshot = await messagesRef
      .orderBy("timestamp", "desc")
      .offset(1000)
      .get();
    
    if (snapshot.empty) {
      console.log("No old messages to delete");
      return null;
    }
    
    const batch = db.batch();
    snapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
    });
    
    await batch.commit();
    console.log(`Deleted ${snapshot.docs.length} old messages`);
    return null;
  });
