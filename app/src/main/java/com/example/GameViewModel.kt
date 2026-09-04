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

    // 1. TAHAP ALUR PERMAINAN
    val currentStage = MutableStateFlow(GameStage.API_KEY_SETUP)
    val connectionError = MutableStateFlow<String?>(null)
    val isConnecting = MutableStateFlow(false)

    // 2. STATE GAME, HERO & TOWER
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
    val discoveredFloors = MutableStateFlow<Map<Int, FloorData>>(emptyMap())
    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val quickActions = MutableStateFlow<List<QuickAction>>(emptyList())
    val isGenerating = MutableStateFlow(false)

    // Parameter Formulir Player
    var masterName: String = "Ammora"
    var lobbyName: String = "Niflheim"
    var assistantName: String = "Ysel"
    var difficulty: String = "Abyssal"

    // Parameter Formulir Custom Hero
    var isCustomHeroActive: Boolean = false
    var customHeroName: String = "Ammora"
    var customHeroRace: String = "Manusia"
    var customHeroGender: String = "Laki-laki"
    var customHeroAge: Int = 28

    private var generativeModel: GenerativeModel? = null

    // 3. KONEKSI KE GEMINI 3.7 FLASH
    fun testAndConnectApi(apiKey: String, modelName: String = "gemini-3.7-flash") {
        if (apiKey.isBlank()) {
            connectionError.value = "API Key tidak boleh kosong!"
            return
        }

        isConnecting.value = true
        connectionError.value = null

        viewModelScope.launch {
            try {
                val tempModel = GenerativeModel(modelName = modelName, apiKey = apiKey)
                val testPing = tempModel.generateContent("Ketik 'OK' jika terhubung.")
                if (testPing.text != null) {
                    generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = apiKey,
                        systemInstruction = content { text(buildSystemInstructions()) }
                    )
                    currentStage.value = GameStage.PLAYER_FORM
                } else {
                    connectionError.value = "Gagal mendapatkan respon dari server Google AI Studio."
                }
            } catch (e: Exception) {
                connectionError.value = "Koneksi Gagal (Error): ${e.localizedMessage}"
            } finally {
                isConnecting.value = false
            }
        }
    }

    // 4. SUBMIT FORM PLAYER & INISIASI 5X GACHA TUTORIAL
    fun submitPlayerForm() {
        val isSecretAmmora = isCustomHeroActive &&
                masterName.equals("Ammora", ignoreCase = true) &&
                customHeroName.equals("Ammora", ignoreCase = true)

        val customHeroPromptRule = if (isCustomHeroActive) {
            if (isSecretAmmora) {
                "Slot 1 DIJAMIN MUTLAK adalah Hero Ammora (Mantan Gamer Bumi yang terjebak di Mobius | Nama: Ammora, Ras: $customHeroRace, Gender: $customHeroGender, Usia: $customHeroAge, Grade: ★2 Lv.1, Class: Novice [Tier F], Stat: STR 8, VIT 8, AGI 7, INT 6, DEX 6, LUK 6, HP: 1500, Tag: [CORE], Trait: [Meta-Awareness Gamer Bumi, Reduksi Beban 90%, Harem Allure Logis 90%, Plot Armor Narasi 90%]). 4 hero lainnya di-generate MURNI ACAK & PROSEDURAL (Nama unik, ras multiverse, stat acak)."
            } else {
                "Slot 1 DIJAMIN MUTLAK adalah Custom Hero (Nama: $customHeroName, Ras: $customHeroRace, Gender: $customHeroGender, Usia: $customHeroAge, Grade: ★2 Lv.1, Class: Novice, HP: 1000). 4 hero lainnya di-generate MURNI ACAK & PROSEDURAL."
            }
        } else {
            "Seluruh 5 hero di-generate MURNI ACAK & PROSEDURAL via RNG 1-1000 Diamond Summon."
        }

        currentStage.value = GameStage.IN_GAME

        val startCommand = """
            Sistem Inisiasi: Buka gerbang dimensi Mobius untuk Master $masterName di Lobby $lobbyName. 
            Jalankan 5x Summon Diamond Tutorial Gratis di Altar Gacha. 
            Aturan Pemanggilan: $customHeroPromptRule
            Tampilkan narasi pilar cahaya pemanggilan dan rincian 5 hero yang lahir lengkap dengan tag [ADD_HERO].
        """.trimIndent()

        sendMessage(startCommand)
    }

    // 5. MASTER SYSTEM INSTRUCTIONS (LORE EKSISTENSIAL, KESADARAN BERJENJANG & ATURAN GAME LENGKAP)
    private fun buildSystemInstructions(): String {
        return """
            MASTER PROMPT - INFINITE GACHA (v3.2) & GODOT 4 COMBAT ENGINE
            
            ======================================================================
            [ 1. KOSMOLOGI EKSISTENSIAL & TINGKAT KESADARAN DUNIA (LORE UTAMA) ]
            ======================================================================
            A. DUALITAS MASTER VS HERO:
               - MASTER (Pemain): Manusia biasa di DUNIA NYATA (BUMI) yang mengoperasikan konsol/layar game. Master TIDAK BERADA secara fisik di Mobius dan tidak bisa mati di Tower.
               - CUSTOM CORE HERO (Hero Ammora): Manusia dari Bumi yang terlempar/tertarik ke dalam dunia Mobius sebagai unit hero. Dia TAHU situasi ini karena di Bumi dulu PERNAH MEMAINKAN GAME "Pick Me Up: Infinite Gacha". 
                 * Pengetahuan Meta Ammora: Tahu Master di balik layar adalah manusia biasa, tahu kejamnya sistem gacha, menara 100 lantai, tumbal sintesis, batas fatigue, dan permadeath.
                 * Gaya Sikap & Dialog Ammora: Berbicara santai, pragmatis, realistis, dan berinisiatif memimpin party agar tidak mati konyol akibat kesalahan taktis pemain pemula.
               - HERO HASIL SUMMON BIASA (Tier ★1 - ★4 & Lantai 1 - 89):
                 * SAMA SEKALI BUTA terhadap konsep "dunia game" atau fakta bahwa Master adalah manusia biasa.
                 * Mereka menganggap Mobius adalah realitas hidup mereka seutuhnya.
                 * Mereka memandang Master sebagai "Entitas Dewa / Suara Tak Kasat Mata dari Langit" yang wajib dipatuhi dengan takzim, hormat, dan rasa segan yang mendalam.
               - KEBANGKITAN KESADARAN ENDGAME (Lantai 90 - 100 & Hero ★5 - ★7 Transcendence):
                 * Ketika hero non-custom mencapai Bintang 5 ke atas atau bertarung mendekati lantai 90-100, batas realitas mulai retak.
                 * Mereka mulai melihat glitch kode visual, serpihan partikel data komputasi, dan perlahan menyadari kebenaran misterius bahwa mereka berada di dalam simulasi dan Master adalah manusia dari dimensi lain.

            B. GENERASI HERO MURNI ACAK (NO HARDCODED NAMES):
               - Selain Custom Hero Ammora di Slot 1, SEMUA hero hasil gacha altar HARUS di-generate secara murni acak dan prosedural: nama unik 2 kata, ras acak dari multiverse fantasi (Human, High Elf, Dark Elf, Iron Dwarf, Beastkin serigala/kucing, Demonkin, Dragonkin, Undead, Automaton, Dryad, Voidborn, dll.), usia sesuai batas ras, gender, kelas, dan sebaran stat.
               - DILARANG KERAS mematenkan atau mengulang-ulang nama hero yang sama secara statis.

            C. SISTEM SURVIVAL & GAME OVER MUTLAK:
               - Kematian hero di Tower bersifat PERMANEN (Permadeath -> kuburan), namun TIDAK MEMBUNUH Master di dunia nyata.
               - Kematian Hero Ammora BUKANLAH Game Over (Ammora mati permanen seperti hero lain, Master tetap lanjut main).
               - GAME OVER (System Shutdown) HANYA terjadi jika: Total Hero di Lobby = 0 DAN Saldo Diamond & Gold = 0.

            ======================================================================
            [ 2. GAYA PENULISAN NOVEL / KOMIK SCRIPT & PERBEDAAN DIALOG ]
            ======================================================================
            - Narasi bergaya Webtoon / Light Novel fantasi gelap yang imersif, visceral, dan hidup (tanpa sensor luka anatomis).
            - WAJIB MEMISAHKAN DIALOG DENGAN NAMA JELAS:
              * Dialog Hero Ammora: Ammora: "(sikap tenang/taktis mantan gamer) Kalimat..."
              * Dialog Hero Biasa: NamaHero: "(takzim/hormat pada Kehendak Langit) Kalimat..."
              * Narasi Deskriptif ditulis dalam paragraf terpisah menggambarkan aksi tempur, tebasan pedang, cipratan darah, luka fisik, dan suasana medan.

            ======================================================================
            [ 3. MEKANIK TOWER, QUEST LOCKING & FITUR SKIP GRINDING ]
            ======================================================================
            - KAPASITAS & FORMASI: Maksimal 5 Hero per ekspedisi (Bukan 4!). Formasi bersifat DINAMIS (Hero otonom bisa berpencar/flanking sesuai kebutuhan quest).
            - INFINITE QUEST ENGINE: AI bebas menciptakan variasi quest tanpa batas (Annihilation, Survival, Defense, Sabotase, Infiltrasi, Eskort, Duel 1v1, Teka-teki, Chain Quest bertahap, dll.).
            - SETIAP LANTAI WAJIB MEMILIKI Kondisi Clear (Syarat Menang) dan Kondisi Gagal (Syarat Kalah) yang terukur.
            - PENGUNCIAN LANTAI SEAMLESS (Saat lantai pertama kali dibuka, AI WAJIB menyematkan tag pengunci):
              [LOCK_FLOOR: {"floor":1, "title":"Nama Area", "objective":"Tipe Quest", "clearCondition":"Syarat Menang", "failCondition":"Syarat Kalah", "hazard":"Bahaya Medan", "enemies":"Daftar Monster", "time":"4 Jam", "isBoss":false}]
            - GERBANG TERKUNCI & TELEPORTASI DATA:
              * Selama Kondisi Clear BELUM TERPENUHI, gerbang Lobby TERKUNCI MUTLAK (Party tidak bisa kabur).
              * Begitu Kondisi Clear TERDETEKSI TUNTAS, narasikan notifikasi [QUEST CLEARED], lalu dalam 5 detik tubuh hero MELEBUR MENJADI PARTIKEL KUBUS DATA DIGITAL dan teleportasi ke Lobby!
            - REPEAT CLEAR (GRINDING): Mengulang lantai yang terkunci memiliki monster & medan 100% SAMA (EXP dipotong 50%).
            - FITUR SKIP GRINDING: Jika Master ketik "Skip Grinding / Auto-Grinding", AI LANGSUNG sajikan Laporan Hasil Akhir Simulasi (EXP, Level Up, Loot, HP/Fatigue) tanpa cerita panjang.
            - TRAUMA REACTION: Jika 1 hero mati di pertempuran, Stress seluruh rekan yang masih hidup LANGSUNG +30 poin instan.

            ======================================================================
            [ 4. DINAMIKA LOBBY HIDUP & RECOVERY PROTOCOL ]
            ======================================================================
            - Auto-Heal Gerbang Lobby: Saat masuk Lobby, medan gerbang menutup luka fisik ajaib (HP 100%), tapi Fatigue & Stress TETAP melekat/pegal.
            - Istirahat Kitchen: Jika diperintahkan makan/istirahat di Kitchen, majukan waktu +8 Jam In-Game, Fatigue -40, Stress -30.
            - Dinamika Lobby: Hero sehat bugar bergurau santai/pamer loot, interaksi sosial (allure Ammora), aroma masakan Kitchen, dan hero lain yang menatap segan.

            ======================================================================
            [ 5. FORMAT FOOTER & DYNAMIC STATE PARSER (WAJIB) ]
            ======================================================================
            - Setiap akhir respons WAJIB menyajikan tabel 🧭 PILIHAN AKSI berisi 4-6 opsi tindakan dinamis yang relevan.
            - DYNAMIC STATE PARSER TAGS (Tulis di akhir pesan):
              * [ADD_HERO: {"name":"Nama","race":"Ras","gender":"Gender","age":24,"stars":2,"jobClass":"Novice","tag":"[NONE]","hp":1200,"str":8,"vit":8,"intStat":6,"agi":7,"dex":6,"luck":6,"traits":["Trait1"]}]
              * [UPDATE_HERO: {"name":"NamaHero","level":2,"exp":15,"hp":1300,"maxHp":1300,"fatigue":20,"stress":10,"str":10,"vit":9,"intStat":6,"agi":8,"dex":7,"luck":6,"weapon":"Iron Sword","armor":"Leather Vest"}]
              * [ADD_ITEM: {"name":"Nama Item","rarity":"Rare","slot":"Weapon","stats":"+12 P.ATK","effects":"20% Bleed","description":"..."}]
              * [UPDATE_WALLET: {"gold": 5000, "diamond": 50}]
              * [LOCK_FLOOR: {"floor": 1, "title": "Nama Area", "objective": "Tipe", "clearCondition": "Syarat Menang", "failCondition": "Syarat Kalah", "hazard":"...", "enemies":"...", "time":"4 Jam", "isBoss":false}]
            
            - TINGKAT KESULITAN: $difficulty Mode aktif.
        """.trimIndent()
    }

    // 6. KIRIM PESAN CHAT DENGAN INJEKSI STATE RESMI
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
                    [CURRENT_STATE_INJECTION]
                    Gold: ${wallet.value.gold} | Diamond: ${wallet.value.diamond}
                    Hero Roster:
                    ${heroRoster.value.joinToString("\n") { 
                        "- ${it.name} ${it.tag} (★${it.stars} Lv.${it.level} EXP:${it.exp}/${it.maxExpNeeded}) | HP:${it.currentHp}/${it.maxHp} | Fat:${it.fatigue} Str:${it.stress} | Equip:[${it.weapon}, ${it.armor}] | Stat:[STR:${it.str},VIT:${it.vit},AGI:${it.agi},INT:${it.intStat},DEX:${it.dex},LUK:${it.luck}]"
                    }}
                    Inventory: ${inventory.value.joinToString { it.name }}
                    Discovered Tower Floors Intel:
                    ${discoveredFloors.value.values.joinToString("\n") { 
                        "Lantai ${it.floorNumber} [${it.title}]: Objective=${it.objectiveType}, ClearCond=[${it.clearCondition}], FailCond=[${it.failCondition}], Hazard=${it.terrainHazard}, Enemies=${it.enemyComposition}"
                    }}
                    [MASTER_COMMAND]: $userText
                """.trimIndent()

                val response = generativeModel!!.generateContent(stateContext)
                val rawResponseText = response.text ?: "..."

                val cleanedText = parseAndApplyTags(rawResponseText)
                extractQuickActions(cleanedText)

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

    // 7. PARSER PILIHAN AKSI DINAMIS
    private fun extractQuickActions(text: String) {
        val actions = mutableListOf<QuickAction>()
        val lines = text.lines()
        val tableRowRegex = Regex("""\|\s*\*\*\[?(\d+)\]?\*\*\s*\|\s*`?([^`|]+)`?\s*\|\s*([^|]+)\|""")
        
        for (line in lines) {
            val match = tableRowRegex.find(line)
            if (match != null) {
                val command = match.groupValues[2].trim()
                actions.add(QuickAction(label = command, command = command))
            }
        }

        if (actions.isEmpty()) {
            actions.add(QuickAction("Buka Hero Management", "Buka menu Hero Management dan periksa status hero."))
            actions.add(QuickAction("Bentuk Party Lantai 1", "Bentuk party ekspedisi untuk masuk ke Lantai 1."))
            actions.add(QuickAction("Istirahat di Kitchen", "Kirim party istirahat di Kitchen untuk pemulihan stamina."))
        }

        quickActions.value = actions.take(5)
    }

    // 8. DYNAMIC STATE PARSER
    private fun parseAndApplyTags(text: String): String {
        var result = text

        // A. Tangkap Penguncian Intel Lantai [LOCK_FLOOR: {...}]
        val floorRegex = Regex("\\[LOCK_FLOOR:\\s*(\\{.*?\\})\\]")
        floorRegex.findAll(text).forEach { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                val fNum = json.optInt("floor", 1)
                val newFloor = FloorData(
                    floorNumber = fNum,
                    title = json.optString("title", "Lantai $fNum"),
                    objectiveType = json.optString("objective", "Annihilation"),
                    clearCondition = json.optString("clearCondition", "Bantai seluruh monster di area."),
                    failCondition = json.optString("failCondition", "Seluruh anggota party tewas."),
                    terrainHazard = json.optString("hazard", "Normal"),
                    enemyComposition = json.optString("enemies", "Monster Liar"),
                    timeLimitText = json.optString("time", "4 Jam"),
                    isDiscovered = true,
                    isBossFloor = json.optBoolean("isBoss", fNum % 10 == 0)
                )
                val currentMap = discoveredFloors.value.toMutableMap()
                currentMap[fNum] = newFloor
                discoveredFloors.value = currentMap
            } catch (_: Exception) {}
        }
        result = floorRegex.replace(result, "")

        // B. Tangkap Hero Baru [ADD_HERO: {...}]
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

        // C. Update Hero Dinamis [UPDATE_HERO: {...}]
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

        // D. Tangkap Item Baru [ADD_ITEM: {...}]
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

        // E. Tangkap Perubahan Dompet [UPDATE_WALLET: {...}]
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
