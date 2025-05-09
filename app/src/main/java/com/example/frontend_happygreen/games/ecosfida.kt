package com.example.frontend_happygreen.games

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.api.UpdatePointsRequest
import com.example.frontend_happygreen.data.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colori personalizzati per il tema dell'app
val EcoGreen = Color(0xFF2E7D32)
val EcoLightGreen = Color(0xFFAED581)
val EcoBlue = Color(0xFF1565C0)
val EcoLightBlue = Color(0xFF81D4FA)
val EcoBackground = Color(0xFFF5F5F5)
val EcoError = Color(0xFFD32F2F)
val EcoSuccess = Color(0xFF388E3C)

// Modello dei dati per i rifiuti con informazioni educative aggiuntive
data class Waste(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val decompositionTimeYears: Float,
    val pollutionLevel: Int, // da 1 a 10
    val prevalenceLevel: Int, // da 1 a 10, quanto è diffuso sulla Terra
    val educationalFact: String // Un fatto educativo sul rifiuto
)

// Enum class per il tipo di domanda
enum class QuestionType {
    DECOMPOSITION_TIME,
    POLLUTION_LEVEL,
    PREVALENCE_LEVEL
}

// ViewModel per la logica di gioco
class EcoGameViewModel : ViewModel() {

    private val apiService = RetrofitClient.create(ApiService::class.java)

    // Database dei rifiuti con fatti educativi
    private val wasteDatabase = listOf(
        Waste(
            1,
            "Bottiglia di plastica",
            R.drawable.placeholder,
            450f,
            8,
            10,
            "Le bottiglie di plastica impiegano centinaia di anni per decomporsi e rilasciano microplastiche nell'ambiente."
        ),
        Waste(
            2,
            "Sacchetto di plastica",
            R.drawable.placeholder,
            20f,
            7,
            9,
            "I sacchetti di plastica uccidono migliaia di animali marini ogni anno che li scambiano per cibo."
        ),
        Waste(
            3,
            "Lattina di alluminio",
            R.drawable.placeholder,
            200f,
            6,
            8,
            "Il riciclaggio dell'alluminio richiede solo il 5% dell'energia necessaria per produrlo da zero."
        ),
        Waste(
            4,
            "Buccia di banana",
            R.drawable.placeholder,
            0.1f,
            1,
            5,
            "Le bucce di banana sono biodegradabili e possono essere usate per il compostaggio."
        ),
        Waste(
            5,
            "Mozzicone di sigaretta",
            R.drawable.placeholder,
            5f,
            9,
            10,
            "I mozziconi di sigaretta sono il rifiuto più comune al mondo e contengono plastiche e sostanze tossiche."
        ),
        Waste(
            6,
            "Pannolino usa e getta",
            R.drawable.placeholder,
            500f,
            7,
            8,
            "Un bambino usa in media 6.000 pannolini prima di imparare a usare il bagno."
        ),
        Waste(
            7,
            "Giornale",
            R.drawable.placeholder,
            0.2f,
            2,
            6,
            "La carta può essere riciclata fino a 7 volte prima che le fibre diventino troppo corte."
        ),
        Waste(
            8,
            "Bicchiere di carta",
            R.drawable.placeholder,
            0.1f,
            3,
            7,
            "Molti bicchieri di carta hanno un rivestimento in plastica che li rende difficili da riciclare."
        ),
        Waste(
            9,
            "Bottiglia di vetro",
            R.drawable.placeholder,
            1000f,
            4,
            7,
            "Il vetro può essere riciclato infinite volte senza perdere qualità o purezza."
        ),
        Waste(
            10,
            "Gomma da masticare",
            R.drawable.placeholder,
            5f,
            7,
            9,
            "La gomma da masticare è fatta di polimeri sintetici simili alla plastica e non è biodegradabile."
        ),
        Waste(
            11,
            "Apparecchio elettronico",
            R.drawable.placeholder,
            1000f,
            10,
            8,
            "I rifiuti elettronici contengono metalli pesanti tossici che possono contaminare suolo e acqua."
        ),
        Waste(
            12,
            "Batteria",
            R.drawable.placeholder,
            100f,
            10,
            7,
            "Una singola batteria può contaminare 600.000 litri di acqua se smaltita in discarica."
        ),
        Waste(
            13,
            "Mascherina monouso",
            R.drawable.placeholder,
            450f,
            7,
            9,
            "Durante la pandemia, miliardi di mascherine sono finite negli oceani, causando problemi alla fauna marina."
        ),
        Waste(
            14,
            "Carta stagnola",
            R.drawable.placeholder,
            400f,
            5,
            8,
            "La carta stagnola può essere riciclata ma deve essere pulita dai residui di cibo."
        ),
        Waste(
            15,
            "Scontrino fiscale",
            R.drawable.placeholder,
            0.1f,
            6,
            9,
            "Molti scontrini contengono BPA e non possono essere riciclati con la carta normale."
        )
    )

    // Stato del gioco
    private var _leftWaste = mutableStateOf<Waste?>(null)
    val leftWaste: State<Waste?> = _leftWaste

    private var _rightWaste = mutableStateOf<Waste?>(null)
    val rightWaste: State<Waste?> = _rightWaste

    private var _currentQuestion = mutableStateOf<QuestionType>(QuestionType.DECOMPOSITION_TIME)
    val currentQuestion: State<QuestionType> = _currentQuestion

    private var _currentScore = mutableStateOf(0)
    val currentScore: State<Int> = _currentScore

    private var _highScore = mutableStateOf(0)
    val highScore: State<Int> = _highScore

    private var _gameOver = mutableStateOf(false)
    val gameOver: State<Boolean> = _gameOver

    // Motivo della sconfitta
    private var _gameOverReason = mutableStateOf("")
    val gameOverReason: State<String> = _gameOverReason

    // Livello attuale (aumenta la difficoltà)
    private var _currentLevel = mutableStateOf(1)
    val currentLevel: State<Int> = _currentLevel

    // Per mostrare il risultato della scelta
    private var _showResult = mutableStateOf(false)
    val showResult: State<Boolean> = _showResult

    private var _lastChoiceCorrect = mutableStateOf(false)
    val lastChoiceCorrect: State<Boolean> = _lastChoiceCorrect

    // Per tenere traccia dei rifiuti già utilizzati in questa partita
    private val usedWasteIds = mutableSetOf<Int>()

    // Stato per la modalità tutorial
    private var _showTutorial = mutableStateOf(true)
    val showTutorial: State<Boolean> = _showTutorial

    // Combo streak per bonus punti
    private var _comboStreak = mutableStateOf(0)
    val comboStreak: State<Int> = _comboStreak

    // Tempo rimanente per rispondere
    private var _timeRemaining = mutableStateOf(15)
    val timeRemaining: State<Int> = _timeRemaining

    // Flag per indicare se il timer è attivo
    private var _timerActive = mutableStateOf(false)
    val timerActive: State<Boolean> = _timerActive

    init {
        startNewRound()
    }

    /**
     * Invia il punteggio al server quando il gioco termina
     */
    fun sendScoreToServer(onSuccess: (Int) -> Unit, onError: (String) -> Unit = {}) {
        val gameId = "eco_sfida"
        val scoreToSend = currentScore.value

        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: run {
                    onError("Non sei autenticato")
                    return@launch
                }

                val request = UpdatePointsRequest(
                    points = scoreToSend,
                    game_id = gameId
                )

                val response = apiService.updateUserPoints(token, request)

                if (response.isSuccessful && response.body() != null) {
                    // Aggiorna i punti dell'utente localmente
                    val totalPoints = response.body()!!.total_points
                    UserSession.setEcoPoints(totalPoints)
                    onSuccess(totalPoints)
                } else {
                    onError("Errore nell'invio del punteggio: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("EcoGameViewModel", "Error sending score: ${e.message}")
                onError("Errore di connessione: ${e.message}")
            }
        }
    }

    // Inizia un nuovo round
    fun startNewRound() {
        // Dopo 10 punti, passa al livello 2 (tempo ridotto)
        if (currentScore.value >= 10 && _currentLevel.value == 1) {
            _currentLevel.value = 2
        }
        // Dopo 20 punti, passa al livello 3 (tempo ulteriormente ridotto)
        else if (currentScore.value >= 20 && _currentLevel.value == 2) {
            _currentLevel.value = 3
        }

        // Reset del timer in base al livello
        _timeRemaining.value = when(_currentLevel.value) {
            1 -> 15
            2 -> 10
            else -> 7
        }

        // Seleziona due rifiuti diversi casualmente che non sono stati usati di recente
        var availableWastes = wasteDatabase.filter { it.id !in usedWasteIds }

        // Se abbiamo esaurito i rifiuti disponibili, resettiamo la lista
        if (availableWastes.size < 2) {
            usedWasteIds.clear()
            availableWastes = wasteDatabase
        }

        val left = availableWastes.random()
        usedWasteIds.add(left.id)

        // Seleziona un secondo rifiuto diverso dal primo
        availableWastes = availableWastes.filter { it.id != left.id }
        val right = availableWastes.random()
        usedWasteIds.add(right.id)

        _leftWaste.value = left
        _rightWaste.value = right

        // Scegli casualmente il tipo di domanda
        _currentQuestion.value = QuestionType.values().random()

        _showResult.value = false
        _timerActive.value = true
    }

    // Gestisce la selezione dell'utente
    fun makeSelection(selectedLeft: Boolean) {
        _timerActive.value = false

        val correct = isCorrectAnswer(selectedLeft)

        _lastChoiceCorrect.value = correct
        _showResult.value = true

        if (correct) {
            // Aumenta il combo streak
            _comboStreak.value += 1

            // Calcola i punti bonus in base al combo streak
            val comboBonus = minOf(_comboStreak.value / 3, 3) // Massimo 3 punti bonus

            // Aumenta il punteggio se la risposta è corretta (1 punto base + bonus combo + bonus tempo)
            val timeBonus = (_timeRemaining.value / 5) // Bonus tempo: max 3 punti
            val totalPoints = 1 + comboBonus + timeBonus

            _currentScore.value += totalPoints
        } else {
            // Resetta il combo streak
            _comboStreak.value = 0

            // Imposta il motivo della sconfitta
            setGameOverReason()

            // Aggiorna il record se necessario
            if (_currentScore.value > _highScore.value) {
                _highScore.value = _currentScore.value
            }
            _gameOver.value = true
        }
    }

    // Verifica se la risposta è corretta
    private fun isCorrectAnswer(selectedLeft: Boolean): Boolean {
        val left = _leftWaste.value ?: return false
        val right = _rightWaste.value ?: return false

        return when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> {
                // Se la domanda è sul tempo di decomposizione, vince il maggiore
                if (left.decompositionTimeYears > right.decompositionTimeYears) selectedLeft else !selectedLeft
            }
            QuestionType.POLLUTION_LEVEL -> {
                // Se la domanda è sul livello di inquinamento, vince il maggiore
                if (left.pollutionLevel > right.pollutionLevel) selectedLeft else !selectedLeft
            }
            QuestionType.PREVALENCE_LEVEL -> {
                // Se la domanda è sulla prevalenza, vince il maggiore
                if (left.prevalenceLevel > right.prevalenceLevel) selectedLeft else !selectedLeft
            }
        }
    }

    // Imposta il motivo della sconfitta
    private fun setGameOverReason() {
        val left = _leftWaste.value
        val right = _rightWaste.value

        if (left == null || right == null) {
            _gameOverReason.value = "Errore imprevisto."
            return
        }

        when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> {
                val correct = if (left.decompositionTimeYears > right.decompositionTimeYears) left else right
                val incorrect = if (correct == left) right else left
                val correctYears = if (correct.decompositionTimeYears < 1)
                    "${(correct.decompositionTimeYears * 12).toInt()} mesi"
                else
                    "${correct.decompositionTimeYears} anni"
                val incorrectYears = if (incorrect.decompositionTimeYears < 1)
                    "${(incorrect.decompositionTimeYears * 12).toInt()} mesi"
                else
                    "${incorrect.decompositionTimeYears} anni"

                _gameOverReason.value = "${correct.name} impiega $correctYears a decomporsi, mentre ${incorrect.name} solo $incorrectYears."
            }
            QuestionType.POLLUTION_LEVEL -> {
                val correct = if (left.pollutionLevel > right.pollutionLevel) left else right
                val incorrect = if (correct == left) right else left

                _gameOverReason.value = "${correct.name} ha un livello di inquinamento di ${correct.pollutionLevel}/10, mentre ${incorrect.name} di ${incorrect.pollutionLevel}/10."
            }
            QuestionType.PREVALENCE_LEVEL -> {
                val correct = if (left.prevalenceLevel > right.prevalenceLevel) left else right
                val incorrect = if (correct == left) right else left

                _gameOverReason.value = "${correct.name} ha un livello di presenza sulla Terra di ${correct.prevalenceLevel}/10, mentre ${incorrect.name} di ${incorrect.prevalenceLevel}/10."
            }
        }
    }

    // Continua al prossimo round
    fun continueGame() {
        startNewRound()
    }

    // Reinizializza il gioco
    fun resetGame() {
        _currentScore.value = 0
        _gameOver.value = false
        _currentLevel.value = 1
        _comboStreak.value = 0
        usedWasteIds.clear()
        startNewRound()
    }

    // Chiude il tutorial
    fun dismissTutorial() {
        _showTutorial.value = false
    }

    // Aggiorna il timer
    fun updateTimer() {
        if (_timerActive.value && _timeRemaining.value > 0) {
            _timeRemaining.value -= 1

            // Tempo scaduto
            if (_timeRemaining.value == 0) {
                _timerActive.value = false
                // Termina il gioco se il tempo scade
                if (_currentScore.value > _highScore.value) {
                    _highScore.value = _currentScore.value
                }
                _gameOverReason.value = "Tempo scaduto! Devi rispondere più velocemente."
                _gameOver.value = true
                _showResult.value = true
                _lastChoiceCorrect.value = false
            }
        }
    }

    // Ottieni il testo della domanda corrente
    fun getCurrentQuestionText(): String {
        return when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> "Quale impiega PIÙ TEMPO a decomporsi?"
            QuestionType.POLLUTION_LEVEL -> "Quale INQUINA DI PIÙ l'ambiente?"
            QuestionType.PREVALENCE_LEVEL -> "Quale è PIÙ PRESENTE sulla Terra?"
        }
    }

    // Ottieni la corretta risposta per mostrare nel feedback
    fun getCorrectAnswerText(): String {
        val left = _leftWaste.value
        val right = _rightWaste.value

        if (left == null || right == null) return ""

        val correctWaste = when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> {
                if (left.decompositionTimeYears > right.decompositionTimeYears) left else right
            }
            QuestionType.POLLUTION_LEVEL -> {
                if (left.pollutionLevel > right.pollutionLevel) left else right
            }
            QuestionType.PREVALENCE_LEVEL -> {
                if (left.prevalenceLevel > right.prevalenceLevel) left else right
            }
        }

        val value = when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> {
                val years = correctWaste.decompositionTimeYears
                if (years < 1) {
                    "${(years * 12).toInt()} mesi"
                } else {
                    "$years anni"
                }
            }
            QuestionType.POLLUTION_LEVEL -> "${correctWaste.pollutionLevel}/10"
            QuestionType.PREVALENCE_LEVEL -> "${correctWaste.prevalenceLevel}/10"
        }

        return "Risposta: ${correctWaste.name} - $value"
    }

    // Ottieni il dettaglio delle statistiche di entrambi gli oggetti
    fun getDetailedComparison(): String {
        val left = _leftWaste.value
        val right = _rightWaste.value

        if (left == null || right == null) return ""

        return when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> {
                val leftTime = if (left.decompositionTimeYears < 1)
                    "${(left.decompositionTimeYears * 12).toInt()} mesi"
                else
                    "${left.decompositionTimeYears} anni"
                val rightTime = if (right.decompositionTimeYears < 1)
                    "${(right.decompositionTimeYears * 12).toInt()} mesi"
                else
                    "${right.decompositionTimeYears} anni"

                "${left.name}: $leftTime vs ${right.name}: $rightTime"
            }
            QuestionType.POLLUTION_LEVEL -> {
                "${left.name}: ${left.pollutionLevel}/10 vs ${right.name}: ${right.pollutionLevel}/10\n" +
                        "La scala va da 1 (impatto minimo) a 10 (massimo impatto ambientale)"
            }
            QuestionType.PREVALENCE_LEVEL -> {
                // Convertire i livelli di prevalenza in stime reali
                val leftEstimate = when(left.prevalenceLevel) {
                    10 -> "Oltre 500 miliardi"
                    9 -> "100-500 miliardi"
                    8 -> "50-100 miliardi"
                    7 -> "10-50 miliardi"
                    6 -> "1-10 miliardi"
                    5 -> "500 milioni-1 miliardo"
                    4 -> "100-500 milioni"
                    3 -> "10-100 milioni"
                    2 -> "1-10 milioni"
                    else -> "Meno di 1 milione"
                }

                val rightEstimate = when(right.prevalenceLevel) {
                    10 -> "Oltre 500 miliardi"
                    9 -> "100-500 miliardi"
                    8 -> "50-100 miliardi"
                    7 -> "10-50 miliardi"
                    6 -> "1-10 miliardi"
                    5 -> "500 milioni-1 miliardo"
                    4 -> "100-500 milioni"
                    3 -> "10-100 milioni"
                    2 -> "1-10 milioni"
                    else -> "Meno di 1 milione"
                }

                "${left.name}: ~$leftEstimate unità (${left.prevalenceLevel}/10)\n" +
                        "${right.name}: ~$rightEstimate unità (${right.prevalenceLevel}/10)"
            }
        }
    }

    // Ottieni il fatto educativo sul rifiuto corretto
    fun getEducationalFact(): String {
        val left = _leftWaste.value
        val right = _rightWaste.value

        if (left == null || right == null) return ""

        return when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> {
                if (left.decompositionTimeYears > right.decompositionTimeYears) left.educationalFact else right.educationalFact
            }
            QuestionType.POLLUTION_LEVEL -> {
                if (left.pollutionLevel > right.pollutionLevel) left.educationalFact else right.educationalFact
            }
            QuestionType.PREVALENCE_LEVEL -> {
                if (left.prevalenceLevel > right.prevalenceLevel) left.educationalFact else right.educationalFact
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = EcoGreen,
                    primaryContainer = EcoLightGreen,
                    secondary = EcoBlue,
                    secondaryContainer = EcoLightBlue,
                    background = EcoBackground,
                    error = EcoError
                )
            ) {
                EcoGameScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoGameScreen(viewModel: EcoGameViewModel = viewModel()) {
    val leftWaste by viewModel.leftWaste
    val rightWaste by viewModel.rightWaste
    val currentScore by viewModel.currentScore
    val highScore by viewModel.highScore
    val gameOver by viewModel.gameOver
    val showResult by viewModel.showResult
    val lastChoiceCorrect by viewModel.lastChoiceCorrect
    val currentQuestion = viewModel.getCurrentQuestionText()
    val currentLevel by viewModel.currentLevel
    val comboStreak by viewModel.comboStreak
    val timeRemaining by viewModel.timeRemaining
    val timerActive by viewModel.timerActive
    val showTutorial by viewModel.showTutorial

    // Per gestire l'uscita dall'app
    val context = LocalContext.current
    val activity = (context as? ComponentActivity)

    // Timer
    LaunchedEffect(timerActive) {
        while (timerActive) {
            delay(1000L)
            viewModel.updateTimer()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (gameOver) {
            GameOverScreen(
                score = currentScore,
                onRestart = { viewModel.resetGame() },
                onBack = { activity?.finish() },
                viewModel = viewModel
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Barra superiore con informazioni sul punteggio
                    TopBar(currentScore, highScore, currentLevel, comboStreak, timeRemaining)

                    // Domanda attuale
                    QuestionHeader(currentQuestion)

                    // Area di gioco principale
                    Row(modifier = Modifier.weight(1f)) {
                        // Lato sinistro
                        WasteCard(
                            waste = leftWaste,
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            cardColor = EcoLightGreen,
                            borderColor = EcoGreen,
                            onClick = { viewModel.makeSelection(true) },
                            enabled = !showResult && !showTutorial
                        )

                        // Lato destro
                        WasteCard(
                            waste = rightWaste,
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            cardColor = EcoLightBlue,
                            borderColor = EcoBlue,
                            onClick = { viewModel.makeSelection(false) },
                            enabled = !showResult && !showTutorial
                        )
                    }

                    // Area per i risultati e pulsante continua
                    if (showResult) {
                        ResultSection(
                            correct = lastChoiceCorrect,
                            correctAnswerText = viewModel.getCorrectAnswerText(),
                            educationalFact = viewModel.getEducationalFact(),
                            onContinue = { viewModel.continueGame() }
                        )
                    } else if (!showTutorial) {
                        // Indicazioni all'utente
                        InstructionFooter()
                    }
                }

                // Mostra il tutorial se necessario
                if (showTutorial) {
                    TutorialOverlay(onDismiss = { viewModel.dismissTutorial() })
                }
            }
        }
    }
}

@Composable
fun TopBar(currentScore: Int, highScore: Int, currentLevel: Int, comboStreak: Int, timeRemaining: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Punteggio: $currentScore",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Record: $highScore",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Livello
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Livello: $currentLevel",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Combo
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                    Text(
                        text = "Combo: $comboStreak",
                        color = Color.Yellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                } else {
                    Text(
                        text = "Combo: $comboStreak",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Timer
            Row(verticalAlignment = Alignment.CenterVertically) {
                val timerColor = when {
                    timeRemaining <= 3 -> Color.Red
                    timeRemaining <= 7 -> Color.Yellow
                    else -> Color.White
                }

                Text(
                    text = "Tempo: $timeRemaining",
                    color = timerColor,
                    fontWeight = if (timeRemaining <= 5) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }

        // Barra del tempo
        LinearProgressIndicator(
            progress = timeRemaining / 15f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = when {
                timeRemaining <= 3 -> Color.Red
                timeRemaining <= 7 -> Color.Yellow
                else -> Color.White
            },
            trackColor = Color.DarkGray
        )
    }
}

@Composable
fun QuestionHeader(question: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = question,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WasteCard(
    waste: Waste?,
    modifier: Modifier = Modifier,
    cardColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val cardElevation by animateDpAsState(
        targetValue = if (enabled) 4.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(3.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        waste?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Immagine del rifiuto con ombra
                Surface(
                    modifier = Modifier
                        .size(160.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = cardElevation
                ) {
                    Image(
                        painter = painterResource(id = it.imageResId),
                        contentDescription = it.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nome del rifiuto
                Text(
                    text = it.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ResultSection(
    correct: Boolean,
    correctAnswerText: String,
    educationalFact: String,
    onContinue: () -> Unit
) {
    val backgroundColor = if (correct) Color(0xFFDCEDC8) else Color(0xFFFFCDD2)
    val textColor = if (correct) Color(0xFF33691E) else Color(0xFFB71C1C)
    val viewModel: EcoGameViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (correct) "Corretto!" else "Sbagliato!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = correctAnswerText,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mostra la comparazione dettagliata
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFEEEEEE)
        ) {
            Text(
                text = viewModel.getDetailedComparison(),
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fatto educativo
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFEEEEEE)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Lo sapevi?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = educationalFact,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (correct) {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Continua", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

@Composable
fun InstructionFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tocca l'oggetto che pensi sia la risposta corretta!",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )
    }
}

@Composable
fun TutorialOverlay(onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 3

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable {
                if (currentStep < totalSteps) {
                    currentStep++
                } else {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when(currentStep) {
                        1 -> "Benvenuto a EcoGame!"
                        2 -> "Come si gioca"
                        else -> "Consigli finali"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreen
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when(currentStep) {
                        1 -> "Questo gioco ti aiuterà a imparare l'impatto ambientale dei rifiuti in modo divertente! Ad ogni turno dovrai scegliere tra due oggetti in base alla domanda."
                        2 -> "Rispondi prima che scada il tempo. Più veloce sei, più punti guadagnerai! Risposte corrette consecutive ti daranno un bonus combo."
                        else -> "Ricorda: Livelli più alti = meno tempo. Impara dai fatti educativi per aumentare la tua conoscenza sull'ambiente!"
                    },
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Indicatori di step
                    for (i in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (i <= currentStep) EcoGreen else Color.LightGray)
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (currentStep < totalSteps) "Continua" else "Inizia a giocare",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    viewModel: EcoGameViewModel = viewModel()
) {
    var totalPoints by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Invia il punteggio al server quando viene mostrata la schermata di Game Over
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Over",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = EcoGreen
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Punteggio Finale",
            style = MaterialTheme.typography.titleLarge,
            color = Color.DarkGray
        )

        Text(
            text = "$score",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = EcoGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostra il motivo della sconfitta
        Text(
            text = viewModel.gameOverReason.value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mostra i punti totali o l'errore
        if (isLoading) {
            CircularProgressIndicator(color = EcoGreen)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Aggiornamento punti in corso...", color = Color.Gray)
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = EcoError,
                textAlign = TextAlign.Center
            )
        } else {
            // Card con i punti totali
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = EcoLightGreen
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Eco Points Totali",
                        style = MaterialTheme.typography.titleMedium,
                        color = EcoGreen,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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

        // Pulsanti
        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gioca ancora", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Torna alla Home", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}