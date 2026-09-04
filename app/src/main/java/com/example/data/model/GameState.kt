package com.example.data.model

data class Materials(
    val cm: Int = 10,  // Common Material
    val um: Int = 3,   // Uncommon Material
    val rm: Int = 0,   // Rare Material
    val em: Int = 0,   // Epic Material
    val lm: Int = 0,   // Legendary Material
    val mm: Int = 0    // Mythic Material
)

data class PvPEnemyTarget(
    val id: String,
    val masterName: String,
    val highestFloor: Int,
    val starComposition: String,
    val hiddenType: String, // "Fasad/Normal", "Standar/Hard", "Anomali/Abyssal"
    val rewardSummary: String
)

data class GameState(
    val masterName: String = "Master",
    val lobbyName: String = "Valhalla Citadel",
    val fairyName: String = "Peri Navi",
    val difficulty: String = "Normal", // "Normal", "Hard", "Abyssal"
    
    // In-Game Time
    val inGameDay: Int = 1,
    val inGameHour: Int = 8,
    val inGameMinute: Int = 0,
    
    // Currencies
    val gold: Int = 5000,
    val diamond: Int = 50,
    val tutorialTickets: Int = 5,
    val materials: Materials = Materials(),
    val soulStabilizers: Int = 1,
    
    // Tower & Expedition
    val towerFloorCurrent: Int = 1,
    val towerFloorHighest: Int = 1,
    val clearedFloors: Set<Int> = emptySet(),
    val partyIds: List<String> = emptyList(), // Up to 5 hero IDs
    val inExpedition: Boolean = false,
    val currentMissionObjective: String = "Annihilation",
    
    // Heroes
    val heroes: List<Hero> = emptyList(),
    val graveyard: List<Hero> = emptyList(),
    
    // Status
    val socialPressure: Int = 10, // 0 to 100
    val lobbyMorale: Int = 90,     // 0 to 100
    val isGameOver: Boolean = false,
    
    // PvP Radar targets
    val pvpTargets: List<PvPEnemyTarget> = emptyList(),
    
    // Chat History
    val chatMessages: List<ChatMessage> = emptyList()
) {
    val formattedTime: String
        get() = String.format("📅 In-Game Time: Hari ke-%d | Jam %02d:%02d", inGameDay, inGameHour, inGameMinute)

    val activePartyHeroes: List<Hero>
        get() = heroes.filter { it.id in partyIds && it.isAlive }

    val livingHeroesCount: Int
        get() = heroes.count { it.isAlive }

    val canAffordSummon: Boolean
        get() = gold >= 1000 || diamond >= 10 || tutorialTickets > 0
}
