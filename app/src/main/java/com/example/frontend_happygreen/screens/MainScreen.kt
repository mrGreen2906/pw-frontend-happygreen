package com.example.frontend_happygreen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.games.EcoDetectiveGameScreen
import com.example.frontend_happygreen.games.EcoGameScreen
import com.example.frontend_happygreen.ui.components.SectionHeader
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green300
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800
import kotlinx.coroutines.launch

// Data Models
data class ClassRoom(
    val name: String,
    val backgroundImageId: Int,
    val teacherName: String = "Unknown Teacher"
)

data class Game(
    val id: String,
    val name: String,
    val description: String,
    val iconId: Int
)

// ViewModel
class MainScreenViewModel : ViewModel() {
    // User data
    var userName = mutableStateOf("John Doe")
    var userEmail = mutableStateOf("john.doe@example.com")
    var userPoints = mutableStateOf(1250)
    var userLevel = mutableStateOf("Eco Warrior")

    // UI states
    var isLoading = mutableStateOf(true)
    var classes = mutableStateOf<List<ClassRoom>>(emptyList())
    var availableGames = mutableStateOf<List<Game>>(emptyList())
    var showGamesBanner = mutableStateOf(true)
    var currentTab = mutableStateOf(0)
    var hasNotifications = mutableStateOf(true)
    var notificationCount = mutableStateOf(3)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // This would be API calls in a real app
        classes.value = listOf(
            ClassRoom("Eco Science 101", R.drawable.happy_green_logo, "Prof. Smith"),
            ClassRoom("Environmental Studies", R.drawable.happy_green_logo, "Prof. Johnson"),
            ClassRoom("Green Technologies", R.drawable.happy_green_logo, "Prof. Martinez"),
            ClassRoom("Sustainable Development", R.drawable.happy_green_logo, "Prof. Garcia")
        )

        availableGames.value = listOf(
            Game("eco_detective", "Eco Detective", "Smista i rifiuti nei cestini corretti", R.drawable.happy_green_logo),
            Game("eco_sfida", "Eco Sfida", "Confronta l'impatto ambientale", R.drawable.happy_green_logo),
            Game("tree_planter", "Tree Planter", "Simulatore di piantumazione", R.drawable.happy_green_logo)
        )

        isLoading.value = false
    }

    fun setCurrentTab(tabIndex: Int) {
        currentTab.value = tabIndex
    }

    fun hideGamesBanner() {
        showGamesBanner.value = false
    }

    fun createClass(newClass: ClassRoom) {
        classes.value = classes.value + newClass
    }

    fun joinClass(classRoom: ClassRoom) {
        // This would make an API call in a real app
    }
}

// Main Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    volumeLevel: Float = 0.5f,
    onVolumeChange: (Float) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MainScreenViewModel = viewModel()
) {
    // States from ViewModel
    val currentTab by viewModel.currentTab
    val isLoading by viewModel.isLoading
    val showGamesBanner by viewModel.showGamesBanner
    val classList by viewModel.classes
    val gamesList by viewModel.availableGames
    val hasNotifications by viewModel.hasNotifications
    val notificationCount by viewModel.notificationCount

    // Local dialog states
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCreateClassDialog by remember { mutableStateOf(false) }
    var showEcoDetectiveGame by remember { mutableStateOf(false) }
    var showEcoSfidaGame by remember { mutableStateOf(false) }

    val tabItems = listOf("Home", "Esplora", "Profilo")
    val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Person)
    val coroutineScope = rememberCoroutineScope()

    // Game screens
    when {
        showEcoDetectiveGame -> EcoDetectiveGameScreen(onBack = { showEcoDetectiveGame = false })
        showEcoSfidaGame -> EcoGameScreen()
        else -> MainAppScaffold(
            currentTab = currentTab,
            tabItems = tabItems,
            icons = icons,
            hasNotifications = hasNotifications,
            notificationCount = notificationCount,
            isLoading = isLoading,
            classList = classList,
            showGamesBanner = showGamesBanner,
            gamesList = gamesList,
            onTabSelected = viewModel::setCurrentTab,
            onProfileClick = { showProfileDialog = true },
            onCreateClassClick = { showCreateClassDialog = true },
            onClassSelected = viewModel::joinClass,
            onGameSelected = { gameId ->
                when (gameId) {
                    "eco_detective" -> showEcoDetectiveGame = true
                    "eco_sfida" -> showEcoSfidaGame = true
                }
            },
            onHideGamesBanner = viewModel::hideGamesBanner
        )
    }

    // Dialogs
    if (showProfileDialog) {
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

    if (showCreateClassDialog) {
        CreateClassDialog(
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



    // Top Bar
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppTopBar(
        onProfileClick: () -> Unit,
        hasNotifications: Boolean = false,
        notificationCount: Int = 0
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.happy_green_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "HappyGreen",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Notifications
                    if (hasNotifications) {
                        NotificationIcon(count = notificationCount)
                    }

                    // Profile
                    UserAvatar(onClick = onProfileClick)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Green600)
        )
    }

    @Composable
    fun NotificationIcon(count: Int) {
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable { /* Handle notifications */ }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifiche",
                tint = Color.White
            )

            if (count > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (count > 9) "9+" else count.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    @Composable
    fun UserAvatar(onClick: () -> Unit) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .border(1.dp, Color.White, CircleShape)
                .clickable(onClick = onClick)
        ) {
            Image(
                painter = painterResource(id = R.drawable.happy_green_logo),
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }


    @Composable
    fun WelcomeHeader() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Green100, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Benvenuto in HappyGreen!",
                style = MaterialTheme.typography.titleLarge,
                color = Green800
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Unisciti a classi eco-friendly e guadagna Green Points!",
                style = MaterialTheme.typography.bodyMedium,
                color = Green600
            )
        }
    }
@Composable
fun SectionHeader(
    title: String,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Green800
        )
        action?.invoke()
    }
}

    @Composable
    fun ClassCard(
        classRoom: ClassRoom,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = classRoom.backgroundImageId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = classRoom.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Insegnante: ${classRoom.teacherName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
        }
    }
@Composable
fun ActivityCard(
    title: String,
    description: String,
    iconId: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Green100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
    // Home Content
    @Composable
    fun HomeContent(
        classList: List<ClassRoom>,
        onClassSelected: (ClassRoom) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeHeader()
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SectionHeader(
                    title = "Le tue classi",
                    action = {
                        TextButton(onClick = { /* View all */ }) {
                            Text("Vedi tutte")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(
                items = classList,
                key = { it.name }
            ) { classRoom ->
                ClassCard(
                    classRoom = classRoom,
                    onClick = { onClassSelected(classRoom) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SectionHeader(title = "Attività recenti")
                Spacer(modifier = Modifier.height(8.dp))

                ActivityCard(
                    title = "Albero piantato",
                    description = "Hai guadagnato 50 punti per aver contribuito alla campagna di riforestazione",
                    iconId = R.drawable.happy_green_logo
                )
                Spacer(modifier = Modifier.height(8.dp))

                ActivityCard(
                    title = "Quiz completato",
                    description = "Hai risposto correttamente a 8/10 domande sul cambiamento climatico",
                    iconId = R.drawable.happy_green_logo
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }




    @Composable
    fun ExploreCard(title: String, description: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
    // Explore Content
    @Composable
    fun ExploreContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Esplora",
                style = MaterialTheme.typography.headlineMedium,
                color = Green600
            )

            Spacer(modifier = Modifier.height(24.dp))

            ExploreCard(
                title = "Trova eventi eco-friendly",
                description = "Scopri eventi di pulizia, piantumazione e sensibilizzazione ambientale nella tua zona."
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExploreCard(
                title = "Trova altri eco-warriors",
                description = "Connettiti con altri appassionati di sostenibilità per attività e collaborazioni."
            )
        }
    }


    @Composable
    fun StatisticsItem(
        title: String,
        value: String,
        icon: ImageVector
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Green600,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Green800,
                fontWeight = FontWeight.Bold
            )
        }
    }


    @Composable
    fun StatisticsCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatisticsItem(
                    title = "Rifiuti riciclati",
                    value = "324 kg",
                    icon = Icons.Default.Delete
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                StatisticsItem(
                    title = "Alberi piantati",
                    value = "12",
                    icon = Icons.Default.Forest
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                StatisticsItem(
                    title = "Quiz completati",
                    value = "35",
                    icon = Icons.Default.Check
                )
            }
        }
    }
    @Composable
    fun PointsCard(points: Int, level: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Green100)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Green Points",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = points.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = Green600
                )
                Text(
                    text = "Livello: $level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green800
                )
            }
        }
    }
    @Composable
    fun BadgeItem(name: String, iconId: Int) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(80.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Green100)
                    .border(2.dp, Green300, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = name,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }

    @Composable
    fun BadgesRow() {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(8) { index ->
                BadgeItem(
                    name = "Badge ${index + 1}",
                    iconId = R.drawable.happy_green_logo
                )
            }
        }
    }
    // Profile Content
    @Composable
    fun ProfileContent() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // User avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Green100)
                        .border(2.dp, Green600, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.happy_green_logo),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "john.doe@example.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Points card
                PointsCard(points = 1250, level = "Eco Warrior")
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Badges section
                SectionHeader(title = "Le mie badge")
                Spacer(modifier = Modifier.height(8.dp))
                BadgesRow()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Statistics section
                SectionHeader(title = "Le mie statistiche")
                Spacer(modifier = Modifier.height(8.dp))
                StatisticsCard()
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }








    @Composable
    fun GameCardCompact(
        name: String,
        description: String,
        iconId: Int,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .width(140.dp)
                .height(160.dp)
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconId),
                        contentDescription = name,
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
        }
    }


    // Games Banner
    @Composable
    fun GamesBanner(
        games: List<Game>,
        onGameSelected: (String) -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Green100)
                .padding(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Giochi",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green600,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi Banner Giochi",
                            tint = Green600
                        )
                    }
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(
                        items = games,
                        key = { it.id }
                    ) { game ->
                        GameCardCompact(
                            name = game.name,
                            description = game.description,
                            iconId = game.iconId,
                            onClick = { onGameSelected(game.id) }
                        )
                    }
                }
            }
        }
    }


    // Dialogs
    @Composable
    fun ProfileDialog(
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                            Icon(Icons.Default.Close, contentDescription = "Chiudi")
                        }
                    }

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .border(2.dp, Green600, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.happy_green_logo),
                            contentDescription = "Immagine Profilo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "John Doe",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "john.doe@example.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Points card
                    PointsCard(points = 1250, level = "Eco Warrior")

                    Spacer(modifier = Modifier.height(24.dp))

                    // Volume control
                    Text(
                        text = "Musica di sottofondo",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Volume giù",
                            tint = Green600
                        )

                        Slider(
                            value = volumeLevel,
                            onValueChange = onVolumeChange,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Green600,
                                activeTrackColor = Green300,
                                inactiveTrackColor = Green100
                            )
                        )

                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume su",
                            tint = Green600
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Logout button
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.8f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassDialog(
    onDismiss: () -> Unit,
    onClassCreated: (ClassRoom) -> Unit
) {
    var className by remember { mutableStateOf("") }
    var selectedBackgroundIndex by remember { mutableStateOf(0) }
    var showBackgroundOptions by remember { mutableStateOf(false) }

    // Background options (would be different images in a real app)
    val backgroundOptions = listOf(
        R.drawable.pattern_1,
        R.drawable.pattern_2,
        R.drawable.pattern_3,
        R.drawable.pattern_4
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crea Nuova Classe",
                        style = MaterialTheme.typography.titleLarge
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Class name input
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Nome Classe") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Background selection
                Text(
                    text = "Scegli Sfondo",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Background preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showBackgroundOptions = true }
                ) {
                    Image(
                        painter = painterResource(id = backgroundOptions[selectedBackgroundIndex]),
                        contentDescription = "Sfondo Selezionato",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tocca per cambiare sfondo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Background dropdown con griglia 2x2 di dimensioni ridotte
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center  // Centra il dropdown
                ) {
                    DropdownMenu(
                        expanded = showBackgroundOptions,
                        onDismissRequest = { showBackgroundOptions = false },
                        modifier = Modifier.width(240.dp)  // Larghezza fissa più contenuta
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Prima riga (2 elementi)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Primo sfondo
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(90.dp)  // Dimensione fissa più piccola
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedBackgroundIndex = 0
                                            showBackgroundOptions = false
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = backgroundOptions[0]),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Secondo sfondo
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(90.dp)  // Dimensione fissa più piccola
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedBackgroundIndex = 1
                                            showBackgroundOptions = false
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = backgroundOptions[1]),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))  // Spazio tra le righe aumentato

                            // Seconda riga (2 elementi)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Terzo sfondo
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(90.dp)  // Dimensione fissa più piccola
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedBackgroundIndex = 2
                                            showBackgroundOptions = false
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = backgroundOptions[2]),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Quarto sfondo
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(90.dp)  // Dimensione fissa più piccola
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedBackgroundIndex = 3
                                            showBackgroundOptions = false
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = backgroundOptions[3]),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Create button
                Button(
                    onClick = {
                        if (className.isNotBlank()) {
                            val newClass = ClassRoom(
                                name = className,
                                backgroundImageId = backgroundOptions[selectedBackgroundIndex]
                            )
                            onClassCreated(newClass)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = className.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600,
                        disabledContainerColor = Green100
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crea Classe")
                }
            }
        }
    }
}

    // Helper components
    @Composable
    fun CenteredLoader(message: String) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Green600
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green600
                )
            }
        }
    }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    currentTab: Int,
    tabItems: List<String>,
    icons: List<ImageVector>,
    hasNotifications: Boolean,
    notificationCount: Int,
    isLoading: Boolean,
    classList: List<ClassRoom>,
    showGamesBanner: Boolean,
    gamesList: List<Game>,
    onTabSelected: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onCreateClassClick: () -> Unit,
    onClassSelected: (ClassRoom) -> Unit,
    onGameSelected: (String) -> Unit,
    onHideGamesBanner: () -> Unit
) {
    var showEcoAIChat by remember { mutableStateOf(false) }

    if (showEcoAIChat) {
        EcoAIChatScreen(onBack = { showEcoAIChat = false })
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    onProfileClick = onProfileClick,
                    hasNotifications = hasNotifications,
                    notificationCount = notificationCount
                )
            },
            bottomBar = {
                NavigationBar {
                    tabItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = currentTab == index,
                            onClick = { onTabSelected(index) }
                        )
                    }
                }
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    // AI Chat FAB
                    FloatingActionButton(
                        onClick = { showEcoAIChat = true },
                        containerColor = Color(0xFF009688),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Chiedi a EcoAI",
                            tint = Color.White
                        )
                    }

                    // Create Class FAB (only on Home tab)
                    if (currentTab == 0) {
                        FloatingActionButton(
                            onClick = onCreateClassClick,
                            containerColor = Green600
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Crea Classe",
                                tint = Color.White
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
                    .background(Color.White),
            ) {
                when {
                    isLoading -> CenteredLoader(message = "Caricamento in corso...")
                    else -> {
                        when (currentTab) {
                            0 -> HomeContent(classList = classList, onClassSelected = onClassSelected)
                            1 -> ExploreContent()
                            2 -> ProfileContent()
                            else -> HomeContent(classList = classList, onClassSelected = onClassSelected)
                        }
                    }
                }

                // Games banner
                if (showGamesBanner) {
                    GamesBanner(
                        games = gamesList,
                        onGameSelected = onGameSelected,
                        onDismiss = onHideGamesBanner,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
