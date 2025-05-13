// MainScreen.kt - Implementazione completamente rinnovata

package com.example.frontend_happygreen.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.barcode.BarcodeScannerScreen
import com.example.frontend_happygreen.data.*
import com.example.frontend_happygreen.games.EcoDetectiveGameScreen
import com.example.frontend_happygreen.games.EcoGameScreen
import com.example.frontend_happygreen.ui.components.CenteredLoader
import com.example.frontend_happygreen.ui.components.SectionHeader
import com.example.frontend_happygreen.ui.theme.*
import kotlinx.coroutines.launch

// Main Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    volumeLevel: Float = 0.5f,
    onVolumeChange: (Float) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MainScreenViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    // States from ViewModel
    val currentTab by viewModel.currentTab
    val isLoading by viewModel.isLoading
    val showGamesBanner by viewModel.showGamesBanner
    val classList by viewModel.classes
    val gamesList by viewModel.availableGames
    val hasNotifications by viewModel.hasNotifications
    val notificationCount by viewModel.notificationCount
    val errorMessage by viewModel.errorMessage

    // User data from ViewModel
    val userName by viewModel.userName
    val userEmail by viewModel.userEmail
    val userPoints by viewModel.userPoints
    val userLevel by viewModel.userLevel
    val userBadges by viewModel.userBadges

    // Local dialog states
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCreateClassDialog by remember { mutableStateOf(false) }
    var showEcoDetectiveGame by remember { mutableStateOf(false) }
    var showEcoSfidaGame by remember { mutableStateOf(false) }
    var showLeaderboardScreen by remember { mutableStateOf(false) }
    var showClassroomScreen by remember { mutableStateOf<ClassRoom?>(null) }
    var showEcoAIChat by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showGamesScreen by remember { mutableStateOf(false) }

    val tabItems = listOf("Home", "Esplora", "Scanner", "Profilo")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Search,
        Icons.Default.CropFree,
        Icons.Default.Person
    )
    val coroutineScope = rememberCoroutineScope()

    // Error message dialog
    if (errorMessage != null) {
        AlertDialog(
            containerColor = Color.White,
            titleContentColor = Green800,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(20.dp),
            onDismissRequest = { viewModel.errorMessage.value = null },
            title = {
                Text(
                    "Errore",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    errorMessage!!,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.errorMessage.value = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    // Game screens, Classroom Screen e Leaderboard Screen
    when {
        showLeaderboardScreen -> viewModel.LeaderboardScreen(
            onBack = { showLeaderboardScreen = false }
        )
        showClassroomScreen != null -> ClassroomScreen(
            classRoom = showClassroomScreen!!,
            onBack = { showClassroomScreen = null }
        )
        showEcoDetectiveGame -> EcoDetectiveGameScreen(onBack = { showEcoDetectiveGame = false })
        showEcoSfidaGame -> EcoGameScreen()
        showEcoAIChat -> EcoAIChatScreen(onBack = { showEcoAIChat = false })
        showBarcodeScanner -> BarcodeScannerScreen(onBack = { showBarcodeScanner = false })
        showGamesScreen -> GamesScreenImproved(
            games = gamesList,
            onGameSelected = { gameId ->
                showGamesScreen = false
                onGameSelected(gameId)
            },
            onBack = { showGamesScreen = false }
        )
        else -> MainAppScaffoldImproved(
            currentTab = currentTab,
            tabItems = tabItems,
            icons = icons,
            hasNotifications = hasNotifications,
            notificationCount = notificationCount,
            isLoading = isLoading,
            classList = classList,
            showGamesBanner = showGamesBanner,
            gamesList = gamesList,
            userName = userName,
            userEmail = userEmail,
            userPoints = userPoints,
            userLevel = userLevel,
            userBadges = userBadges.map { badge ->
                BadgeItem(
                    id = badge.id,
                    name = badge.name,
                    description = badge.description,
                    iconUrl = badge.iconUrl
                )
            },
            onTabSelected = viewModel::setCurrentTab,
            onProfileClick = { showProfileDialog = true },
            onCreateClassClick = { showCreateClassDialog = true },
            onClassSelected = { classroom ->
                showClassroomScreen = classroom
            },
            onGameSelected = { gameId ->
                when (gameId) {
                    "eco_detective" -> showEcoDetectiveGame = true
                    "eco_sfida" -> showEcoSfidaGame = true
                }
            },
            onLeaderboardClick = { showLeaderboardScreen = true },
            onChatClick = { showEcoAIChat = true },
            onScannerClick = { showBarcodeScanner = true },
            onGamesClick = { showGamesScreen = true },
            onHideGamesBanner = viewModel::hideGamesBanner
        )
    }

    // Profile Dialog
    if (showProfileDialog) {
        ProfileDialogImproved(
            userName = userName,
            userEmail = userEmail,
            userPoints = userPoints,
            userLevel = userLevel,
            volumeLevel = volumeLevel,
            onVolumeChange = onVolumeChange,
            onDismiss = { showProfileDialog = false },
            onLogout = {
                showProfileDialog = false
                viewModel.logout()
                onLogout()
            }
        )
    }

    // Dialog per creare una nuova classe
    if (showCreateClassDialog) {
        CreateClassDialogImproved(
            onDismiss = { showCreateClassDialog = false },
            onClassCreated = { newClass ->
                coroutineScope.launch {
                    viewModel.createClass(newClass)
                    showCreateClassDialog = false
                }
            }
        )
    }
}

@Composable
fun MainAppScaffoldImproved(
    currentTab: Int,
    tabItems: List<String>,
    icons: List<ImageVector>,
    hasNotifications: Boolean,
    notificationCount: Int,
    isLoading: Boolean,
    classList: List<ClassRoom>,
    showGamesBanner: Boolean,
    gamesList: List<Game>,
    userName: String,
    userEmail: String,
    userPoints: Int,
    userLevel: String,
    userBadges: List<BadgeItem>,
    onTabSelected: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onCreateClassClick: () -> Unit,
    onClassSelected: (ClassRoom) -> Unit,
    onGameSelected: (String) -> Unit,
    onLeaderboardClick: () -> Unit,
    onChatClick: () -> Unit,
    onScannerClick: () -> Unit,
    onGamesClick: () -> Unit,
    onHideGamesBanner: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBarImproved(
                onProfileClick = onProfileClick,
                onGamesClick = onGamesClick,
                onLeaderboardClick = onLeaderboardClick,
                hasNotifications = hasNotifications,
                notificationCount = notificationCount
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Green600,
                tonalElevation = 8.dp
            ) {
                tabItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = item,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                item,
                                fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        selected = currentTab == index,
                        onClick = {
                            // Gestione speciale per il tab scanner
                            if (index == 2) { // Scanner tab
                                onScannerClick()
                            } else {
                                onTabSelected(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green600,
                            selectedTextColor = Green600,
                            indicatorColor = Green100,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // FAB per chat
                FloatingActionButton(
                    onClick = onChatClick,
                    containerColor = Green600,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Chiedi a EcoAI",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // FAB per creare classe (solo nella tab Home)
                if (currentTab == 0) {
                    FloatingActionButton(
                        onClick = onCreateClassClick,
                        containerColor = Green600,
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crea Classe",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF9F9F9)) // Sfondo leggermente grigio per un look più moderno
        ) {
            // Contenuto principale
            when {
                isLoading -> CenteredLoaderImproved(message = "Caricamento in corso...")
                else -> {
                    when (currentTab) {
                        0 -> HomeContentImproved(
                            classList = classList,
                            onClassSelected = onClassSelected,
                            showGamesBanner = showGamesBanner,
                            gamesList = gamesList,
                            onGameSelected = onGameSelected,
                            onHideGamesBanner = onHideGamesBanner
                        )
                        1 -> ExploreContentImproved()
                        3 -> ProfileContentImproved(
                            userName = userName,
                            userEmail = userEmail,
                            userPoints = userPoints,
                            userLevel = userLevel,
                            userBadges = userBadges
                        )
                        else -> HomeContentImproved(
                            classList = classList,
                            onClassSelected = onClassSelected,
                            showGamesBanner = showGamesBanner,
                            gamesList = gamesList,
                            onGameSelected = onGameSelected,
                            onHideGamesBanner = onHideGamesBanner
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppTopBarImproved(
    onProfileClick: () -> Unit,
    onGamesClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    hasNotifications: Boolean = false,
    notificationCount: Int = 0
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        color = Green600,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row con logo e azioni
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo e nome app con design migliorato
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo con effetto elevazione
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.happy_green_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "HappyGreen",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Pulsanti azione con design unificato
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Leaderboard button
                    ActionButton(
                        icon = Icons.Default.EmojiEvents,
                        contentDescription = "Classifica",
                        onClick = onLeaderboardClick
                    )

                    // Games button
                    ActionButton(
                        icon = Icons.Default.SportsEsports,
                        contentDescription = "Giochi",
                        onClick = onGamesClick
                    )

                    // Notifications
                    if (hasNotifications) {
                        NotificationIconImproved(count = notificationCount)
                    }

                    // Profile
                    UserAvatarImproved(onClick = onProfileClick)
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun UserAvatarImproved(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Green300, CircleShape)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.happy_green_logo),
            contentDescription = "Profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bordo luminoso
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.5.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun NotificationIconImproved(count: Int) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable { /* Handle notifications */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifiche",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-4).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Red,
                                Color.Red.copy(alpha = 0.8f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CenteredLoaderImproved(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animazione pulsante caricamento
        val infiniteTransition = rememberInfiniteTransition(label = "loader")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            ),
            label = "rotation"
        )

        // Loader con logo HappyGreen
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            // Cerchio rotante esterno
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 4.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Green600.copy(alpha = 0.2f),
                                Green600.copy(alpha = 1f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Logo centrale
            Image(
                painter = painterResource(id = R.drawable.happy_green_logo),
                contentDescription = "Loading",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Green600,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
    }
}

@Composable
fun WelcomeHeaderImproved() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Green600,
                            Green400
                        )
                    )
                )
        ) {
            // Elementi decorativi
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-60).dp)
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .align(Alignment.BottomStart)
                    .offset(x = (-20).dp, y = 20.dp)
            )

            // Contenuto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icona decorativa
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forest,
                            contentDescription = null,
                            tint = Green600,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Benvenuto in HappyGreen!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Unisciti a classi eco-friendly e guadagna Green Points!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )
                    }
                }

                // Pulsanti rapidi di azione
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionChip(
                        text = "Gioca",
                        icon = Icons.Default.SportsEsports,
                        onClick = { /* Vai ai giochi */ }
                    )

                    ActionChip(
                        text = "Tutorial",
                        icon = Icons.Default.Search,
                        onClick = { /* Mostra tutorial */ }
                    )

                    ActionChip(
                        text = "Classifica",
                        icon = Icons.Default.EmojiEvents,
                        onClick = { /* Vai alla classifica */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionChip(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ClassCardImproved(
    classRoom: ClassRoom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Sfondo con effetto blur e overlay migliorato
            Image(
                painter = painterResource(id = classRoom.backgroundImageId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay gradiente per migliorare la leggibilità
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = 300f
                        )
                    )
            )

            // Badge per insegnante
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Green600.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = classRoom.teacherName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Contenuto testuale migliorato
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = classRoom.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Riga con informazioni aggiuntive e call-to-action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "25 membri",  // Esempio di dato (in una app reale verrebbe dai dati)
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "250 punti",  // Esempio di dato
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }

                    // Piccolo pulsante "Entra"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Entra →",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameBannerImproved(
    games: List<Game>,
    onGameSelected: (String) -> Unit,
    onHideBanner: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF3A8068),  // Verde scuro
                            Color(0xFF66BB6A)   // Verde chiaro
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
                .padding(16.dp)
        ) {
            // Header del banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Giochi eco-friendly",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                // Pulsante chiudi
                IconButton(
                    onClick = onHideBanner,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Chiudi",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Testo descrittivo
            Text(
                text = "Gioca e guadagna punti ecologici!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Carousel orizzontale di giochi
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(games) { game ->
                    GameItemPreview(
                        game = game,
                        onClick = { onGameSelected(game.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun GameItemPreview(
    game: Game,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        // Icona del gioco
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = game.iconId),
                contentDescription = game.name,
                modifier = Modifier
                    .size(60.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nome del gioco
        Text(
            text = game.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ActivityCardImproved(
    title: String,
    description: String,
    iconId: Int,
    timestamp: String,
    points: Int? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona migliorata
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Green100.copy(alpha = 0.8f),
                                Green200.copy(alpha = 0.9f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Green800
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            // Punti (se presenti)
            if (points != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Green100)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+$points pt",
                        color = Green800,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HomeContentImproved(
    classList: List<ClassRoom>,
    onClassSelected: (ClassRoom) -> Unit,
    showGamesBanner: Boolean,
    gamesList: List<Game>,
    onGameSelected: (String) -> Unit,
    onHideGamesBanner: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WelcomeHeaderImproved()
        }

        // Banner giochi (condizionale)
        if (showGamesBanner) {
            item {
                GameBannerImproved(
                    games = gamesList,
                    onGameSelected = onGameSelected,
                    onHideBanner = onHideGamesBanner
                )
            }
        }

        item {
            SectionHeaderImproved(
                title = "Le tue classi",
                action = {
                    TextButton(onClick = { /* View all */ }) {
                        Text(
                            "Vedi tutte",
                            color = Green600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }

        items(
            items = classList,
            key = { it.name }
        ) { classRoom ->
            ClassCardImproved(
                classRoom = classRoom,
                onClick = { onClassSelected(classRoom) }
            )
        }

        item {
            SectionHeaderImproved(title = "Attività recenti")

            ActivityCardImproved(
                title = "Albero piantato",
                description = "Hai contribuito alla campagna di riforestazione",
                iconId = R.drawable.happy_green_logo,
                timestamp = "Oggi, 14:30",
                points = 50
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActivityCardImproved(
                title = "Quiz completato",
                description = "Hai risposto correttamente a 8/10 domande sul cambiamento climatico",
                iconId = R.drawable.happy_green_logo,
                timestamp = "Ieri, 16:45",
                points = 25
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActivityCardImproved(
                title = "Lezione completata",
                description = "Hai completato la lezione 'Energie rinnovabili'",
                iconId = R.drawable.happy_green_logo,
                timestamp = "2 giorni fa",
                points = 30
            )

            Spacer(modifier = Modifier.height(80.dp)) // Spazio extra per FAB
        }
    }
}

@Composable
fun SectionHeaderImproved(
    title: String,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(
                        Green600,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Green800,
                fontSize = 20.sp
            )
        }

        if (action != null) {
            action()
        }
    }
}

@Composable
fun ExploreContentImproved() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header grafico accattivante
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2E7D32),  // Verde scuro
                                    Color(0xFF66BB6A)   // Verde chiaro
                                )
                            )
                        )
                ) {
                    // Elementi decorativi
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-30).dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .align(Alignment.BottomStart)
                            .offset(x = (-20).dp, y = 20.dp)
                    )

                    // Contenuto
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Esplora",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Scopri eventi, persone e iniziative eco-friendly",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Contenuto Esplora
        item {
            SectionHeaderImproved(title = "Eventi eco-friendly")

            ExploreCardImproved(
                title = "Pulizia della spiaggia",
                description = "Unisciti a noi per ripulire la spiaggia dai rifiuti plastici",
                iconId = R.drawable.happy_green_logo,
                date = "23 Maggio",
                location = "Spiaggia Comunale"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExploreCardImproved(
                title = "Workshop energia solare",
                description = "Impara a costruire piccoli dispositivi ad energia solare",
                iconId = R.drawable.happy_green_logo,
                date = "5 Giugno",
                location = "Centro Civico"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionHeaderImproved(title = "Eco-warriors vicino a te")

            EcoWarriorCardImproved(
                name = "Maria Rossi",
                points = 4550,
                level = "Eco Champion",
                interests = listOf("Riciclo", "Fotovoltaico")
            )

            Spacer(modifier = Modifier.height(12.dp))

            EcoWarriorCardImproved(
                name = "Luca Bianchi",
                points = 3200,
                level = "Eco Warrior",
                interests = listOf("Biodiversità", "Trasporti sostenibili")
            )

            Spacer(modifier = Modifier.height(12.dp))

            EcoWarriorCardImproved(
                name = "Giulia Verdi",
                points = 5100,
                level = "Eco Master",
                interests = listOf("Energia rinnovabile", "Permacultura")
            )

            Spacer(modifier = Modifier.height(80.dp)) // Spazio extra per FAB
        }
    }
}

@Composable
fun ExploreCardImproved(
    title: String,
    description: String,
    iconId: Int,
    date: String,
    location: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona dell'evento
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Green200,
                                Green100
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Green800
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dettagli evento
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Green600,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = Green600,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Pulsante partecipa
        Button(
            onClick = { /* Partecipa all'evento */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Green600
            ),
            shape = RoundedCornerShape(0, 0, 16.dp, 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Partecipa",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EcoWarriorCardImproved(
    name: String,
    points: Int,
    level: String,
    interests: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar utente
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Green100)
                    .border(2.dp, Green300, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Green600,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Green800
                    )

                    // Badge livello
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Green100)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = level,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Green800,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Punti
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "$points punti",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green600,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tag interessi
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interests.forEach { interest ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Green50)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = interest,
                                style = MaterialTheme.typography.bodySmall,
                                color = Green600,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Pulsante connetti
        Button(
            onClick = { /* Connetti */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Green600
            ),
            shape = RoundedCornerShape(0, 0, 16.dp, 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            Text(
                text = "Connetti",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PointsCardImproved(points: Int, level: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Green100.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Testo "Green Points" con design migliorato
            Text(
                text = "Green Points",
                style = MaterialTheme.typography.titleMedium,
                color = Green700,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Punti con cerchio evidenziato
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Green600,
                                Green700
                            )
                        )
                    )
                    .border(3.dp, Green300, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = points.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 36.sp
                    )

                    Text(
                        text = "punti",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Livello con design più evidente
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Green600)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Livello: $level",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Una barra di avanzamento per il livello successivo
            Spacer(modifier = Modifier.height(24.dp))

            val nextLevelPoints = when (level) {
                "Eco Beginner" -> 500
                "Eco Enthusiast" -> 1000
                "Eco Warrior" -> 3000
                "Eco Champion" -> 5000
                else -> points + 1000
            }

            val progress = points.toFloat() / nextLevelPoints

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$points pt",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green800
                    )

                    Text(
                        text = "$nextLevelPoints pt",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green800
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Barra progresso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Green200)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Green600)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mancano ${nextLevelPoints - points} punti per salire di livello",
                    style = MaterialTheme.typography.bodySmall,
                    color = Green700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun BadgeItemImproved(name: String, description: String = "", iconId: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .padding(8.dp)
    ) {
        // Badge background con effetti visuali migliorati
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Green200,
                            Green100
                        )
                    )
                )
                .border(2.dp, Green300, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Cerchio interno luminoso
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                Color.White.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Icona del badge
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = name,
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Bordo luminoso
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nome del badge
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Green800
        )

        // Descrizione opzionale del badge
        if (description.isNotEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun StatisticsCardImproved() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Le tue statistiche",
                style = MaterialTheme.typography.titleMedium,
                color = Green800,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Statistiche con design migliorato
            StatisticsItemImproved(
                title = "Rifiuti riciclati",
                value = "324",
                unit = "kg",
                icon = Icons.Default.Delete,
                color = Color(0xFF009688) // Teal
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            StatisticsItemImproved(
                title = "Alberi piantati",
                value = "12",
                unit = "alberi",
                icon = Icons.Default.Forest,
                color = Color(0xFF4CAF50) // Verde
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            StatisticsItemImproved(
                title = "Quiz completati",
                value = "35",
                unit = "quiz",
                icon = Icons.Default.Check,
                color = Color(0xFF2196F3) // Blu
            )

            // Pulsante per vedere più statistiche
            TextButton(
                onClick = { /* Mostra tutte le statistiche */ },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = "Vedi tutte le statistiche",
                    color = Green600,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatisticsItemImproved(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icona con sfondo circolare colorato
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Titolo e dettagli
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Il tuo contributo all'ambiente",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Valore con unità di misura
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 24.sp
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun BadgesRowImproved(badges: List<BadgeItem>) {
    if (badges.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Green50
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Green300,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Non hai ancora guadagnato nessun badge",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Green600,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Completa attività e sfide per guadagnare badge",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Vai alle sfide */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Scopri le sfide",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(badges.size) { index ->
                val badge = badges[index]
                BadgeItemImproved(
                    name = badge.name,
                    description = badge.description,
                    iconId = R.drawable.happy_green_logo
                )
            }
        }
    }
}

@Composable
fun ProfileContentImproved(
    userName: String,
    userEmail: String,
    userPoints: Int,
    userLevel: String,
    userBadges: List<BadgeItem>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // User profile header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar utente migliorato
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Green100,
                                        Green50
                                    )
                                )
                            )
                            .border(3.dp, Green300, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.happy_green_logo),
                            contentDescription = "Avatar",
                            modifier = Modifier.size(80.dp)
                        )

                        // Effetto luminoso
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.dp,
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.8f),
                                            Color.White.copy(alpha = 0f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Green800,
                        fontSize = 24.sp
                    )

                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )

                    // Pulsanti per opzioni profilo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileOptionButton(
                            text = "Modifica",
                            icon = Icons.Default.Edit,
                            onClick = { /* Modifica profilo */ }
                        )

                        ProfileOptionButton(
                            text = "Impostazioni",
                            icon = Icons.Default.Settings,
                            onClick = { /* Impostazioni */ }
                        )

                        ProfileOptionButton(
                            text = "Amici",
                            icon = Icons.Default.Group,
                            onClick = { /* Amici */ }
                        )
                    }
                }
            }
        }

        item {
            // Points card migliorata
            PointsCardImproved(points = userPoints, level = userLevel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Badge section
            SectionHeaderImproved(
                title = "Le mie badge",
                action = {
                    TextButton(onClick = { /* Vedi tutti i badge */ }) {
                        Text(
                            "Vedi tutte",
                            color = Green600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )

            BadgesRowImproved(badges = userBadges)

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Statistics section
            SectionHeaderImproved(title = "Le mie statistiche")
            StatisticsCardImproved()

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Progressi ecologici
            SectionHeaderImproved(title = "I miei progressi")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Progresso settimanale
                    Text(
                        text = "Questa settimana",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Green700
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grafico a barre semplificato
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        BarChartColumn(
                            day = "Lun",
                            height = 0.4f,
                            points = 20
                        )

                        BarChartColumn(
                            day = "Mar",
                            height = 0.8f,
                            points = 40
                        )

                        BarChartColumn(
                            day = "Mer",
                            height = 0.6f,
                            points = 30
                        )

                        BarChartColumn(
                            day = "Gio",
                            height = 0.5f,
                            points = 25
                        )

                        BarChartColumn(
                            day = "Ven",
                            height = 1f,
                            points = 50,
                            isHighlighted = true
                        )

                        BarChartColumn(
                            day = "Sab",
                            height = 0.3f,
                            points = 15
                        )

                        BarChartColumn(
                            day = "Dom",
                            height = 0.2f,
                            points = 10
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Statistiche settimanali
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeeklyStatItem(
                            value = "190",
                            label = "Punti Totali",
                            color = Green600
                        )

                        WeeklyStatItem(
                            value = "5",
                            label = "Giorni Attivo",
                            color = Green400
                        )

                        WeeklyStatItem(
                            value = "+25%",
                            label = "vs Scorsa Settimana",
                            color = Color(0xFF66BB6A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Spazio extra per FAB
        }
    }
}

@Composable
fun ProfileOptionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Green50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Green600,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Green600,
            fontSize = 12.sp
        )
    }
}

@Composable
fun BarChartColumn(
    day: String,
    height: Float,
    points: Int,
    isHighlighted: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Punti
        Text(
            text = points.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = if (isHighlighted) Green700 else Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Barra
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(100.dp * height)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    if (isHighlighted)
                        Green600
                    else
                        Green300.copy(alpha = 0.7f)
                )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Giorno
        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = if (isHighlighted) Green800 else Color.Gray,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun WeeklyStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 22.sp
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProfileDialogImproved(
    userName: String,
    userEmail: String,
    userPoints: Int,
    userLevel: String,
    volumeLevel: Float,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Green50)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = Green600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Avatar migliorato
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Green100,
                                    Green50
                                )
                            )
                        )
                        .border(3.dp, Green300, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.happy_green_logo),
                        contentDescription = "Immagine Profilo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp)
                    )

                    // Effetto luminoso
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.8f),
                                        Color.White.copy(alpha = 0f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Green800,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Livello e punti
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Green50)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = userLevel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Green800
                        )

                        Text(
                            text = "$userPoints punti",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Green600
                        )
                    }

                    // Badge livello
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Green600)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Livello ${getLevelNumber(userLevel)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Volume control migliorato
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Green100)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Musica di sottofondo",
                            style = MaterialTheme.typography.titleMedium,
                            color = Green700,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeOff,
                                contentDescription = "Volume giù",
                                tint = Green600,
                                modifier = Modifier.size(20.dp)
                            )

                            Slider(
                                value = volumeLevel,
                                onValueChange = onVolumeChange,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Green600,
                                    activeTrackColor = Green300,
                                    inactiveTrackColor = Green100
                                )
                            )

                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Volume su",
                                tint = Green600,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "Volume: ${(volumeLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout button migliorato
                Button(
                    onClick = {
                        onLogout()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)  // Rosso
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Logout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateClassDialogImproved(
    onDismiss: () -> Unit,
    onClassCreated: (ClassRoom) -> Unit
) {
    var className by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }

    // Valida i campi quando cambiano
    LaunchedEffect(className, teacherName) {
        isValid = className.isNotBlank() && teacherName.isNotBlank()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Crea una nuova classe",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Green800
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nome classe
                Column {
                    Text(
                        text = "Nome della classe",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Green700
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo di testo per il nome della classe
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Inserisci nome classe") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green600,
                            unfocusedBorderColor = Green200,
                            focusedLabelColor = Green600,
                            cursorColor = Green600
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nome insegnante
                Column {
                    Text(
                        text = "Nome insegnante",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Green700
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo di testo per il nome dell'insegnante
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Inserisci nome insegnante") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green600,
                            unfocusedBorderColor = Green200,
                            focusedLabelColor = Green600,
                            cursorColor = Green600
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsanti di azione
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Pulsante annulla
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Green600
                        ),
                        border = BorderStroke(1.dp, Green300),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Annulla")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Pulsante crea
                    Button(
                        onClick = {
                            // Crea la nuova classe e chiama il callback
                            val newClass = ClassRoom(
                                name = className,
                                backgroundImageId = R.drawable.happy_green_logo,
                                teacherName = teacherName
                            )
                            onClassCreated(newClass)
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green600,
                            disabledContainerColor = Green200
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Crea Classe")
                    }
                }
            }
        }
    }
}

// Funzione per ottenere il numero del livello in base al nome
private fun getLevelNumber(level: String): String {
    return when (level) {
        "Eco Beginner" -> "1"
        "Eco Enthusiast" -> "2"
        "Eco Warrior" -> "3"
        "Eco Champion" -> "4"
        "Eco Master" -> "5"
        else -> "?"
    }
}

@Composable
fun GamesScreenImproved(
    games: List<Game>,
    onGameSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        // Header personalizzato
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Sfondo con gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Green700,
                                Green600,
                                Green500
                            )
                        )
                    )
            ) {
                // Elementi decorativi
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-30).dp)
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.BottomStart)
                        .offset(x = (-20).dp, y = 20.dp)
                )
            }

            // Contenuto header
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 16.dp)
            ) {
                // Top row con back button e titolo
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icona indietro
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Titolo
                    Column {
                        Text(
                            text = "Giochi Eco-friendly",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )

                        Text(
                            text = "Divertiti e guadagna punti ecologici",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Informazioni sui punti
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Completa i giochi per guadagnare Green Points e badge speciali",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Contenuto principale - lista giochi
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Giochi disponibili",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green800,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(games) { game ->
                GameCardImproved(
                    game = game,
                    onClick = { onGameSelected(game.id) }
                )
            }

            // Spazio extra in fondo
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
data class ClassRoom(
    val name: String,
    val backgroundImageId: Int,
    val teacherName: String = "Unknown Teacher"
)
// Aggiunta di colori che potrebbero essere utili
val Green50 = Color(0xFFE8F5E9)
val Green400 = Color(0xFF66BB6A)
val Green500 = Color(0xFF4CAF50)
val Green700 = Color(0xFF388E3C)
val Green900 = Color(0xFF1B5E20)