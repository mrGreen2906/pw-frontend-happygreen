package com.example.frontend_happygreen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.frontend_happygreen.ui.components.*
import com.example.frontend_happygreen.ui.theme.*
import kotlinx.coroutines.launch

/**
 * ViewModel per la MainScreen
 * Centralizza tutta la logica e lo stato dell'UI
 */
class MainScreenViewModel : ViewModel() {
    // Dati dell'utente
    var userName = mutableStateOf("John Doe")
    var userEmail = mutableStateOf("john.doe@example.com")
    var userPoints = mutableStateOf(1250)
    var userLevel = mutableStateOf("Eco Warrior")

    // Stato di caricamento
    var isLoading = mutableStateOf(true)

    // Gestione delle classi
    var classes = mutableStateOf<List<ClassRoom>>(emptyList())

    // Gestione dei giochi disponibili
    var availableGames = mutableStateOf<List<Game>>(emptyList())

    // Stato di visibilità del banner dei giochi
    var showGamesBanner = mutableStateOf(true)

    // Stato attuale dell'app
    var currentTab = mutableStateOf(0)

    // Notifiche
    var hasNotifications = mutableStateOf(true)
    var notificationCount = mutableStateOf(3)

    init {
        // Caricamento iniziale dei dati
        loadInitialData()
    }

    /**
     * Simula il caricamento dei dati iniziali
     */
    private fun loadInitialData() {
        // Nella versione reale, questa funzione farebbe chiamate API
        // Qui inizializziamo semplicemente i dati di esempio

        classes.value = listOf(
            ClassRoom("Eco Science 101", R.drawable.happy_green_logo, "Prof. Smith"),
            ClassRoom("Environmental Studies", R.drawable.happy_green_logo, "Prof. Johnson"),
            ClassRoom("Green Technologies", R.drawable.happy_green_logo, "Prof. Martinez"),
            ClassRoom("Sustainable Development", R.drawable.happy_green_logo, "Prof. Garcia")
        )

        availableGames.value = listOf(
            Game(
                id = "eco_detective",
                name = "Eco Detective",
                description = "Smista i rifiuti nei cestini corretti",
                iconId = R.drawable.happy_green_logo
            ),
            Game(
                id = "eco_sfida",
                name = "Eco Sfida",
                description = "Confronta l'impatto ambientale",
                iconId = R.drawable.happy_green_logo
            ),
            Game(
                id = "tree_planter",
                name = "Tree Planter",
                description = "Simulatore di piantumazione",
                iconId = R.drawable.happy_green_logo
            )
        )

        // Simuliamo un breve caricamento
        isLoading.value = false
    }

    /**
     * Cambia il tab corrente
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
        classes.value = classes.value + newClass
    }

    /**
     * Simula l'unione ad una classe esistente
     */
    fun joinClass(classRoom: ClassRoom) {
        // In una versione reale, questa funzione farebbe una chiamata API
        // per unirsi alla classe e aggiornare il backend
    }
}

/**
 * Modello per una classe
 */
data class ClassRoom(
    val name: String,
    val backgroundImageId: Int,
    val teacherName: String = "Unknown Teacher"
)

/**
 * Modello per un gioco
 */
data class Game(
    val id: String,
    val name: String,
    val description: String,
    val iconId: Int
)

/**
 * Componente principale della schermata principale
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    volumeLevel: Float = 0.5f,
    onVolumeChange: (Float) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MainScreenViewModel = viewModel()
) {
    // Stati dal ViewModel
    val currentTab by viewModel.currentTab
    val isLoading by viewModel.isLoading
    val showGamesBanner by viewModel.showGamesBanner
    val classList by viewModel.classes
    val gamesList by viewModel.availableGames
    val hasNotifications by viewModel.hasNotifications
    val notificationCount by viewModel.notificationCount

    // Stati locali per i dialoghi e i giochi
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCreateClassDialog by remember { mutableStateOf(false) }
    var showEcoDetectiveGame by remember { mutableStateOf(false) }
    var showEcoSfidaGame by remember { mutableStateOf(false) }

    val tabItems = listOf("Home", "Esplora", "Profilo")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Search,
        Icons.Default.Person
    )

    val coroutineScope = rememberCoroutineScope()

    if (showEcoDetectiveGame) {
        // Mostra schermata del gioco EcoDetective
        EcoDetectiveGameScreen(
            onBack = { showEcoDetectiveGame = false }
        )
    } else if (showEcoSfidaGame) {
        // Mostra il gioco EcoSfida
        EcoGameScreen(
        )
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    onProfileClick = { showProfileDialog = true },
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
                            onClick = { viewModel.setCurrentTab(index) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentTab == 0) {
                    FloatingActionButton(
                        onClick = { showCreateClassDialog = true },
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
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White),
            ) {
                // Contenuto principale basato sul tab selezionato
                when {
                    isLoading -> {
                        CenteredLoader(message = "Caricamento in corso...")
                    }
                    else -> {
                        when (currentTab) {
                            0 -> HomeContent(
                                classList = classList,
                                onClassSelected = { viewModel.joinClass(it) }
                            )
                            1 -> ExploreContent()
                            2 -> ProfileContent()
                            else -> HomeContent(
                                classList = classList,
                                onClassSelected = { viewModel.joinClass(it) }
                            )
                        }
                    }
                }

                // Banner dei giochi in basso
                if (showGamesBanner) {
                    GamesBanner(
                        games = gamesList,
                        onGameSelected = { gameId ->
                            when (gameId) {
                                "eco_detective" -> showEcoDetectiveGame = true
                                "eco_sfida" -> showEcoSfidaGame = true
                                // Altri giochi verrebbero gestiti qui
                            }
                        },
                        onDismiss = { viewModel.hideGamesBanner() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            // Dialoghi
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
    }
}

/**
 * Top bar dell'applicazione
 */
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

                // Notifiche
                if (hasNotifications) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable { /* Gestisci notifiche */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifiche",
                            tint = Color.White
                        )

                        // Badge contatore notifiche
                        if (notificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .background(Color.Red, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(1.dp, Color.White, CircleShape)
                        .clickable { onProfileClick() }
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
            containerColor = Green600
        )
    )
}

/**
 * Home Content - Mostra le classi e le attività principali
 */
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
            // Welcome header con animazione
            WelcomeHeader()

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Sezione classi
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

        // Lista delle classi
        items(
            items = classList,
            key = { it.name } // Usa chiave univoca per migliorare la performance
        ) { classRoom ->
            ClassCard(
                classRoom = classRoom,
                onClick = { onClassSelected(classRoom) }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            // Sezione attività
            SectionHeader(title = "Attività recenti")

            Spacer(modifier = Modifier.height(8.dp))

            // Card per le attività
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

            // Padding extra in fondo per evitare sovrapposizioni con il banner
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Header di benvenuto
 */
@Composable
fun WelcomeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Green100,
                shape = RoundedCornerShape(16.dp)
            )
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

        // Contenuto dell'esplorazione
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Trova eventi eco-friendly",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Scopri eventi di pulizia, piantumazione e sensibilizzazione ambientale nella tua zona.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Trova altri eco-warriors",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connettiti con altri appassionati di sostenibilità per attività e collaborazioni.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Card per attività recenti
 */
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
            // Icona
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

            // Contenuto
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

/**
 * Card per una classe
 */
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
            // Immagine di sfondo
            Image(
                painter = painterResource(id = classRoom.backgroundImageId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay semitrasparente per migliorare la leggibilità del testo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            // Info della classe
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

/**
 * Banner dei giochi
 */
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

            // Lista orizzontale dei giochi
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

/**
 * Card compatta per un gioco
 */
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
            // Icona del gioco
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

            // Info del gioco
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

/**
 * Contenuto della scheda profilo
 */
@Composable
fun ProfileContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Avatar dell'utente
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
            // Card con statistiche
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
                        text = "1250",
                        style = MaterialTheme.typography.displaySmall,
                        color = Green600
                    )

                    Text(
                        text = "Livello: Eco Warrior",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green800
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Sezioni del profilo
            SectionHeader(title = "Le mie badge")

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(8) { index ->
                    BadgeItem(
                        name = "Badge ${index + 1}",
                        iconId = R.drawable.happy_green_logo
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionHeader(title = "Le mie statistiche")

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Più spazio in fondo per il banner dei giochi
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun BadgeItem(
    name: String,
    iconId: Int
) {
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
fun StatisticsItem(
    title: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icona
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Green600,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Titolo
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Valore
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Green800,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Dialogo profilo utente
 */
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
                    Icon(Icons.Default.Close, contentDescription = "Chiudi")
                }
            }

            // Immagine profilo
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

            // Nome utente
            Text(
                text = "John Doe",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email utente
            Text(
                text = "john.doe@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Green Points
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Green100
                )
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
                        text = "1250",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Green600
                    )

                    Text(
                        text = "Livello: Eco Warrior",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green800
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Controllo volume musica
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

            // Pulsante Logout
            HappyGreenDangerButton(
                text = "Logout",
                onClick = onLogout,
                icon = Icons.Default.Logout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Dialogo creazione nuova classe
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassDialog(
    onDismiss: () -> Unit,
    onClassCreated: (ClassRoom) -> Unit
) {
    var className by remember { mutableStateOf("") }
    var selectedBackgroundIndex by remember { mutableStateOf(0) }
    var showBackgroundOptions by remember { mutableStateOf(false) }

    // Background options (in una vera app, sarebbero immagini diverse)
    val backgroundOptions = listOf(
        R.drawable.happy_green_logo,
        R.drawable.happy_green_logo,
        R.drawable.happy_green_logo,
        R.drawable.happy_green_logo
    )

    Dialog(onDismissRequest = onDismiss) {
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

                // Input nome classe
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Nome Classe") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selezione sfondo
                Text(
                    text = "Scegli Sfondo",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Anteprima dello sfondo selezionato
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

                    // Testo indicante che è cliccabile
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

                // Dropdown opzioni sfondo
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenu(
                        expanded = showBackgroundOptions,
                        onDismissRequest = { showBackgroundOptions = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        backgroundOptions.forEachIndexed { index, backgroundId ->
                            DropdownMenuItem(
                                text = { Text("Sfondo ${index + 1}") },
                                leadingIcon = {
                                    Image(
                                        painter = painterResource(id = backgroundId),
                                        contentDescription = "Sfondo ${index + 1}",
                                        modifier = Modifier.size(40.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                onClick = {
                                    selectedBackgroundIndex = index
                                    showBackgroundOptions = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsante creazione
                HappyGreenButton(
                    text = "Crea Classe",
                    onClick = {
                        if (className.isNotBlank()) {
                            val newClass = ClassRoom(
                                name = className,
                                backgroundImageId = backgroundOptions[selectedBackgroundIndex]
                            )
                            onClassCreated(newClass)
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = className.isNotBlank()
                )
            }
        }
    }
}