package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class GameViewModel : ViewModel() {

    // 1. STATE GAME
    val wallet = MutableStateFlow(WalletData())
    val inGameDay = MutableStateFlow(1)
    val inGameHour = MutableStateFlow(8)
    val inGameMinute = MutableStateFlow(0)
    
    val inventory = MutableStateFlow<List<ItemData>>(
        listOf(
            ItemData(
                id = UUID.randomUUID().toString(),
                name = "Soul Stabilizer",
                rarity = Rarity.COMMON,
                slotType = "Consumable",
                effectsText = "-25 Stress Instan",
                description = "Menstabilkan fluktuasi jiwa hero."
            )
        )
    )
    
    val heroRoster = MutableStateFlow<List<HeroData>>(emptyList())
    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val isGenerating = MutableStateFlow(false)

    // Master Info
    var masterName: String = "Ammora"
    var lobbyName: String = "Niflheim"
    var assistantName: String = "Ysel"
    var difficulty: String = "Abyssal"

    // 2. SISTEM PROMPT LENGKAP
    private val systemInstructions = """
        MASTER PROMPT - INFINITE GACHA (v3.2) & GODOT 4 COMBAT ENGINE
        1. Bahasa Indonesia natural, gaya cerita gelap (visceral/anatomi brutal), tanpa plot armor.
        2. Pemain berperan sebagai Master di ruang komando, Peri ($assistantName) menyampaikan komando fisik.
        3. Anti-Halu State Parser: Jika ada perubahan loot/uang/hero, WAJIB tuliskan tag di akhir pesan:
           [ADD_ITEM: {"name":"Nama Item","rarity":"Rare","slot":"Weapon","stats":"+12 P.ATK","effects":"20% Bleed","description":"..."}]
           [UPDATE_WALLET: {"gold": 4500, "diamond": 50}]
           [UPDATE_HERO: {"name": "Ammora", "hp": 1200, "fatigue": 25, "stress": 10}]
        4. Kesulitan: $difficulty Mode aktif.
    """.trimIndent()

    private var generativeModel: GenerativeModel? = null

    fun initModel(apiKey: String, modelName: String = "gemini-2.5-flash") {
        generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            systemInstruction = content { text(systemInstructions) }
        )
        // Inisiasi Game Awal
        startNewGame()
    }

    private fun startNewGame() {
        val initialHeroes = mutableListOf<HeroData>()
        
        // Stealth Ammora Trigger
        if (masterName.equals("Ammora", ignoreCase = true)) {
            initialHeroes.add(
                HeroData(
                    id = UUID.randomUUID().toString(),
                    name = "Ammora",
                    race = "Manusia",
                    gender = "Laki-laki",
                    age = 28,
                    stars = 2,
                    tag = "[CORE]",
                    maxHp = 1500,
                    currentHp = 1500,
                    str = 8, vit = 8, intStat = 6, agi = 7, dex = 6, luck = 6
                )
            )
        }
        
        // Hero tutorial lainnya
        initialHeroes.add(HeroData(UUID.randomUUID().toString(), "Bron", "Dwarf", "Laki-laki", 34, 2, 1, 0, "Fighter", "[NONE]", 1200, 1200, 0, 0, 12, 11, 3, 4, 5, 5))
        initialHeroes.add(HeroData(UUID.randomUUID().toString(), "Lyra", "Elf", "Perempuan", 22, 3, 1, 0, "Archer", "[NONE]", 800, 800, 0, 0, 6, 5, 8, 14, 11, 7))
        initialHeroes.add(HeroData(UUID.randomUUID().toString(), "Selen", "Manusia", "Perempuan", 20, 2, 1, 0, "Cleric", "[NONE]", 900, 900, 0, 0, 4, 6, 11, 6, 8, 8))
        initialHeroes.add(HeroData(UUID.randomUUID().toString(), "Vane", "Beastkin", "Laki-laki", 24, 2, 1, 0, "Rogue", "[NONE]", 850, 850, 0, 0, 7, 5, 4, 13, 10, 6))

        heroRoster.value = initialHeroes

        // Pesan Pembuka
        val welcomeText = "📅 Hari ke-1 | Jam 08:00\n\nSelamat datang di Lobby $lobbyName, Master $masterName. Protokol $difficulty telah aktif. Lima hero awal telah ditarik dari Altar Gacha dan menunggu komando pertama Anda."
        messages.value = listOf(ChatMessage(sender = "AI", text = welcomeText))
    }

    // 3. KIRIM PESAN CHAT DENGAN INJEKSI STATE (ANTI-HALU)
    fun sendMessage(userText: String) {
        if (userText.isBlank() || generativeModel == null) return

        val updatedList = messages.value.toMutableList()
        updatedList.add(ChatMessage(sender = "USER", text = userText))
        messages.value = updatedList
        isGenerating.value = true

        viewModelScope.launch {
            try {
                // Injeksi Catatan State Resmi di Belakang Layar
                val stateContext = """
                    [CURRENT_STATE_INJECTION]
                    Gold: ${wallet.value.gold}, Diamond: ${wallet.value.diamond}
                    Party: ${heroRoster.value.joinToString { "${it.name} (HP:${it.currentHp}/${it.maxHp}, Fat:${it.fatigue}, Str:${it.stress})" }}
                    Inventory Items: ${inventory.value.joinToString { it.name }}
                    [USER_COMMAND]: $userText
                """.trimIndent()

                val response = generativeModel!!.generateContent(stateContext)
                val rawResponseText = response.text ?: "..."

                // Tangkap Tag dan Bersihkan Teks Chat
                val cleanedText = parseAndApplyTags(rawResponseText)

                val finalMessages = messages.value.toMutableList()
                finalMessages.add(ChatMessage(sender = "AI", text = cleanedText))
                messages.value = finalMessages

            } catch (e: Exception) {
                val finalMessages = messages.value.toMutableList()
                finalMessages.add(ChatMessage(sender = "SYSTEM", text = "❌ Error: ${e.localizedMessage}"))
                messages.value = finalMessages
            } finally {
                isGenerating.value = false
            }
        }
    }

    // 4. PARSER TAG UNTUK MEMPERBARUI TAS & STATUS SECARA OTOMATIS
    private fun parseAndApplyTags(text: String): String {
        var result = text

        // Tangkap [ADD_ITEM: {...}]
        val itemRegex = Regex("\\[ADD_ITEM:\\s*(\\{.*?\\})\\]")
        itemRegex.findAll(text).forEach { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                val newItem = ItemData(
                    id = UUID.randomUUID().toString(),
                    name = json.optString("name", "Unknown Item"),
                    rarity = Rarity.entries.find { it.name.equals(json.optString("rarity"), true) } ?: Rarity.RARE,
                    slotType = json.optString("slot", "Weapon"),
                    statsText = json.optString("stats", ""),
                    effectsText = json.optString("effects", ""),
                    description = json.optString("description", "")
                )
                inventory.value = inventory.value + newItem
            } catch (_: Exception) {}
        }
        result = itemRegex.replace(result, "")

        // Tangkap [UPDATE_WALLET: {...}]
        val walletRegex = Regex("\\[UPDATE_WALLET:\\s*(\\{.*?\\})\\]")
        walletRegex.findAll(text).forEach { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                val currentW = wallet.value
                wallet.value = currentW.copy(
                    gold = json.optInt("gold", currentW.gold),
                    diamond = json.optInt("diamond", currentW.diamond)
                )
            } catch (_: Exception) {}
        }
        result = walletRegex.replace(result, "")

        return result.trim()
    }
}
