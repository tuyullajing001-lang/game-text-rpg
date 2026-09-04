package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun NewGameDialog(
    currentMaster: String,
    currentLobby: String,
    currentFairy: String,
    currentDiff: String,
    onConfirm: (master: String, lobby: String, fairy: String, difficulty: String, customHero: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var masterName by remember { mutableStateOf(currentMaster) }
    var lobbyName by remember { mutableStateOf(currentLobby) }
    var fairyName by remember { mutableStateOf(currentFairy) }
    var selectedDifficulty by remember { mutableStateOf(currentDiff) }
    var customHeroName by remember { mutableStateOf("") }

    val difficulties = listOf("Normal", "Hard", "Abyssal")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⚔️ INISIASI NEW GAME (DIMENSI MOBIUS)",
                color = ArcaneGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Sesuai Master Prompt File 09, masukkan identitas awal:",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                OutlinedTextField(
                    value = masterName,
                    onValueChange = { masterName = it },
                    label = { Text("1. Nama Master") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcaneGold,
                        unfocusedBorderColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_master_name")
                )

                OutlinedTextField(
                    value = lobbyName,
                    onValueChange = { lobbyName = it },
                    label = { Text("2. Nama Lobby") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcaneGold,
                        unfocusedBorderColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_lobby_name")
                )

                OutlinedTextField(
                    value = fairyName,
                    onValueChange = { fairyName = it },
                    label = { Text("3. Nama Peri Asisten") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcaneGold,
                        unfocusedBorderColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_fairy_name")
                )

                Text(
                    text = "4. Tingkat Kesulitan:",
                    color = ArcaneGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    difficulties.forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (diff == "Abyssal") BloodCrimson else ArcaneGold,
                                selectedLabelColor = DarkBackground
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = customHeroName,
                    onValueChange = { customHeroName = it },
                    label = { Text("Hero Kustom (Opsional)") },
                    placeholder = { Text("Contoh: Ryosuke") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PortalCyan,
                        unfocusedBorderColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_custom_hero")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        masterName.ifBlank { "Master" },
                        lobbyName.ifBlank { "Valhalla Citadel" },
                        fairyName.ifBlank { "Peri Navi" },
                        selectedDifficulty,
                        customHeroName.takeIf { it.isNotBlank() }
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArcaneGold)
            ) {
                Text("Mulai Game Baru", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
        containerColor = DarkSurfaceVariant
    )
}
