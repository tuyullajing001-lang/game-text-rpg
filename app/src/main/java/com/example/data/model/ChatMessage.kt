package com.example.data.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "PERI", "MASTER", "SYSTEM", "DICE"
    val text: String,
    val timestamp: String = "",
    val suggestedActions: List<String> = emptyList(),
    val isDiceRoll: Boolean = false,
    val isSystemShutdown: Boolean = false
)
