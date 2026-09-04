package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameState
import com.example.ui.theme.*

@Composable
fun TopStatusBar(
    gameState: GameState,
    onOpenSettings: () -> Unit,
    onNewGameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            // Row 1: Time, Difficulty, Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📅 H-${gameState.inGameDay} ",
                        color = ArcaneGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = String.format("%02d:%02d", gameState.inGameHour, gameState.inGameMinute),
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (gameState.difficulty) {
                                    "Abyssal" -> BloodCrimson.copy(alpha = 0.3f)
                                    "Hard" -> ArcaneGold.copy(alpha = 0.3f)
                                    else -> PortalCyan.copy(alpha = 0.3f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = gameState.difficulty.uppercase(),
                            color = when (gameState.difficulty) {
                                "Abyssal" -> BloodCrimson
                                "Hard" -> ArcaneGoldLight
                                else -> PortalCyan
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNewGameClick,
                        modifier = Modifier.size(36.dp).testTag("btn_new_game")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Game",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(36.dp).testTag("btn_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = ArcaneGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Row 2: Currencies
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CurrencyChip(symbol = "🪙", amount = gameState.gold.toString(), label = "Gold", color = ArcaneGold)
                    CurrencyChip(symbol = "💎", amount = gameState.diamond.toString(), label = "Diamond", color = PortalCyan)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CM:${gameState.materials.cm}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "UM:${gameState.materials.um}",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (gameState.materials.rm > 0) {
                        Text(
                            text = "RM:${gameState.materials.rm}",
                            color = PortalCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyChip(symbol: String, amount: String, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = symbol, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = amount, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
