package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Hero
import com.example.data.model.HeroTag
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroesScreen(
    heroes: List<Hero>,
    graveyard: List<Hero>,
    gold: Int,
    onPromoteHero: (String) -> Unit,
    onUpdateTag: (String, HeroTag) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTagFilter by remember { mutableStateOf<HeroTag?>(null) }
    var showGraveyard by remember { mutableStateOf(false) }
    var inspectedHero by remember { mutableStateOf<Hero?>(null) }
    var heroToPromote by remember { mutableStateOf<Hero?>(null) }

    val displayedHeroes = remember(heroes, graveyard, selectedTagFilter, showGraveyard) {
        if (showGraveyard) {
            graveyard
        } else {
            if (selectedTagFilter == null) {
                heroes.filter { it.isAlive }
            } else {
                heroes.filter { it.isAlive && it.tag == selectedTagFilter }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tag Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = !showGraveyard && selectedTagFilter == null,
                onClick = {
                    showGraveyard = false
                    selectedTagFilter = null
                },
                label = { Text("Semua (${heroes.count { it.isAlive }})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ArcaneGold,
                    selectedLabelColor = DarkBackground
                )
            )

            HeroTag.values().forEach { tag ->
                FilterChip(
                    selected = !showGraveyard && selectedTagFilter == tag,
                    onClick = {
                        showGraveyard = false
                        selectedTagFilter = tag
                    },
                    label = { Text("[${tag.name}]", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PortalCyan,
                        selectedLabelColor = DarkBackground
                    )
                )
            }

            FilterChip(
                selected = showGraveyard,
                onClick = { showGraveyard = true },
                label = { Text("💀 Gugur (${graveyard.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BloodCrimson,
                    selectedLabelColor = TextPrimary
                )
            )
        }

        // Hero List
        if (displayedHeroes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showGraveyard) "Belum ada pahlawan yang gugur di dimensi ini." else "Tidak ada pahlawan pada kategori ini.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hero_roster_list")
            ) {
                items(displayedHeroes) { hero ->
                    HeroCard(
                        hero = hero,
                        onClick = { inspectedHero = hero }
                    )
                }
            }
        }
    }

    // Hero Inspection Modal BottomSheet
    inspectedHero?.let { hero ->
        ModalBottomSheet(
            onDismissRequest = { inspectedHero = null },
            containerColor = DarkSurface
        ) {
            HeroDetailSheet(
                hero = hero,
                gold = gold,
                onPromote = {
                    heroToPromote = hero
                },
                onUpdateTag = { newTag ->
                    onUpdateTag(hero.id, newTag)
                    inspectedHero = hero.copy(tag = newTag)
                },
                onClose = { inspectedHero = null }
            )
        }
    }

    // Promotion Warning Dialog
    heroToPromote?.let { hero ->
        val successRate = when (hero.starGrade) {
            1 -> 95
            2 -> 90
            3 -> 65
            4 -> 45
            5 -> 15
            else -> 1
        }
        val cost = when (hero.starGrade) {
            1 -> 500
            2 -> 2000
            3 -> 5000
            4 -> 15000
            5 -> 50000
            else -> 150000
        }

        AlertDialog(
            onDismissRequest = { heroToPromote = null },
            title = {
                Text(
                    text = "⚠️ PERINGATAN SISTEM PROMOSI",
                    color = BloodCrimson,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Text(
                    text = "Promosi ★${hero.starGrade} ke ★${hero.starGrade + 1} untuk ${hero.name} memiliki tingkat keberhasilan $successRate% dengan biaya $cost Gold.\n\nJika GAGAL, pahlawan akan mengalami kelebihan energi internal dan MATI PERMANEN SECARA BRUTAL di Altar!\n\nApakah Master yakin ingin melanjutkan?",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = hero.id
                        heroToPromote = null
                        inspectedHero = null
                        onPromoteHero(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson)
                ) {
                    Text("Ya, Jalankan Promosi!", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { heroToPromote = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }
}

@Composable
fun HeroCard(hero: Hero, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hero.isAlive) getStarColor(hero.starGrade).copy(alpha = 0.4f) else BloodCrimson.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth().testTag("hero_card_${hero.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★${hero.starGrade}",
                        color = getStarColor(hero.starGrade),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = hero.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (hero.isCustomHero) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "👑", fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "[${hero.tag.name}]",
                        color = when (hero.tag) {
                            HeroTag.CORE -> ArcaneGold
                            HeroTag.OFFICER -> PortalCyan
                            HeroTag.LABORER -> Color(0xFF4ADE80)
                            HeroTag.SCRAP -> BloodCrimson
                            else -> TextSecondary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${hero.race} (${hero.gender}) | Class: ${hero.jobClass} (${hero.jobTier}) | Lv.${hero.level}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Health, Fatigue, Stress Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBar(
                    label = "HP",
                    value = hero.currentHp,
                    max = hero.maxHp,
                    color = PortalCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricBar(
                    label = "Fatigue",
                    value = hero.fatigue,
                    max = 100,
                    color = if (hero.fatigue >= 60) BloodCrimson else ArcaneGold,
                    modifier = Modifier.weight(1f)
                )
                MetricBar(
                    label = "Stress",
                    value = hero.stress,
                    max = 100,
                    color = if (hero.stress >= 60) BloodCrimson else RunicPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricBar(
    label: String,
    value: Int,
    max: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextMuted, fontSize = 9.sp)
            Text(text = "$value", color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        val fraction = (value.toFloat() / max.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { fraction },
            color = color,
            trackColor = DarkSurfaceHighlight,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun HeroDetailSheet(
    hero: Hero,
    gold: Int,
    onPromote: () -> Unit,
    onUpdateTag: (HeroTag) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★${hero.starGrade} ${hero.name}",
                        color = getStarColor(hero.starGrade),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (hero.isCustomHero) {
                        Text(text = " (Custom Trait 90% Survival)", color = ArcaneGold, fontSize = 11.sp)
                    }
                }
                Text(
                    text = "${hero.race} • ${hero.gender} • ${hero.age} Tahun • ${hero.jobClass} [Tier ${hero.jobTier}]",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Level & EXP Check
        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Level ${hero.level} / Max Lv.${hero.maxLevelAllowed}",
                        color = ArcaneGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${hero.currentExp} / ${hero.expRequiredForNextLevel} EXP",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (hero.currentExp.toFloat() / hero.expRequiredForNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    color = ArcaneGold,
                    trackColor = DarkSurfaceHighlight,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stats Matrix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatBox("STR", hero.str.toString())
            StatBox("VIT", hero.vit.toString())
            StatBox("AGI", hero.agi.toString())
            StatBox("INT", hero.intStat.toString())
            StatBox("DEX", hero.dex.toString())
            StatBox("LUK", hero.luck.toString())
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Combat Numbers
        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "P.ATK", color = TextMuted, fontSize = 10.sp)
                    Text(text = "${hero.physicalAtk}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "M.ATK", color = TextMuted, fontSize = 10.sp)
                    Text(text = "${hero.magicAtk}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "P.DEF", color = TextMuted, fontSize = 10.sp)
                    Text(text = "${hero.physicalDef}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "M.DEF", color = TextMuted, fontSize = 10.sp)
                    Text(text = "${hero.magicDef}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "CRIT", color = TextMuted, fontSize = 10.sp)
                    Text(text = "${hero.critRate.toInt()}%", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Skills List
        Text(text = "SKILL HERO (${hero.skills.size}/${hero.maxSkillSlots})", color = ArcaneGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        hero.skills.forEach { skill ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${skill.rarityEmoji} ${skill.name} Lv.${skill.level}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = skill.description, color = TextMuted, fontSize = 11.sp, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tag Selector
        Text(text = "GANTI LABEL / TAG:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HeroTag.values().forEach { t ->
                AssistChip(
                    onClick = { onUpdateTag(t) },
                    label = { Text(t.name, fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (hero.tag == t) ArcaneGold else DarkSurfaceVariant,
                        labelColor = if (hero.tag == t) DarkBackground else TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Promote Action
        if (hero.isAlive && hero.starGrade < 7) {
            Button(
                onClick = onPromote,
                colors = ButtonDefaults.buttonColors(containerColor = ArcaneGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_promote_${hero.id}")
            ) {
                Text(
                    text = "Promosi Naik Bintang (★${hero.starGrade} → ★${hero.starGrade + 1})",
                    color = DarkBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.width(48.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = ArcaneGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun getStarColor(star: Int): Color = when (star) {
    1 -> Star1Color
    2 -> Star2Color
    3 -> Star3Color
    4 -> Star4Color
    5 -> Star5Color
    6 -> Star6Color
    else -> Star7Color
}
