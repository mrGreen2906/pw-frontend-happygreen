package com.example.frontend_happygreen.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Modello dei dati per i rifiuti
data class Waste(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val decompositionTimeYears: Float,
    val pollutionLevel: Int, // da 1 a 10
    val prevalenceLevel: Int // da 1 a 10, quanto è diffuso sulla Terra
)

// Enum class per il tipo di domanda
enum class QuestionType {
    DECOMPOSITION_TIME,
    POLLUTION_LEVEL,
    PREVALENCE_LEVEL
}

// ViewModel per la logica di gioco
class EcoGameViewModel : ViewModel() {
    // Database dei rifiuti (in una vera app lo prenderesti da una fonte esterna)
    private val wasteDatabase = listOf(
        Waste(1, "Bottiglia di plastica", R.drawable.placeholder, 450f, 8, 10),
        Waste(2, "Sacchetto di plastica", R.drawable.placeholder, 20f, 7, 9),
        Waste(3, "Lattina di alluminio", R.drawable.placeholder, 200f, 6, 8),
        Waste(4, "Buccia di banana", R.drawable.placeholder, 0.1f, 1, 5),
        Waste(5, "Mozzicone di sigaretta", R.drawable.placeholder, 5f, 9, 10),
        Waste(6, "Pannolino usa e getta", R.drawable.placeholder, 500f, 7, 8),
        Waste(7, "Giornale", R.drawable.placeholder, 0.2f, 2, 6),
        Waste(8, "Bicchiere di carta", R.drawable.placeholder, 0.1f, 3, 7),
        Waste(9, "Bottiglia di vetro", R.drawable.placeholder, 1000f, 4, 7),
        Waste(10, "Gomma da masticare", R.drawable.placeholder, 5f, 7, 9),
        Waste(11, "Apparecchio elettronico", R.drawable.placeholder, 1000f, 10, 8),
        Waste(12, "Batteria", R.drawable.placeholder, 100f, 10, 7)
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

    // Per mostrare il risultato della scelta
    private var _showResult = mutableStateOf(false)
    val showResult: State<Boolean> = _showResult

    private var _lastChoiceCorrect = mutableStateOf(false)
    val lastChoiceCorrect: State<Boolean> = _lastChoiceCorrect

    init {
        startNewRound()
    }

    // Inizia un nuovo round
    fun startNewRound() {
        // Seleziona due rifiuti diversi casualmente
        var left = wasteDatabase.random()
        var right = wasteDatabase.random()

        // Assicurati che siano diversi
        while (left.id == right.id) {
            right = wasteDatabase.random()
        }

        _leftWaste.value = left
        _rightWaste.value = right

        // Scegli casualmente il tipo di domanda
        _currentQuestion.value = QuestionType.values().random()

        _showResult.value = false
    }

    // Gestisce la selezione dell'utente
    fun makeSelection(selectedLeft: Boolean) {
        val correct = isCorrectAnswer(selectedLeft)

        _lastChoiceCorrect.value = correct
        _showResult.value = true

        if (correct) {
            // Aumenta il punteggio se la risposta è corretta
            _currentScore.value += 1
        } else {
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

    // Continua al prossimo round
    fun continueGame() {
        startNewRound()
    }

    // Reinizializza il gioco
    fun resetGame() {
        _currentScore.value = 0
        _gameOver.value = false
        startNewRound()
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

        return "${correctWaste.name}: $value"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (gameOver) {
            GameOverScreen(
                currentScore = currentScore,
                highScore = highScore,
                onRestart = { viewModel.resetGame() }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra superiore con informazioni sul punteggio
                TopBar(currentScore, highScore)

                // Domanda attuale
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentQuestion,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }

                // Area di gioco principale
                Row(modifier = Modifier.weight(1f)) {
                    // Lato sinistro
                    WasteCard(
                        waste = leftWaste,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                            .border(2.dp, Color(0xFF388E3C), RoundedCornerShape(12.dp)),
                        onClick = { viewModel.makeSelection(true) },
                        enabled = !showResult
                    )

                    // Lato destro
                    WasteCard(
                        waste = rightWaste,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                            .border(2.dp, Color(0xFF1976D2), RoundedCornerShape(12.dp)),
                        onClick = { viewModel.makeSelection(false) },
                        enabled = !showResult
                    )
                }

                // Area per i risultati e pulsante continua
                if (showResult) {
                    ResultSection(
                        correct = lastChoiceCorrect,
                        correctAnswerText = viewModel.getCorrectAnswerText(),
                        onContinue = { viewModel.continueGame() }
                    )
                } else {
                    // Indicazioni all'utente
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
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(currentScore: Int, highScore: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
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
}

@Composable
fun WasteCard(
    waste: Waste?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        waste?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Immagine del rifiuto
                Image(
                    painter = painterResource(id = it.imageResId),
                    contentDescription = it.name,
                    modifier = Modifier
                        .size(160.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nome del rifiuto
                Text(
                    text = it.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ResultSection(correct: Boolean, correctAnswerText: String, onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (correct) Color(0xFFDCEDC8) else Color(0xFFFFCDD2))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (correct) "Corretto!" else "Sbagliato!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (correct) Color(0xFF33691E) else Color(0xFFB71C1C)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = correctAnswerText,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (correct) {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Continua", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun GameOverScreen(currentScore: Int, highScore: Int, onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Over!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Il tuo punteggio: $currentScore",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (currentScore >= highScore && highScore > 0) {
            Text(
                text = "Nuovo Record!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Record attuale: $highScore",
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Gioca ancora", fontSize = 18.sp)
        }
    }
}