package com.example.frontend_happygreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.audio.AudioController
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.screens.AuthScreen
import com.example.frontend_happygreen.screens.AuthViewModel
import com.example.frontend_happygreen.screens.LoadingScreen
import com.example.frontend_happygreen.screens.MainScreen
import com.example.frontend_happygreen.screens.MainScreenViewModel
import com.example.frontend_happygreen.screens.VerifyOTPScreen
import com.example.frontend_happygreen.screens.WelcomeScreen
import com.example.frontend_happygreen.ui.theme.FrontendhappygreenTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var audioController: AudioController
    private var volumeLevel = 0.5f  // Default volume level

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize audio controller
        audioController = AudioController(this)

        // Start the background music immediately
        audioController.startBackgroundMusic()

        // Inizializza UserSession con l'applicazione
        UserSession.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            FrontendhappygreenTheme {
                // Track volume level state
                var currentVolumeLevel by remember { mutableFloatStateOf(volumeLevel) }

                HappyGreenApp(
                    onVolumeChange = { newVolume ->
                        currentVolumeLevel = newVolume
                        audioController.setVolume(newVolume)
                        volumeLevel = newVolume // Save to activity field
                    },
                    volumeLevel = currentVolumeLevel
                )
            }
        }

        // Salva le preferenze per l'audio quando l'activity viene distrutta
        // per garantire la persistenza anche in caso di cambio configurazione
        getPreferences(MODE_PRIVATE).edit().putFloat("volume_level", volumeLevel).apply()
    }

    override fun onResume() {
        super.onResume()
        // Carica le preferenze dell'audio quando l'activity viene ripresa
        volumeLevel = getPreferences(MODE_PRIVATE).getFloat("volume_level", 0.5f)
        audioController.setVolume(volumeLevel)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioController.unbind()
    }
}

@Composable
fun HappyGreenApp(
    onVolumeChange: (Float) -> Unit = {},
    volumeLevel: Float = 0.5f
) {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    // Stato attuale dell'autenticazione
    val isLoggedIn by UserSession.isLoggedInFlow.collectAsState(initial = false)

    // Stato della schermata
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var userId by remember { mutableStateOf<Int?>(null) }

    // Usiamo un effetto che esegue solo all'avvio

    // FLUSSO PRINCIPALE DELL'APP
    when (currentScreen) {
        Screen.Welcome -> {
            WelcomeScreen(
                onGetStartedClick = {
                    // Qui forziamo il passaggio alla schermata Auth
                    currentScreen = Screen.Auth
                }
            )
        }

        Screen.Auth -> {
            AuthScreen(
                onAuthComplete = {
                    // Se l'autenticazione è completata, passa alla schermata Loading
                    if (isLoggedIn) {
                        currentScreen = Screen.Loading
                    }
                },
                onNeedVerification = { id ->
                    userId = id
                    currentScreen = Screen.VerifyOTP
                }
            )

            // Se l'utente viene autenticato mentre è già nella schermata di auth
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    currentScreen = Screen.Loading
                }
            }
        }

        Screen.VerifyOTP -> {
            userId?.let { id ->
                VerifyOTPScreen(
                    userId = id,
                    onVerificationComplete = {
                        if (isLoggedIn) {
                            currentScreen = Screen.Loading
                        } else {
                            // Se non siamo ancora loggati, torna alla schermata di auth
                            currentScreen = Screen.Auth
                        }
                    }
                )
            } ?: run {
                // Se userId è null, torna alla schermata di autenticazione
                currentScreen = Screen.Auth
            }

            // Se l'utente viene autenticato durante la verifica
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    currentScreen = Screen.Loading
                    mainScreenViewModel.refreshData()
                }
            }
        }

        Screen.Loading -> {
            // Verifica che l'utente sia effettivamente loggato
            if (isLoggedIn) {
                LoadingScreen(
                    onLoadingComplete = { currentScreen = Screen.Main }
                )
            } else {
                // Non sei loggato? Torna alla schermata di auth
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Auth
                }
            }
        }

        Screen.Main -> {
            // Verifica costantemente che l'utente sia loggato
            if (isLoggedIn) {
                MainScreen(
                    volumeLevel = volumeLevel,
                    onVolumeChange = onVolumeChange,
                    onLogout = {
                        UserSession.clear()
                        currentScreen = Screen.Welcome
                    }
                )
            } else {
                // Non sei loggato? Torna alla schermata di welcome
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Welcome
                }
            }
        }
    }
}

sealed class Screen {
    object Welcome : Screen()
    object Auth : Screen()
    object Loading : Screen()
    object Main : Screen()
    object VerifyOTP : Screen()
}