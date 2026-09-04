package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.GameState
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NarrativeConsoleScreen(
    gameState: GameState,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onUseMiracle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll when new messages arrive
    LaunchedEffect(gameState.chatMessages.size, isLoading) {
        if (gameState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(gameState.chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Quick Miracle Bar (During combat/emergencies)
        MiracleBar(
            diamond = gameState.diamond,
            gold = gameState.gold,
            onUseMiracle = onUseMiracle
        )

        // Message Stream
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize().testTag("narrative_message_list")
            ) {
                items(gameState.chatMessages) { message ->
                    ChatMessageItem(
                        message = message,
                        onActionClick = { actionText ->
                            onSendMessage(actionText)
                        }
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = ArcaneGold,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${gameState.fairyName} sedang merangkai takdir narasi...",
                                color = ArcaneGoldLight,
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }

        // Action Suggestions if available on the last message
        val latestActions = gameState.chatMessages.lastOrNull()?.suggestedActions
        if (!latestActions.isNullOrEmpty() && !isLoading && !gameState.isGameOver) {
            ScrollableActionChips(
                actions = latestActions,
                onSelectAction = { action ->
                    onSendMessage(action)
                }
            )
        }

        // Text Input Bar
        if (!gameState.isGameOver) {
            Surface(
                color = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Beri titah kepada Peri (atau pilih aksi)...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = ArcaneGold,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("narrative_input_field"),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val text = inputText
                                inputText = ""
                                onSendMessage(text)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) ArcaneGold else DarkSurfaceVariant)
                            .testTag("btn_send_prompt")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            tint = if (inputText.isNotBlank()) DarkBackground else TextMuted
                        )
                    }
                }
            }
        } else {
            // Game Over Locked Bar
            Surface(
                color = BloodCrimson.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, BloodCrimson, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "🔴 SISTEM TERKUNCI [GAME OVER] - Hubungan dimensi Mobius telah putus total. Silakan mulai New Game.",
                    color = BloodCrimson,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun MiracleBar(
    diamond: Int,
    gold: Int,
    onUseMiracle: (String) -> Unit
) {
    Surface(
        color = DarkSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨ MIRACLE:",
                color = ArcaneGoldLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            MiracleButton(
                title = "Heal",
                cost = "10💎 / 1k🪙",
                icon = Icons.Default.Healing,
                onClick = { onUseMiracle("HEAL") }
            )

            MiracleButton(
                title = "Shield",
                cost = "15💎 / 1.5k🪙",
                icon = Icons.Default.Security,
                onClick = { onUseMiracle("SHIELD") }
            )

            MiracleButton(
                title = "Shift",
                cost = "5💎",
                icon = Icons.Default.SwapHoriz,
                onClick = { onUseMiracle("TACTICAL_SHIFT") }
            )
        }
    }
}

@Composable
fun MiracleButton(
    title: String,
    cost: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = DarkSurfaceHighlight
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcaneGold.copy(alpha = 0.5f)),
        modifier = Modifier.height(28.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = ArcaneGold, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$title ($cost)", fontSize = 10.sp, color = TextPrimary)
    }
}

@Composable
fun ScrollableActionChips(
    actions: List<String>,
    onSelectAction: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(DarkSurfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🧭 PILIHAN AKSI:",
            color = ArcaneGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        actions.forEach { action ->
            AssistChip(
                onClick = { onSelectAction(action) },
                label = {
                    Text(
                        text = action,
                        color = PortalCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = DarkSurfaceHighlight
                ),
                border = AssistChipDefaults.assistChipBorder(
                    borderColor = PortalCyan.copy(alpha = 0.4f),
                    enabled = true
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(30.dp)
            )
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onActionClick: (String) -> Unit
) {
    val isMaster = message.sender == "MASTER"
    val isSystem = message.sender == "SYSTEM"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMaster) Alignment.End else Alignment.Start
    ) {
        // Sender Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            val badgeColor = when {
                isMaster -> PortalCyan
                message.isSystemShutdown -> BloodCrimson
                message.isDiceRoll -> ArcaneGold
                else -> RunicPurple
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when {
                    isMaster -> "MASTER (Anda)"
                    isSystem -> "SISTEM MOBIUS"
                    message.isSystemShutdown -> "🔴 SYSTEM SHUTDOWN"
                    else -> "🧚 PERI ASISTEN"
                },
                color = badgeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            if (message.timestamp.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message.timestamp,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        // Bubble Box
        Surface(
            color = when {
                isMaster -> DarkSurfaceHighlight
                message.isSystemShutdown -> BloodCrimson.copy(alpha = 0.15f)
                else -> DarkSurface
            },
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isMaster) 12.dp else 2.dp,
                bottomEnd = if (isMaster) 2.dp else 12.dp
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = when {
                    isMaster -> PortalCyan.copy(alpha = 0.3f)
                    message.isSystemShutdown -> BloodCrimson
                    message.isDiceRoll -> ArcaneGold.copy(alpha = 0.3f)
                    else -> DarkSurfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth(if (isMaster) 0.85f else 1.0f)
        ) {
            SelectionContainer {
                FormattedNarrativeText(rawText = message.text)
            }
        }
    }
}

@Composable
fun FormattedNarrativeText(rawText: String) {
    // Check if contains Markdown code block
    val parts = remember(rawText) { splitCodeBlocks(rawText) }

    Column(modifier = Modifier.padding(12.dp)) {
        parts.forEach { part ->
            if (part.isCodeBlock) {
                // Rendered as dark terminal code block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF07050A))
                        .border(1.dp, ArcaneGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = part.content,
                        color = Color(0xFFE2E8F0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            } else {
                Text(
                    text = part.content,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

data class TextPart(val content: String, val isCodeBlock: Boolean)

fun splitCodeBlocks(text: String): List<TextPart> {
    val result = mutableListOf<TextPart>()
    val codeBlockRegex = Regex("```(?:text)?\\n?([\\s\\S]*?)```")
    var lastIndex = 0

    codeBlockRegex.findAll(text).forEach { matchResult ->
        val range = matchResult.range
        if (range.first > lastIndex) {
            val normal = text.substring(lastIndex, range.first)
            if (normal.isNotBlank()) {
                result.add(TextPart(normal.trim(), false))
            }
        }
        val codeContent = matchResult.groups[1]?.value?.trim() ?: ""
        result.add(TextPart(codeContent, true))
        lastIndex = range.last + 1
    }

    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex)
        if (remaining.isNotBlank()) {
            result.add(TextPart(remaining.trim(), false))
        }
    }

    return if (result.isEmpty()) listOf(TextPart(text, false)) else result
}
