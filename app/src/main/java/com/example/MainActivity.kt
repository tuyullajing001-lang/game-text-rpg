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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
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
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator(vm: GameViewModel = viewModel()) {
    val stage by vm.currentStage.collectAsState()

    when (stage) {
        GameStage.API_KEY_SETUP -> ApiKeySetupScreen(vm)
        GameStage.PLAYER_FORM -> PlayerFormScreen(vm)
        GameStage.IN_GAME -> GamePlayScreen(vm)
    }
}

// ==========================================
// LAYAR 1: KONEKSI API KEY GOOGLE STUDIO
// ==========================================
@Composable
fun ApiKeySetupScreen(vm: GameViewModel) {
    var apiKey by remember { mutableStateOf("") }
    val isConnecting by vm.isConnecting.collectAsState()
    val errorMsg by vm.connectionError.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF090A0F)).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12141C)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔️ INFINITE GACHA", color = Color(0xFFE11D48), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("MOBIUS DIMENSION ENGINE", color = Color(0xFF06B6D4), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text("Tempel API Key Gemini...") },
                    label = { Text("API Key Google Studio") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 2
                )

                errorMsg?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("❌ $it", color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { vm.testAndConnectApi(apiKey.trim()) },
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("HUBUNGKAN KE AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// LAYAR 2: FORMULIR DATA PLAYER & CUSTOM HERO
// ==========================================
@Composable
fun PlayerFormScreen(vm: GameViewModel) {
    var masterName by remember { mutableStateOf("Ammora") }
    var lobbyName by remember { mutableStateOf("Niflheim") }
    var periName by remember { mutableStateOf("Ysel") }
    var diff by remember { mutableStateOf("Abyssal") }

    var isCustomHero by remember { mutableStateOf(true) }
    var heroName by remember { mutableStateOf("Ammora") }
    var heroRace by remember { mutableStateOf("Manusia") }
    var heroGender by remember { mutableStateOf("Laki-laki") }
    var heroAge by remember { mutableStateOf("28") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF090A0F)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("📜 FORMULIR INISIASI MASTER", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = masterName, onValueChange = { masterName = it }, label = { Text("Nama Master") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lobbyName, onValueChange = { lobbyName = it }, label = { Text("Nama Lobby") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = periName, onValueChange = { periName = it }, label = { Text("Nama Peri Asisten") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = diff, onValueChange = { diff = it }, label = { Text("Tingkat Kesulitan (Normal/Hard/Abyssal)") }, modifier = Modifier.fillMaxWidth())
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isCustomHero, onCheckedChange = { isCustomHero = it })
                Text("Aktifkan Custom Hero Awal", color = Color.White)
            }
        }

        if (isCustomHero) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🧬 DATA CUSTOM HERO", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = heroName, onValueChange = { heroName = it }, label = { Text("Nama Hero") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = heroRace, onValueChange = { heroRace = it }, label = { Text("Ras") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = heroGender, onValueChange = { heroGender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = heroAge, onValueChange = { heroAge = it }, label = { Text("Usia") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    vm.masterName = masterName.trim()
                    vm.lobbyName = lobbyName.trim()
                    vm.assistantName = periName.trim()
                    vm.difficulty = diff.trim()
                    vm.isCustomHeroActive = isCustomHero
                    vm.customHeroName = heroName.trim()
                    vm.customHeroRace = heroRace.trim()
                    vm.customHeroGender = heroGender.trim()
                    vm.customHeroAge = heroAge.toIntOrNull() ?: 20
                    vm.submitPlayerForm()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("BUKA GERBANG & MULAI 5X GACHA", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// LAYAR 3: GAMEPLAY CLEAN CHAT & TOWER CODEX
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(vm: GameViewModel) {
    val wallet by vm.wallet.collectAsState()
    val day by vm.inGameDay.collectAsState()
    val hour by vm.inGameHour.collectAsState()
    val minute by vm.inGameMinute.collectAsState()
    val inventory by vm.inventory.collectAsState()
    val heroRoster by vm.heroRoster.collectAsState()
    val discoveredFloors by vm.discoveredFloors.collectAsState()
    val messages by vm.messages.collectAsState()
    val isGenerating by vm.isGenerating.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var showHeroManagementDialog by remember { mutableStateOf(false) }
    var showTowerIntelDialog by remember { mutableStateOf(false) }
    var selectedHero by remember { mutableStateOf<HeroData?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("📅 H-$day | %02d:%02d".format(hour, minute), fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💰 ${wallet.gold}", color = Color(0xFFFACC15), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("💎 ${wallet.diamond}", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showHeroManagementDialog = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Hero Management", tint = Color(0xFF06B6D4))
                    }
                },
                actions = {
                    IconButton(onClick = { showTowerIntelDialog = true }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Tower Intel", tint = Color(0xFFE11D48))
                    }
                    IconButton(onClick = { showInventoryDialog = true }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Inventory", tint = Color(0xFFFACC15))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF12141C))
            )
        },
        bottomBar = {
            // KOLOM CHAT BERSIH (FULL WIDTH)
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF12141C)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ketik perintah Master...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFF090A0F)).padding(padding).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                val bgColor = if (isUser) Color(0xFF1E293B) else Color(0xFF12141C)
                val borderColor = if (isUser) Color(0xFF38BDF8) else Color(0xFFE11D48)

                Box(
                    modifier = Modifier.fillMaxWidth().border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).background(bgColor, RoundedCornerShape(8.dp)).padding(10.dp)
                ) {
                    Column {
                        Text(if (isUser) "👑 MASTER" else "🧚 YSEL / SISTEM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = borderColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.text, fontSize = 13.sp, color = Color(0xFFE2E8F0), lineHeight = 18.sp)
                    }
                }
            }
        }
    }

    // 🏰 MODAL INTEL MENARA
    if (showTowerIntelDialog) {
        AlertDialog(
            onDismissRequest = { showTowerIntelDialog = false },
            confirmButton = { TextButton(onClick = { showTowerIntelDialog = false }) { Text("Tutup", color = Color(0xFF38BDF8)) } },
            title = { Text("🏰 INTEL LANTAI MENARA", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold) },
            text = {
                if (discoveredFloors.isEmpty()) {
                    Text("Belum ada lantai yang diintai. Masuk ke lantai baru untuk membuka dan mengunci intel lantai.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    LazyColumn(modifier = Modifier.height(350.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(discoveredFloors.values.toList().sortedBy { it.floorNumber }) { fl ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Lantai ${fl.floorNumber}: ${fl.title}", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("🎯 Tipe: ${fl.objectiveType} | ⏱️ ${fl.timeLimitText}", color = Color.White, fontSize = 11.sp)
                                    Text("⚠️ Bahaya: ${fl.terrainHazard}", color = Color(0xFFF43F5E), fontSize = 11.sp)
                                    Text("👾 Monster: ${fl.enemyComposition}", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF12141C)
        )
    }

    // 👥 MODAL HERO MANAGEMENT
    if (showHeroManagementDialog) {
        AlertDialog(
            onDismissRequest = { showHeroManagementDialog = false },
            confirmButton = { TextButton(onClick = { showHeroManagementDialog = false }) { Text("Tutup", color = Color(0xFF38BDF8)) } },
            title = { Text("👥 HERO MANAGEMENT", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(heroRoster) { hero ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedHero = hero },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${hero.name} ${hero.tag}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("★".repeat(hero.stars) + " Lv.${hero.level} (EXP:${hero.exp}/${hero.maxExpNeeded})", color = Color(0xFFFACC15), fontSize = 11.sp)
                                }
                                Text("Class: ${hero.jobClass} | Ras: ${hero.race} (${hero.gender}, ${hero.age}th)", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("❤️ HP: ${hero.currentHp}/${hero.maxHp}", color = Color(0xFFF43F5E), fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("⚡ Fat: ${hero.fatigue}/100", color = if (hero.fatigue > 60) Color.Red else Color.Green, fontSize = 11.sp)
                                    Text("🧠 Strs: ${hero.stress}/100", color = if (hero.stress > 60) Color.Red else Color.Cyan, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("🗡️ Equip: ${hero.weapon} | 🛡️ ${hero.armor}", color = Color.LightGray, fontSize = 10.sp)
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
            confirmButton = { TextButton(onClick = { selectedHero = null }) { Text("Kembali", color = Color(0xFF38BDF8)) } },
            title = { Text("🔍 DETAIL STAT: ${hero.name}", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("💪 Physical ATK : ${hero.physicalAtk}", color = Color.White, fontSize = 12.sp)
                    Text("🔮 Magic ATK    : ${hero.magicAtk}", color = Color.White, fontSize = 12.sp)
                    Text("🛡️ P. DEF       : ${hero.pDef}", color = Color.White, fontSize = 12.sp)
                    Text("✨ M. DEF (Res) : ${hero.mDef}", color = Color.White, fontSize = 12.sp)
                    Text("💥 Crit Rate    : %.1f%%".format(hero.critRate), color = Color.White, fontSize = 12.sp)
                    Text("📊 Stat Mentah  : STR:${hero.str} VIT:${hero.vit} AGI:${hero.agi} INT:${hero.intStat} DEX:${hero.dex} LUK:${hero.luck}", color = Color.LightGray, fontSize = 10.sp)
                    if (hero.specialTraits.isNotEmpty()) {
                        Text("🧬 Trait: ${hero.specialTraits.joinToString()}", color = Color(0xFFFACC15), fontSize = 10.sp)
                    }
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // 🎒 MODAL INVENTORY
    if (showInventoryDialog) {
        AlertDialog(
            onDismissRequest = { showInventoryDialog = false },
            confirmButton = { TextButton(onClick = { showInventoryDialog = false }) { Text("Tutup", color = Color(0xFF38BDF8)) } },
            title = { Text("🎒 TAS & INVENTORY", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold) },
            text = {
                if (inventory.isEmpty()) {
                    Text("Tas masih kosong.", color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(inventory) { item ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(item.name, color = Color(item.rarity.hexColor), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${item.slotType} | ${item.effectsText}", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF12141C)
        )
    }
}
