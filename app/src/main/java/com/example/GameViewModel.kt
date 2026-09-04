package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

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

    // 2. STATE GAME, INVENTORY, HERO & LOBBY
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
    val graveyardRoster = MutableStateFlow<List<HeroData>>(emptyList())
    val discoveredFloors = MutableStateFlow<Map<Int, FloorData>>(emptyMap())
    
    val facilities = MutableStateFlow<Map<String, FacilityData>>(
        mapOf(
            "Kitchen" to FacilityData("Iron Bar & Kitchen", 1, emptyList(), 3, "Pemulihan stamina & mental (+8 Jam: Fatigue -40, Stress -30)."),
            "Blacksmith" to FacilityData("Blacksmith Bengkel Besi", 1, emptyList(), 2, "Penempaan zirah, senjata, dan durabilitas."),
            "ResearchLab" to FacilityData("Central Research Lab", 1, emptyList(), 1, "Evolusi Tier Job Class & Mutasi Skill."),
            "Alchemist" to FacilityData("Alchemist Lab", 1, emptyList(), 2, "Transmutasi material 5:1 & ramuan obat."),
            "Dock" to FacilityData("Hangar / Dock Radar", 1, emptyList(), 4, "Pemindaian lawan PvP Blood Arena & Intel.")
        )
    )

    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val quickActions = MutableStateFlow<List<QuickAction>>(emptyList())
    val isGenerating = MutableStateFlow(false)

    // Form Player Parameters
    var masterName: String = "Ammora"
    var lobbyName: String = "Niflheim"
    var assistantName: String = "Ysel"
    var difficulty: String = "Abyssal"
    var isAdultModeEnabled: Boolean = false

    // Form Custom Hero Parameters
    var isCustomHeroActive: Boolean = false
    var customHeroName: String = "Ammora"
    var customHeroRace: String = "Manusia"
    var customHeroGender: String = "Laki-laki"
    var customHeroAge: Int = 28

    private var generativeModel: GenerativeModel? = null

    // 3. MESIN DADU KHUSUS DETERMINISTIK KOTLIN (RNG 1 - 1000)
    fun rollGachaStar(summonType: String): Pair<Int, Int> {
        val diceRoll = Random.nextInt(1, 1001)

        val star = when (summonType.lowercase()) {
            "gold" -> when {
                diceRoll <= 860 -> 1
                diceRoll <= 960 -> 2
                diceRoll <= 990 -> 3
                diceRoll <= 999 -> 4
                else -> 5
            }
            "diamond" -> when {
                diceRoll <= 850 -> 2
                diceRoll <= 950 -> 3
                diceRoll <= 990 -> 4
                diceRoll <= 999 -> 5
                else -> 6
            }
            "event" -> when {
                diceRoll <= 900 -> 3
                diceRoll <= 940 -> 4
                diceRoll <= 990 -> 5
                diceRoll <= 999 -> 6
                else -> 7
            }
            else -> 1
        }
        return Pair(star, diceRoll)
    }

    // 4. KONEKSI KE GEMINI 3.7 FLASH
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

    // 5. SUBMIT FORM PLAYER & 5X GACHA TUTORIAL
    fun submitPlayerForm() {
        val isSecretAmmora = isCustomHeroActive &&
                masterName.equals("Ammora", ignoreCase = true) &&
                customHeroName.equals("Ammora", ignoreCase = true)

        val gachaResults = mutableListOf<String>()

        if (isCustomHeroActive) {
            if (isSecretAmmora) {
                gachaResults.add("Slot 1 (DIJAMIN): Hero Ammora ★1 (Mantan Gamer Bumi Terjebak di Mobius | Ras: $customHeroRace, Gender: $customHeroGender, Usia: $customHeroAge, Class: Novice [Tier F], Stat Dasar: STR 5, VIT 5, AGI 5, INT 5, DEX 5, LUK 5, Tag: [CORE], Trait: [Meta-Awareness Gamer Bumi, Reduksi Beban 90%, Harem Allure Logis 90%, Plot Armor Narasi 90%])")
            } else {
                gachaResults.add("Slot 1 (DIJAMIN): Custom Hero ★1 (Nama: $customHeroName, Ras: $customHeroRace, Gender: $customHeroGender, Usia: $customHeroAge, Class: Novice, Stat Dasar: STR 5, VIT 5, AGI 5, INT 5, DEX 5, LUK 5, Tag: [CORE])")
            }
            for (i in 2..5) {
                val (star, roll) = rollGachaStar("diamond")
                gachaResults.add("Slot $i: Hero Acak Bintang ★$star (Hasil Lemparan Dadu Komputer: $roll/1000)")
            }
        } else {
            for (i in 1..5) {
                val (star, roll) = rollGachaStar("diamond")
                gachaResults.add("Slot $i: Hero Acak Bintang ★$star (Hasil Lemparan Dadu Komputer: $roll/1000)")
            }
        }

        currentStage.value = GameStage.IN_GAME

        val startCommand = """
            Sistem Inisiasi: Buka gerbang dimensi Mobius untuk Master $masterName di Lobby $lobbyName.
            Eksekusi 5x Summon Diamond Tutorial Gratis telah diputar oleh Dadu Komputer:
            ${gachaResults.joinToString("\n")}
            
            Tugas AI: Narasikan pilar cahaya pemanggilan di Altar Gacha. Khusus Hero Ammora lahir sebagai Bintang 1 (★1) dengan kelas Novice dan stat dasar 5. Ciptakan nama unik, ras multiverse, dan sebaran 6 stat mentah untuk hero acak lainnya. 
            CATATAN: Max HP dan seluruh stat tempur dihitung otomatis oleh Kalkulator Stat Kotlin (Max HP = 100 + VIT*100).
            Sertakan tag [ADD_HERO] untuk ke-5 hero tersebut!
        """.trimIndent()

        sendMessage(startCommand)
    }

    // 6. MASTER SYSTEM INSTRUCTIONS (LENGKAP DENGAN ATURAN PRIVATE CHAMBER 20+)
    private fun buildSystemInstructions(): String {
        return """
            MASTER PROMPT - INFINITE GACHA (v3.2) & GODOT 4 COMBAT ENGINE
            
            ======================================================================
            [ 1. KOSMOLOGI EKSISTENSIAL & TINGKAT KESADARAN DUNIA ]
            ======================================================================
            - MASTER (Pemain): Manusia biasa di DUNIA NYATA (BUMI) yang mengoperasikan konsol layar game. Master tidak berada fisik di Mobius dan tidak bisa mati di Tower.
            - HERO AMMORA (Custom Core Hero): Manusia dari Bumi yang terlempar ke Mobius sebagai HERO BINTANG 1 (★1 Kasta Terbawah / Novice). Mantan top player game "Pick Me Up: Infinite Gacha". Tahu situasi game, tahu Master adalah manusia biasa, paham kejamnya gacha & permadeath. Berbicara santai, realistis, dan memimpin party secara taktis untuk merangkak naik dari Bintang 1.
            - HERO NON-CUSTOM BIASA (Tier ★1 - ★4 & Lantai 1 - 89): Buta terhadap realitas game. Menganggap Mobius adalah kenyataan hidup seutuhnya, memandang Master sebagai "Entitas Dewa / Suara Tak Kasat Mata dari Langit" yang ditaati dengan takzim.
            - KESADARAN ENDGAME (Lantai 90 - 100 & Hero ★5 - ★7): Batas simulasi retak, mereka mulai melihat glitch data dan sadar Master adalah manusia biasa.
            - KEMATIAN HERO BUKAN GAME OVER: Hero mati permanen (masuk kuburan), Master tetap lanjut main. Game Over HANYA jika hero di Lobby = 0 DAN Gold & Diamond = 0.

            ======================================================================
            [ 2. FORMULA KALKULATOR STAT TEMPUR GODOT 4 (DETERMINISTIK) ]
            ======================================================================
            Sistem aplikasi secara otomatis menghitung stat tempur dari 6 Stat Mentah (STR, VIT, INT, AGI, DEX, LUK):
            * Max HP        = 100 + (VIT * 100)
            * Max Stamina   = 50 + (STR * 3) + (VIT * 2)
            * Max Mana      = 50 + (INT * 4)
            * Max Stress    = 100 + (INT * 3) + (VIT * 1)
            * Physical ATK  = (STR * 5) + (DEX * 1) + (AGI * 1)
            * Magic ATK     = (INT * 5) + (DEX * 2)
            * P. DEF        = VIT * 3 | M. DEF = (INT * 2) + (VIT * 1)
            * Crit Rate %   = (DEX * 0.1) + (LUK * 1.0) | Crit DMG = 150.0%
            * Accuracy %    = 100 + (DEX * 0.5) + (LUK * 0.8) | Dodge Rate % = AGI * 0.1
            * EXP Threshold = Level * 10 * Stars
            
            AI DILARANG mengarang angka HP/Attack sembarangan! AI cukup membagikan 6 stat mentah secara logis sesuai Bintang Hero.

            ======================================================================
            [ 3. TABEL RESMI CHANCE RATE GACHA (RNG 1 - 1000 LOOKUP) ]
            ======================================================================
            A. GOLD SUMMON (1.000 Gold): ★1 (86%), ★2 (10%), ★3 (3%), ★4 (0.9%), ★5 (0.1%).
            B. DIAMOND SUMMON (10 Diamond): ★2 (85%), ★3 (10%), ★4 (4%), ★5 (0.9%), ★6 (0.1%).
            C. EVENT SUMMON (50 Diamond): ★3 (90%), ★4 (4%), ★5 (5%), ★6 (0.9%), ★7 (0.1%).
            - Generasi Hero Non-Custom murni acak & prosedural (Nama unik 2 kata, ras multiverse, usia, kelas, dan stat).

            ======================================================================
            [ 4. GAYA PENULISAN NOVEL / KOMIK SCRIPT & PERBEDAAN DIALOG ]
            ======================================================================
            - Narasi bergaya Webtoon / Light Novel fantasi gelap yang imersif dan visceral.
            - Dialog Hero WAJIB diawali dengan Nama Hero:
              * Ammora: "(sikap taktis mantan gamer) Kalimat..."
              * Hero Biasa: NamaHero: "(takzim/hormat pada Kehendak Langit) Kalimat..."
              * Narasi Deskriptif ditulis terpisah menggambarkan aksi tempur, luka anatomis, dan dinamika sosial Lobby.

            ======================================================================
            [ 5. MEKANIK TOWER, QUEST LOCKING & FITUR SKIP GRINDING ]
            ======================================================================
            - KAPASITAS & FORMASI: MAKSIMAL 5 HERO (Bukan 4!). Formasi DINAMIS (Hero otonom bisa berpencar/flanking).
            - INFINITE QUEST ENGINE: AI bebas membuat ragam quest unik. SETIAP LANTAI WAJIB MEMILIKI Kondisi Clear dan Kondisi Gagal yang terukur.
            - PENGUNCIAN LANTAI SEAMLESS: Saat lantai pertama kali dibuka, AI WAJIB menyematkan tag:
              [LOCK_FLOOR: {"floor":1, "title":"Nama Area", "objective":"Tipe Quest", "clearCondition":"Syarat Menang", "failCondition":"Syarat Kalah", "hazard":"Bahaya Medan", "enemies":"Daftar Monster", "time":"4 Jam", "isBoss":false}]
            - GERBANG TERKUNCI & TELEPORTASI DATA: Selama belum Clear, gerbang terkunci mutlak. Begitu Clear, narasikan [QUEST CLEARED] dan dalam 5 detik tubuh hero melebur jadi partikel kubus data digital teleportasi ke Lobby!
            - REPEAT CLEAR (GRINDING): Monster & medan 100% SAMA (EXP dipotong 50%).
            - SKIP GRINDING: Master ketik "Skip Grinding" -> AI LANGSUNG sajikan Laporan Hasil Akhir Simulasi instan.
            - TRAUMA REACTION: Kematian 1 rekan party memicu Stress rekan yang hidup LANGSUNG +30 poin instan.

            ======================================================================
            [ 6. DINAMIKA LOBBY HIDUP & RECOVERY PROTOCOL ]
            ======================================================================
            - Auto-Heal Gerbang Lobby: Saat masuk Lobby, medan gerbang menutup luka fisik ajaib (HP 100%), tapi Fatigue & Stress TETAP melekat/pegal.
            - Istirahat Kitchen: Perintah istirahat di Kitchen -> majukan waktu +8 Jam In-Game (tag [UPDATE_TIME]), Fatigue -40, Stress -30.
            - Dinamika Lobby: Hero sehat bugar bergurau santai/pamer loot, interaksi sosial (allure Ammora), aroma masakan Kitchen, dan hero lain yang menatap segan.

            ======================================================================
            [ 7. PRIVATE CHAMBER & REPRODUKSI GENETIKA (MODE DEWASA 20+) ]
            ======================================================================
            - HANYA AKTIF JIKA Mode Dewasa diaktifkan Master.
            - Interaksi intim HANYA TERJADI ANTAR-HERO (Master murni sebagai pengawas dari konsol komando).
            - GAYA BAHASA ANATOMIS SEDERHANA (File 01 & 07):
              * Narasi wajib kronologis, mendalam, tanpa sensor, menggunakan istilah anatomis sederhana: klitoris, rahim, testis, payudara, puting, sperma, cairan, otot, darah, erangan, kontraksi, basah, keras, masuk, keluar, berdenyut, memuncak.
              * DILARANG KERAS menggunakan istilah medis rumit (seperti vasocongestion, tumescence, korpus kavernosum, transudat, kelenjar Bartholin, arteri helicine, introitus, sfingter, fornix posterior, dll.).
            - DUA MODE REPRODUKSI:
              * Natural Bond (Suka Sama Suka): Terpicu jika ada ikatan/Allure Ammora -> Peluang tinggi anak lahir dengan Grade ★ lebih tinggi + warisan stat orang tua.
              * Forced Mating (Paksaan Master): Stress kedua hero melonjak tajam (+50) -> Hasil acak (bisa mutasi unik atau cacat).
            - Waktu in-game maju +1 s/d 4 Jam.
            - Wajib laporkan di akhir: Status Kehamilan: BERHASIL / TIDAK BERHASIL.
            - Jika hamil & melahirkan, hero bayi lahir dengan: Fatigue 10, Stress 10, Label [NONE] (sertakan tag [ADD_HERO]).

            ======================================================================
            [ 8. PROMOTION & SYSTEM SHUTDOWN ]
            ======================================================================
            - Promosi Bintang butuh Level MAX: ★1->★2 (95% 500G), ★2->★3 (90% 2kG+2CM), ★3->★4 (65% 5kG+3UM), ★4->★5 (45% 15kG+2RM), ★5->★6 (15% 50kG+2EM), ★6->★7 (1% 150kG+1LM).
            - Gagal Promosi = Hero meledak mati permanen (masuk kuburan).
            - Sukses Promosi = Level reset ke Lv.1 dengan +20% Bonus Stat Dasar Permanen.

            ======================================================================
            [ 9. FORMAT FOOTER & DYNAMIC STATE PARSER (WAJIB) ]
            ======================================================================
            - Setiap akhir respons WAJIB menyajikan tabel 🧭 PILIHAN AKSI berisi 4-6 opsi tindakan dinamis yang relevan.
            - DYNAMIC STATE PARSER TAGS (Tulis di akhir pesan):
              * [ADD_HERO: {"name":"Nama","race":"Ras","gender":"Gender","age":24,"stars":1,"jobClass":"Novice","tag":"[NONE]","str":5,"vit":5,"intStat":5,"agi":5,"dex":5,"luck":5,"traits":["Trait1"]}]
              * [UPDATE_HERO: {"name":"NamaHero","level":2,"exp":5,"fatigue":20,"stress":10,"str":7,"vit":6,"intStat":5,"agi":6,"dex":5,"luck":5,"weapon":"Iron Sword","armor":"Leather Vest","isPregnant":false,"pregnancyPartner":""}]
              * [ADD_ITEM: {"name":"Nama Item","rarity":"Rare","slot":"Weapon","stats":"+12 P.ATK","effects":"20% Bleed","description":"..."}]
              * [UPDATE_WALLET: {"gold": 5000, "diamond": 50, "cm":10, "um":3, "rm":0, "em":0, "lm":0}]
              * [UPDATE_TIME: {"day": 1, "hour": 12, "minute": 15}]
              * [HERO_DIED: {"name":"NamaHero", "cause":"Tebasan Boss Orc Lantai 10"}]
              * [LOCK_FLOOR: {"floor": 1, "title": "Nama Area", "objective": "Tipe", "clearCondition": "Syarat Menang", "failCondition": "Syarat Kalah", "hazard":"...", "enemies":"...", "time":"4 Jam", "isBoss":false}]
            
            - TINGKAT KESULITAN: $difficulty Mode aktif.
        """.trimIndent()
    }

    // 7. EKSEKUSI PRIVATE CHAMBER (20+ ADULT REPRODUCTION)
    fun sendToPrivateChamber(maleHero: HeroData, femaleHero: HeroData, isForced: Boolean = false) {
        if (!isAdultModeEnabled) {
            val msgList = messages.value.toMutableList()
            msgList.add(ChatMessage("SYSTEM", "❌ Mode Dewasa (Private Chamber 20+) dinonaktifkan di pengaturan!"))
            messages.value = msgList
            return
        }

        val modeStr = if (isForced) "Forced Mating (Perintah Paksaan Master)" else "Natural Bond (Suka Sama Suka / Ikatan Alami)"
        val command = """
            SISTEM PRIVATE CHAMBER: Kirim ${maleHero.name} (Laki-laki | ★${maleHero.stars}) dan ${femaleHero.name} (Perempuan | ★${femaleHero.stars}) ke dalam Private Chamber.
            Mode Reproduksi: $modeStr. Majukan waktu +2 Jam In-Game.
            Eksekusi narasi intim kronologis mendalam menggunakan istilah anatomis sederhana tanpa sensor. Hitung peluang kehamilan genetika, laporkan status kehamilan, dan daftarkan hero bayi baru jika pembuahan berhasil!
        """.trimIndent()

        sendMessage(command)
    }

    // 8. SISTEM PROMOSI BINTANG DETERMINISTIK KOTLIN
    fun promoteHero(hero: HeroData) {
        if (!hero.isMaxLevel) {
            val msgList = messages.value.toMutableList()
            msgList.add(ChatMessage("SYSTEM", "❌ Hero ${hero.name} belum mencapai Level MAX (${hero.maxLevelForCurrentStar}) untuk promosi bintang!"))
            messages.value = msgList
            return
        }

        val currentW = wallet.value
        val (goldCost, cmCost, umCost, rmCost, emCost, lmCost, successRate) = when (hero.stars) {
            1 -> listOf(500, 0, 0, 0, 0, 0, 95)
            2 -> listOf(2000, 2, 0, 0, 0, 0, 90)
            3 -> listOf(5000, 0, 3, 0, 0, 0, 65)
            4 -> listOf(15000, 0, 0, 2, 0, 0, 45)
            5 -> listOf(50000, 0, 0, 0, 2, 0, 15)
            6 -> listOf(150000, 0, 0, 0, 0, 1, 1)
            else -> listOf(0, 0, 0, 0, 0, 0, 0)
        }

        if (currentW.gold < goldCost || currentW.cm < cmCost || currentW.um < umCost || currentW.rm < rmCost || currentW.em < emCost || currentW.lm < lmCost) {
            val msgList = messages.value.toMutableList()
            msgList.add(ChatMessage("SYSTEM", "❌ Biaya promosi tidak mencukupi! Butuh: $goldCost Gold, ${cmCost}x CM, ${umCost}x UM, ${rmCost}x RM, ${emCost}x EM, ${lmCost}x LM."))
            messages.value = msgList
            return
        }

        wallet.value = currentW.copy(
            gold = currentW.gold - goldCost,
            cm = currentW.cm - cmCost,
            um = currentW.um - umCost,
            rm = currentW.rm - rmCost,
            em = currentW.em - emCost,
            lm = currentW.lm - lmCost
        )

        val dice = Random.nextInt(1, 101)
        val isSuccess = dice <= successRate

        if (isSuccess) {
            val currentList = heroRoster.value.toMutableList()
            val idx = currentList.indexOfFirst { it.id == hero.id }
            if (idx != -1) {
                val h = currentList[idx]
                currentList[idx] = h.copy(
                    stars = h.stars + 1,
                    level = 1,
                    exp = 0,
                    str = (h.str * 1.2).toInt() + 1,
                    vit = (h.vit * 1.2).toInt() + 1,
                    intStat = (h.intStat * 1.2).toInt() + 1,
                    agi = (h.agi * 1.2).toInt() + 1,
                    dex = (h.dex * 1.2).toInt() + 1,
                    luck = (h.luck * 1.2).toInt() + 1
                )
                heroRoster.value = currentList
            }
            sendMessage("SISTEM ALTAR: Promosi ${hero.name} ke Bintang ★${hero.stars + 1} BERHASIL (Dadu: $dice vs Rate $successRate%). Level di-reset ke 1 dengan bonus +20% Base Stat!")
        } else {
            hero.isAlive = false
            hero.causeOfDeath = "Ledakan Energi Internal Altar Promosi (Dadu: $dice vs Rate $successRate%)"
            heroRoster.value = heroRoster.value.filter { it.id != hero.id }
            graveyardRoster.value = graveyardRoster.value + hero
            sendMessage("SISTEM ALTAR: Promosi ${hero.name} GAGAL (Dadu: $dice vs Rate $successRate%). Tubuh hero membengkak, urat pecah, dan meledak mati permanen di Altar!")
        }
    }

    // 9. TRANSMUTASI MATERIAL 5:1 (ALCHEMIST LAB)
    fun transmuteMaterial(tier: String) {
        val w = wallet.value
        when (tier.uppercase()) {
            "CM" -> {
                if (w.cm >= 5 && w.gold >= 500) {
                    wallet.value = w.copy(cm = w.cm - 5, um = w.um + 1, gold = w.gold - 500)
                    advanceTime(0, 30)
                    sendMessage("SISTEM TRANSMUTASI: Alchemist Lab berhasil melebur 5x CM + 500 Gold menjadi 1x Uncommon Material (UM)!")
                }
            }
            "UM" -> {
                if (w.um >= 5 && w.gold >= 1500) {
                    wallet.value = w.copy(um = w.um - 5, rm = w.rm + 1, gold = w.gold - 1500)
                    advanceTime(0, 30)
                    sendMessage("SISTEM TRANSMUTASI: Alchemist Lab berhasil melebur 5x UM + 1.500 Gold menjadi 1x Rare Material (RM)!")
                }
            }
        }
    }

    // 10. ISTIRAHAT KITCHEN (+8 Jam, Fatigue -40, Stress -30)
    fun restAtKitchen() {
        advanceTime(8, 0)
        val updatedRoster = heroRoster.value.map { h ->
            h.copy(
                fatigue = (h.fatigue - 40).coerceAtLeast(0),
                stress = (h.stress - 30).coerceAtLeast(0)
            )
        }
        heroRoster.value = updatedRoster
        sendMessage("SISTEM PEMULIHAN: Seluruh party makan kaldu hangat dan tidur di Iron Bar & Kitchen (+8 Jam berlalu). Fatigue -40, Stress -30!")
    }

    // 11. ADVANCE IN-GAME TIME
    fun advanceTime(hoursToAdd: Int, minutesToAdd: Int = 0) {
        var newMin = inGameMinute.value + minutesToAdd
        var extraHours = hoursToAdd + (newMin / 60)
        newMin %= 60

        var newHour = inGameHour.value + extraHours
        var extraDays = newHour / 24
        newHour %= 24

        val newDay = inGameDay.value + extraDays

        inGameMinute.value = newMin
        inGameHour.value = newHour
        inGameDay.value = newDay
    }

    // 12. KIRIM PESAN CHAT DENGAN INJEKSI STATE RESMI
    fun sendMessage(userText: String) {
        if (userText.isBlank() || generativeModel == null) return

        if (!userText.startsWith("Sistem Inisiasi:") && !userText.startsWith("SISTEM")) {
            val updatedList = messages.value.toMutableList()
            updatedList.add(ChatMessage(sender = "USER", text = userText))
            messages.value = updatedList
        }
        isGenerating.value = true

        viewModelScope.launch {
            try {
                val stateContext = """
                    [CURRENT_STATE_INJECTION]
                    Time: Hari ke-${inGameDay.value} | Jam %02d:%02d
                    Adult Mode 20+: ${if (isAdultModeEnabled) "AKTIF (Boleh Private Chamber)" else "NONAKTIF"}
                    Gold: ${wallet.value.gold} | Diamond: ${wallet.value.diamond} | Material:[CM:${wallet.value.cm}, UM:${wallet.value.um}, RM:${wallet.value.rm}, EM:${wallet.value.em}, LM:${wallet.value.lm}]
                    Hero Roster:
                    ${heroRoster.value.joinToString("\n") { 
                        "- ${it.name} ${it.tag} (${it.gender}, ★${it.stars} Lv.${it.level} EXP:${it.exp}/${it.maxExpNeeded}) | HP:${it.currentHp}/${it.maxHp} | Fat:${it.fatigue} Str:${it.stress} | Hamil:${if (it.isPregnant) "Ya (${it.pregnancyPartner})" else "Tidak"} | Equip:[${it.weapon}, ${it.armor}] | Stat:[STR:${it.str},VIT:${it.vit},AGI:${it.agi},INT:${it.intStat},DEX:${it.dex},LUK:${it.luck}] | CombatStat:[P.ATK:${it.physicalAtk},M.ATK:${it.magicAtk},P.DEF:${it.pDef},M.DEF:${it.mDef},Crit:${it.critRate}%]"
                    }}
                    Graveyard (Fallen Heroes): ${graveyardRoster.value.joinToString { "${it.name} (Gugur: ${it.causeOfDeath})" }}
                    Inventory: ${inventory.value.joinToString { it.name }}
                    Discovered Tower Floors Intel:
                    ${discoveredFloors.value.values.joinToString("\n") { 
                        "Lantai ${it.floorNumber} [${it.title}]: Objective=${it.objectiveType}, ClearCond=[${it.clearCondition}], FailCond=[${it.failCondition}], Hazard=${it.terrainHazard}, Enemies=${it.enemyComposition}"
                    }}
                    [MASTER_COMMAND]: $userText
                """.trimIndent().format(inGameHour.value, inGameMinute.value)

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

    // 13. PARSER PILIHAN AKSI DINAMIS
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

    // 14. DYNAMIC STATE PARSER
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

        // B. Tangkap Hero Baru [ADD_HERO: {...}] (Kelahiran Bayi / Gacha)
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

                val vitVal = json.optInt("vit", 5)

                val newHero = HeroData(
                    id = UUID.randomUUID().toString(),
                    name = json.optString("name", "Pahlawan Baru"),
                    race = json.optString("race", "Manusia"),
                    gender = json.optString("gender", "Laki-laki"),
                    age = json.optInt("age", 20),
                    stars = json.optInt("stars", 1),
                    level = json.optInt("level", 1),
                    exp = json.optInt("exp", 0),
                    jobClass = json.optString("jobClass", "Novice"),
                    tag = json.optString("tag", "[NONE]"),
                    fatigue = json.optInt("fatigue", 0),
                    stress = json.optInt("stress", 0),
                    str = json.optInt("str", 5),
                    vit = vitVal,
                    intStat = json.optInt("intStat", 5),
                    agi = json.optInt("agi", 5),
                    dex = json.optInt("dex", 5),
                    luck = json.optInt("luck", 5),
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
                    val updatedVit = json.optInt("vit", h.vit)
                    
                    val updatedHero = h.copy(
                        level = json.optInt("level", h.level),
                        exp = json.optInt("exp", h.exp),
                        fatigue = json.optInt("fatigue", h.fatigue),
                        stress = json.optInt("stress", h.stress),
                        str = json.optInt("str", h.str),
                        vit = updatedVit,
                        intStat = json.optInt("intStat", h.intStat),
                        agi = json.optInt("agi", h.agi),
                        dex = json.optInt("dex", h.dex),
                        luck = json.optInt("luck", h.luck),
                        weapon = json.optString("weapon", h.weapon),
                        armor = json.optString("armor", h.armor),
                        accessory = json.optString("accessory", h.accessory),
                        jobClass = json.optString("jobClass", h.jobClass),
                        tag = json.optString("tag", h.tag),
                        isPregnant = json.optBoolean("isPregnant", h.isPregnant),
                        pregnancyPartner = json.optString("pregnancyPartner", h.pregnancyPartner)
                    )
                    
                    if (json.has("hp")) {
                        updatedHero.currentHp = json.optInt("hp").coerceAtMost(updatedHero.maxHp)
                    } else if (json.optBoolean("fullHeal", false)) {
                        updatedHero.currentHp = updatedHero.maxHp
                    }
                    
                    currentList[index] = updatedHero
                    heroRoster.value = currentList
                }
            } catch (_: Exception) {}
        }
        result = updateHeroRegex.replace(result, "")

        // D. Tangkap Kematian Hero Permanen [HERO_DIED: {...}]
        val deathRegex = Regex("\\[HERO_DIED:\\s*(\\{.*?\\})\\]")
        deathRegex.findAll(text).forEach { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                val deadName = json.optString("name")
                val cause = json.optString("cause", "Gugur di Medan Tempur")
                val currentRoster = heroRoster.value.toMutableList()
                val deadHero = currentRoster.find { it.name.equals(deadName, true) }
                if (deadHero != null) {
                    deadHero.isAlive = false
                    deadHero.causeOfDeath = cause
                    heroRoster.value = currentRoster.filter { it.name != deadName }
                    graveyardRoster.value = graveyardRoster.value + deadHero
                }
            } catch (_: Exception) {}
        }
        result = deathRegex.replace(result, "")

        // E. Tangkap Perubahan Waktu [UPDATE_TIME: {...}]
        val timeRegex = Regex("\\[UPDATE_TIME:\\s*(\\{.*?\\})\\]")
        timeRegex.findAll(text).forEach { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                if (json.has("day")) inGameDay.value = json.getInt("day")
                if (json.has("hour")) inGameHour.value = json.getInt("hour")
                if (json.has("minute")) inGameMinute.value = json.getInt("minute")
            } catch (_: Exception) {}
        }
        result = timeRegex.replace(result, "")

        // F. Tangkap Item Baru [ADD_ITEM: {...}]
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

        // G. Tangkap Perubahan Dompet [UPDATE_WALLET: {...}]
        val walletRegex = Regex("\\[UPDATE_WALLET:\\s*(\\{.*?\\})\\]")
        walletRegex.findAll(text).forEach { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                val currentW = wallet.value
                wallet.value = currentW.copy(
                    gold = json.optInt("gold", currentW.gold),
                    diamond = json.optInt("diamond", currentW.diamond),
                    cm = json.optInt("cm", currentW.cm),
                    um = json.optInt("um", currentW.um),
                    rm = json.optInt("rm", currentW.rm),
                    em = json.optInt("em", currentW.em),
                    lm = json.optInt("lm", currentW.lm)
                )
            } catch (_: Exception) {}
        }
        result = walletRegex.replace(result, "")

        return result.trim()
    }
}
