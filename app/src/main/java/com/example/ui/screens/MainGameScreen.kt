package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeroTag
import com.example.data.repository.GameRepository
import com.example.ui.components.TopStatusBar
import com.example.ui.dialogs.NewGameDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.*

enum class GameTab(val label: String, val icon: ImageVector) {
    CHRONICLE("Narasi", Icons.Default.ChatBubble),
    HEROES("Pahlawan", Icons.Default.Groups),
    ALTAR("Altar", Icons.Default.AutoAwesome),
    FACILITIES("Fasilitas", Icons.Default.Castle),
    TOWER("Menara", Icons.Default.Fort)
}

@Composable
fun MainGameScreen(
    repository: GameRepository,
    modifier: Modifier = Modifier
) {
    val gameState by repository.gameState.collectAsState()
    val isLoadingStory by repository.isLoadingStory.collectAsState()
    val customApiKey by repository.customApiKey.collectAsState()
    val selectedModel by repository.selectedModel.collectAsState()

    var currentTab by remember { mutableStateOf(GameTab.CHRONICLE) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNewGameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopStatusBar(
                gameState = gameState,
                onOpenSettings = { showSettingsDialog = true },
                onNewGameClick = { showNewGameDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                GameTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (currentTab == tab) ArcaneGold else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                color = if (currentTab == tab) ArcaneGold else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = DarkSurfaceHighlight
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (currentTab) {
                GameTab.CHRONICLE -> NarrativeConsoleScreen(
                    gameState = gameState,
                    isLoading = isLoadingStory,
                    onSendMessage = { prompt ->
                        repository.sendUserPrompt(prompt)
                    },
                    onUseMiracle = { miracle ->
                        repository.useMiracle(miracle)
                    }
                )
                GameTab.HEROES -> HeroesScreen(
                    heroes = gameState.heroes,
                    graveyard = gameState.graveyard,
                    gold = gameState.gold,
                    onPromoteHero = { heroId ->
                        repository.promoteHero(heroId)
                    },
                    onUpdateTag = { heroId, tag ->
                        repository.updateHeroTag(heroId, tag)
                    }
                )
                GameTab.ALTAR -> AltarGachaScreen(
                    gold = gameState.gold,
                    diamond = gameState.diamond,
                    onSummon = { type, count ->
                        repository.summon(type, count)
                    }
                )
                GameTab.FACILITIES -> FacilitiesScreen(
                    gameState = gameState,
                    onRestInKitchen = {
                        repository.restInKitchen()
                        currentTab = GameTab.CHRONICLE
                    },
                    onScanPvP = {
                        repository.scanPvPRadar()
                        currentTab = GameTab.CHRONICLE
                    },
                    onSendMessage = { msg ->
                        repository.sendUserPrompt(msg)
                        currentTab = GameTab.CHRONICLE
                    }
                )
                GameTab.TOWER -> TowerScreen(
                    gameState = gameState,
                    onLaunchExpedition = { floor ->
                        repository.launchTowerExpedition(floor)
                    },
                    onNavigateToNarrative = {
                        currentTab = GameTab.CHRONICLE
                    }
                )
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentApiKey = customApiKey,
            currentModel = selectedModel,
            onSaveSettings = { key, model ->
                repository.setCustomApiKey(key)
                repository.setSelectedModel(model)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showNewGameDialog) {
        NewGameDialog(
            currentMaster = gameState.masterName,
            currentLobby = gameState.lobbyName,
            currentFairy = gameState.fairyName,
            currentDiff = gameState.difficulty,
            onConfirm = { master, lobby, fairy, diff, customHero ->
                repository.startNewGame(
                    masterName = master,
                    lobbyName = lobby,
                    fairyName = fairy,
                    difficulty = diff,
                    customHeroName = customHero
                )
                currentTab = GameTab.CHRONICLE
            },
            onDismiss = { showNewGameDialog = false }
        )
    }
}
