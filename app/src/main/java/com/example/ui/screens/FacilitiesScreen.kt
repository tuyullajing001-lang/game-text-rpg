package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameState
import com.example.ui.theme.*

@Composable
fun FacilitiesScreen(
    gameState: GameState,
    onRestInKitchen: () -> Unit,
    onScanPvP: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "🏰 FASILITAS LOBBY (${gameState.lobbyName})",
            color = ArcaneGold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = "Fasilitas berjalan 24/7. Pemulihan aktif di Kitchen memajukan waktu +8 jam dan mereduksi kelelahan hero.",
            color = TextSecondary,
            fontSize = 11.5.sp
        )

        // 1. Iron Bar & Kitchen
        FacilityCard(
            title = "Iron Bar & Kitchen (Lv.1)",
            description = "Fasilitas utama pemulihan stamina & mental. Menyajikan hidangan bergizi dan arak Mobius hangat.",
            icon = Icons.Default.Restaurant,
            accentColor = ArcaneGold,
            statusText = "AKTIF • Kapasitas: 3 Staff",
            actionLabel = "Makan & Istirahat (+8 Jam)",
            actionDetail = "Fatigue -40 • Stress -30",
            onAction = onRestInKitchen,
            testTag = "btn_kitchen_rest"
        )

        // 2. Blacksmith
        FacilityCard(
            title = "Blacksmith (Bengkel Tempa Lv.1)",
            description = "Tempa senjata dan zirah tempur dari pecahan material logam monster Mobius.",
            icon = Icons.Default.Build,
            accentColor = PortalCyan,
            statusText = "AKTIF • 1 Craft = 1 Item Gear",
            actionLabel = "Tempa Perlengkapan (4 CM + 500🪙)",
            actionDetail = "Menghasilkan Gear Senjata/Zirah",
            onAction = {
                onSendMessage("Peri, tempa perlengkapan tempur di Blacksmith menggunakan 4 Common Material dan 500 Gold.")
            },
            testTag = "btn_blacksmith_craft"
        )

        // 3. Alchemist Lab
        FacilityCard(
            title = "Alchemist Lab (Laboratorium Transmutasi)",
            description = "Rasio 5:1 untuk mengubah material tingkat rendah menjadi material tingkat atas.",
            icon = Icons.Default.Science,
            accentColor = Color(0xFF4ADE80),
            statusText = "AKTIF • Formula Rasio 5:1",
            actionLabel = "Transmutasi Material (5 CM → 1 UM)",
            actionDetail = "Biaya 500 Gold",
            onAction = {
                onSendMessage("Peri, lakukan transmutasi 5 Common Material menjadi 1 Uncommon Material di Alchemist Lab.")
            },
            testTag = "btn_alchemist_transmute"
        )

        // 4. Central Research Lab
        FacilityCard(
            title = "Central Research Lab (Evolusi & Skill)",
            description = "Pusat mutasi skill maksimal [MAX] dan evolusi promosi Tier Job Class (F → E → D).",
            icon = Icons.Default.AutoStories,
            accentColor = RunicPurple,
            statusText = "AKTIF • Butuh Skill [MAX]",
            actionLabel = "Inspeksi Riset Skill & Class",
            actionDetail = "Kombinasi & Mutasi Skill",
            onAction = {
                onSendMessage("Peri, jelaskan opsi riset mutasi skill dan evolusi class yang tersedia saat ini.")
            },
            testTag = "btn_research_lab"
        )

        // 5. Hangar & Dock (Blood Arena PvP Radar)
        FacilityCard(
            title = "Hangar & Dock (Radar Blood Arena)",
            description = "Radar dimensi melacak sinyal Master lain. Sistem menyembunyikan tingkat kesulitan (Normal/Hard/Abyssal). Waspadai jebakan!",
            icon = Icons.Default.Radar,
            accentColor = BloodCrimson,
            statusText = "Radar Siaga • 1x Challenge per Hari",
            actionLabel = "Scan Target PvP Arena",
            actionDetail = "Lacak 3 Master Musuh di Radar",
            onAction = onScanPvP,
            testTag = "btn_radar_scan"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FacilityCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    statusText: String,
    actionLabel: String,
    actionDetail: String,
    onAction: () -> Unit,
    testTag: String
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text(text = statusText, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, color = TextSecondary, fontSize = 11.5.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag(testTag)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = actionLabel, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    Text(text = actionDetail, color = accentColor, fontSize = 10.sp)
                }
            }
        }
    }
}
