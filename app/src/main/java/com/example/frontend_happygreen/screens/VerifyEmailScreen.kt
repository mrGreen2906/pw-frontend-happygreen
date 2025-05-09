package com.example.frontend_happygreen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green600
import kotlinx.coroutines.launch

/**
 * ViewModel per la verifica tramite codice OTP
 */
class VerifyOTPViewModel : ViewModel() {
    private val apiService = RetrofitClient.create(ApiService::class.java)

    var isLoading = mutableStateOf(false)
    var error = mutableStateOf<String?>(null)
    var resendSuccess = mutableStateOf(false)

    /**
     * Verifica il codice OTP con il backend
     */
    suspend fun verifyCode(userId: Int, code: String, onSuccess: () -> Unit) {
        if (code.length != 6) {
            error.value = "Il codice deve essere di 6 cifre"
            return
        }

        isLoading.value = true
        error.value = null

        try {
            // Chiamata API per verificare il codice
            val response = apiService.verifyOTP(userId, mapOf("code" to code))

            if (response.isSuccessful) {
                // Salva token e dati utente se presenti nella risposta
                response.body()?.let { authResponse ->
                    val token = authResponse["token"] as? String
                    val userData = authResponse["user"] as? Map<String, Any>

                    if (token != null && userData != null) {
                        // Salva sessione utente
                        UserSession.saveUserSession(
                            token = token,
                            userId = (userData["id"] as Double).toInt(),
                            username = userData["username"] as String,
                            email = userData["email"] as String,
                            firstName = (userData["first_name"] as? String) ?: "",
                            lastName = (userData["last_name"] as? String) ?: "",
                            avatar = userData["avatar"] as? String,
                            ecoPoints = (userData["eco_points"] as? Double)?.toInt() ?: 0
                        )
                    }
                }
                onSuccess()
            } else {
                when (response.code()) {
                    400 -> error.value = "Codice non valido o scaduto"
                    404 -> error.value = "Utente non trovato"
                    else -> error.value = "Errore nella verifica: ${response.code()}"
                }
            }
        } catch (e: Exception) {
            error.value = "Errore di connessione: ${e.message}"
        } finally {
            isLoading.value = false
        }
    }

    /**
     * Richiede l'invio di un nuovo codice di verifica
     */
    suspend fun resendCode(userId: Int, email: String) {
        isLoading.value = true
        error.value = null
        resendSuccess.value = false

        try {
            val response = apiService.resendVerification(mapOf(
                "user_id" to userId.toString(),
                "email" to email
            ))

            if (response.isSuccessful) {
                resendSuccess.value = true
            } else {
                error.value = "Impossibile inviare un nuovo codice"
            }
        } catch (e: Exception) {
            error.value = "Errore di connessione: ${e.message}"
        } finally {
            isLoading.value = false
        }
    }
}

/**
 * Schermata per la verifica tramite codice OTP
 */
@Composable
fun VerifyOTPScreen(
    userId: Int,
    onVerificationComplete: () -> Unit,
    viewModel: VerifyOTPViewModel = viewModel()
) {
    val code = remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val resendSuccess by viewModel.resendSuccess
    val coroutineScope = rememberCoroutineScope()

    // Dialogo per il reinvio del codice
    var showResendDialog by remember { mutableStateOf(false) }
    var emailForResend by remember { mutableStateOf("") }

    // Dialogo di conferma reinvio
    if (resendSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.resendSuccess.value = false },
            title = { Text("Codice inviato") },
            text = { Text("Abbiamo inviato un nuovo codice di verifica alla tua email.") },
            confirmButton = {
                Button(onClick = { viewModel.resendSuccess.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Dialogo per inserire l'email per il reinvio
    if (showResendDialog) {
        AlertDialog(
            onDismissRequest = { showResendDialog = false },
            title = { Text("Reinvia codice di verifica") },
            text = {
                Column {
                    Text("Inserisci la tua email per ricevere un nuovo codice di verifica")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailForResend,
                        onValueChange = { emailForResend = it },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.resendCode(userId, emailForResend)
                            showResendDialog = false
                        }
                    },
                    enabled = emailForResend.isNotEmpty() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Invia")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResendDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.happy_green_logo),
            contentDescription = "HappyGreen Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 32.dp)
        )

        Text(
            text = "Inserisci il codice di verifica",
            style = MaterialTheme.typography.headlineMedium,
            color = Green600,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Abbiamo inviato un codice a 6 cifre alla tua email",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo per il codice OTP
        OutlinedTextField(
            value = code.value,
            onValueChange = { newValue ->
                // Accetta solo cifre e limita a 6 caratteri
                if (newValue.all { it.isDigit() } && newValue.length <= 6) {
                    code.value = newValue
                }
            },
            label = { Text("Codice di verifica") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.verifyCode(userId, code.value, onVerificationComplete)
                }
            },
            enabled = code.value.length == 6 && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green600)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Verifica",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opzione per reinviare il codice
        TextButton(
            onClick = { showResendDialog = true }
        ) {
            Text("Non hai ricevuto il codice? Invia di nuovo")
        }
        
        if (userId > 0) {
            Text(
                text = "UserID: $userId",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}