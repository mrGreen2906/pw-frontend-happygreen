package com.example.frontend_happygreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.frontend_happygreen.audio.AudioController
import com.example.frontend_happygreen.screens.AuthScreen
import com.example.frontend_happygreen.screens.LoadingScreen
import com.example.frontend_happygreen.screens.MainScreen
import com.example.frontend_happygreen.screens.VerifyOTPScreen
import com.example.frontend_happygreen.screens.WelcomeScreen
import com.example.frontend_happygreen.ui.theme.FrontendhappygreenTheme

class MainActivity : ComponentActivity() {
    private lateinit var audioController: AudioController
    private var volumeLevel = 0.5f  // Default volume level

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize audio controller
        audioController = AudioController(this)

        // Start the background music immediately
        audioController.startBackgroundMusic()

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
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var userId by remember { mutableStateOf<Int?>(null) }

    when (val screen = currentScreen) {
        Screen.Welcome -> WelcomeScreen(
            onGetStartedClick = { currentScreen = Screen.Auth }
        )

        Screen.Auth -> AuthScreen(
            onAuthComplete = {
                currentScreen = Screen.Loading
            },
            onNeedVerification = { id ->
                userId = id
                currentScreen = Screen.VerifyOTP
            }
        )

        Screen.Loading -> LoadingScreen(
            onLoadingComplete = { currentScreen = Screen.Main }
        )

        Screen.Main -> MainScreen(
            volumeLevel = volumeLevel,
            onVolumeChange = onVolumeChange,
            onLogout = { currentScreen = Screen.Welcome }
        )

        Screen.VerifyOTP -> {
            userId?.let { id ->
                VerifyOTPScreen(
                    userId = id,
                    onVerificationComplete = {
                        currentScreen = Screen.Loading
                    }
                )
            } ?: run {
                // Se userId è null, torna alla schermata di autenticazione
                currentScreen = Screen.Auth
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