package com.example.frontend_happygreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800
import kotlinx.coroutines.launch

/**
 * ViewModel per la schermata della classifica
 */
class LeaderboardViewModel : ViewModel() {
    // Stati per la classifica
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _leaderboardItems = mutableStateOf<List<LeaderboardItem>>(emptyList())
    val leaderboardItems: State<List<LeaderboardItem>> = _leaderboardItems

    private val _selectedGameId = mutableStateOf<String?>(null)
    val selectedGameId: State<String?> = _selectedGameId

    // Servizio API
    private val apiService = RetrofitClient.create(ApiService::class.java)

    // Carica i dati della classifica (globale o per gioco specifico)
    fun loadLeaderboard(gameId: String? = null) {
        _isLoading.value = true
        _errorMessage.value = null
        _selectedGameId.value = gameId

        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: run {
                    _errorMessage.value = "Devi accedere per visualizzare la classifica"
                    _isLoading.value = false
                    return@launch
                }

                val response = if (gameId != null) {
                    apiService.getLeaderboard(token, gameId)
                } else {
                    apiService.getGlobalLeaderboard(token)
                }

                if (response.isSuccessful && response.body() != null) {
                    val leaderboardData = response.body()!!
                    _leaderboardItems.value = leaderboardData.map { item ->
                        LeaderboardItem(
                            userId = item.userId,
                            username = item.username,
                            score = item.score ?: item.ecoPoints ?: 0,
                            avatarUrl = item.avatar,
                            isCurrentUser = item.userId == UserSession.getUserId()
                        )
                    }
                } else {
                    _errorMessage.value = "Errore nel caricamento della classifica"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Si è verificato un errore: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Modello di dati per un elemento della classifica
data class LeaderboardItem(
    val userId: Int,
    val username: String,
    val score: Int,
    val avatarUrl: String? = null,
    val isCurrentUser: Boolean = false
)

// Enumeration per i tipi di classifica
enum class LeaderboardType(val title: String, val gameId: String?) {
    GLOBAL("Classifica Globale", null),
    ECO_DETECTIVE("Eco Detective", "eco_detective"),
    ECO_SFIDA("Eco Sfida", "eco_sfida")
}

/**
 * Schermata principale della classifica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val leaderboardItems by viewModel.leaderboardItems
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val selectedGameId by viewModel.selectedGameId

    var selectedTab by remember { mutableStateOf(0) }
    val leaderboardTypes = LeaderboardType.values()

    // Carica la classifica quando cambia la tab
    LaunchedEffect(selectedTab) {
        viewModel.loadLeaderboard(leaderboardTypes[selectedTab].gameId)
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

        // Tab per selezionare il tipo di classifica
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Green100,
            contentColor = Green800
        ) {
            leaderboardTypes.forEachIndexed { index, type ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(type.title) }
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
                errorMessage != null -> {
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
                            text = errorMessage!!,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadLeaderboard(leaderboardTypes[selectedTab].gameId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Green600)
                        ) {
                            Text("Riprova")
                        }
                    }
                }
                leaderboardItems.isEmpty() -> {
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
                        itemsIndexed(leaderboardItems) { index, item ->
                            LeaderboardItemRow(
                                rank = index + 1,
                                item = item
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Riga singola della classifica
 */
@Composable
fun LeaderboardItemRow(
    rank: Int,
    item: LeaderboardItem
) {
    val backgroundColor = if (item.isCurrentUser) Green100 else Color.Transparent
    val fontWeight = if (item.isCurrentUser) FontWeight.Bold else FontWeight.Normal

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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = fontWeight,
            modifier = Modifier.width(36.dp)
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
            text = item.username,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = fontWeight,
            modifier = Modifier.weight(1f)
        )

        // Punteggio
        Text(
            text = "${item.score} pts",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = fontWeight,
            color = Green800
        )
    }
}