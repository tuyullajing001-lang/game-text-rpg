package com.example

enum class Rarity(val label: String, val hexColor: Long) {
    COMMON("Common", 0xFF94A3B8),
    RARE("Rare", 0xFF38BDF8),
    EPIC("Epic", 0xFFC084FC),
    LEGENDARY("Legendary", 0xFFFACC15),
    MYTHIC("Mythic", 0xFFF43F5E)
}

data class ItemData(
    val id: String,
    val name: String,
    val rarity: Rarity = Rarity.COMMON,
    val slotType: String = "Weapon", // Weapon, Armor, Accessory, Consumable, Material
    val statsText: String = "",
    val effectsText: String = "",
    val description: String = "",
    val isLocked: Boolean = true
)

data class FloorData(
    val floorNumber: Int,
    val title: String,
    val objectiveType: String = "Bebas",
    val clearCondition: String = "Selesaikan Objektif Utama",
    val failCondition: String = "Seluruh Party Gugur (HP 0)",
    val terrainHazard: String = "Normal",
    val enemyComposition: String = "Belum Diketahui",
    val timeLimitText: String = "4-6 Jam",
    val isDiscovered: Boolean = false,
    val isBossFloor: Boolean = false
)

data class FacilityData(
    val name: String,
    val level: Int = 1,
    val assignedWorkers: List<String> = emptyList(),
    val maxCapacity: Int = 2,
    val description: String
)

data class HeroData(
    val id: String,
    val name: String,
    val race: String = "Manusia",
    val gender: String = "Laki-laki", // Laki-laki / Perempuan
    val age: Int = 20,
    var stars: Int = 1,
    var level: Int = 1,
    var exp: Int = 0,
    var jobClass: String = "Novice",
    var classExp: Int = 0,
    var tag: String = "[NONE]",
    
    // Status Vitalitas & Beban
    var currentHp: Int = -1,
    var fatigue: Int = 0, // 0 - 100
    var stress: Int = 0,  // 0 - 100
    var isAlive: Boolean = true,
    var causeOfDeath: String = "",
    
    // Status Reproduksi Private Chamber
    var isPregnant: Boolean = false,
    var pregnancyPartner: String = "",
    
    // 6 Stat Mentah Dasar
    var str: Int = 5,
    var vit: Int = 5,
    var intStat: Int = 5,
    var agi: Int = 5,
    var dex: Int = 5,
    var luck: Int = 5,
    
    // Limit Break Persentase Ras (Godot Core - Max 100%)
    var bonusRasBawaanPercent: Double = 5.0,
    var bonusRasEkstraPercent: Double = 0.0,
    
    // Equipment Terpasang
    var weapon: String = "Tanpa Senjata",
    var armor: String = "Pakaian Biasa",
    var accessory: String = "Tidak Ada",
    
    val specialTraits: List<String> = emptyList(),
    var totalKill: Int = 0,
    var bossKill: Int = 0,
    var highestFloor: Int = 1
) {
    val totalRacialBonus: Double get() = (bonusRasBawaanPercent + bonusRasEkstraPercent).coerceAtMost(100.0)

    val maxHp: Int get() = 100 + (vit * 100)
    val maxStamina: Int get() = 50 + (str * 3) + (vit * 2)
    val maxMana: Int get() = 50 + (intStat * 4)
    val maxStress: Int get() = 100 + (intStat * 3) + (vit * 1)

    val physicalAtk: Int get() = (str * 5) + (dex * 1) + (agi * 1)
    val magicAtk: Int get() = (intStat * 5) + (dex * 2)
    val pDef: Int get() = vit * 3
    val mDef: Int get() = (intStat * 2) + (vit * 1)
    val critRate: Double get() = (dex * 0.1) + (luck * 1.0)
    val critDmg: Double get() = 150.0
    val accuracy: Double get() = 100.0 + (dex * 0.5) + (luck * 0.8)
    val dodgeRate: Double get() = agi * 0.1
    val combatSpeed: Int get() = agi * 1

    val maxExpNeeded: Int get() = level * 10 * stars

    val maxLevelForCurrentStar: Int get() = when (stars) {
        1 -> 10
        2 -> 20
        3 -> 30
        4 -> 50
        5 -> 70
        6 -> 85
        else -> 100
    }

    val isMaxLevel: Boolean get() = level >= maxLevelForCurrentStar

    init {
        if (currentHp <= 0 || currentHp > maxHp) {
            currentHp = maxHp
        }
    }
}

data class WalletData(
    var gold: Int = 5000,
    var diamond: Int = 50,
    var cm: Int = 10,
    var um: Int = 3,
    var rm: Int = 0,
    var em: Int = 0,
    var lm: Int = 0
)

data class ChatMessage(
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuickAction(
    val label: String,
    val command: String
)
