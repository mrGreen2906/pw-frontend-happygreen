
// MainScreen.kt (Aggiornato con Game sealed class e supporto a EcoGameScreen)
package com.example.frontend_happygreen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.games.EcoDetectiveGameScreen
import com.example.frontend_happygreen.games.EcoGameScreen
import com.example.frontend_happygreen.ui.theme.FrontendhappygreenTheme

sealed class Game(val name: String, val description: String) {
    object EcoDetective : Game("EcoDetective", "Sort waste into the correct bins")
    object EcoQuiz : Game("EcoQuiz", "Environmental quiz: waste decomposition, pollution, prevalence")
    companion object {
        val allGames = listOf(EcoDetective, EcoQuiz)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    volumeLevel: Float = 0.5f,
    onVolumeChange: (Float) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabItems = listOf("Home", "Profile", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Person, Icons.Default.Settings)

    var showProfileDialog by remember { mutableStateOf(false) }
    var showEcoDetectiveGame by remember { mutableStateOf(false) }
    var showEcoGame by remember { mutableStateOf(false) }

    val gamesList = Game.allGames

    when {
        showEcoDetectiveGame -> EcoDetectiveGameScreen(onBack = { showEcoDetectiveGame = false })
        showEcoGame -> EcoGameScreen()
        else -> Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Image(painter = painterResource(id = R.drawable.happy_green_logo), contentDescription = "App Logo", modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "HappyGreen", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                                    .border(1.dp, Color.White, CircleShape)
                                    .clickable { showProfileDialog = true }
                            ) {
                                Image(painter = painterResource(id = R.drawable.happy_green_logo), contentDescription = "Profile", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
                )
            },
            bottomBar = {
                NavigationBar {
                    tabItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { }, containerColor = Color(0xFF4CAF50)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.White)) {
                when (selectedTab) {
                    0 -> HomeContent(games = gamesList, onGameSelected = { game ->
                        when (game) {
                            is Game.EcoDetective -> showEcoDetectiveGame = true
                            is Game.EcoQuiz -> showEcoGame = true
                        }
                    })
                    1 -> ProfileContent()
                    2 -> SettingsContent()
                }
            }

            if (showProfileDialog) {
                Dialog(onDismissRequest = { showProfileDialog = false }) {
                    ProfileDialog(volumeLevel = volumeLevel, onVolumeChange = onVolumeChange, onDismiss = { showProfileDialog = false }, onLogout = {
                        showProfileDialog = false
                        onLogout()
                    })
                }
            }
        }
    }
}

@Composable
fun HomeContent(games: List<Game>, onGameSelected: (Game) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Welcome to HappyGreen!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Play eco-friendly games and earn Green Points!", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Games", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        games.forEach { game ->
            GameCard(game = game, onClick = { onGameSelected(game) })
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun GameCard(game: Game, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8F5E9)), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = R.drawable.happy_green_logo), contentDescription = game.name, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(game.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(game.description, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}


@Composable
fun ProfileContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile Content", fontSize = 20.sp, color = Color.Gray)
    }
}

@Composable
fun SettingsContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings Content", fontSize = 20.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileDialog(
    volumeLevel: Float,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.LightGray).border(2.dp, Color(0xFF4CAF50), CircleShape)
            ) {
                Image(painter = painterResource(id = R.drawable.happy_green_logo), contentDescription = "Profile Picture", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("John Doe", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("john.doe@example.com", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Green Points", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("1250", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    Text("Level: Eco Warrior", fontSize = 14.sp, color = Color(0xFF4CAF50))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Background Music", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Volume Down", tint = Color(0xFF4CAF50))
                Slider(
                    value = volumeLevel,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4CAF50),
                        activeTrackColor = Color(0xFFAED581),
                        inactiveTrackColor = Color(0xFFE8F5E9)
                    )
                )
                Icon(imageVector = Icons.Default.Check, contentDescription = "Volume Up", tint = Color(0xFF4CAF50))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontSize = 16.sp)
            }
        }
    }
}
