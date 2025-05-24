package com.example.frontend_happygreen.games

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.api.UpdatePointsRequest
import kotlinx.coroutines.MainScope



/**
 * Enumerazione per i tipi di rifiuto con colori aggiornati
 */
enum class WasteType(val colorCode: Color, val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PAPER(EcoBlue, "Carta", Icons.Default.Description),
    PLASTIC(EcoWarning, "Plastica", Icons.Default.LocalDrink),
    ORGANIC(EcoGreen, "Organico", Icons.Default.Eco),
    GLASS(EcoLightBlue, "Vetro", Icons.Default.LocalBar)
}

/**
 * Classe dati per gli elementi di rifiuto
 */
data class WasteItem(
    val id: Int,
    val name: String,
    val imageRes: String,
    val type: WasteType,
    val description: String,
    val educationalFact: String
)

/**
 * ViewModel per gestire la logica del gioco
 */
class EcoDetectiveViewModel : ViewModel() {
    // Database degli oggetti rifiuto espanso con fatti educativi
    private val allWasteItems = listOf(
        WasteItem(
            id = 1,
            name = "Bottiglia di plastica",
            imageRes = "https://videos.openai.com/vg-assets/assets%2Ftask_01jw0dt3nceq3byhy7n4fwasp8%2F1748066250_img_0.webp?st=2025-05-24T04%3A52%3A48Z&se=2025-05-30T05%3A52%3A48Z&sks=b&skt=2025-05-24T04%3A52%3A48Z&ske=2025-05-30T05%3A52%3A48Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=Xk2jQS44485%2Bv20CoTxd49g19klx%2ByjPB1Nw4La6qbI%3D&az=oaivgprodscus",
            type = WasteType.PLASTIC,
            description = "Le bottiglie di plastica impiegano fino a 450 anni per decomporsi",
            educationalFact = "Una bottiglia di plastica PET produce 82g di CO2 durante la produzione"
        ),
        WasteItem(
            id = 2,
            name = "Giornale",
            imageRes = "https://videos.openai.com/vg-assets/assets%2Ftask_01jw0e0a23f3wbx1w7q313yvfc%2F1748066410_img_0.webp?st=2025-05-24T04%3A52%3A44Z&se=2025-05-30T05%3A52%3A44Z&sks=b&skt=2025-05-24T04%3A52%3A44Z&ske=2025-05-30T05%3A52%3A44Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=7%2FhHG0Uq76Ww5EFMwW6%2BBlNMHh5cacA1TTfUqChNFZk%3D&az=oaivgprodscus",
            type = WasteType.PAPER,
            description = "La carta si decompone in circa 2-5 mesi se correttamente smaltita",
            educationalFact = "Riciclare una tonnellata di carta salva 17 alberi"
        ),
        WasteItem(
            id = 3,
            name = "Buccia di banana",
            imageRes = "https://videos.openai.com/vg-assets/assets%2Ftask_01jw0e2fd9fyramryqa07efrnb%2F1748066490_img_0.webp?st=2025-05-24T04%3A52%3A48Z&se=2025-05-30T05%3A52%3A48Z&sks=b&skt=2025-05-24T04%3A52%3A48Z&ske=2025-05-30T05%3A52%3A48Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=Ut9tmIyvCmVqycxo8zzGTslF2tQOJIOYCsXmmtIk%2Bls%3D&az=oaivgprodscus",
            type = WasteType.ORGANIC,
            description = "Gli scarti organici si decompongono in poche settimane in compost",
            educationalFact = "Il compost domestico riduce i rifiuti del 30%"
        ),
        WasteItem(
            id = 4,
            name = "Bottiglia di vetro",
            imageRes = "https://videos.openai.com/vg-assets/assets%2Ftask_01jw0e54w2e6q9ms4acta9jzgv%2F1748066564_img_0.webp?st=2025-05-24T04%3A54%3A35Z&se=2025-05-30T05%3A54%3A35Z&sks=b&skt=2025-05-24T04%3A54%3A35Z&ske=2025-05-30T05%3A54%3A35Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=uxqwmOEIPPDi57BlqneDtiMv8ceJQSJmFrTk4EHyEmA%3D&az=oaivgprodscus",
            type = WasteType.GLASS,
            description = "Il vetro può impiegare oltre 4000 anni per decomporsi naturalmente",
            educationalFact = "Il vetro è riciclabile infinite volte senza perdere qualità"
        ),
        WasteItem(
            id = 5,
            name = "Sacchetto di plastica",
            imageRes = "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhe4d2e87tpgev8aam5xjf%2F1748036470_img_0.webp?st=2025-05-24T03%3A57%3A48Z&se=2025-05-30T04%3A57%3A48Z&sks=b&skt=2025-05-24T03%3A57%3A48Z&ske=2025-05-30T04%3A57%3A48Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=FmjH0xIulpO8axnpjm092iX64NRxYaXnQJPOvS14cpI%3D&az=oaivgprodscus",
            type = WasteType.PLASTIC,
            description = "I sacchetti di plastica impiegano fino a 20 anni per decomporsi",
            educationalFact = "Ogni anno finiscono negli oceani 8 milioni di tonnellate di plastica"
        ),
        WasteItem(
            id = 6,
            name = "Cartone del latte",
            imageRes = "https://videos.openai.com/vg-assets/assets%2Ftask_01jw0e8dfaf4msbrnw9w1vzsp5%2F1748066667_img_0.webp?st=2025-05-24T04%3A53%3A45Z&se=2025-05-30T05%3A53%3A45Z&sks=b&skt=2025-05-24T04%3A53%3A45Z&ske=2025-05-30T05%3A53%3A45Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=ZCYdy2lnGhhAUnmxcJSg1i9p6DhuuFO7Tq1FgcWWU2w%3D&az=oaivgprodscus",
            type = WasteType.PAPER,
            description = "I contenitori Tetra Pak sono riciclabili nella carta",
            educationalFact = "I Tetra Pak sono composti per il 75% da carta riciclabile"
        ),
        WasteItem(
            id = 7,
            name = "Avanzi di cibo",
            imageRes = "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhkyj3exd845y5sj7hwx7v%2F1748036642_img_0.webp?st=2025-05-24T03%3A58%3A36Z&se=2025-05-30T04%3A58%3A36Z&sks=b&skt=2025-05-24T03%3A58%3A36Z&ske=2025-05-30T04%3A58%3A36Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=TH4mn7gDKRQN6HvUoYZyxy1tB6YsMQrYeNZ6evmLnN4%3D&az=oaivgprodscus",
            type = WasteType.ORGANIC,
            description = "Gli avanzi di cibo possono diventare compost per le piante",
            educationalFact = "Il 30% del cibo prodotto nel mondo viene sprecato"
        ),
        WasteItem(
            id = 8,
            name = "Barattolo di vetro",
            imageRes = "https://videos.openai.com/vg-assets/assets%2Ftask_01jw0ebrrqe9frd0qzfb0epc77%2F1748066776_img_0.webp?st=2025-05-24T04%3A53%3A36Z&se=2025-05-30T05%3A53%3A36Z&sks=b&skt=2025-05-24T04%3A53%3A36Z&ske=2025-05-30T05%3A53%3A36Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=IxdH6eaalJf1H1vpX0p9AaP6RSV1RNsu7X4oFGz6VuY%3D&az=oaivgprodscus",
            type = WasteType.GLASS,
            description = "Il vetro è riciclabile infinite volte senza perdere qualità",
            educationalFact = "Riciclare vetro consuma il 40% meno energia della produzione da materie prime"
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
    var comboStreak by mutableStateOf(0)
    var perfectStreak by mutableStateOf(0)
    var showConfetti by mutableStateOf(false)
    var currentLevel by mutableStateOf(1)

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

    // Verifica la selezione del bidone con sistema di punteggio migliorato
    fun checkSelection(selectedType: WasteType): Boolean {
        val isCorrect = (selectedType == currentWasteItem.type)

        if (isCorrect) {
            comboStreak += 1
            perfectStreak += 1

            // Sistema di punteggio progressivo
            val basePoints = when {
                comboStreak >= 10 -> 25
                comboStreak >= 5 -> 20
                comboStreak >= 3 -> 15
                else -> 10
            }

            val timeBonus = if (timeLeft > 50) 5 else 0
            val totalPoints = basePoints + timeBonus
            score += totalPoints

            // Aggiorna livello
            currentLevel = (score / 100) + 1

            feedbackMessage = FeedbackMessage(
                "Corretto! +$totalPoints punti",
                "Hai smaltito correttamente ${currentWasteItem.name}",
                currentWasteItem.educationalFact,
                isError = false
            )

            // Attiva confetti per combo lunghi
            if (comboStreak >= 5) {
                showConfetti = true
                viewModelScope.launch {
                    delay(2000)
                    showConfetti = false
                }
            }
        } else {
            lives -= 1
            comboStreak = 0
            perfectStreak = 0

            feedbackMessage = FeedbackMessage(
                "Sbagliato! -1 vita",
                "${currentWasteItem.name} va nella raccolta ${currentWasteItem.type.displayName}",
                currentWasteItem.educationalFact,
                isError = true
            )

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
        comboStreak = 0
        perfectStreak = 0
        showConfetti = false
        currentLevel = 1
    }

    fun sendScoreToServer(onSuccess: (Int) -> Unit, onError: (String) -> Unit = {}) {
        val gameId = "eco_detective"
        val scoreToSend = score
        val apiService = RetrofitClient.create(ApiService::class.java)

        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: run {
                    onError("Non sei autenticato")
                    return@launch
                }

                val response = apiService.updateUserPoints(
                    token,
                    UpdatePointsRequest(
                        points = scoreToSend,
                        game_id = gameId
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val totalPoints = response.body()!!.total_points
                    UserSession.setEcoPoints(totalPoints)
                    onSuccess(totalPoints)
                } else {
                    onError("Errore nell'invio del punteggio: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("EcoDetectiveViewModel", "Error sending score: ${e.message}")
                onError("Errore di connessione: ${e.message}")
            }
        }
    }
}

/**
 * Classe dati per i messaggi di feedback aggiornata
 */
data class FeedbackMessage(
    val title: String,
    val description: String,
    val educationalFact: String,
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
 * Schermata principale del gioco EcoDetective con grafica aggiornata
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoDetectiveGameScreen(
    onBack: () -> Unit,
    viewModel: EcoDetectiveViewModel = viewModel()
) {
    // Avvia il timer del gioco
    LaunchedEffect(Unit) {
        viewModel.startTimer {}
    }

    // Gestione del feedback
    LaunchedEffect(viewModel.showFeedback) {
        if (viewModel.showFeedback) {
            delay(3000)
            if (viewModel.gameState == GameState.Playing) {
                viewModel.nextItem()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Sfondo gradiente come EcoSfida
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(EcoBackground, Color.White)
                    )
                )
        ) {
            if (viewModel.gameState == GameState.GameOver) {
                EnhancedGameOverScreen(
                    score = viewModel.score,
                    onRestart = { viewModel.resetGame() },
                    onBack = onBack,
                    viewModel = viewModel
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header migliorato
                    EnhancedGameHeader(
                        score = viewModel.score,
                        lives = viewModel.lives,
                        timeLeft = viewModel.timeLeft,
                        comboStreak = viewModel.comboStreak,
                        currentLevel = viewModel.currentLevel,
                        perfectStreak = viewModel.perfectStreak
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Item da smistare con stile migliorato
                    EnhancedWasteItemCard(
                        wasteItem = viewModel.currentWasteItem,
                        showFeedback = viewModel.showFeedback,
                        modifier = Modifier.weight(1f)
                    )

                    // Messaggio di feedback migliorato
                    if (viewModel.showFeedback) {
                        viewModel.feedbackMessage?.let { message ->
                            EnhancedFeedbackMessageCard(message)
                        }
                    } else {
                        // Istruzioni stilizzate
                        EnhancedInstructionCard()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bidoni di riciclaggio migliorati
                    EnhancedRecycleBinSelector(
                        onBinSelected = { binType ->
                            if (!viewModel.showFeedback) {
                                viewModel.checkSelection(binType)
                            }
                        },
                        enabled = !viewModel.showFeedback
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Effetti speciali
        if (viewModel.showConfetti) {
            ConfettiAnimation()
        }

        // Pulsante indietro stilizzato
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .background(
                    EcoGreen.copy(alpha = 0.9f),
                    CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Indietro",
                tint = Color.White
            )
        }
    }
}

/**
 * Header del gioco migliorato stile EcoSfida
 */
@Composable
fun EnhancedGameHeader(
    score: Int,
    lives: Int,
    timeLeft: Int,
    comboStreak: Int,
    currentLevel: Int,
    perfectStreak: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGreen),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Punteggio con icona
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$score",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }


                // Vite rimanenti
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { i ->
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (i < lives) Color.Red else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(horizontal = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Combo con animazione
                if (comboStreak >= 3) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Combo $comboStreak",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = "Combo: $comboStreak",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Perfect streak
                if (perfectStreak >= 5) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Perfect: $perfectStreak",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Timer migliorato
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val timerColor = when {
                        timeLeft <= 10 -> Color.Red
                        timeLeft <= 20 -> EcoWarning
                        else -> Color.White
                    }

                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = timerColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$timeLeft",
                        color = timerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra del tempo
            LinearProgressIndicator(
                progress = timeLeft / 60f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    timeLeft <= 10 -> Color.Red
                    timeLeft <= 20 -> EcoWarning
                    else -> Color.Green
                },
                trackColor = Color.DarkGray
            )
        }
    }
}

/**
 * Card dell'oggetto migliorata
 */
@Composable
fun EnhancedWasteItemCard(
    wasteItem: WasteItem,
    showFeedback: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (showFeedback) 1f else 1.05f,
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
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Green900)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Immagine con styling migliorato
                Card(
                    modifier = Modifier
                        .size(160.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        wasteItem.type.colorCode.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = wasteItem.imageRes, // ✅ AsyncImage accetta String URL
                            contentDescription = wasteItem.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp)
                        )

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nome dell'item
                Text(
                    text = wasteItem.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = EcoDarkGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Descrizione con stile migliorato
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = wasteItem.type.colorCode.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = wasteItem.description,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Bidoni selezionabili migliorati
 */
@Composable
fun EnhancedRecycleBinSelector(
    onBinSelected: (WasteType) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WasteType.values().forEach { binType ->
            EnhancedRecycleBinItem(
                type = binType,
                onClick = { onBinSelected(binType) },
                enabled = enabled
            )
        }
    }
}

/**
 * Singolo bidone migliorato
 */
@Composable
fun EnhancedRecycleBinItem(
    type: WasteType,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(enabled = enabled) { onClick() }
            .graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        // Icona del bidone migliorata
        Card(
            modifier = Modifier.size(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) type.colorCode else type.colorCode.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (enabled) 8.dp else 2.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = type.displayName,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nome del tipo di rifiuto
        Text(
            text = type.displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) type.colorCode else type.colorCode.copy(alpha = 0.5f)
        )
    }
}

/**
 * Card per istruzioni stilizzata
 */
@Composable
fun EnhancedInstructionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = EcoCardBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = EcoGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Tocca il bidone corretto per questo rifiuto!",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = EcoDarkGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Card feedback migliorata
 */
@Composable
fun EnhancedFeedbackMessageCard(message: FeedbackMessage) {
    val backgroundColor = if (message.isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
    val accentColor = if (message.isError) EcoError else EcoSuccess

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icona risultato con animazione
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (!message.isError) 1.2f else 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Icon(
                imageVector = if (!message.isError) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descrizione
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = message.description,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fatto educativo
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = EcoLightGreen.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = EcoWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lo sapevi?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = EcoDarkGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message.educationalFact,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Animazione confetti
 */



/**
 * Schermata Game Over migliorata
 */
@Composable
fun EnhancedGameOverScreen(
    score: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    viewModel: EcoDetectiveViewModel = viewModel()
) {
    var totalPoints by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.sendScoreToServer(
            onSuccess = { points ->
                totalPoints = points
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EcoBackground, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icona game over animata
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Icon(
                imageVector = if (score > 50) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                contentDescription = null,
                tint = if (score > 50) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Game Over",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = EcoGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Punteggio finale
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = EcoLightGreen.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Punteggio Finale",
                        style = MaterialTheme.typography.titleMedium,
                        color = EcoDarkGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Punti totali
            if (isLoading) {
                CircularProgressIndicator(color = EcoGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aggiornamento punti...", color = Color.Gray)
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = EcoError,
                    textAlign = TextAlign.Center
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EcoLightGreen),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiNature,
                            contentDescription = null,
                            tint = EcoGreen,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Eco Points Totali",
                            style = MaterialTheme.typography.titleMedium,
                            color = EcoDarkGreen,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "$totalPoints",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pulsanti stilizzati
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gioca ancora",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Torna alla Home",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}