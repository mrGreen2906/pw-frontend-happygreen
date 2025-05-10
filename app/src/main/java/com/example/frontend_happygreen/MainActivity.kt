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
import com.example.frontend_happygreen.audio.AudioController
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.screens.AuthScreen
import com.example.frontend_happygreen.screens.AuthViewModel
import com.example.frontend_happygreen.screens.LoadingScreen
import com.example.frontend_happygreen.screens.MainScreen
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
    // Usa lo stato di login da UserSession.isLoggedInFlow
    val isLoggedIn by UserSession.isLoggedInFlow.collectAsState()

    // Stato per tracciare il flusso dell'app
    val (currentScreen, setCurrentScreen) = remember {
        mutableStateOf<Screen>(if (isLoggedIn) Screen.Loading else Screen.Welcome)
    }
    var userId by remember { mutableStateOf<Int?>(null) }

    // Importante: reagisci ai cambiamenti dello stato di login
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            setCurrentScreen(Screen.Loading)
        } else {
            setCurrentScreen(Screen.Welcome)
        }
    }

    when (val screen = currentScreen) {
        Screen.Welcome -> WelcomeScreen(
            onGetStartedClick = { setCurrentScreen(Screen.Auth) }
        )

        Screen.Auth -> AuthScreen(
            onAuthComplete = {
                setCurrentScreen(Screen.Loading)
            },
            onNeedVerification = { id ->
                userId = id
                setCurrentScreen(Screen.VerifyOTP)
            }
        )

        Screen.Loading -> LoadingScreen(
            onLoadingComplete = { setCurrentScreen(Screen.Main) }
        )

        Screen.Main -> MainScreen(
            volumeLevel = volumeLevel,
            onVolumeChange = onVolumeChange,
            onLogout = {
                // Esegui il logout e torna alla schermata di benvenuto
                UserSession.clear()
                setCurrentScreen(Screen.Welcome)
            }
        )

        Screen.VerifyOTP -> {
            userId?.let { id ->
                VerifyOTPScreen(
                    userId = id,
                    onVerificationComplete = {
                        setCurrentScreen(Screen.Loading)
                    }
                )
            } ?: run {
                // Se userId è null, torna alla schermata di autenticazione
                setCurrentScreen(Screen.Auth)
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