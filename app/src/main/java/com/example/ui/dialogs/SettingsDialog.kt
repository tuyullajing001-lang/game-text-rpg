package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.api.SystemPrompt
import com.example.ui.theme.*

@Composable
fun SettingsDialog(
    currentApiKey: String,
    currentModel: String,
    onSaveSettings: (apiKey: String, model: String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyText by remember { mutableStateOf(currentApiKey) }
    var selectedModel by remember { mutableStateOf(currentModel) }
    var showPromptText by remember { mutableStateOf(false) }

    val models = listOf("gemini-3.5-flash", "gemini-3.8-flash", "gemini-3.1-pro-preview")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⚙️ PENGATURAN GEMINI API & SISTEM",
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
                    text = "Konfigurasi Gemini API untuk respon naratif interaktif:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    label = { Text("Gemini API Key (Opsional jika via BuildConfig)") },
                    placeholder = { Text("Masukkan AI Studio API Key...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcaneGold,
                        unfocusedBorderColor = DarkSurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_api_key")
                )

                Text(
                    text = "Catatan: Jika dikosongkan, aplikasi otomatis menggunakan kunci dari BuildConfig Secrets atau fallback ke local dynamic engine.",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Pilih Model Gemini:", color = ArcaneGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    models.forEach { model ->
                        FilterChip(
                            selected = selectedModel == model,
                            onClick = { selectedModel = model },
                            label = { Text(model.replace("gemini-", ""), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArcaneGold,
                                selectedLabelColor = DarkBackground
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = { showPromptText = !showPromptText }
                ) {
                    Text(
                        text = if (showPromptText) "Sembunyikan Master Prompt v3.2" else "Lihat Master Prompt v3.2 (System Instruction)",
                        color = PortalCyan,
                        fontSize = 11.sp
                    )
                }

                if (showPromptText) {
                    Surface(
                        color = DarkBackground,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = SystemPrompt.FULL_PROMPT,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveSettings(apiKeyText.trim(), selectedModel)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArcaneGold)
            ) {
                Text("Simpan", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = TextSecondary)
            }
        },
        containerColor = DarkSurfaceVariant
    )
}
