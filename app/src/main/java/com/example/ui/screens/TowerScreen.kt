package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CombatPosition
import com.example.data.model.GameState
import com.example.data.model.Hero
import com.example.ui.theme.*

@Composable
fun TowerScreen(
    gameState: GameState,
    onLaunchExpedition: (Int) -> Unit,
    onNavigateToNarrative: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFloor by remember { mutableStateOf(gameState.towerFloorCurrent) }
    var showBlindWarningDialog by remember { mutableStateOf(false) }

    val party = remember(gameState.heroes, gameState.partyIds) {
        gameState.activePartyHeroes.ifEmpty {
            gameState.heroes.filter { it.isAlive }.take(5)
        }
    }

    val isBlindFloor = selectedFloor !in gameState.clearedFloors && selectedFloor > 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Floor Selector Header
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BloodCrimson.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🗼 MENARA MOBIUS",
                    color = BloodCrimson,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { if (selectedFloor > 1) selectedFloor-- },
                        enabled = selectedFloor > 1
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Floor", tint = ArcaneGold)
                    }

                    Text(
                        text = "LANTAI $selectedFloor",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    IconButton(
                        onClick = { if (selectedFloor < gameState.towerFloorHighest + 1) selectedFloor++ },
                        enabled = selectedFloor < gameState.towerFloorHighest + 1
                    ) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Floor", tint = ArcaneGold)
                    }
                }

                Text(
                    text = if (selectedFloor in gameState.clearedFloors) "Status: Pernah Diclear (Penalti EXP Repeat 50%)" else "Status: LANTAI BARU (Lethal Blind Entry)",
                    color = if (selectedFloor in gameState.clearedFloors) PortalCyan else BloodCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Quest Sheet Intel
        Surface(
            color = Color(0xFF09070E),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArcaneGold.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "BERKAS QUEST SHEET (INTEL)",
                    color = ArcaneGoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Zona: Koridor Kehancuran Sektor $selectedFloor\n• Tipe Objektif: ${if (selectedFloor % 10 == 0) "BOSS ANNIHILATION (Double EXP + 10💎)" else "Annihilation / Survival"}\n• Kondisi Medan: Gelap pekat, lantai berlumur darah monster\n• Mode Tempur: Chronicler Mode (Pertarungan Otomatis Penuh)",
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Active Party Roster
        Text(
            text = "FORMASI PARTY TEMPUR (${party.size}/5 HERO):",
            color = ArcaneGold,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        if (party.isEmpty()) {
            Text(
                text = "Tidak ada hero yang hidup untuk dikirim! Silakan summon pahlawan baru di Altar.",
                color = BloodCrimson,
                fontSize = 12.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                party.forEach { hero ->
                    TowerPartyHeroItem(hero = hero)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Launch Button
        Button(
            onClick = {
                if (isBlindFloor) {
                    showBlindWarningDialog = true
                } else {
                    onLaunchExpedition(selectedFloor)
                    onNavigateToNarrative()
                }
            },
            enabled = party.isNotEmpty() && !gameState.isGameOver,
            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_launch_expedition")
        ) {
            Text(
                text = "⚔️ TEROBOS GERBANG LANTAI $selectedFloor",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Blind Entry Warning Dialog
    if (showBlindWarningDialog) {
        AlertDialog(
            onDismissRequest = { showBlindWarningDialog = false },
            icon = {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = BloodCrimson)
            },
            title = {
                Text(
                    text = "⚠️ PERINGATAN SISTEM: BLIND ENTRY",
                    color = BloodCrimson,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    text = "Lantai $selectedFloor belum pernah dimasuki sama sekali dan detail monster masih buta.\n\nApakah Master yakin ingin mengirim party saat ini, atau ingin mengganti formasi (misal: mengirim unit Scout/Tumbal [SCRAP] terlebih dahulu)?",
                    color = TextPrimary,
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlindWarningDialog = false
                        onLaunchExpedition(selectedFloor)
                        onNavigateToNarrative()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson)
                ) {
                    Text("Lanjutkan Ekspedisi", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlindWarningDialog = false }) {
                    Text("Batal & Ganti Formasi", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }
}

@Composable
fun TowerPartyHeroItem(hero: Hero) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hero.willRefuseOrder) BloodCrimson else DarkSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★${hero.starGrade} ${hero.name}",
                        color = getStarColor(hero.starGrade),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lv.${hero.level} ${hero.jobClass}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "Posisi: ${hero.position.name} • [${hero.tag.name}]",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Fatigue: ${hero.fatigue}/100",
                        color = if (hero.fatigue >= 60) BloodCrimson else ArcaneGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stress: ${hero.stress}/100",
                        color = if (hero.stress >= 60) BloodCrimson else RunicPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
