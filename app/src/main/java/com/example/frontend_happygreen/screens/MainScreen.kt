// MainScreen.kt aggiornato con classifica integrata ed eco-centers

package com.example.frontend_happygreen.screens

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.barcode.BarcodeScannerScreen
import com.example.frontend_happygreen.data.Badge
import com.example.frontend_happygreen.data.Group
import com.example.frontend_happygreen.data.Post
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.games.EcoDetectiveGameScreen
import com.example.frontend_happygreen.games.EcoGameScreen
import com.example.frontend_happygreen.ui.components.CenteredLoader
import com.example.frontend_happygreen.ui.components.SectionHeader
import com.example.frontend_happygreen.ui.theme.Blue500
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green300
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    var currentTab = mutableStateOf(0)
    var hasNotifications = mutableStateOf(false)
    var notificationCount = mutableStateOf(0)

    // Post dell'utente
    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())

    // Per i punteggi
    var leaderboardData = mutableStateOf<List<LeaderboardItems>>(emptyList())
    var isLoadingLeaderboard = mutableStateOf(false)
    var selectedGameForLeaderboard = mutableStateOf<String?>(null)

    // Flag per tenere traccia se la classifica è stata inizializzata
    private var leaderboardInitialized = false
    init {
        viewModelScope.launch {
            UserSession.isLoggedInFlow.collect { isLoggedIn ->
                if (isLoggedIn) {
                    // Quando l'utente effettua l'accesso, ricarica i dati
                    loadUserData()
                    loadInitialData()
                    loadLeaderboard()
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            loadUserData()
            loadInitialData()
            loadLeaderboard()
        }
    }


    data class LeaderboardItems(
        val id: Int,
        val username: String,
        val score: Int,
        val avatar: String? = null,
        val isCurrentUser: Boolean = false
    )

    private fun loadLeaderboard(gameId: String? = null) {
        isLoadingLeaderboard.value = true
        selectedGameForLeaderboard.value = gameId
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: run {
                    errorMessage.value = "Devi accedere per visualizzare la classifica"
                    isLoadingLeaderboard.value = false
                    return@launch
                }

                val response = if (gameId != null) {
                    apiService.getLeaderboard(token, gameId)
                } else {
                    apiService.getGlobalLeaderboard(token)
                }

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val currentUserId = UserSession.getUserId() ?: -1

                    leaderboardData.value = data.map { item ->
                        LeaderboardItems(
                            id = item.userId,
                            username = item.username,
                            score = (if (gameId != null) item.score else item.ecoPoints) ?: 0,
                            avatar = item.avatar,
                            isCurrentUser = item.userId == currentUserId
                        )
                    }

                    leaderboardInitialized = true
                } else {
                    errorMessage.value = "Errore nel caricamento della classifica: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Errore di connessione: ${e.message}"
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
        val error by viewModel.errorMessage

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

        // Se la classifica non è stata ancora inizializzata, la carichiamo
        LaunchedEffect(Unit) {
            if (!leaderboardInitialized) {
                viewModel.loadLeaderboard(games[selectedTab].first)
            }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Green600)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Caricamento classifica in corso...")
                        }
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error!!,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { loadLeaderboard(games[selectedTab].first) },
                                colors = ButtonDefaults.buttonColors(containerColor = Green600)
                            ) {
                                Text("Riprova")
                            }
                        }
                    }
                    leaderboardData.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Nessun dato disponibile per questa classifica",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        LazyColumn {
                            itemsIndexed(leaderboardData) { index, item ->
                                LeaderboardItem(
                                    rank = index + 1,
                                    username = item.username,
                                    score = item.score,
                                    avatar = item.avatar,
                                    isCurrentUser = item.isCurrentUser
                                )
                                Divider()
                            }
                        }
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
        val backgroundColor = if (isCurrentUser) Green100 else Color.Transparent
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
                    } else if (response.code() == 401) {
                        // Token non valido, forse scaduto
                        errorMessage.value = "Sessione scaduta. Effettua nuovamente l'accesso."
                        // In questo caso potremmo anche reindirizzare l'utente al login
                    } else {
                        errorMessage.value = "Errore nel caricamento dei dati: ${response.code()}"
                        // Prova a usare i dati memorizzati localmente
                        fallbackToLocalUserData()
                    }
                } else {
                    // Nessun token, usa i dati memorizzati se disponibili
                    fallbackToLocalUserData()
                }
            } catch (e: Exception) {
                errorMessage.value = "Errore di connessione: ${e.message}"
                // In caso di errore di rete, usa i dati memorizzati localmente
                fallbackToLocalUserData()
            }
        }
    }

    // Metodo per utilizzare i dati locali in caso di problemi di rete
    private fun fallbackToLocalUserData() {
        // Utilizza i dati già memorizzati in UserSession
        userName.value = UserSession.getFullName()
        userEmail.value = UserSession.getEmail() ?: ""
        userPoints.value = UserSession.getEcoPoints() ?: 0
        userLevel.value = calculateLevel(userPoints.value)
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
                // CORRETTO: Carica solo i gruppi dell'utente autenticato
                loadUserGroups()

                // Carica giochi (dato statico per ora)
                loadGames()

                isLoading.value = false
            } catch (e: Exception) {
                errorMessage.value = "Errore nel caricamento dei dati: ${e.message}"
                isLoading.value = false
            }
        }
    }
    fun loadUserGroups() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val token = UserSession.getAuthHeader() ?: run {
                    Log.e("MainScreenViewModel", "No auth token available")
                    errorMessage.value = "Devi effettuare l'accesso per visualizzare i gruppi"
                    isLoading.value = false
                    return@launch
                }

                Log.d("MainScreenViewModel", "Loading groups for authenticated user...")

                // Usa l'endpoint aggiornato che restituisce GroupWithCounters
                val response = apiService.getMyGroups(token)

                if (response.isSuccessful && response.body() != null) {
                    val userGroupsWithCounters = response.body()!!

                    Log.d("MainScreenViewModel", "Received ${userGroupsWithCounters.size} groups for user")

                    // Conversione con i contatori reali
                    classes.value = userGroupsWithCounters.mapNotNull { groupData ->
                        val groupId = groupData.id
                        if (groupId == null || groupId <= 0) {
                            Log.w("MainScreenViewModel", "Skipping group with invalid ID: ${groupData.name}")
                            return@mapNotNull null
                        }

                        val currentUserId = UserSession.getUserId()
                        val isOwner = groupData.owner == currentUserId
                        val teacherName = if (isOwner) {
                            "Tu (Proprietario)"
                        } else {
                            groupData.owner_name ?: "Proprietario sconosciuto"
                        }

                        ClassRoom(
                            id = groupId,
                            name = groupData.name,
                            description = groupData.description ?: "",
                            backgroundImageId = R.drawable.pattern_1,
                            teacherName = teacherName,
                            // USA I CONTATORI REALI DAL BACKEND
                            memberCount = groupData.member_count ?: 0,
                            postCount = groupData.post_count ?: 0,
                            ownerID = groupData.owner,
                            userRole = if (isOwner) "admin" else "member"
                        )
                    }.also { classRoomList ->
                        Log.d("MainScreenViewModel", "Mapped ${classRoomList.size} ClassRoom objects with real counters:")
                        classRoomList.forEach { classRoom ->
                            Log.d("MainScreenViewModel",
                                "ClassRoom: ID=${classRoom.id}, Name=${classRoom.name}, " +
                                        "Members=${classRoom.memberCount}, Posts=${classRoom.postCount}")
                        }
                    }

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Errore sconosciuto"
                    Log.e("MainScreenViewModel", "Errore caricamento gruppi: ${response.code()} - $errorBody")

                    when (response.code()) {
                        401 -> errorMessage.value = "Sessione scaduta, effettua nuovamente l'accesso"
                        403 -> errorMessage.value = "Non hai i permessi per accedere ai gruppi"
                        404 -> errorMessage.value = "Endpoint dei gruppi non trovato"
                        else -> errorMessage.value = "Errore nel caricamento dei gruppi: ${response.code()}"
                    }

                    classes.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("MainScreenViewModel", "Eccezione nel caricamento gruppi: ${e.message}", e)
                errorMessage.value = when {
                    e.message?.contains("timeout") == true -> "Tempo di attesa scaduto. Riprova."
                    e.message?.contains("network") == true -> "Errore di rete. Controlla la connessione."
                    else -> "Errore di connessione: ${e.message}"
                }
                classes.value = emptyList()
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Unisciti a un gruppo esistente
     */
    fun joinGroup(groupId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: run {
                    onError("Devi effettuare l'accesso per unirti a un gruppo")
                    return@launch
                }

                Log.d("MainScreenViewModel", "Attempting to join group with ID: $groupId")

                val response = apiService.joinGroup(groupId, token)

                if (response.isSuccessful && response.body() != null) {
                    Log.d("MainScreenViewModel", "Successfully joined group")
                    // Ricarica i gruppi dell'utente
                    loadUserGroups()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("MainScreenViewModel", "Failed to join group: ${response.code()} - $errorBody")

                    val errorMsg = when (response.code()) {
                        400 -> {
                            when {
                                errorBody.contains("già membro") -> "Sei già membro di questo gruppo"
                                else -> "Richiesta non valida"
                            }
                        }
                        403 -> "Non hai i permessi per unirti a questo gruppo"
                        404 -> "Gruppo non trovato"
                        else -> "Errore nell'unirsi al gruppo: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("MainScreenViewModel", "Exception joining group: ${e.message}", e)
                onError("Errore di connessione: ${e.message}")
            }
        }
    }

    fun searchGroups(query: String, onSuccess: (List<ClassRoom>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: return@launch

                Log.d("MainScreenViewModel", "Searching groups with query: $query")

                val response = apiService.getGroups(token)

                if (response.isSuccessful && response.body() != null) {
                    val allGroups = response.body()!!

                    Log.d("MainScreenViewModel", "Retrieved ${allGroups.size} total groups")

                    // Ottieni i gruppi di cui l'utente è già membro
                    val myGroupsResponse = apiService.getMyGroups(token)
                    val myGroupIds = if (myGroupsResponse.isSuccessful && myGroupsResponse.body() != null) {
                        myGroupsResponse.body()!!.mapNotNull { it.id }.toSet()
                    } else {
                        emptySet()
                    }

                    // Filtra i gruppi in base alla query e escludi quelli dell'utente
                    val filteredGroups = allGroups.filter { group ->
                        val matchesQuery = group.name.contains(query, ignoreCase = true) ||
                                (group.description?.contains(query, ignoreCase = true) ?: false)
                        val notMyGroup = group.id !in myGroupIds
                        matchesQuery && notMyGroup
                    }

                    Log.d("MainScreenViewModel", "Found ${filteredGroups.size} matching groups")

                    // Converti in ClassRoom con contatori reali
                    val groupsAsClassRooms = filteredGroups.mapNotNull { groupData ->
                        val groupId = groupData.id ?: return@mapNotNull null
                        if (groupId <= 0) return@mapNotNull null

                        ClassRoom(
                            id = groupId,
                            name = groupData.name,
                            description = groupData.description ?: "",
                            backgroundImageId = R.drawable.happy_green_logo,
                            teacherName = groupData.owner_name ?: "Proprietario: ID ${groupData.owner}",
                            // USA I CONTATORI REALI
                            memberCount = groupData.member_count ?: 0,
                            postCount = groupData.post_count ?: 0
                        )
                    }

                    onSuccess(groupsAsClassRooms)
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("MainScreenViewModel", "Error searching groups: ${response.code()} - $errorBody")
                    onError("Errore nella ricerca dei gruppi: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MainScreenViewModel", "Exception searching groups: ${e.message}", e)
                onError("Errore di connessione: ${e.message}")
            }
        }
    }

    fun createGroup(newGroup: ClassRoom, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                isLoading.value = true

                val token = UserSession.getAuthHeader() ?: run {
                    onError("Devi effettuare l'accesso per creare un gruppo")
                    isLoading.value = false
                    return@launch
                }

                val userId = UserSession.getUserId() ?: run {
                    onError("ID utente non disponibile")
                    isLoading.value = false
                    return@launch
                }

                // Crea oggetto gruppo per API
                val group = Group(
                    name = newGroup.name,
                    description = newGroup.description,
                    ownerId = userId
                )

                Log.d("MainScreenViewModel", "Creating group: ${group.name} for user: $userId")

                // Chiamata API per creare il gruppo
                val response = apiService.createGroup(group, token)

                if (response.isSuccessful && response.body() != null) {
                    val createdGroup = response.body()!!
                    Log.d("MainScreenViewModel", "Group created successfully with ID: ${createdGroup.id}")

                    // Ricarica tutti i gruppi dell'utente
                    loadUserGroups()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("MainScreenViewModel", "Failed to create group: ${response.code()} - $errorBody")

                    val errorMsg = when (response.code()) {
                        400 -> "Dati non validi. Verifica il nome del gruppo."
                        401 -> "Sessione scaduta. Effettua nuovamente il login."
                        403 -> "Non hai i permessi per creare gruppi."
                        else -> "Errore nella creazione del gruppo: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("MainScreenViewModel", "Exception creating group: ${e.message}", e)
                onError("Errore di connessione: ${e.message ?: "Sconosciuto"}")
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun loadGames() {
        availableGames.value = listOf(
            Game("eco_detective", "Eco Detective", "Smista i rifiuti nei cestini corretti", R.drawable.detective),
            Game("eco_sfida", "Eco Sfida", "Confronta l'impatto ambientale", R.drawable.ecogames),
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
     * Effettua il logout
     */
    fun logout() {
        // Cancella completamente la sessione utente
        UserSession.clear()

        // Resetta tutti gli stati del ViewModel
        userName.value = ""
        userEmail.value = ""
        userPoints.value = 0
        userLevel.value = calculateLevel(0)
        classes.value = emptyList()
        userBadges.value = emptyList()
        leaderboardData.value = emptyList()
    }
}

typealias ClassRoom = com.example.frontend_happygreen.data.ClassRoom

data class Game(
    val id: String,
    val name: String,
    val description: String,
    val iconId: Int
)

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

    // UI States
    var showGroupSearchScreen by remember { mutableStateOf(false) }
    var showEcoDetectiveGame by remember { mutableStateOf(false) }
    var showEcoSfidaGame by remember { mutableStateOf(false) }
    var showLeaderboardScreen by remember { mutableStateOf(false) }
    var showEcoCenterMap by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showClassroomScreen by remember { mutableStateOf<ClassRoom?>(null) }
    var showCreateClassDialog by remember { mutableStateOf(false) }

    // States from ViewModel
    val currentTab by viewModel.currentTab
    val isLoading by viewModel.isLoading
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

    val tabItems = listOf("Home", "Esplora", "Scanner")
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
                Button(
                    onClick = { viewModel.errorMessage.value = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Different screens based on user navigation
    when {
        showGroupSearchScreen -> {
            GroupSearchScreen(
                onBack = { showGroupSearchScreen = false },
                viewModel = viewModel
            )
        }
        showLeaderboardScreen -> viewModel.LeaderboardScreen(
            onBack = { showLeaderboardScreen = false }
        )
        showEcoCenterMap -> EcoCentersMapScreen(
            onBack = { showEcoCenterMap = false }
        )
        showClassroomScreen != null -> {
            // CORRETTO: Verifica l'ID prima di navigare
            val classroom = showClassroomScreen!!
            if (classroom.id > 0) {
                ClassroomScreen(
                    classRoom = classroom,
                    onBack = {
                        Log.d("MainScreen", "Closing classroom: ${classroom.name} (ID: ${classroom.id})")
                        showClassroomScreen = null
                    }
                )
            } else {
                // ID non valido, mostra errore e torna alla lista
                LaunchedEffect(Unit) {
                    viewModel.errorMessage.value = "Gruppo non valido: ID = ${classroom.id}"
                    showClassroomScreen = null
                }
            }
        }
        showEcoDetectiveGame -> EcoDetectiveGameScreen(onBack = { showEcoDetectiveGame = false })
        showEcoSfidaGame -> EcoGameScreen(onBack = { showEcoSfidaGame = false })
        showBarcodeScanner -> BarcodeScannerScreen(onBack = { showBarcodeScanner = false })
        else -> MainAppScaffold(
            currentTab = currentTab,
            tabItems = tabItems,
            icons = icons,
            hasNotifications = hasNotifications,
            notificationCount = notificationCount,
            isLoading = isLoading,
            classList = classList,
            gamesList = gamesList,
            userName = userName,
            userEmail = userEmail,
            userPoints = userPoints,
            userLevel = userLevel,
            onTabSelected = viewModel::setCurrentTab,
            onProfileClick = { showProfileDialog = true },
            onCreateClassClick = { showCreateClassDialog = true },
            onJoinClassClick = { showGroupSearchScreen = true },
            onClassSelected = { classroom ->
                // CORRETTO: Log e validazione prima della navigazione
                Log.d("MainScreen", "Class selected: ${classroom.name} with ID: ${classroom.id}")
                if (classroom.id > 0 && classroom.isValid()) {
                    showClassroomScreen = classroom
                } else {
                    Log.e("MainScreen", "Invalid classroom: ID=${classroom.id}, Name=${classroom.name}")
                    viewModel.errorMessage.value = "Gruppo non valido. Riprova."
                }
            },
            onGameSelected = { gameId ->
                when (gameId) {
                    "eco_detective" -> showEcoDetectiveGame = true
                    "eco_sfida" -> showEcoSfidaGame = true
                }
            },
            onBarcodeScanClick = { showBarcodeScanner = true },
            onLeaderboardClick = { showLeaderboardScreen = true },
            onEcoCenterMapClick = { showEcoCenterMap = true }
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
    if (showCreateClassDialog) {
        CreateClassDialog(
            onDismiss = { showCreateClassDialog = false },
            onClassCreated = { classRoom ->
                coroutineScope.launch {
                    viewModel.createGroup(
                        newGroup = classRoom,
                        onSuccess = {
                            Log.d("MainScreen", "Group created successfully: ${classRoom.name}")
                        },
                        onError = { errorMsg ->
                            Log.e("MainScreen", "Error creating group: $errorMsg")
                            viewModel.errorMessage.value = errorMsg
                        }
                    )
                    showCreateClassDialog = false
                }
            }
        )
    }
}

/**
 * CORRETTO: HomeContent aggiornato con gestione migliore degli errori
 */
@Composable
fun HomeContent(
    classList: List<ClassRoom>,
    onClassSelected: (ClassRoom) -> Unit,
    onCreateGroupClick: () -> Unit = {},
    onJoinGroupClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "welcome_header") {
            WelcomeHeader()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item(key = "groups_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "I tuoi gruppi",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green800,
                    fontWeight = FontWeight.Bold
                )

                // Singolo pulsante FAB con menu a comparsa
                Box {
                    var expanded by remember { mutableStateOf(false) }

                    FloatingActionButton(
                        onClick = { expanded = true },
                        containerColor = Green600,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Gruppi",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GroupAdd,
                                        contentDescription = null,
                                        tint = Green600
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cerca Gruppo")
                                }
                            },
                            onClick = {
                                expanded = false
                                onJoinGroupClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Green600
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Crea Gruppo")
                                }
                            },
                            onClick = {
                                expanded = false
                                onCreateGroupClick()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (classList.isEmpty()) {
            item(key = "empty_state") {
                EmptyGroupsCard()
            }
        } else {
            // Usa itemsIndexed con chiave stabile e unica
            itemsIndexed(
                items = classList,
                key = { index, classRoom ->
                    // Chiave unica che combina ID e index come fallback
                    "class_${classRoom.id}_${classRoom.name}_$index"
                }
            ) { index, classRoom ->
                // Validazione prima di mostrare la carta
                if (classRoom.isValid()) {
                    EnhancedClassCard(
                        classRoom = classRoom,
                        onClick = {
                            Log.d("HomeContent", "Clicked class: ${classRoom.name} with ID: ${classRoom.id}")
                            onClassSelected(classRoom)
                        }
                    )

                    if (index < classList.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    // Se il gruppo non è valido, mostra un placeholder di errore
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Gruppo non valido",
                                tint = Color.Red
                            )
                            Text(
                                text = "Gruppo non valido: ${classRoom.name}",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "ID: ${classRoom.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }

        // Aggiungere un FAB fisso in basso per creare/unirsi a gruppi
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Spazio in fondo per evitare sovrapposizioni
        }
    }
}

@Composable
private fun EmptyGroupsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Green100),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icona più grande e con animazione
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Green300.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Green600,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Non sei ancora iscritto a nessun gruppo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Usa il pulsante + in alto per creare un nuovo gruppo o unirti a un gruppo esistente.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            // Aggiunta illustrazione o animazione
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.happy_green_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .alpha(0.7f),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun EnhancedClassCard(
    classRoom: ClassRoom,
    onClick: () -> Unit
) {
    // Validazione preventiva
    val isValid = classRoom.isValid()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Altezza leggermente aumentata
            .clickable(enabled = isValid, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) Color.White else Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Immagine di sfondo
            Image(
                painter = painterResource(id = classRoom.backgroundImageId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = if (isValid) 1f else 0.5f
            )

            // Overlay con gradiente per migliorare la leggibilità
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Contenuto
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge superiore in una row per allineamento
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isValid) Green600 else Color.Gray
                        ),
                        shape = RoundedCornerShape(50.dp) // Bordi più arrotondati
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isValid) "Gruppo Attivo" else "Gruppo Non Valido",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Mostra ID per debug se non valido
                    if (!isValid) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ID: ${classRoom.id}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Informazioni principali
                Column {
                    Text(
                        text = classRoom.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Badge con icona per il teacherName
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (classRoom.userRole == "admin")
                                Icons.Default.Person else Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = classRoom.teacherName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Descrizione se presente
                    if (classRoom.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = classRoom.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AGGIORNATO: Icone informative con contatori reali
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${classRoom.memberCount} membri",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    // AGGIORNATO: Usa il contatore reale dei post
                                    text = "${classRoom.postCount} post",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateClassDialog(
    onDismiss: () -> Unit,
    onClassCreated: (ClassRoom) -> Unit
) {
    var className by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crea Nuovo Gruppo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Green800
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Nome Gruppo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = className.isBlank() && isCreating
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione (opzionale)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annulla")
                    }

                    Button(
                        onClick = {
                            if (className.isNotBlank()) {
                                isCreating = true
                                val newClass = ClassRoom(
                                    id = 0, // Sarà assegnato dal server
                                    name = className.trim(),
                                    description = description.trim(),
                                    backgroundImageId = R.drawable.happy_green_logo,
                                    teacherName = "Tu (Proprietario)",
                                    memberCount = 1,
                                    ownerID = UserSession.getUserId()
                                )
                                onClassCreated(newClass)
                            }
                        },
                        enabled = className.isNotBlank() && !isCreating,
                        colors = ButtonDefaults.buttonColors(containerColor = Green600)
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Crea")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MainAppScaffold(
    currentTab: Int,
    tabItems: List<String>,
    icons: List<ImageVector>,
    hasNotifications: Boolean,
    notificationCount: Int,
    isLoading: Boolean,
    classList: List<ClassRoom>,
    gamesList: List<Game>,
    userName: String,
    userEmail: String,
    userPoints: Int,
    userLevel: String,
    onTabSelected: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onCreateClassClick: () -> Unit,
    onJoinClassClick: () -> Unit,
    onClassSelected: (ClassRoom) -> Unit,
    onGameSelected: (String) -> Unit,
    onBarcodeScanClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit,
    onEcoCenterMapClick: () -> Unit = {}
) {
    var showGamesScreen by remember { mutableStateOf(false) }
    var selectedClassroom by remember { mutableStateOf<ClassRoom?>(null) }
    var showEcoCenterMapState by remember { mutableStateOf(false) }
    var showEcoAIChat by remember { mutableStateOf(false) }
    // Log per debug
    LaunchedEffect(selectedClassroom) {
        selectedClassroom?.let { classroom ->
            Log.d("MainAppScaffold", "Navigating to classroom: ${classroom.name} with ID: ${classroom.id}")
        }
    }

    // When the Explore tab (index 1) is selected, automatically show the map
    LaunchedEffect(currentTab) {
        showEcoCenterMapState = currentTab == 1
    }

    // Handle different screens based on navigation state
    when {
        showEcoAIChat -> {
            EcoAIChatScreen(onBack = { showEcoAIChat = false })
        }
        showGamesScreen -> {
            GamesScreen(
                games = gamesList,
                onGameSelected = { gameId ->
                    showGamesScreen = false
                    onGameSelected(gameId)
                },
                onBack = { showGamesScreen = false }
            )
        }
        showEcoCenterMapState -> {
            EcoCentersMapScreen(
                onBack = {
                    showEcoCenterMapState = false
                    onTabSelected(0) // Return to Home tab after closing the map
                }
            )
        }
        selectedClassroom != null -> {
            // Verifica che l'ID del classroom sia valido prima di navigare
            if (selectedClassroom!!.id > 0) {
                ClassroomScreen(
                    classRoom = selectedClassroom!!,
                    onBack = {
                        Log.d("MainAppScaffold", "Returning from classroom: ${selectedClassroom!!.name}")
                        selectedClassroom = null
                    }
                )
            } else {
                // ID non valido, torna alla schermata principale
                LaunchedEffect(Unit) {
                    selectedClassroom = null
                }
                Text("Errore: ID gruppo non valido")
            }
        }
        else -> {
            // Main app scaffold
            Scaffold(
                topBar = {
                    AppTopBar(
                        onProfileClick = onProfileClick,
                        onGamesClick = { showGamesScreen = true },
                        onLeaderboardClick = onLeaderboardClick,
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
                                    if (index == 2) { // Scanner tab
                                        onBarcodeScanClick()
                                    } else {
                                        onTabSelected(index)
                                    }
                                }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showEcoAIChat = true },
                        containerColor = Green600,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Chiedi a EcoAI",
                            tint = Color.White
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.White)
                ) {
                    when {
                        isLoading -> CenteredLoader(message = "Caricamento in corso...")
                        else -> {
                            when (currentTab) {
                                0 -> HomeContent(
                                    classList = classList,
                                    onClassSelected = { classRoom ->
                                        Log.d("MainAppScaffold", "Class selected: ${classRoom.name} with ID: ${classRoom.id}")
                                        if (classRoom.id > 0) {
                                            selectedClassroom = classRoom
                                        } else {
                                            Log.e("MainAppScaffold", "Invalid class ID: ${classRoom.id}")
                                        }
                                    },
                                    onCreateGroupClick = onCreateClassClick,
                                    onJoinGroupClick = onJoinClassClick
                                )
                                else -> HomeContent(
                                    classList = classList,
                                    onClassSelected = { classRoom ->
                                        Log.d("MainAppScaffold", "Class selected: ${classRoom.name} with ID: ${classRoom.id}")
                                        if (classRoom.id > 0) {
                                            selectedClassroom = classRoom
                                        } else {
                                            Log.e("MainAppScaffold", "Invalid class ID: ${classRoom.id}")
                                        }
                                    },
                                    onCreateGroupClick = onCreateClassClick,
                                    onJoinGroupClick = onJoinClassClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
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
                modifier = Modifier.weight(1f)) {
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
public fun AppTopBar(
    onProfileClick: () -> Unit,
    onGamesClick: () -> Unit,
    onLeaderboardClick: () -> Unit,  // Aggiunto questo parametro
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
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White)
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

@Composable
fun WelcomeHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Green100),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo app
            Image(
                painter = painterResource(id = R.drawable.happy_green_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, Green300, CircleShape)
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Benvenuto in HappyGreen!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green800,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Unisciti a gruppi eco-friendly e guadagna Green Points!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green600
                )
            }
        }
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
                    onClick = {
                        onLogout()
                        onDismiss()
                    },
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