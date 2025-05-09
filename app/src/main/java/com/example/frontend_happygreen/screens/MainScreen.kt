// MainScreen.kt aggiornato con classifica integrata

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.frontend_happygreen.barcode.BarcodeScannerScreen
import com.example.frontend_happygreen.games.EcoDetectiveGameScreen
import com.example.frontend_happygreen.games.EcoGameScreen
import com.example.frontend_happygreen.ui.components.SectionHeader
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green300
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.ui.components.CenteredLoader

class MainScreenViewModel : ViewModel() {
    private val apiService = RetrofitClient.create(ApiService::class.java)

    // Dati utente (presi da UserSession)
    var userName = mutableStateOf(UserSession.getFullName())
    var userEmail = mutableStateOf(UserSession.getEmail() ?: "")
    var userPoints = mutableStateOf(UserSession.getEcoPoints() ?: 0)
    var userLevel = mutableStateOf(calculateLevel(UserSession.getEcoPoints() ?: 0))

    // Stati UI
    var isLoading = mutableStateOf(true)
    var errorMessage = mutableStateOf<String?>(null)

    // Dati dell'app
    var classes = mutableStateOf<List<ClassRoom>>(emptyList())
    var availableGames = mutableStateOf<List<Game>>(emptyList())
    var userBadges = mutableStateOf<List<Badge>>(emptyList())
    var showGamesBanner = mutableStateOf(true)
    var currentTab = mutableStateOf(0)
    var hasNotifications = mutableStateOf(false)
    var notificationCount = mutableStateOf(0)

    // Post dell'utente
    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

    // Per i punteggi
    var leaderboardData = mutableStateOf<List<LeaderboardItem>>(emptyList())
    var isLoadingLeaderboard = mutableStateOf(false)
    var selectedGameForLeaderboard = mutableStateOf<String?>(null)

    init {
        loadUserData()
        loadInitialData()
    }

    data class LeaderboardItem(
        val id: Int,
        val username: String,
        val score: Int,
        val avatar: String? = null
    )

    fun loadLeaderboard(gameId: String? = null) {
        isLoadingLeaderboard.value = true
        selectedGameForLeaderboard.value = gameId

        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: return@launch

                val response = if (gameId != null) {
                    apiService.getLeaderboard(token, gameId)
                } else {
                    apiService.getGlobalLeaderboard(token)
                }

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // Mappa la risposta alle LeaderboardItem
                    leaderboardData.value = data.map { item ->
                        (if (gameId != null) item.score else item.ecoPoints)?.let {
                            LeaderboardItem(
                                id = item.userId,
                                username = item.username,
                                score = it,
                                avatar = item.avatar
                            )
                        }!!
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoadingLeaderboard.value = false
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LeaderboardScreen(
        viewModel: MainScreenViewModel = viewModel(),
        onBack: () -> Unit
    ) {
        val leaderboardData by viewModel.leaderboardData
        val isLoading by viewModel.isLoadingLeaderboard
        val selectedGame by viewModel.selectedGameForLeaderboard

        var selectedTab by remember { mutableStateOf(0) }
        val games = listOf(
            null to "Classifica Globale",
            "eco_detective" to "Eco Detective",
            "eco_sfida" to "Eco Sfida"
        )

        // Carica la classifica quando cambia la tab
        LaunchedEffect(selectedTab) {
            viewModel.loadLeaderboard(games[selectedTab].first)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Barra superiore
            TopAppBar(
                title = { Text("Classifica") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )

            // Tab per selezionare il gioco
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Green100,
                contentColor = Green800
            ) {
                games.forEachIndexed { index, (_, name) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(name) }
                    )
                }
            }

            // Contenuto della classifica
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green600)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Classifica ${games[selectedTab].second}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    itemsIndexed(leaderboardData) { index, item ->
                        LeaderboardItem(
                            rank = index + 1,
                            username = item.username,
                            score = item.score,
                            avatar = item.avatar,
                            isCurrentUser = item.id == UserSession.getUserId()
                        )
                        Divider()
                    }
                }
            }
        }
    }

    @Composable
    fun LeaderboardItem(
        rank: Int,
        username: String,
        score: Int,
        avatar: String? = null,
        isCurrentUser: Boolean = false
    ) {
        val backgroundColor = if (isCurrentUser) Green100 else Color.White
        val fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posizione
            Text(
                text = "$rank.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = fontWeight,
                modifier = Modifier.width(40.dp)
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Green100),
                contentAlignment = Alignment.Center
            ) {
                // Se avatar è null, mostra un'icona predefinita
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Green600
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Username
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = fontWeight,
                modifier = Modifier.weight(1f)
            )

            // Punteggio
            Text(
                text = "$score pts",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = fontWeight,
                color = Green800
            )
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader()
                if (token != null) {
                    // Ottieni dati utente attuali
                    val response = apiService.getCurrentUser(token)
                    if (response.isSuccessful && response.body() != null) {
                        val userData = response.body()!!
                        UserSession.updateUserData(userData)

                        // Aggiorna i dati dell'utente nel ViewModel
                        userName.value = UserSession.getFullName()
                        userEmail.value = UserSession.getEmail() ?: ""
                        userPoints.value = UserSession.getEcoPoints() ?: 0
                        userLevel.value = calculateLevel(userPoints.value)

                        // Carica badge utente
                        loadUserBadges()

                        // Carica anche notifiche
                        checkNotifications()
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = "Errore nel caricamento dei dati utente: ${e.message}"
            }
        }
    }

    /**
     * Determina il livello dell'utente in base ai punti eco
     */
    private fun calculateLevel(points: Int): String {
        return when {
            points >= 5000 -> "Eco Master"
            points >= 3000 -> "Eco Champion"
            points >= 1000 -> "Eco Warrior"
            points >= 500 -> "Eco Enthusiast"
            else -> "Eco Beginner"
        }
    }

    /**
     * Controlla se ci sono notifiche per l'utente
     */
    private fun checkNotifications() {
        // In un'app reale, qui faresti una chiamata API per ottenere le notifiche
        // Per ora, simuliamo alcune notifiche solo per demo
        hasNotifications.value = true
        notificationCount.value = 3
    }

    /**
     * Carica i dati iniziali dell'app
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Carica gruppi (classi)
                loadGroups()

                // Carica giochi (dato statico per ora)
                loadGames()

                isLoading.value = false
            } catch (e: Exception) {
                errorMessage.value = "Errore nel caricamento dei dati: ${e.message}"
                isLoading.value = false
            }
        }
    }

    /**
     * Carica i gruppi dal server
     */
    private suspend fun loadGroups() {
        val token = UserSession.getAuthHeader()
        if (token != null) {
            try {
                val response = apiService.getGroups(token)
                if (response.isSuccessful && response.body() != null) {
                    val groups = response.body()!!

                    // Converti in oggetti ClassRoom
                    classes.value = groups.map { group ->
                        ClassRoom(
                            name = group.name,
                            backgroundImageId = R.drawable.happy_green_logo,
                            teacherName = "Insegnante ${group.id}" // Idealmente dovresti ottenere il nome dal server
                        )
                    }
                }
            } catch (e: Exception) {
                // In caso di errore, mantieni i dati di demo
                classes.value = listOf(
                    ClassRoom("Eco Science 101", R.drawable.happy_green_logo, "Prof. Smith"),
                    ClassRoom("Environmental Studies", R.drawable.happy_green_logo, "Prof. Johnson"),
                    ClassRoom("Green Technologies", R.drawable.happy_green_logo, "Prof. Martinez"),
                    ClassRoom("Sustainable Development", R.drawable.happy_green_logo, "Prof. Garcia")
                )
            }
        }
    }

    /**
     * Carica i dati dei giochi (statico per ora)
     */
    private fun loadGames() {
        availableGames.value = listOf(
            Game("eco_detective", "Eco Detective", "Smista i rifiuti nei cestini corretti", R.drawable.happy_green_logo),
            Game("eco_sfida", "Eco Sfida", "Confronta l'impatto ambientale", R.drawable.happy_green_logo),
            Game("tree_planter", "Tree Planter", "Simulatore di piantumazione", R.drawable.happy_green_logo)
        )
    }

    /**
     * Carica i badge dell'utente
     */
    private suspend fun loadUserBadges() {
        val token = UserSession.getAuthHeader()
        if (token != null) {
            try {
                // Ottieni i badge dell'utente
                val userBadgesResponse = apiService.getUserBadges(token)
                if (userBadgesResponse.isSuccessful && userBadgesResponse.body() != null) {
                    val userBadgesList = userBadgesResponse.body()!!

                    // Ottieni tutti i badge
                    val badgesResponse = apiService.getBadges(token)
                    if (badgesResponse.isSuccessful && badgesResponse.body() != null) {
                        val allBadges = badgesResponse.body()!!

                        // Filtra i badge che l'utente possiede
                        val userBadgeIds = userBadgesList.map { it.badgeId }
                        userBadges.value = allBadges.filter { it.id in userBadgeIds }
                    }
                }
            } catch (e: Exception) {
                // In caso di errore, usa badge di demo
                userBadges.value = listOf(
                    Badge(1, "Eco Starter", "Hai completato il primo quiz", ""),
                    Badge(2, "Green Thumb", "Hai piantato il tuo primo albero virtuale", "")
                )
            }
        }
    }

    /**
     * Imposta la tab corrente
     */
    fun setCurrentTab(tabIndex: Int) {
        currentTab.value = tabIndex
    }

    /**
     * Nasconde il banner dei giochi
     */
    fun hideGamesBanner() {
        showGamesBanner.value = false
    }

    /**
     * Crea una nuova classe
     */
    fun createClass(newClass: ClassRoom) {
        viewModelScope.launch {
            val token = UserSession.getAuthHeader()
            if (token != null) {
                try {
                    val userId = UserSession.getUserId()
                    if (userId != null) {
                        // Crea un oggetto gruppo
                        val group = Group(
                            name = newClass.name,
                            description = null,
                            ownerId = userId
                        )

                        // Chiamata API per creare il gruppo
                        val response = apiService.createGroup(group, token)
                        if (response.isSuccessful && response.body() != null) {
                            // Ricarica i gruppi
                            loadGroups()
                        }
                    }
                } catch (e: Exception) {
                    // Fallback a operazione locale se l'API fallisce
                    classes.value = classes.value + newClass
                }
            } else {
                // Fallback locale
                classes.value = classes.value + newClass
            }
        }
    }

    /**
     * Unisciti a una classe
     */
    fun joinClass(classRoom: ClassRoom) {
        viewModelScope.launch {
            val token = UserSession.getAuthHeader()
            if (token != null) {
                try {
                    val userId = UserSession.getUserId()
                    if (userId != null) {
                        // In un'app reale, qui faresti una chiamata API per unirsi al gruppo
                        // Per ora, simuliamo l'operazione
                    }
                } catch (e: Exception) {
                    // Gestione errore
                }
            }
        }
    }

    /**
     * Effettua il logout
     */
    fun logout() {
        UserSession.clear()
    }
}

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

// Main Screen
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
    var showLeaderboardScreen by remember { mutableStateOf(false) }  // Aggiunta questa variabile

    // Stato per Classroom Screen
    var showClassroomScreen by remember { mutableStateOf<ClassRoom?>(null) }

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
            onDismissRequest = { viewModel.errorMessage.value = null },
            title = { Text("Errore") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { viewModel.errorMessage.value = null }) {
                    Text("OK")
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
            onLeaderboardClick = { showLeaderboardScreen = true },  // Aggiungi questo parametro
            onHideGamesBanner = viewModel::hideGamesBanner
        )
    }

    // Dialogs
    if (showProfileDialog) {
        ProfileDialog(
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
    @Composable
    fun CreateClassDialog(
        onDismiss: () -> Unit,
        onClassCreated: (ClassRoom) -> Unit
    ) {
        // ... definizione CreateClassDialog ...
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

// Data model for badges in UI
data class BadgeItem(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String
)

// MainAppScaffold

@Composable
fun GameCard(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Immagine del gioco (dimensione aumentata)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Green100),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = game.iconId),
                    contentDescription = game.name,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Dettagli del gioco
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp // Dimensione testo aumentata
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 16.sp // Dimensione testo aumentata
                )

                Spacer(modifier = Modifier.weight(1f))

                // Pulsante Gioca (dimensione aumentata)
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600
                    ),
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(48.dp), // Altezza aumentata
                    shape = RoundedCornerShape(24.dp) // Bordi più arrotondati
                ) {
                    Text(
                        "Gioca",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun GamesScreen(
    games: List<Game>,
    onGameSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Barra superiore personalizzata con padding maggiore
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green600)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icona indietro
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Titolo
                Text(
                    text = "Giochi Eco-friendly",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Icona giochi (aggiunta vicino al titolo)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Giochi",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Contenuto con padding maggiore
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Padding aumentato
            verticalArrangement = Arrangement.spacedBy(24.dp) // Spazio tra gli elementi aumentato
        ) {
            item {
                Text(
                    text = "Scegli un gioco per divertirti e imparare",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green800,
                    fontSize = 18.sp // Dimensione testo aumentata
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(games) { game ->
                GameCard(
                    game = game,
                    onClick = { onGameSelected(game.id) }
                )
            }

            // Aggiungi spazio extra in fondo
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}



@Composable
fun MainAppScaffold(
    onLeaderboardClick: () -> Unit,
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
    onHideGamesBanner: () -> Unit,

) {
    var showEcoAIChat by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showGamesScreen by remember { mutableStateOf(false) }

    if (showEcoAIChat) {
        EcoAIChatScreen(onBack = { showEcoAIChat = false })
    } else if (showBarcodeScanner) {
        BarcodeScannerScreen(onBack = { showBarcodeScanner = false })
    } else if (showGamesScreen) {
        GamesScreen(
            games = gamesList,
            onGameSelected = { gameId ->
                showGamesScreen = false
                onGameSelected(gameId)
            },
            onBack = { showGamesScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    onProfileClick = onProfileClick,
                    onGamesClick = { showGamesScreen = true },
                    onLeaderboardClick = onLeaderboardClick,  // Assicurati che questo sia presente
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
                            onClick = {
                                // Gestione speciale per il tab scanner
                                if (index == 2) { // Scanner tab
                                    showBarcodeScanner = true
                                } else {
                                    onTabSelected(index)
                                }
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
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
                    .background(Color.White)
            ) {
                // Contenuto principale
                when {
                    isLoading -> CenteredLoader(message = "Caricamento in corso...")
                    else -> {
                        when (currentTab) {
                            0 -> HomeContent(classList = classList, onClassSelected = onClassSelected)
                            1 -> ExploreContent()
                            3 -> ProfileContent(
                                userName = userName,
                                userEmail = userEmail,
                                userPoints = userPoints,
                                userLevel = userLevel,
                                userBadges = userBadges
                            )
                            else -> HomeContent(classList = classList, onClassSelected = onClassSelected)
                        }
                    }
                }
            }
        }
    }
}

    // Top Bar
    @Composable
    public fun AppTopBar(
        onProfileClick: () -> Unit,
        onGamesClick: () -> Unit,
        onLeaderboardClick: () -> Unit,  // Aggiunto questo parametro
        hasNotifications: Boolean = false,
        notificationCount: Int = 0
    ): Unit {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            color = Green600
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 42.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row with logo and actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // App logo and name with larger size
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.happy_green_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "HappyGreen",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Action buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Leaderboard button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable(onClick = onLeaderboardClick)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Classifica",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Games button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable(onClick = onGamesClick)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Giochi",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Notifications
                        if (hasNotifications) {
                            NotificationIcon(count = notificationCount)
                        }

                        // Profile
                        UserAvatar(onClick = onProfileClick)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

// Updated UserAvatar function with larger size
@Composable
fun UserAvatar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp) // Increased from 46dp
            .clip(CircleShape)
            .background(Color.LightGray)
            .border(2.dp, Color.White, CircleShape)
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

// Updated NotificationIcon function with larger size
@Composable
fun NotificationIcon(count: Int) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(48.dp) // Increased size to match avatar
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { /* Handle notifications */ }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifiche",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(22.dp) // Increased size
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp // Slightly increased
                )
            }
        }
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
// Updated ProfileContent to use real user data
    @Composable
    fun ProfileContent(
        userName: String,
        userEmail: String,
        userPoints: Int,
        userLevel: String,
        userBadges: List<BadgeItem>
    ) {
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
                    text = userName,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Points card
                PointsCard(points = userPoints, level = userLevel)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Badges section
                SectionHeader(title = "Le mie badge")
                Spacer(modifier = Modifier.height(8.dp))

                if (userBadges.isEmpty()) {
                    Text(
                        text = "Non hai ancora guadagnato nessuna badge",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    BadgesRow(badges = userBadges)
                }

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
fun BadgesRow(badges: List<BadgeItem>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(badges.size) { index ->
            val badge = badges[index]
            BadgeItemUI(name = badge.name, iconId = R.drawable.happy_green_logo)
        }
    }
}

@Composable
fun BadgeItemUI(name: String, iconId: Int) {
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

// Updated ProfileDialog to use real user data
@Composable
fun ProfileDialog(
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
                    text = userName,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Points card
                PointsCard(points = userPoints, level = userLevel)

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