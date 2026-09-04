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
    val slotType: String = "Weapon", // Weapon, Armor, Accessory, Consumable
    val statsText: String = "",
    val effectsText: String = "",
    val description: String = "",
    val isLocked: Boolean = true
)

data class HeroData(
    val id: String,
    val name: String,
    val race: String = "Manusia",
    val gender: String = "Laki-laki",
    val age: Int = 20,
    val stars: Int = 2,
    var level: Int = 1,
    var exp: Int = 0,
    var jobClass: String = "Novice",
    var tag: String = "[NONE]",
    
    // Status Dinamis
    var currentHp: Int = 1000,
    var maxHp: Int = 1000,
    var fatigue: Int = 0, // 0 - 100
    var stress: Int = 0,  // 0 - 100
    
    // Stat Mentah Dinamis
    var str: Int = 6,
    var vit: Int = 6,
    var intStat: Int = 6,
    var agi: Int = 6,
    var dex: Int = 6,
    var luck: Int = 6,
    
    // Equipment Terpasang
    var weapon: String = "Tanpa Senjata",
    var armor: String = "Pakaian Biasa",
    var accessory: String = "Tidak Ada",
    
    // Trait Khusus
    val specialTraits: List<String> = emptyList(),
    
    // Achievement Tracker
    var totalKill: Int = 0,
    var bossKill: Int = 0,
    var highestFloor: Int = 1
) {
    // Rumus Stat Tempur Godot Engine (Terhitung Otomatis)
    val physicalAtk: Int get() = (str * 5) + (dex * 1) + (agi * 1)
    val magicAtk: Int get() = (intStat * 5) + (dex * 2)
    val pDef: Int get() = vit * 3
    val mDef: Int get() = (intStat * 2) + (vit * 1)
    val critRate: Double get() = (dex * 0.1) + (luck * 1.0)
    val maxExpNeeded: Int get() = level * 10 * stars
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
