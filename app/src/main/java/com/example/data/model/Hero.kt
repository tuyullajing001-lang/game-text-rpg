package com.example.data.model

enum class HeroTag {
    NONE,
    CORE,
    OFFICER,
    LABORER,
    SCRAP
}

enum class CombatPosition {
    FRONTLINE,
    MIDLINE,
    BACKLINE
}

enum class FacilityType(val displayName: String) {
    KITCHEN("Iron Bar & Kitchen"),
    BLACKSMITH("Blacksmith"),
    ALCHEMIST("Alchemist Lab"),
    RESEARCH("Central Research Lab"),
    HANGAR("Hangar & PvP Radar")
}

data class HeroSkill(
    val id: String,
    val name: String,
    val rarityEmoji: String, // ⚪, 🔵, 🟣, 🟡
    val level: Int = 1,
    val isMax: Boolean = false,
    val description: String = ""
)

data class Hero(
    val id: String,
    val name: String,
    val gender: String, // "Laki-laki" or "Perempuan"
    val race: String,   // "Manusia", "Elf", "Dwarf", "Beastkin", "Demon", "Dragonkin"
    val age: Int,
    val starGrade: Int, // 1 to 7
    val level: Int = 1,
    val currentExp: Int = 0,
    val jobClass: String = "Fighter",
    val jobTier: String = "F",
    val classExp: Int = 0,
    val tag: HeroTag = HeroTag.NONE,
    val position: CombatPosition = CombatPosition.FRONTLINE,
    val assignedFacility: FacilityType? = null,
    val isAlive: Boolean = true,
    
    // Base Stats
    val str: Int = 10,
    val vit: Int = 10,
    val agi: Int = 10,
    val intStat: Int = 10,
    val dex: Int = 10,
    val luck: Int = 10,
    
    // Physical & Mental Health
    val maxHp: Int = 1000,
    val currentHp: Int = 1000,
    val fatigue: Int = 0, // 0 to 100
    val stress: Int = 0,  // 0 to 100
    
    // Derived Combat Stats
    val physicalAtk: Int = 50,
    val magicAtk: Int = 20,
    val physicalDef: Int = 30,
    val magicDef: Int = 10,
    val critRate: Float = 5.0f,
    val dodgeRate: Float = 5.0f,
    
    // Skills
    val skills: List<HeroSkill> = emptyList(),
    
    // Custom Hero Trait
    val isCustomHero: Boolean = false
) {
    val expRequiredForNextLevel: Int
        get() = level * 10 * starGrade

    val maxLevelAllowed: Int
        get() = when (starGrade) {
            1 -> 10
            2 -> 20
            3 -> 30
            4 -> 50
            5 -> 70
            6 -> 85
            else -> 100
        }

    val maxSkillSlots: Int
        get() = when {
            starGrade <= 2 -> 2
            starGrade <= 4 -> 3
            else -> 4
        }

    val conditionText: String
        get() = when {
            !isAlive -> "Gugur (Perma-Death)"
            currentHp <= maxHp * 0.39f -> "Luka Kritis"
            currentHp <= maxHp * 0.69f -> "Luka Sedang"
            fatigue >= 81 -> "Kelelahan Ekstrem"
            fatigue >= 61 -> "Sangat Lelah"
            fatigue >= 31 -> "Lelah"
            else -> "Bugar"
        }

    val mentalStateText: String
        get() = when {
            stress >= 81 -> "Gila / Breakdown"
            stress >= 61 -> "Depresi / Trauma"
            stress >= 31 -> "Cemas / Waspada"
            else -> "Stabil / Tenang"
        }

    val willRefuseOrder: Boolean
        get() = fatigue >= 60 || stress >= 60

    val statTotal: Int
        get() = str + vit + agi + intStat + dex + luck
}
