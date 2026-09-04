package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.GeminiApi
import com.example.data.api.LocalMasterEngine
import com.example.data.generator.GachaResult
import com.example.data.generator.HeroFactory
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class GameRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("infinite_gacha_prefs", Context.MODE_PRIVATE)
    private val geminiApi = GeminiApi()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(prefs.getString("selected_model", "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isLoadingStory = MutableStateFlow(false)
    val isLoadingStory: StateFlow<Boolean> = _isLoadingStory.asStateFlow()

    init {
        loadSavedGame()
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        prefs.edit().putString("custom_api_key", key).apply()
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
        prefs.edit().putString("selected_model", model).apply()
    }

    fun startNewGame(
        masterName: String = "Master",
        lobbyName: String = "Valhalla Citadel",
        fairyName: String = "Peri Navi",
        difficulty: String = "Normal",
        customHeroName: String? = null
    ) {
        // Generate initial 5 tutorial heroes
        val initialHeroes = mutableListOf<Hero>()
        if (!customHeroName.isNullOrBlank()) {
            initialHeroes.add(
                HeroFactory.createRandomHero(
                    starGrade = 3,
                    customName = customHeroName,
                    isCustom = true
                )
            )
        }
        while (initialHeroes.size < 5) {
            initialHeroes.add(HeroFactory.createRandomHero(starGrade = Random.nextInt(1, 4)))
        }

        val party = initialHeroes.take(3).map { it.id }

        val welcomeText = """
Selamat datang di dimensi Mobius, Master $masterName.

Saya adalah $fairyName, antarmuka pemandu yang bertugas mengawasi ketertiban sistem dan kelangsungan hidup Anda di dimensi ini. Sesuai protokol inisiasi, Lobby **$lobbyName** kini aktif dengan tingkat kesulitan **$difficulty**.

```text
[INVENTARIS AWAL DIBERIKAN]
• Wallet      : 5.000 Gold | 50 Diamond
• Material    : 10x CM | 3x UM
• Konsumsi    : 1x Soul Stabilizer (Common)
• Pahlawan    : 5 Pahlawan Tutorial telah ditarik ke Lobby (Fatigue: 0, Stress: 0)
```

Aturan mutlak dimensi ini telah tertanam dalam pilar hukum: **Fatigue dan Stress** akan menentukan kepatuhan pahlawan Anda. Jika mereka terlalu lelah atau depresi, mereka berhak menolak perintah. Kematian di dalam Menara Mobius bersifat **permanen** tanpa plot armor.

---
### 🧭 PILIHAN AKSI
| No | Perintah | Keterangan |
|---|---|---|
| 1 | `Masuk Tower Lantai 1` | Buka ekspedisi perdana menara Mobius |
| 2 | `Inspeksi Roster Hero` | Periksa data statistik STR/VIT/AGI pahlawan Anda |
| 3 | `Cek Altar Gacha` | Lihat pilar pemanggilan hero baru |
| 4 | `Atur Fasilitas Lobby` | Tugaskan hero ke Dapur atau Bengkel |
| 5 | `Buka Pengaturan API` | Konfigurasi Gemini API untuk respon narasi mendalam |

Saya siap menyampaikan titah fisik Master kepada para hero di Lobby. Apa perintah pertama Anda?
""".trimIndent()

        val welcomeMsg = ChatMessage(
            sender = "PERI",
            text = welcomeText,
            timestamp = "Hari 1 - 08:00",
            suggestedActions = listOf(
                "Masuk Tower Lantai 1",
                "Inspeksi Roster Hero",
                "Cek Altar Gacha",
                "Istirahat di Kitchen"
            )
        )

        val newState = GameState(
            masterName = masterName,
            lobbyName = lobbyName,
            fairyName = fairyName,
            difficulty = difficulty,
            inGameDay = 1,
            inGameHour = 8,
            inGameMinute = 0,
            gold = 5000,
            diamond = 50,
            tutorialTickets = 0,
            materials = Materials(cm = 10, um = 3),
            soulStabilizers = 1,
            towerFloorCurrent = 1,
            towerFloorHighest = 1,
            heroes = initialHeroes,
            partyIds = party,
            chatMessages = listOf(welcomeMsg)
        )

        _gameState.value = newState
        saveGame(newState)
    }

    fun advanceTime(hours: Int, minutes: Int = 0) {
        val current = _gameState.value
        var m = current.inGameMinute + minutes
        var h = current.inGameHour + hours + (m / 60)
        m %= 60
        var d = current.inGameDay + (h / 24)
        h %= 24

        _gameState.value = current.copy(
            inGameDay = d,
            inGameHour = h,
            inGameMinute = m
        )
        saveGame(_gameState.value)
    }

    fun summon(type: String, count: Int = 1): List<Hero> {
        val current = _gameState.value
        if (current.isGameOver) return emptyList()

        val costGoldPer = if (type.lowercase() == "gold") 1000 else 0
        val costDiamondPer = when (type.lowercase()) {
            "diamond" -> 10
            "event" -> 50
            else -> 0
        }
        val totalGoldCost = costGoldPer * count
        val totalDiamondCost = costDiamondPer * count

        if (current.gold < totalGoldCost || current.diamond < totalDiamondCost) {
            val failMsg = ChatMessage(
                sender = "PERI",
                text = "⚠️ Transaksi gagal! Saldo tidak mencukupi untuk pemanggilan $count x ${type.uppercase()}. (Butuh $totalGoldCost Gold / $totalDiamondCost Diamond)",
                timestamp = current.formattedTime
            )
            _gameState.value = current.copy(chatMessages = current.chatMessages + failMsg)
            return emptyList()
        }

        val results = mutableListOf<GachaResult>()
        for (i in 0 until count) {
            results.add(HeroFactory.rollGacha(type))
        }

        val newHeroes = results.map { it.hero }
        val newGold = current.gold - totalGoldCost
        val newDiamond = current.diamond - totalDiamondCost

        // Build Ledger & Dice Report
        val ledgerText = if (totalGoldCost > 0) {
            "Gold: ${current.gold} - $totalGoldCost = $newGold Gold"
        } else {
            "Diamond: ${current.diamond} - $totalDiamondCost = $newDiamond Diamond"
        }

        val diceSummary = results.joinToString("\n") { r ->
            "[RNG Summon (${r.rollType}): Angka ${r.diceNumber} vs Target -> Hasil: ★${r.hero.starGrade} ${r.hero.name} (${r.hero.jobClass})]"
        }

        val heroListText = results.joinToString("\n") { r ->
            val h = r.hero
            "• ★${h.starGrade} ${h.name} | ${h.race} (${h.gender}) | Class: ${h.jobClass} | STR:${h.str} VIT:${h.vit} AGI:${h.agi} INT:${h.intStat}"
        }

        val gachaLog = """
Pilar Altar Pemanggilan menyala terang! Kabut portal ungu terbelah dan $count jiwa baru terpanggil melintasi batas dimensi.

```text
[LEDGER TRANSAKSI SUMMON]
$ledgerText
Jumlah Pemanggilan: $count x ${type.uppercase()}
Hasil Pemanggilan:
$heroListText
```

$diceSummary

Peri (${current.fairyName}): *"Para hero baru telah menjejakkan kaki di Lobby dengan Fatigue 0 / Stress 0 dan label [NONE]. Rawat mereka dengan baik, atau jadikan tumbal kemajuan dimensi ini."*

---
### 🧭 PILIHAN AKSI
| No | Perintah | Keterangan |
|---|---|---|
| 1 | `Inspeksi Roster Hero` | Lihat profil lengkap dan stat mereka |
| 2 | `Beri Label [CORE]/[SCRAP]` | Klasifikasikan fungsi hero di Lobby |
| 3 | `Masuk Tower Lantai ${current.towerFloorCurrent}` | Uji kekuatan mereka di Menara Mobius |
| 4 | `Summon Lagi (${type.uppercase()})` | Lakukan pemanggilan berikutnya |
""".trimIndent()

        val gachaMsg = ChatMessage(
            sender = "PERI",
            text = gachaLog,
            timestamp = current.formattedTime,
            isDiceRoll = true,
            suggestedActions = listOf(
                "Inspeksi Roster Hero",
                "Masuk Tower Lantai ${current.towerFloorCurrent}",
                "Istirahat di Kitchen"
            )
        )

        val updated = current.copy(
            gold = newGold,
            diamond = newDiamond,
            heroes = current.heroes + newHeroes,
            chatMessages = current.chatMessages + gachaMsg
        )
        _gameState.value = updated
        saveGame(updated)
        return newHeroes
    }

    fun restInKitchen() {
        val current = _gameState.value
        advanceTime(8, 0)
        val afterTime = _gameState.value

        val recoveredHeroes = afterTime.heroes.map { hero ->
            if (hero.isAlive) {
                hero.copy(
                    fatigue = maxOf(0, hero.fatigue - 40),
                    stress = maxOf(0, hero.stress - 30),
                    currentHp = hero.maxHp // Lobby recovery
                )
            } else hero
        }

        val msg = ChatMessage(
            sender = "PERI",
            text = LocalMasterEngine.generateResponse("kitchen", afterTime),
            timestamp = afterTime.formattedTime,
            suggestedActions = listOf(
                "Masuk Tower Lantai ${afterTime.towerFloorCurrent}",
                "Inspeksi Roster Hero",
                "Cek Altar Gacha"
            )
        )

        val updated = afterTime.copy(
            heroes = recoveredHeroes,
            chatMessages = afterTime.chatMessages + msg
        )
        _gameState.value = updated
        saveGame(updated)
    }

    fun launchTowerExpedition(floor: Int) {
        val current = _gameState.value
        if (current.isGameOver) return

        val partyHeroes = current.activePartyHeroes.ifEmpty {
            current.heroes.filter { it.isAlive }.take(5)
        }

        if (partyHeroes.isEmpty()) {
            val noHeroMsg = ChatMessage(
                sender = "PERI",
                text = "⚠️ [TIDAK ADA HERO AKTIF]: Anda tidak memiliki hero yang hidup untuk dikirim ke Menara! Silakan summon pahlawan baru di Altar.",
                timestamp = current.formattedTime
            )
            _gameState.value = current.copy(chatMessages = current.chatMessages + noHeroMsg)
            checkGameOver()
            return
        }

        // Hero Autonomy Pre-Check: if fatigue or stress >= 60, they protest
        val protestingHero = partyHeroes.firstOrNull { it.willRefuseOrder }
        if (protestingHero != null) {
            val protestMsg = ChatMessage(
                sender = "PERI",
                text = """
⚠️ **[PERINGATAN OTONOMI HERO - PROTES KERAS!]**
Pahlawan **${protestingHero.name}** [★${protestingHero.starGrade}] menolak melangkah ke gerbang Menara!
Kondisi saat ini: **Fatigue: ${protestingHero.fatigue}/100**, **Stress: ${protestingHero.stress}/100** (${protestingHero.mentalStateText}).

*${protestingHero.name} menangis gemetar sambil memegang senjatanya yang retak: "Jangan paksa aku masuk ke neraka itu lagi, Master! Tubuhku remuk, dan aku tidak bisa berpikir jernih... Aku akan mati di sana!"*

(Saran Peri: Kirim party beristirahat di **Iron Bar & Kitchen** atau ganti formasi dengan pahlawan yang bugar).
""".trimIndent(),
                timestamp = current.formattedTime,
                suggestedActions = listOf(
                    "Istirahat di Kitchen",
                    "Ubah Susunan Party",
                    "Tetap Paksa Masuk (Risiko Memberontak/Mati)"
                )
            )
            _gameState.value = current.copy(chatMessages = current.chatMessages + protestMsg)
            return
        }

        // Execute Expedition
        advanceTime(Random.nextInt(4, 7), 0)
        val afterTime = _gameState.value

        val isBoss = (floor % 10 == 0)
        val expMultiplier = if (isBoss) 2 else 1
        val diffMultiplier = when (current.difficulty) {
            "Abyssal" -> 3
            "Hard" -> 2
            else -> 1
        }

        val baseHeroExp = (20 + (floor * 2)) * expMultiplier * diffMultiplier
        val baseSkillExp = (10 + (floor * 2)) * expMultiplier * diffMultiplier

        val updatedHeroes = afterTime.heroes.toMutableList()
        val newGraveyard = afterTime.graveyard.toMutableList()
        val battleReports = StringBuilder()

        var someoneDied = false
        var goldLoot = Random.nextInt(200, 600) * floor
        var cmLoot = Random.nextInt(2, 5)
        var diamondLoot = if (isBoss && floor !in afterTime.clearedFloors) 10 else 0

        val partyMap = partyHeroes.map { it.id }.toSet()

        for (i in updatedHeroes.indices) {
            val h = updatedHeroes[i]
            if (h.id !in partyMap || !h.isAlive) continue

            // Combat calculation: damage taken
            val dangerRoll = Random.nextInt(1, 101)
            val hpLossPercent = when {
                dangerRoll > 95 && current.difficulty == "Abyssal" -> 1.0f // Fatal
                dangerRoll > 98 -> 1.0f // Fatal
                dangerRoll > 75 -> 0.6f
                dangerRoll > 45 -> 0.3f
                else -> 0.15f
            }

            val remainingHp = maxOf(0, (h.currentHp - (h.maxHp * hpLossPercent)).toInt())
            val isDead = (remainingHp <= 0)

            if (isDead) {
                someoneDied = true
                val deadHero = h.copy(isAlive = false, currentHp = 0)
                updatedHeroes[i] = deadHero
                newGraveyard.add(deadHero)
                battleReports.append("\n• 💀 **${h.name}** [★${h.starGrade} ${h.jobClass}]: Gugur secara brutal! Daging dan tulangnya dihancurkan monster di Lantai $floor.")
            } else {
                // Survived: calculate fatigue and stress
                val fatGain = when {
                    remainingHp < h.maxHp * 0.4f -> 60
                    remainingHp < h.maxHp * 0.7f -> 40
                    else -> 20
                }
                val strGain = when {
                    remainingHp < h.maxHp * 0.4f -> 40
                    remainingHp < h.maxHp * 0.7f -> 20
                    else -> 10
                }
                val newFatigue = minOf(100, h.fatigue + fatGain)
                var newStress = minOf(100, h.stress + strGain)

                // EXP and Level up calculation
                val totalExp = h.currentExp + baseHeroExp
                val requiredExp = h.expRequiredForNextLevel
                val isLevelUp = totalExp >= requiredExp && h.level < h.maxLevelAllowed

                val newLevel = if (isLevelUp) h.level + 1 else h.level
                val remainingExp = if (isLevelUp) totalExp - requiredExp else totalExp

                val newStr = if (isLevelUp) h.str + 2 else h.str
                val newVit = if (isLevelUp) h.vit + 2 else h.vit
                val newAgi = if (isLevelUp) h.agi + 1 else h.agi

                updatedHeroes[i] = h.copy(
                    level = newLevel,
                    currentExp = remainingExp,
                    str = newStr,
                    vit = newVit,
                    agi = newAgi,
                    currentHp = h.maxHp, // Restored upon entering Lobby
                    fatigue = newFatigue,
                    stress = newStress
                )

                val levelCheckReport = "[Level Check: ${h.name} (★${h.starGrade}) Lv.${h.level} | Req: $requiredExp EXP | Dapat: +$baseHeroExp | Hasil: ${if (isLevelUp) "NAIK ke Lv.$newLevel (+5 Stat)" else "Tetap Lv.${h.level}"}]"
                battleReports.append("\n$levelCheckReport")
            }
        }

        // Stress Chain-Reaction if someone died (+30 Stress to survivors)
        if (someoneDied) {
            for (i in updatedHeroes.indices) {
                val h = updatedHeroes[i]
                if (h.id in partyMap && h.isAlive) {
                    updatedHeroes[i] = h.copy(stress = minOf(100, h.stress + 30))
                }
            }
            battleReports.append("\n⚠️ **[STRESS SPIKE CHAIN-REACTION]**: Kematian rekan di depan mata memicu trauma massal (+30 Stress ke seluruh pahlawan yang selamat)!")
        }

        val newCleared = afterTime.clearedFloors + floor
        val nextFloor = maxOf(afterTime.towerFloorHighest, floor + 1)
        val newGold = afterTime.gold + goldLoot
        val newDiamond = afterTime.diamond + diamondLoot
        val newCM = afterTime.materials.cm + cmLoot

        val reportNarrative = """
Pertempuran di **Menara Mobius Lantai $floor** telah berakhir!

Para pahlawan bertarung dalam mode otomatis melawan gerombolan makhluk bayangan beracun dan binatang bertanduk obsidian. Darah hitam berceceran di lantai batu dingin. Gerbang kepulangan terbuka, membawa kembali mereka yang selamat ke perlindungan Lobby.
$battleReports

```text
[TOWER BATTLE REPORT - LANTAI $floor]
Status Misi     : ${if (someoneDied) "Berhasil (Dengan Korban Gugur)" else "Kemenangan Penuh"}
Loot Didapat    : +$goldLoot Gold | +$cmLoot Common Material (CM)${if (diamondLoot > 0) " | +$diamondLoot Diamond (First Boss Clear!)" else ""}
EXP Diperoleh   : +$baseHeroExp Hero EXP | +$baseSkillExp Skill & Class EXP
Sisa Saldo      : $newGold Gold | $newDiamond Diamond
```

[RNG Battle Duration: 5 Jam | RNG Drop Rate: 82 vs 50]

Peri (${afterTime.fairyName}): *"Luka fisik mereka telah tertutup rapat oleh medan sihir Lobby, namun kelelahan dan trauma pertempuran ini nyata. Segera putuskan apakah mereka butuh istirahat di Kitchen sebelum ekspedisi berikutnya."*

---
### 🧭 PILIHAN AKSI
| No | Perintah | Keterangan |
|---|---|---|
| 1 | `Istirahat di Kitchen` | Pulihkan Fatigue & Stress party |
| 2 | `Masuk Tower Lantai $nextFloor` | Lanjutkan pendakian ke lantai berikutnya |
| 3 | `Inspeksi Roster Hero` | Periksa level dan stat terbaru |
| 4 | `Cek Altar Gacha` | Ganti pahlawan yang gugur atau perkuat party |
""".trimIndent()

        val reportMsg = ChatMessage(
            sender = "PERI",
            text = reportNarrative,
            timestamp = afterTime.formattedTime,
            suggestedActions = listOf(
                "Istirahat di Kitchen",
                "Masuk Tower Lantai $nextFloor",
                "Inspeksi Roster Hero",
                "Cek Altar Gacha"
            )
        )

        val finalState = afterTime.copy(
            gold = newGold,
            diamond = newDiamond,
            materials = afterTime.materials.copy(cm = newCM),
            heroes = updatedHeroes,
            graveyard = newGraveyard,
            towerFloorCurrent = nextFloor,
            towerFloorHighest = maxOf(afterTime.towerFloorHighest, nextFloor),
            clearedFloors = newCleared,
            chatMessages = afterTime.chatMessages + reportMsg
        )

        _gameState.value = finalState
        saveGame(finalState)
        checkGameOver()
    }

    fun useMiracle(type: String) {
        val current = _gameState.value
        val (goldCost, diamondCost, desc) = when (type.uppercase()) {
            "HEAL" -> Triple(1000, 10, "Miracle [Heal]: Medan penyembuh darurat menutup luka menganga pahlawan.")
            "SHIELD" -> Triple(1500, 15, "Miracle [Shield]: Kubus pelindung menyerap gelombang serangan fatal.")
            else -> Triple(0, 5, "Miracle [Tactical Shift]: Memaksa pergantian posisi darurat di medan tempur.")
        }

        if (current.diamond < diamondCost && current.gold < goldCost) {
            val failMsg = ChatMessage(
                sender = "SYSTEM",
                text = "⚠️ Miracle gagal! Saldo tidak cukup (Butuh $diamondCost Diamond atau $goldCost Gold).",
                timestamp = current.formattedTime
            )
            _gameState.value = current.copy(chatMessages = current.chatMessages + failMsg)
            return
        }

        val newDiamond = if (current.diamond >= diamondCost) current.diamond - diamondCost else current.diamond
        val newGold = if (current.diamond < diamondCost) current.gold - goldCost else current.gold

        val ledgerText = if (current.diamond >= diamondCost) {
            "Diamond: ${current.diamond} - $diamondCost = $newDiamond Diamond"
        } else {
            "Gold: ${current.gold} - $goldCost = $newGold Gold"
        }

        val miracleMsg = ChatMessage(
            sender = "SYSTEM",
            text = """
✨ **KONTROL MASTER - MIRACLE DIAKTIFKAN!**
$desc

```text
[LEDGER TRANSAKSI MIRACLE]
$ledgerText
Tipe Keajaiban : $type
```
""".trimIndent(),
            timestamp = current.formattedTime
        )

        val updated = current.copy(
            gold = newGold,
            diamond = newDiamond,
            chatMessages = current.chatMessages + miracleMsg
        )
        _gameState.value = updated
        saveGame(updated)
    }

    fun promoteHero(heroId: String) {
        val current = _gameState.value
        val hero = current.heroes.firstOrNull { it.id == heroId } ?: return

        val costGold = when (hero.starGrade) {
            1 -> 500
            2 -> 2000
            3 -> 5000
            4 -> 15000
            5 -> 50000
            else -> 150000
        }
        val successRate = when (hero.starGrade) {
            1 -> 95
            2 -> 90
            3 -> 65
            4 -> 45
            5 -> 15
            else -> 1
        }

        if (current.gold < costGold) {
            val err = ChatMessage(
                sender = "PERI",
                text = "⚠️ Emas tidak mencukupi untuk Promosi ★${hero.starGrade} (Butuh $costGold Gold).",
                timestamp = current.formattedTime
            )
            _gameState.value = current.copy(chatMessages = current.chatMessages + err)
            return
        }

        val roll = Random.nextInt(1, 101)
        val isSuccess = roll <= successRate
        val newGold = current.gold - costGold

        val updatedHeroes = current.heroes.toMutableList()
        val updatedGraveyard = current.graveyard.toMutableList()

        val reportText = if (isSuccess) {
            val newStar = hero.starGrade + 1
            val boostedStr = (hero.str * 1.2f).toInt()
            val boostedVit = (hero.vit * 1.2f).toInt()
            val boostedAgi = (hero.agi * 1.2f).toInt()
            val boostedInt = (hero.intStat * 1.2f).toInt()

            updatedHeroes.remove(hero)
            updatedHeroes.add(
                hero.copy(
                    starGrade = newStar,
                    level = 1,
                    currentExp = 0,
                    str = boostedStr,
                    vit = boostedVit,
                    agi = boostedAgi,
                    intStat = boostedInt
                )
            )

            """
✨ **PROMOSI BERHASIL!**
Cahaya ilahi menyelimuti tubuh ${hero.name}. Batas kemampuannya hancur dan terlahir kembali dengan bintang yang lebih megah!

```text
[HASIL PROMOSI]
Hero         : ${hero.name}
Grade Baru   : ★$newStar (Reset ke Lv.1 dengan +20% Bonus Stat Permanen)
Biaya        : Gold: ${current.gold} - $costGold = $newGold Gold
```
[RNG Promotion: $roll vs Target Rate $successRate - SUKSES]
""".trimIndent()
        } else {
            updatedHeroes.remove(hero)
            val deadHero = hero.copy(isAlive = false, currentHp = 0)
            updatedGraveyard.add(deadHero)

            """
💀 **GAGAL PROMOSI - KEMATIAN BRUTAL PERMANEN!**
Tubuh ${hero.name} membengkak secara tidak normal akibat ketidakmampuan menahan luapan energi altar. Urat-urat menonjol ungu kehitaman, kulitnya retak bagai kaca. Darah menyembur dari seluruh pori-pori dan rongga mata sebelum tubuhnya hancur meledak!

```text
[HASIL PROMOSI]
Hero         : ${hero.name} (Gugur Permanen)
Status       : Mati Meledak di Altar
Biaya        : Gold: ${current.gold} - $costGold = $newGold Gold
```
[RNG Promotion: $roll vs Target Rate $successRate - GAGAL]
""".trimIndent()
        }

        val promoMsg = ChatMessage(
            sender = "PERI",
            text = reportText,
            timestamp = current.formattedTime,
            isDiceRoll = true
        )

        val updated = current.copy(
            gold = newGold,
            heroes = updatedHeroes,
            graveyard = updatedGraveyard,
            chatMessages = current.chatMessages + promoMsg
        )
        _gameState.value = updated
        saveGame(updated)
        checkGameOver()
    }

    fun updateHeroTag(heroId: String, tag: HeroTag) {
        val current = _gameState.value
        val updatedHeroes = current.heroes.map {
            if (it.id == heroId) it.copy(tag = tag) else it
        }
        val updated = current.copy(heroes = updatedHeroes)
        _gameState.value = updated
        saveGame(updated)
    }

    fun scanPvPRadar() {
        val targets = listOf(
            PvPEnemyTarget(
                id = "pvp_1",
                masterName = "Lord Valerius",
                highestFloor = 38,
                starComposition = "★4 Knight, ★4 Mage, ★3 Cleric",
                hiddenType = "Fasad/Normal",
                rewardSummary = "1 Diamond + 50 EXP (Kaku, skill rendah)"
            ),
            PvPEnemyTarget(
                id = "pvp_2",
                masterName = "Guildmaster Drake",
                highestFloor = 22,
                starComposition = "★3 Warrior, ★3 Berserker, ★3 Rogue",
                hiddenType = "Standar/Hard",
                rewardSummary = "2 Diamond + 100 EXP (Taktik lumayan)"
            ),
            PvPEnemyTarget(
                id = "pvp_3",
                masterName = "Anonym (Bloodstained)",
                highestFloor = 14,
                starComposition = "★2 Assassin [MAX], ★1 Fighter [MAX]",
                hiddenType = "Anomali/Abyssal",
                rewardSummary = "5 Diamond + 250 EXP + 1x RM (VETERAN MEMATIKAN!)"
            )
        )

        val radarText = """
📡 **RADAR BLOOD ARENA (HANGAR & DOCK) AKTIF**
Sinyal distorsi dimensi mendeteksi 3 Lord/Master lain di sekitar koordinat Mobius:

```text
1. Target: Lord Valerius
   Lantai Tertinggi: Lantai 38
   Komposisi Hero  : ★4 Knight, ★4 Mage, ★3 Cleric
   Estimasi Reward : 1-2 Diamond + 50 EXP

2. Target: Guildmaster Drake
   Lantai Tertinggi: Lantai 22
   Komposisi Hero  : ★3 Warrior, ★3 Berserker, ★3 Rogue
   Estimasi Reward : 2-3 Diamond + 100 EXP

3. Target: Anonym (Bloodstained Veteran)
   Lantai Tertinggi: Lantai 14
   Komposisi Hero  : ★2 Assassin [MAX], ★1 Fighter [MAX]
   Estimasi Reward : 5 Diamond + 250 EXP + 1x Rare Material (RM)
```

⚠️ Peringatan Peri: Tingkat kesulitan akun tidak pernah ditampilkan oleh sistem. Waspadalah terhadap hero bintang rendah dengan skill maksimal!
""".trimIndent()

        val radarMsg = ChatMessage(
            sender = "PERI",
            text = radarText,
            timestamp = _gameState.value.formattedTime,
            suggestedActions = listOf(
                "Tantang Target 1",
                "Tantang Target 2",
                "Tantang Target 3 (Berisiko Tinggi)",
                "Kembali ke Lobby"
            )
        )

        val updated = _gameState.value.copy(
            pvpTargets = targets,
            chatMessages = _gameState.value.chatMessages + radarMsg
        )
        _gameState.value = updated
        saveGame(updated)
    }

    fun sendUserPrompt(prompt: String) {
        val current = _gameState.value
        val userMsg = ChatMessage(
            sender = "MASTER",
            text = prompt,
            timestamp = current.formattedTime
        )

        val withUser = current.copy(chatMessages = current.chatMessages + userMsg)
        _gameState.value = withUser
        _isLoadingStory.value = true

        scope.launch {
            val responseResult = geminiApi.generateStoryResponse(
                prompt = prompt,
                gameState = withUser,
                customApiKey = _customApiKey.value,
                selectedModel = _selectedModel.value
            )

            val text = responseResult.getOrElse {
                LocalMasterEngine.generateResponse(prompt, withUser)
            }

            val periMsg = ChatMessage(
                sender = "PERI",
                text = text,
                timestamp = withUser.formattedTime,
                suggestedActions = listOf(
                    "Masuk Tower Lantai ${withUser.towerFloorCurrent}",
                    "Istirahat di Kitchen",
                    "Cek Altar Gacha",
                    "Inspeksi Roster Hero"
                )
            )

            val finalState = withUser.copy(chatMessages = withUser.chatMessages + periMsg)
            _gameState.value = finalState
            saveGame(finalState)
            _isLoadingStory.value = false
        }
    }

    private fun checkGameOver() {
        val current = _gameState.value
        if (current.livingHeroesCount == 0 && !current.canAffordSummon) {
            val shutdownText = """
🔴 [SYSTEM SHUTDOWN - GAME OVER]
Semua pahlawan telah gugur di dimensi Mobius. Saldo Gold (${current.gold}) dan Diamond (${current.diamond}) tidak lagi mencukupi untuk memanggil jiwa baru ke Altar.

Sistem telah hancur. Silakan mulai sesi baru untuk inisiasi New Game.
""".trimIndent()

            val shutdownMsg = ChatMessage(
                sender = "PERI",
                text = shutdownText,
                timestamp = current.formattedTime,
                isSystemShutdown = true,
                suggestedActions = listOf("Mulai New Game")
            )

            _gameState.value = current.copy(
                isGameOver = true,
                chatMessages = current.chatMessages + shutdownMsg
            )
            saveGame(_gameState.value)
        }
    }

    private fun saveGame(state: GameState) {
        try {
            val json = JSONObject().apply {
                put("masterName", state.masterName)
                put("lobbyName", state.lobbyName)
                put("fairyName", state.fairyName)
                put("difficulty", state.difficulty)
                put("inGameDay", state.inGameDay)
                put("inGameHour", state.inGameHour)
                put("inGameMinute", state.inGameMinute)
                put("gold", state.gold)
                put("diamond", state.diamond)
                put("towerFloorCurrent", state.towerFloorCurrent)
                put("towerFloorHighest", state.towerFloorHighest)
                put("isGameOver", state.isGameOver)
            }
            prefs.edit().putString("saved_game_state", json.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSavedGame() {
        val savedStr = prefs.getString("saved_game_state", null)
        if (savedStr != null) {
            try {
                val json = JSONObject(savedStr)
                startNewGame(
                    masterName = json.optString("masterName", "Master"),
                    lobbyName = json.optString("lobbyName", "Valhalla Citadel"),
                    fairyName = json.optString("fairyName", "Peri Navi"),
                    difficulty = json.optString("difficulty", "Normal")
                )
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // If not found, init clean new game
        startNewGame()
    }
}
