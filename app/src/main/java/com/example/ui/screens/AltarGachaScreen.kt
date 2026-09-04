package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Hero
import com.example.ui.theme.*

@Composable
fun AltarGachaScreen(
    gold: Int,
    diamond: Int,
    onSummon: (type: String, count: Int) -> List<Hero>,
    modifier: Modifier = Modifier
) {
    var lastSummonedHeroes by remember { mutableStateOf<List<Hero>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Altar Hero Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.altar_banner_1788491676636),
                contentDescription = "Altar Pemanggilan Mobius",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DarkBackground.copy(alpha = 0.85f), DarkBackground)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "⛩️ ALTAR PEMANGGILAN MOBIUS",
                    color = ArcaneGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Merobek jalinan ruang untuk memanggil jiwa pejuang melintasi dimensi",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Wallet Display
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "SALDO GOLD", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$gold Gold", color = ArcaneGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(DarkSurfaceVariant)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💎", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "SALDO DIAMOND", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$diamond Diamond", color = PortalCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Gacha Options
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Gold Summon Card
            GachaOptionCard(
                title = "GOLD SUMMON",
                tagline = "Tarikan jiwa fana (Tingkat keputusasaan tinggi)",
                rates = "★1 (86%) • ★2 (10%) • ★3 (3%) • ★4 (0.9%) • ★5 (0.1%)",
                accentColor = ArcaneGold,
                costSingle = "1.000 Gold",
                costMulti = "10.000 Gold",
                canAffordSingle = gold >= 1000,
                canAffordMulti = gold >= 10000,
                onSingleClick = {
                    val res = onSummon("gold", 1)
                    if (res.isNotEmpty()) lastSummonedHeroes = res
                },
                onMultiClick = {
                    val res = onSummon("gold", 10)
                    if (res.isNotEmpty()) lastSummonedHeroes = res
                },
                singleTag = "btn_summon_gold_1x",
                multiTag = "btn_summon_gold_10x"
            )

            // Diamond Summon Card
            GachaOptionCard(
                title = "DIAMOND SUMMON",
                tagline = "Pilar kristal langit (Tanpa bintang 1)",
                rates = "★2 (85%) • ★3 (10%) • ★4 (4%) • ★5 (0.9%) • ★6 (0.1%)",
                accentColor = PortalCyan,
                costSingle = "10 Diamond",
                costMulti = "100 Diamond",
                canAffordSingle = diamond >= 10,
                canAffordMulti = diamond >= 100,
                onSingleClick = {
                    val res = onSummon("diamond", 1)
                    if (res.isNotEmpty()) lastSummonedHeroes = res
                },
                onMultiClick = {
                    val res = onSummon("diamond", 10)
                    if (res.isNotEmpty()) lastSummonedHeroes = res
                },
                singleTag = "btn_summon_diamond_1x",
                multiTag = "btn_summon_diamond_10x"
            )

            // Event Summon Card
            GachaOptionCard(
                title = "EVENT SUMMON (50 💎)",
                tagline = "Konvergensi Rift Langka (Jaminan minimal ★3)",
                rates = "★3 (90%) • ★4 (4%) • ★5 (5%) • ★6 (0.9%) • ★7 (0.1%)",
                accentColor = RunicPurple,
                costSingle = "50 Diamond",
                costMulti = null,
                canAffordSingle = diamond >= 50,
                canAffordMulti = false,
                onSingleClick = {
                    val res = onSummon("event", 1)
                    if (res.isNotEmpty()) lastSummonedHeroes = res
                },
                onMultiClick = {},
                singleTag = "btn_summon_event_1x",
                multiTag = ""
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Results Dialog
    lastSummonedHeroes?.let { heroes ->
        AlertDialog(
            onDismissRequest = { lastSummonedHeroes = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ArcaneGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HASIL PEMANGGILAN JIWA (${heroes.size} HERO)",
                        color = ArcaneGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                ) {
                    items(heroes) { hero ->
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, getStarColor(hero.starGrade).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "★${hero.starGrade}",
                                            color = getStarColor(hero.starGrade),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = hero.name,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        text = "${hero.race} (${hero.gender}) • ${hero.jobClass}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "HP: ${hero.maxHp}",
                                        color = PortalCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { lastSummonedHeroes = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcaneGold)
                ) {
                    Text("Sambut ke Lobby", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }
}

@Composable
fun GachaOptionCard(
    title: String,
    tagline: String,
    rates: String,
    accentColor: Color,
    costSingle: String,
    costMulti: String?,
    canAffordSingle: Boolean,
    canAffordMulti: Boolean,
    onSingleClick: () -> Unit,
    onMultiClick: () -> Unit,
    singleTag: String,
    multiTag: String
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = tagline, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkSurfaceVariant)
                    .padding(6.dp)
            ) {
                Text(text = "Probabilitas: $rates", color = TextMuted, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSingleClick,
                    enabled = canAffordSingle,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag(singleTag)
                ) {
                    Text(
                        text = "1x ($costSingle)",
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                if (costMulti != null) {
                    Button(
                        onClick = onMultiClick,
                        enabled = canAffordMulti,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag(multiTag)
                    ) {
                        Text(
                            text = "10x ($costMulti)",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
