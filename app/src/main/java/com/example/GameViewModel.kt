package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

enum class GameStage {
    API_KEY_SETUP,
    PLAYER_FORM,
    IN_GAME
}

class GameViewModel : ViewModel() {

    // 1. STAGE FLOW GAME
    val currentStage = MutableStateFlow(GameStage.API_KEY_SETUP)
    val connectionError = MutableStateFlow<String?>(null)
    val isConnecting = MutableStateFlow(false)

    // 2. STATE GAME & HERO MANAGEMENT
    val wallet = MutableStateFlow(WalletData())
    val inGameDay = MutableStateFlow(1)
    val inGameHour = MutableStateFlow(8)
    val inGameMinute = MutableStateFlow(0)
    val inventory = MutableStateFlow<List<ItemData>>(emptyList())
    val heroRoster = MutableStateFlow<List<HeroData>>(emptyList())
    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val isGenerating = MutableStateFlow(false)

    // Form Player
    var masterName: String = "Ammora"
    var lobbyName: String = "Niflheim"
    var assistantName: String = "Ysel"
    var difficulty: String = "Abyssal"

    // Form Custom Hero
    var isCustomHeroActive: Boolean = false
    var customHeroName: String = "Ammora"
    var customHeroRace: String = "Manusia"
    var customHeroGender: String = "Laki-laki"
    var customHeroAge: Int = 28

    private var generativeModel: GenerativeModel? = null

    // 3. TES & HUBUNGKAN KE GEMINI API (WAJIB BERHASIL SEBELUM BISA MULAI)
    fun testAndConnectApi(apiKey: String, modelName: String = "gemini-2.5-flash") {
        if (apiKey.isBlank()) {
            connectionError.value = "API Key tidak boleh kosong!"
            return
        }

        isConnecting.value = true
        connectionError.value = null

        viewModelScope.launch {
            try {
                val tempModel = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey
                )
                // Ping tes koneksi
                val testPing = tempModel.generateContent("Ketik 'OK' jika terhubung.")
                if (testPing.text != null) {
                    generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = apiKey,
                        systemInstruction = content { text(buildSystemInstructions()) }
                    )
                    // Pindah ke formulir pengisian data player
                    currentStage.value = GameStage.PLAYER_FORM
                } else {
                    connectionError.value = "Gagal mendapatkan respon dari AI."
                }
            } catch (e: Exception) {
                connectionError.value = "Koneksi Gagal: ${e.localizedMessage}"
            } finally {
                isConnecting.value = false
            }
        }
    }

    // 4. SUBMIT FORMULIR PLAYER & JALANKAN TUTORIAL GACHA 5X
    fun submitPlayerForm() {
        // Cek Secret Custom Hero Condition
        val isSecretAmmora = isCustomHeroActive &&
                masterName.equals("Ammora", ignoreCase = true) &&
                customHeroName.equals("Ammora", ignoreCase = true)

        val customHeroPromptPart = if (isCustomHeroActive) {
            if (isSecretAmmora) {
                "Slot 1 DIJAMIN MUTLAK adalah Secret Hero Ammora (Nama: Ammora, Ras: $customHeroRace, Gender: $customHeroGender, Usia: $customHeroAge, Grade: ★2 Lv.1, Class: Novice, Trait: [Reduksi Beban 90%, Harem Allure Logis 90%, Plot Armor 90%], Tag: [CORE]). Sisa 4 hero di-roll acak."
            } else {
                "Slot 1 DIJAMIN MUTLAK adalah Custom Hero (Nama: $customHeroName, Ras: $customHeroRace, Gender: $customHeroGender, Usia: $customHeroAge, Grade: ★2 Lv.1). Sisa 4 hero di-roll acak."
            }
        } else {
            "Seluruh 5 hero di-roll murni acak via RNG 1-1000 Diamond Summon."
        }

        currentStage.value = GameStage.IN_GAME

        // Trigger AI untuk eksekusi 5x Gacha Tutorial dan Pengenalan
        val startCommand = """
            Sistem Inisiasi: Buka gerbang dimensi Mobius untuk Master $masterName di Lobby $lobbyName. 
            Jalankan 5x Summon Diamond Tutorial Gratis. Aturan: $customHeroPromptPart
            Narasikan pilar cahaya pemanggilan dan laporkan 5 hero yang lahir.
        """.trimIndent()

        sendMessage(startCommand)
    }

    private fun buildSystemInstructions(): String {
        return """
            MASTER PROMPT - INFINITE GACHA (v3.2) & GODOT 4 COMBAT ENGINE
            1. Bahasa Indonesia natural, narasi gelap/realistis, tanpa plot armor kecuali trait MC.
            2. Pemain berperan sebagai Master di konsol, Peri ($assistantName) menyampaikan komando fisik.
            3. DYNAMIC HERO MANAGEMENT & INVENTORY STATE PARSER:
               Setiap kali ada perubahan status hero (naik level, exp bertambah, stat naik, ganti equipment, luka/fatigue/stress), hero baru lahir, item baru didapat, atau uang berubah, WAJIB tuliskan tag di bagian paling akhir pesan:
               - [ADD_HERO: {"name":"Nama","race":"Ras","gender":"Gender","age":24,"stars":2,"jobClass":"Novice","tag":"[NONE]","hp":1200,"str":8,"vit":8,"intStat":6,"agi":7,"dex":6,"luck":6,"traits":["Trait1"]}]
               - [UPDATE_HERO: {"name":"NamaHero","level":2,"exp":15,"hp":1300,"maxHp":1300,"fatigue":20,"stress":10,"str":10,"vit":9,"intStat":6,"agi":8,"dex":7,"luck":6,"weapon":"Iron Sword","armor":"Leather Vest"}]
               - [ADD_ITEM: {"name":"Nama Item","rarity":"Rare","slot":"Weapon","stats":"+12 P.ATK","effects":"20% Bleed","description":"..."}]
               - [UPDATE_WALLET: {"gold": 5000, "diamond": 50}]
            4. Kesulitan: $difficulty Mode aktif.
        """.trimIndent()
    }

    // 5. KIRIM PESAN CHAT DENGAN INJEKSI STATE HERO & INVENTORY DINAMIS
    fun sendMessage(userText: String) {
        if (userText.isBlank() || generativeModel == null) return

        if (!userText.startsWith("Sistem Inisiasi:")) {
            val updatedList = messages.value.toMutableList()
            updatedList.add(ChatMessage(sender = "USER", text = userText))
            messages.value = updatedList
        }
        isGenerating.value = true

        viewModelScope.launch {
            try {
                val stateContext = """
                    [CURRENT_HERO_MANAGEMENT_STATE]
                    Gold: ${wallet.value.gold} | Diamond: ${wallet.value.diamond}
                    Hero Roster:
                    ${heroRoster.value.joinToString("\n") { 
                        "- ${it.name} ${it.tag} (★${it.stars} Lv.${it.level} EXP:${it.exp}/${it.maxExpNeeded}) | HP:${it.currentHp}/${it.maxHp} | Fat:${it.fatigue} Str:${it.stress} | Equip:[${it.weapon}, ${it.armor}] | Stat:[STR:${it.str},VIT:${it.vit},AGI:${it.agi},INT:${it.intStat},DEX:${it.dex},LUK:${it.luck}]"
                    }}
                    Inventory: ${inventory.value.joinToString { it.name }}
                    [MASTER_COMMAND]: $userText
                """.trimIndent()

                val response = generativeModel!!.generateContent(stateContext)
                val rawResponseText = response.text ?: "..."

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

    // 6. DYNAMIC STATE PARSER (HERO MANAGEMENT, INVENTORY & WALLET)
    private fun parseAndApplyTags(text: String): String {
        var result = text

        // A. Tangkap Hero Baru [ADD_HERO: {...}]
        val addHeroRegex = Regex("\\[ADD_HERO:\\s*(\\{.*?\\})\\]")
        addHeroRegex.findAll(text).forEach { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                val traitsArray = json.optJSONArray("traits")
                val traitsList = mutableListOf<String>()
                if (traitsArray != null) {
                    for (i in 0 until traitsArray.length()) {
                        traitsList.add(traitsArray.getString(i))
                    }
                }

                val newHero = HeroData(
                    id = UUID.randomUUID().toString(),
                    name = json.optString("name", "Pahlawan Baru"),
                    race = json.optString("race", "Manusia"),
                    gender = json.optString("gender", "Laki-laki"),
                    age = json.optInt("age", 20),
                    stars = json.optInt("stars", 2),
                    level = json.optInt("level", 1),
                    exp = json.optInt("exp", 0),
                    jobClass = json.optString("jobClass", "Novice"),
                    tag = json.optString("tag", "[NONE]"),
                    maxHp = json.optInt("hp", 1000),
                    currentHp = json.optInt("hp", 1000),
                    fatigue = json.optInt("fatigue", 0),
                    stress = json.optInt("stress", 0),
                    str = json.optInt("str", 6),
                    vit = json.optInt("vit", 6),
                    intStat = json.optInt("intStat", 6),
                    agi = json.optInt("agi", 6),
                    dex = json.optInt("dex", 6),
                    luck = json.optInt("luck", 6),
                    specialTraits = traitsList
                )
                heroRoster.value = heroRoster.value + newHero
            } catch (_: Exception) {}
        }
        result = addHeroRegex.replace(result, "")

        // B. Update Hero Dinamis di Hero Management [UPDATE_HERO: {...}]
        val updateHeroRegex = Regex("\\[UPDATE_HERO:\\s*(\\{.*?\\})\\]")
        updateHeroRegex.findAll(text).forEach { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                val heroName = json.optString("name")
                val currentList = heroRoster.value.toMutableList()
                val index = currentList.indexOfFirst { it.name.equals(heroName, true) }
                
                if (index != -1) {
                    val h = currentList[index]
                    currentList[index] = h.copy(
                        level = json.optInt("level", h.level),
                        exp = json.optInt("exp", h.exp),
                        currentHp = json.optInt("hp", h.currentHp),
                        maxHp = json.optInt("maxHp", h.maxHp),
                        fatigue = json.optInt("fatigue", h.fatigue),
                        stress = json.optInt("stress", h.stress),
                        str = json.optInt("str", h.str),
                        vit = json.optInt("vit", h.vit),
                        intStat = json.optInt("intStat", h.intStat),
                        agi = json.optInt("agi", h.agi),
                        dex = json.optInt("dex", h.dex),
                        luck = json.optInt("luck", h.luck),
                        weapon = json.optString("weapon", h.weapon),
                        armor = json.optString("armor", h.armor),
                        accessory = json.optString("accessory", h.accessory),
                        jobClass = json.optString("jobClass", h.jobClass),
                        tag = json.optString("tag", h.tag)
                    )
                    heroRoster.value = currentList
                }
            } catch (_: Exception) {}
        }
        result = updateHeroRegex.replace(result, "")

        // C. Tangkap Item Baru [ADD_ITEM: {...}]
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

        // D. Tangkap Perubahan Dompet [UPDATE_WALLET: {...}]
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
