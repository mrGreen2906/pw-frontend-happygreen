// MainScreen.kt (Updated)
package com.example.frontend_happygreen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.games.EcoDetectiveGameScreen
import com.example.frontend_happygreen.ui.theme.FrontendhappygreenTheme

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

    // State for profile dialog
    var showProfileDialog by remember { mutableStateOf(false) }

    // State for showing game screen
    var showEcoDetectiveGame by remember { mutableStateOf(false) }

    // List of available games
    val gamesList = listOf(
        Pair("EcoDetective", "Sort waste into the correct bins"),
        Pair("Green Quiz", "Test your environmental knowledge"),
        Pair("Tree Planter", "Virtual tree planting simulator")
    )

    if (showEcoDetectiveGame) {
        // Show the EcoDetective game screen
        EcoDetectiveGameScreen(
            onBack = { showEcoDetectiveGame = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // App Logo
                            Image(
                                painter = painterResource(id = R.drawable.happy_green_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // App Name
                            Text(
                                text = "HappyGreen",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Profile Picture
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                                    .border(1.dp, Color.White, CircleShape)
                                    .clickable { showProfileDialog = true }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.happy_green_logo),
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF4CAF50)
                    )
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
                FloatingActionButton(
                    onClick = { /* Add action */ },
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White),
            ) {
                when (selectedTab) {
                    0 -> HomeContent(
                        games = gamesList,
                        onGameSelected = { gameName ->
                            when (gameName) {
                                "EcoDetective" -> showEcoDetectiveGame = true
                                // Add other game cases when implemented
                            }
                        }
                    )
                    1 -> ProfileContent()
                    2 -> SettingsContent()
                }
            }

            // Profile Dialog
            if (showProfileDialog) {
                Dialog(onDismissRequest = { showProfileDialog = false }) {
                    ProfileDialog(
                        volumeLevel = volumeLevel,
                        onVolumeChange = onVolumeChange,
                        onDismiss = { showProfileDialog = false },
                        onLogout = {
                            showProfileDialog = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    games: List<Pair<String, String>>,
    onGameSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome header
        Text(
            text = "Welcome to HappyGreen!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Play eco-friendly games and earn Green Points!",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Games section
        Text(
            text = "Games",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Games list
        games.forEach { (name, description) ->
            GameCard(
                name = name,
                description = description,
                onClick = { onGameSelected(name) }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun GameCard(
    name: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.happy_green_logo),
                    contentDescription = name,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Game info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProfileContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Content",
            fontSize = 20.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SettingsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings Content",
            fontSize = 20.sp,
            color = Color.Gray
        )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Profile Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(2.dp, Color(0xFF4CAF50), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.happy_green_logo),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = "John Doe",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User Email
            Text(
                text = "john.doe@example.com",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Green Points
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Green Points",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "1250",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )

                    Text(
                        text = "Level: Eco Warrior",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Music Volume Control
            Text(
                text = "Background Music",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Volume Down",
                    tint = Color(0xFF4CAF50)
                )

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

                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Volume Up",
                    tint = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Logout",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FrontendhappygreenTheme {
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileDialogPreview() {
    FrontendhappygreenTheme {
        Surface {
            ProfileDialog(
                volumeLevel = 0.5f,
                onVolumeChange = {},
                onDismiss = {},
                onLogout = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    FrontendhappygreenTheme {
        Surface {
            HomeContent(
                games = listOf(
                    Pair("EcoDetective", "Sort waste into the correct bins"),
                    Pair("Green Quiz", "Test your environmental knowledge")
                ),
                onGameSelected = {}
            )
        }
    }
}