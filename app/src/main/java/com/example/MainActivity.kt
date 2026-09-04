package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF090A0F),
                    surface = Color(0xFF12141C),
                    primary = Color(0xFFE11D48),
                    secondary = Color(0xFF06B6D4)
                )
            ) {
                GameScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(vm: GameViewModel = viewModel()) {
    val wallet by vm.wallet.collectAsState()
    val day by vm.inGameDay.collectAsState()
    val hour by vm.inGameHour.collectAsState()
    val minute by vm.inGameMinute.collectAsState()
    val inventory by vm.inventory.collectAsState()
    val heroRoster by vm.heroRoster.collectAsState()
    val messages by vm.messages.collectAsState()
    val isGenerating by vm.isGenerating.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var showHeroDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ItemData?>(null) }
    var selectedHero by remember { mutableStateOf<HeroData?>(null) }
    var apiKeyInput by remember { mutableStateOf("") }
    var isModelReady by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 H-$day | %02d:%02d".format(hour, minute),
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💰 ${wallet.gold}", color = Color(0xFFFACC15), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("💎 ${wallet.diamond}", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showHeroDialog = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Hero Roster", tint = Color(0xFF06B6D4))
                    }
                },
                actions = {
                    IconButton(onClick = { showInventoryDialog = true }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Inventory", tint = Color(0xFFFACC15))
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF94A3B8))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF12141C))
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF12141C))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ketik perintah Master...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFE11D48),
                        unfocusedBorderColor = Color(0xFF232738)
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isGenerating) {
                            val textToSend = inputText
                            inputText = ""
                            vm.sendMessage(textToSend)
                        }
                    },
                    modifier = Modifier.background(Color(0xFFE11D48), RoundedCornerShape(8.dp))
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        // CHAT CONSOLE
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090A0F))
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                val bgColor = if (isUser) Color(0xFF1E293B) else Color(0xFF12141C)
                val borderColor = if (isUser) Color(0xFF38BDF8) else Color(0xFFE11D48)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = if (isUser) "👑 MASTER" else "🧚 YSEL / SISTEM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = borderColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.text,
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // 🎒 MODAL INVENTORY
    if (showInventoryDialog) {
        AlertDialog(
            onDismissRequest = { showInventoryDialog = false },
            confirmButton = {
                TextButton(onClick = { showInventoryDialog = false }) {
                    Text("Tutup", color = Color(0xFF38BDF8))
                }
            },
            title = { Text("🎒 TAS & INVENTORY", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(inventory) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItem = item },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = item.name,
                                    color = Color(item.rarity.hexColor),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(text = "${item.slotType} | ${item.effectsText}", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF12141C)
        )
    }

    // 👥 MODAL HERO ROSTER
    if (showHeroDialog) {
        AlertDialog(
            onDismissRequest = { showHeroDialog = false },
            confirmButton = {
                TextButton(onClick = { showHeroDialog = false }) {
                    Text("Tutup", color = Color(0xFF38BDF8))
                }
            },
            title = { Text("👥 ROSTER HERO", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(350.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(heroRoster) { hero ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedHero = hero },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${hero.name} ${hero.tag}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text("★".repeat(hero.stars) + " Lv.${hero.level}", color = Color(0xFFFACC15), fontSize = 12.sp)
                                }
                                Text("Class: ${hero.jobClass} | Ras: ${hero.race}", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("❤️ HP: ${hero.currentHp}/${hero.maxHp}", color = Color(0xFFF43F5E), fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("⚡ Fatigue: ${hero.fatigue}/100", color = if (hero.fatigue > 60) Color.Red else Color.Green, fontSize = 11.sp)
                                    Text("🧠 Stress: ${hero.stress}/100", color = if (hero.stress > 60) Color.Red else Color.Cyan, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF12141C)
        )
    }

    // 🔍 DETAIL INSPEKSI HERO
    selectedHero?.let { hero ->
        AlertDialog(
            onDismissRequest = { selectedHero = null },
            confirmButton = {
                TextButton(onClick = { selectedHero = null }) {
                    Text("Kembali", color = Color(0xFF38BDF8))
                }
            },
            title = { Text("🔍 STATUS LENGKAP: ${hero.name}", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("💪 Physical ATK : ${hero.physicalAtk}", color = Color.White, fontSize = 12.sp)
                    Text("🔮 Magic ATK    : ${hero.magicAtk}", color = Color.White, fontSize = 12.sp)
                    Text("🛡️ P. DEF       : ${hero.pDef}", color = Color.White, fontSize = 12.sp)
                    Text("✨ M. DEF (Res) : ${hero.mDef}", color = Color.White, fontSize = 12.sp)
                    Text("💥 Crit Rate    : %.1f%%".format(hero.critRate), color = Color.White, fontSize = 12.sp)
                    Text("🏆 Total Kill   : ${hero.totalKill} | Boss: ${hero.bossKill}", color = Color.Gray, fontSize = 11.sp)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // ⚙️ MODAL PENGATURAN & API KEY
    if (showSettingsDialog || !isModelReady) {
        AlertDialog(
            onDismissRequest = { if (isModelReady) showSettingsDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (apiKeyInput.isNotBlank()) {
                            vm.initModel(apiKeyInput.trim())
                            isModelReady = true
                            showSettingsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Simpan & Mulai", color = Color.White)
                }
            },
            title = { Text("🔑 SETUP GEMINI API KEY", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Masukkan API Key Gemini dari Google AI Studio untuk menghubungkan asisten Peri Ysel:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("Tempel API Key di sini...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color(0xFF12141C)
        )
    }
}
