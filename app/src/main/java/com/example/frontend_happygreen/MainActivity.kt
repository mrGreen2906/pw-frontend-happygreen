package com.example.frontend_happygreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var audioController: AudioController

    // Stati per la musica
    private var volumeLevel by mutableStateOf(0.5f)
    private var musicStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza UserSession
        UserSession.init(this)

        // Inizializza AudioController
        audioController = AudioController(this)

        setContent {
            FrontendhappygreenTheme {
                var showMainApp by remember { mutableStateOf(false) }
                var showWelcome by remember { mutableStateOf(true) }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Loading) }
                var needsVerification by remember { mutableStateOf<Int?>(null) }

                // Osserva lo stato di login
                val isLoggedIn by UserSession.isLoggedInFlow.collectAsState()

                LaunchedEffect(isLoggedIn) {
                    when {
                        isLoggedIn -> {
                            currentScreen = Screen.Main
                            showWelcome = false
                            showMainApp = true

                            // Avvia la musica quando l'utente è loggato
                            if (!musicStarted) {
                                startBackgroundMusic()
                                musicStarted = true
                            }
                        }
                        else -> {
                            currentScreen = Screen.Welcome
                            showMainApp = false
                            showWelcome = true

                            // Ferma la musica quando l'utente non è loggato
                            if (musicStarted) {
                                stopBackgroundMusic()
                                musicStarted = false
                            }
                        }
                    }
                }

                when (currentScreen) {
                    Screen.Loading -> {
                        LoadingScreen(onLoadingComplete = {
                            lifecycleScope.launch {
                                val loggedIn = UserSession.isLoggedInFlow.first()
                                currentScreen = if (loggedIn) Screen.Main else Screen.Welcome
                            }
                        })
                    }

                    Screen.Welcome -> {
                        if (showWelcome) {
                            WelcomeScreen(
                                onGetStartedClick = {
                                    showWelcome = false
                                    currentScreen = Screen.Auth
                                }
                            )
                        } else {
                            AuthScreen(
                                onAuthComplete = {
                                    currentScreen = Screen.Main
                                    showMainApp = true
                                },
                                onNeedVerification = { userId ->
                                    needsVerification = userId
                                    currentScreen = Screen.Verification
                                }
                            )
                        }
                    }

                    Screen.Verification -> {
                        needsVerification?.let { userId ->
                            VerifyOTPScreen(
                                userId = userId,
                                onVerificationComplete = {
                                    needsVerification = null
                                    currentScreen = Screen.Main
                                    showMainApp = true
                                }
                            )
                        }
                    }

                    Screen.Main -> {
                        if (showMainApp) {
                            MainScreen(
                                volumeLevel = volumeLevel,
                                onVolumeChange = { newVolume ->
                                    volumeLevel = newVolume
                                    audioController.setVolume(newVolume)
                                },
                                onLogout = {
                                    // Ferma la musica al logout
                                    stopBackgroundMusic()
                                    musicStarted = false

                                    currentScreen = Screen.Welcome
                                    showWelcome = true
                                    showMainApp = false
                                }
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun startBackgroundMusic() {
        audioController.startBackgroundMusic()
        audioController.setVolume(volumeLevel)
    }

    private fun stopBackgroundMusic() {
        audioController.stopBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        // Pausa la musica quando l'app va in background
        if (musicStarted && audioController.isPlaying()) {
            audioController.pauseMusic()
        }
    }

    override fun onResume() {
        super.onResume()
        // Riprendi la musica quando l'app torna in foreground
        if (musicStarted && !audioController.isPlaying()) {
            audioController.resumeMusic()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ferma completamente la musica quando l'Activity viene distrutta
        if (musicStarted) {
            stopBackgroundMusic()
            musicStarted = false
        }
        audioController.unbind()
    }

    // Gestisce il pulsante back del sistema
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Ferma la musica quando l'utente esce dall'app
        if (musicStarted) {
            stopBackgroundMusic()
            musicStarted = false
        }
        super.onBackPressed()
    }
}

// Enum per gestire i diversi schermi
sealed class Screen {
    object Loading : Screen()
    object Welcome : Screen()
    object Auth : Screen()
    object Verification : Screen()
    object Main : Screen()
}