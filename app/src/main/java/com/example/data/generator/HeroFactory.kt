package com.example.data.generator

import com.example.data.model.Hero
import com.example.data.model.HeroSkill
import com.example.data.model.HeroTag
import com.example.data.model.CombatPosition
import kotlin.random.Random

data class GachaResult(
    val hero: Hero,
    val diceNumber: Int,
    val rollType: String,
    val rangeDesc: String
)

object HeroFactory {
    val RACES = listOf("Manusia", "Elf", "Dwarf", "Beastkin", "Demon", "Dragonkin")
    
    val CLASSES = listOf(
        "Fighter", "Warrior", "Knight", "Mage", "Cleric", "Rogue", "Assassin", "Berserker"
    )

    fun rollGacha(type: String): GachaResult {
        val dice = Random.nextInt(1, 1001)
        val (star, rangeDesc) = when (type.lowercase()) {
            "diamond" -> when {
                dice <= 850 -> 2 to "1 - 850 (★2)"
                dice <= 950 -> 3 to "851 - 950 (★3)"
                dice <= 990 -> 4 to "951 - 990 (★4)"
                dice <= 999 -> 5 to "991 - 999 (★5)"
                else -> 6 to "1000 (★6)"
            }
            "event" -> when {
                dice <= 900 -> 3 to "1 - 900 (★3)"
                dice <= 940 -> 4 to "901 - 940 (★4)"
                dice <= 990 -> 5 to "941 - 990 (★5)"
                dice <= 999 -> 6 to "991 - 999 (★6)"
                else -> 7 to "1000 (★7)"
            }
            else -> when { // "gold"
                dice <= 860 -> 1 to "1 - 860 (★1)"
                dice <= 960 -> 2 to "861 - 960 (★2)"
                dice <= 990 -> 3 to "961 - 990 (★3)"
                dice <= 999 -> 4 to "991 - 999 (★4)"
                else -> 5 to "1000 (★5)"
            }
        }
        val hero = createRandomHero(starGrade = star)
        return GachaResult(hero, dice, type.uppercase(), rangeDesc)
    }

    fun createRandomHero(
        starGrade: Int,
        customName: String? = null,
        isCustom: Boolean = false
    ): Hero {
        val isMale = Random.nextBoolean()
        val gender = if (isMale) "Laki-laki" else "Perempuan"
        val name = customName ?: NameGenerator.generateName(isMale)
        val race = RACES.random()
        val job = CLASSES.random()
        val age = Random.nextInt(16, 45)
        
        // Stat point pool
        val statPool = calculateStatPool(starGrade)
        
        // Distribution
        val weights = listOf(
            Random.nextInt(10, 40), // str
            Random.nextInt(10, 40), // vit
            Random.nextInt(10, 40), // agi
            Random.nextInt(10, 40), // int
            Random.nextInt(10, 40), // dex
            Random.nextInt(10, 40)  // luck
        )
        val weightSum = weights.sum()
        
        val str = maxOf(5, (statPool * weights[0]) / weightSum)
        val vit = maxOf(5, (statPool * weights[1]) / weightSum)
        val agi = maxOf(5, (statPool * weights[2]) / weightSum)
        val intStat = maxOf(5, (statPool * weights[3]) / weightSum)
        val dex = maxOf(5, (statPool * weights[4]) / weightSum)
        val luck = maxOf(5, (statPool * weights[5]) / weightSum)
        
        val maxHp = 100 + vit * 100
        val physicalAtk = str * 5 + dex * 1 + agi * 1
        val magicAtk = dex * 2 + intStat * 5
        val physicalDef = vit * 3
        val magicDef = intStat * 2 + vit * 1
        val critRate = (5.0f + dex * 0.1f + luck * 0.5f).coerceAtMost(75f)
        val dodgeRate = (5.0f + agi * 0.1f).coerceAtMost(60f)
        
        val position = when (job) {
            "Fighter", "Knight", "Berserker" -> CombatPosition.FRONTLINE
            "Warrior", "Rogue", "Assassin" -> CombatPosition.MIDLINE
            else -> CombatPosition.BACKLINE
        }

        val skills = generateSkillsForClass(job, starGrade)
        
        val id = "hero_" + System.currentTimeMillis() + "_" + Random.nextInt(1000, 9999)
        
        return Hero(
            id = id,
            name = name,
            gender = gender,
            race = race,
            age = age,
            starGrade = starGrade,
            level = 1,
            currentExp = 0,
            jobClass = job,
            jobTier = "F",
            classExp = 0,
            tag = HeroTag.NONE,
            position = position,
            str = str,
            vit = vit,
            agi = agi,
            intStat = intStat,
            dex = dex,
            luck = luck,
            maxHp = maxHp,
            currentHp = maxHp,
            fatigue = 0,
            stress = 0,
            physicalAtk = physicalAtk,
            magicAtk = magicAtk,
            physicalDef = physicalDef,
            magicDef = magicDef,
            critRate = critRate,
            dodgeRate = dodgeRate,
            skills = skills,
            isCustomHero = isCustom
        )
    }

    private fun calculateStatPool(star: Int): Int {
        var total = 30
        if (star >= 2) total += 80 + Random.nextInt(20, 100)
        if (star >= 3) total += 150 + Random.nextInt(30, 140)
        if (star >= 4) total += 220 + Random.nextInt(40, 140)
        if (star >= 5) total += 350 + Random.nextInt(70, 140)
        if (star >= 6) total += 500 + Random.nextInt(140, 250)
        if (star >= 7) total += 900 + Random.nextInt(300, 600)
        return total
    }

    private fun generateSkillsForClass(job: String, star: Int): List<HeroSkill> {
        val skillPool = when (job) {
            "Mage" -> listOf(
                HeroSkill("s_fireball", "Fireball", "⚪", 1, false, "Melempar bola api pekat berdaya hancur tinggi."),
                HeroSkill("s_frost", "Frost Nova", "🔵", 1, false, "Membekukan area sekitar musuh."),
                HeroSkill("s_arcane", "Arcane Disruption", "🟣", 1, false, "Ledakan mana murni penghancur perisai.")
            )
            "Cleric" -> listOf(
                HeroSkill("s_heal", "Holy Light", "⚪", 1, false, "Menutup luka dan memulihkan HP rekan."),
                HeroSkill("s_sanctuary", "Sanctuary Ward", "🔵", 1, false, "Mengurangi damage masuk ke party.")
            )
            "Rogue", "Assassin" -> listOf(
                HeroSkill("s_shadow", "Shadow Step", "⚪", 1, false, "Menghilang dan menusuk titik lemah dari belakang."),
                HeroSkill("s_venom", "Viper Strike", "🔵", 1, false, "Serangan beracun mematikan.")
            )
            "Knight" -> listOf(
                HeroSkill("s_shield", "Iron Fortress", "⚪", 1, false, "Memblokir 80% damage fisik."),
                HeroSkill("s_taunt", "Provocation Roar", "🔵", 1, false, "Menarik perhatian monster ke dirinya.")
            )
            else -> listOf(
                HeroSkill("s_slash", "Heavy Slash", "⚪", 1, false, "Tebasan bertenaga membelah zirah."),
                HeroSkill("s_whirl", "Whirlwind Strike", "🔵", 1, false, "Serangan memutar menghantam banyak musuh.")
            )
        }
        val slots = when {
            star <= 2 -> 1
            star <= 4 -> 2
            else -> 3
        }
        return skillPool.take(slots)
    }
}
