package com.example.frontend_happygreen.games

import android.util.Log
import androidx.compose.animation.AnimatedVisibility as ComposeAnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.ui.components.HappyGreenButton
import com.example.frontend_happygreen.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.api.UpdatePointsRequest
import kotlinx.coroutines.MainScope

/**
 * Enumerazione per i tipi di rifiuto
 */
enum class WasteType(val colorCode: Color, val displayName: String) {
    PAPER(Blue500, "Carta"),
    PLASTIC(Color.Yellow, "Plastica"),
    ORGANIC(Green500, "Organico"),
    GLASS(Orange500, "Vetro")
}

/**
 * Classe dati per gli elementi di rifiuto
 */
data class WasteItem(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val type: WasteType,
    val description: String
)

/**
 * ViewModel per gestire la logica del gioco
 */
class EcoDetectiveViewModel : ViewModel() {
    // Database degli oggetti rifiuto
    private val allWasteItems = listOf(
        WasteItem(
            id = 1,
            name = "Bottiglia di plastica",
            imageRes = R.drawable.happy_green_logo,
            type = WasteType.PLASTIC,
            description = "Le bottiglie di plastica impiegano fino a 450 anni per decomporsi"
        ),
        WasteItem(
            id = 2,
            name = "Giornale",
            imageRes = R.drawable.happy_green_logo,
            type = WasteType.PAPER,
            description = "La carta si decompone in circa 2-5 mesi se correttamente smaltita"
        ),
        WasteItem(
            id = 3,
            name = "Buccia di banana",
            imageRes = R.drawable.happy_green_logo,
            type = WasteType.ORGANIC,
            description = "Gli scarti organici si decompongono in poche settimane in compost"
        ),
        WasteItem(
            id = 4,
            name = "Bottiglia di vetro",
            imageRes = R.drawable.happy_green_logo,
            type = WasteType.GLASS,
            description = "Il vetro può impiegare oltre 4000 anni per decomporsi naturalmente"
        ),
        WasteItem(
            id = 5,
            name = "Sacchetto di plastica",
            imageRes = R.drawable.happy_green_logo,
            type = WasteType.PLASTIC,
            description = "I sacchetti di plastica impiegano fino a 20 anni per decomporsi"
        ),
        WasteItem(
            id = 6,
            name = "Cartone del latte",
            imageRes = R.drawable.happy_green_logo,
            type = WasteType.PAPER,
            description = "I contenitori Tetra Pak sono riciclabili nella carta"
        ),
        WasteItem(
            id = 7,
            name = "Avanzi di cibo",
            imageRes = R.drawable.happy_green_logo,
            type = WasteType.ORGANIC,
            description = "Gli avanzi di cibo possono diventare compost per le piante"
        ),
        WasteItem(
            id = 8,
            name = "Barattolo di vetro",
            imageRes = R.drawable.happy_green_logo,
            type = WasteType.GLASS,
            description = "Il vetro è riciclabile infinite volte senza perdere qualità"
        )
    )

    // Stato del gioco e punteggio
    var gameState by mutableStateOf<GameState>(GameState.Playing)
    var score by mutableStateOf(0)
    var lives by mutableStateOf(3)
    var timeLeft by mutableStateOf(60)
    var currentWasteItem by mutableStateOf(allWasteItems.random())
    var feedbackMessage by mutableStateOf<FeedbackMessage?>(null)
    var showFeedback by mutableStateOf(false)

    // Timer del gioco
    fun startTimer(onGameOver: () -> Unit) {
        val timerJob = MainScope().launch {
            while (timeLeft > 0 && lives > 0) {
                delay(1000)
                timeLeft--
                if (timeLeft <= 0) {
                    gameState = GameState.GameOver
                    onGameOver()
                    break
                }
            }
        }
    }

    // Verifica la selezione del bidone
    fun checkSelection(selectedType: WasteType): Boolean {
        // Controlla se è il bidone corretto
        val isCorrect = (selectedType == currentWasteItem.type)

        if (isCorrect) {
            // Risposta corretta
            score += 10
            feedbackMessage = FeedbackMessage(
                "Corretto! +10 punti",
                "Hai smaltito correttamente ${currentWasteItem.name}",
                isError = false
            )
        } else {
            // Risposta sbagliata
            lives -= 1
            feedbackMessage = FeedbackMessage(
                "Sbagliato! -1 vita",
                "${currentWasteItem.name} va nella raccolta ${currentWasteItem.type.displayName}",
                isError = true
            )

            // Controlla se il gioco è finito
            if (lives <= 0) {
                gameState = GameState.GameOver
            }
        }

        showFeedback = true
        return isCorrect
    }

    // Passa al prossimo elemento
    fun nextItem() {
        currentWasteItem = allWasteItems.random()
        showFeedback = false
        feedbackMessage = null
    }

    // Resetta il gioco
    fun resetGame() {
        score = 0
        lives = 3
        timeLeft = 60
        gameState = GameState.Playing
        showFeedback = false
        feedbackMessage = null
        currentWasteItem = allWasteItems.random()
    }

    // Aggiungiamo una funzione nel ViewModel per inviare i punti al server
// Modifica la funzione sendScoreToServer in EcoDetectiveViewModel
    fun sendScoreToServer(onSuccess: (Int) -> Unit) {
        val gameId = "eco_detective"
        val scoreToSend = score
        val apiService = RetrofitClient.create(ApiService::class.java)
        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: return@launch

                val response = apiService.updateUserPoints(
                    token,
                    UpdatePointsRequest(
                        points = scoreToSend,
                        game_id = gameId  // Nota che qui ho cambiato 'gameid' a 'game_id'
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    // Aggiorna i punti dell'utente localmente
                    val totalPoints = response.body()!!.total_points  // Cambiato da totalPoints a total_points
                    UserSession.setEcoPoints(totalPoints)
                    onSuccess(totalPoints)
                }
            } catch (e: Exception) {
                // Gestisci l'errore (potrebbe essere necessario aggiungere un callback di errore)
                Log.e("EcoDetectiveViewModel", "Error sending score: ${e.message}")
            }
        }
    }
}

/**
 * Classe dati per i messaggi di feedback
 */
data class FeedbackMessage(
    val title: String,
    val description: String,
    val isError: Boolean
)

/**
 * Enumerazione per lo stato del gioco
 */
enum class GameState {
    Playing,
    GameOver
}

/**
 * Schermata principale del gioco EcoDetective
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoDetectiveGameScreen(
    onBack: () -> Unit,
    viewModel: EcoDetectiveViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    // Avvia il timer del gioco
    LaunchedEffect(Unit) {
        viewModel.startTimer {
        }
    }

    // Gestione del feedback
    LaunchedEffect(viewModel.showFeedback) {
        if (viewModel.showFeedback) {
            delay(2000)
            // Se il gioco è ancora in corso, passa al prossimo item
            if (viewModel.gameState == GameState.Playing) {
                viewModel.nextItem()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Detective") },
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (viewModel.gameState == GameState.GameOver) {
                // Schermata di Game Over
                EcoSfidaGameOverScreen(
                    score = viewModel.score,
                    onRestart = { viewModel.resetGame() },
                    onBack = onBack
                )
            } else {
                // Schermata di gioco
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header con punteggio, vite e tempo
                    GameHeader(
                        score = viewModel.score,
                        lives = viewModel.lives,
                        timeLeft = viewModel.timeLeft
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Item da smistare
                    WasteItemCard(
                        wasteItem = viewModel.currentWasteItem,
                        showFeedback = viewModel.showFeedback,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Messaggio di feedback
                    ComposeAnimatedVisibility(
                        visible = viewModel.showFeedback,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        viewModel.feedbackMessage?.let { message ->
                            FeedbackMessageCard(message)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Istruzioni
                    if (!viewModel.showFeedback) {
                        Text(
                            text = "Seleziona il bidone corretto per questo rifiuto:",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Bidoni di riciclaggio
                    RecycleBinSelector(
                        onBinSelected = { binType ->
                            if (!viewModel.showFeedback) {
                                viewModel.checkSelection(binType)
                            }
                        },
                        enabled = !viewModel.showFeedback
                    )
                }
            }
        }
    }
}

/**
 * Header del gioco con punteggio, vite e tempo
 */
@Composable
fun GameHeader(
    score: Int,
    lives: Int,
    timeLeft: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Green100,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Punteggio
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Green800,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Punti",
                style = MaterialTheme.typography.bodyMedium,
                color = Green600
            )
        }

        // Vite rimanenti
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { i ->
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (i < lives) Red500 else Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 2.dp)
                )
            }
        }

        // Tempo rimanente
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeLeft.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = if (timeLeft <= 10) Red500 else Green800,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Secondi",
                style = MaterialTheme.typography.bodyMedium,
                color = Green600
            )
        }
    }
}

/**
 * Card che mostra l'oggetto di rifiuto corrente
 */
@Composable
fun WasteItemCard(
    wasteItem: WasteItem,
    showFeedback: Boolean,
    modifier: Modifier = Modifier
) {
    // Animazione di pulse per attirare l'attenzione
    val scale by animateFloatAsState(
        targetValue = if (showFeedback) 1.0f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .scale(if (showFeedback) 1f else scale),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Immagine dell'item
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(2.dp, wasteItem.type.colorCode, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = wasteItem.imageRes),
                        contentDescription = wasteItem.name,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nome dell'item
                Text(
                    text = wasteItem.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descrizione dell'item
                Text(
                    text = wasteItem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

/**
 * Componente per selezionare il bidone
 */
@Composable
fun RecycleBinSelector(
    onBinSelected: (WasteType) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WasteType.values().forEach { binType ->
            RecycleBinItem(
                type = binType,
                onClick = { onBinSelected(binType) },
                enabled = enabled
            )
        }
    }
}

/**
 * Singolo bidone selezionabile
 */
@Composable
fun RecycleBinItem(
    type: WasteType,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val alpha = if (enabled) 1f else 0.6f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(enabled = enabled) { onClick() }
    ) {
        // Icona del bidone
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(type.colorCode.copy(alpha = alpha * 0.7f))
                .border(2.dp, type.colorCode.copy(alpha = alpha), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = type.displayName,
                tint = Color.White.copy(alpha = alpha),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nome del tipo di rifiuto
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = type.colorCode.copy(alpha = alpha)
        )
    }
}

/**
 * Card per i messaggi di feedback
 */
@Composable
fun FeedbackMessageCard(message: FeedbackMessage) {
    val backgroundColor = if (message.isError) Red100 else Green100
    val textColor = if (message.isError) Red300  else Green800
    val iconTint = if (message.isError) Red500 else Green600

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona feedback
            Icon(
                imageVector = if (message.isError)
                    Icons.Default.Close else Icons.Default.Check,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // Titolo feedback
                Text(
                    text = message.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Descrizione feedback
                Text(
                    text = message.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}

/**
 * Schermata di Game Over
 */
@Composable
fun EcoSfidaGameOverScreen(
    score: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    viewModel: EcoDetectiveViewModel = viewModel()
) {
    // Effetto confetti per punteggi alti
    val showConfetti = score > 50
    var totalPoints by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Invia il punteggio al server quando viene mostrata la schermata di Game Over
    LaunchedEffect(Unit) {
        viewModel.sendScoreToServer { points ->
            totalPoints = points
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Corona animata per punteggi alti
        if (showConfetti) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .size(80.dp)
                    .scale(
                        animateFloatAsState(
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            )
                        ).value
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Game Over",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Green800
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Punteggio Finale",
            style = MaterialTheme.typography.titleLarge,
            color = Gray600
        )

        Text(
            text = "$score",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Green600
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mostra lo stato del caricamento o i punti totali
        if (isLoading) {
            CircularProgressIndicator(color = Green600)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Aggiornamento punti in corso...", color = Gray600)
        } else {
            // Card con i punti totali
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Green100),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Eco Points Totali",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green800
                    )

                    Text(
                        text = "$totalPoints",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Green600
                    )
                }
            }
        }

        // Messaggio basato sul punteggio
        val message = when {
            score >= 100 -> "Eccellente! Sei un vero Eco Detective!"
            score >= 50 -> "Ottimo lavoro! Stai diventando un esperto del riciclo."
            score >= 20 -> "Buon risultato! Continua a migliorare."
            else -> "Continua ad allenarti per diventare un Eco Detective."
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Gray700
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Pulsanti
        HappyGreenButton(
            text = "Gioca ancora",
            onClick = onRestart,
            icon = Icons.Default.Refresh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        HappyGreenButton(
            text = "Torna alla Home",
            onClick = onBack,
            icon = Icons.Default.Home,
            isOutlined = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}