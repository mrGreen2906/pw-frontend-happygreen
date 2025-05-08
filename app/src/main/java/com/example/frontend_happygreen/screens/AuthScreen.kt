package com.example.frontend_happygreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.data.AuthRequest
import com.example.frontend_happygreen.data.RegisterRequest
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.ui.theme.Green600
import kotlinx.coroutines.launch

/**
 * ViewModel per la gestione dell'autenticazione
 */
class AuthViewModel : ViewModel() {
    // API service
    private val apiService = RetrofitClient.create(ApiService::class.java)

    // Stati per il login
    var loginUsername = mutableStateOf("")
    var loginPassword = mutableStateOf("")
    var loginError = mutableStateOf<String?>(null)
    var isLoggingIn = mutableStateOf(false)

    // Stati per la registrazione
    var registerUsername = mutableStateOf("")
    var registerEmail = mutableStateOf("")
    var registerPassword = mutableStateOf("")
    var registerConfirmPassword = mutableStateOf("")
    var registerFirstName = mutableStateOf("")
    var registerLastName = mutableStateOf("")
    var registerError = mutableStateOf<String?>(null)
    var isRegistering = mutableStateOf(false)
    var registrationSuccess = mutableStateOf(false)

    // Stati per la verifica
    var emailVerified = mutableStateOf(false)
    var verificationSent = mutableStateOf(false)

    /**
     * Effettua il login
     */
    suspend fun login(onSuccess: () -> Unit, onNeedVerification: (Int) -> Unit) {
        loginError.value = null
        isLoggingIn.value = true

        try {
            val response = apiService.login(
                AuthRequest(
                    username = loginUsername.value,
                    password = loginPassword.value
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Salva la sessione utente
                UserSession.saveUserSession(
                    token = authResponse.token,
                    userId = authResponse.user.id,
                    username = authResponse.user.username,
                    email = authResponse.user.email,
                    firstName = authResponse.user.firstName ?: "",
                    lastName = authResponse.user.lastName ?: "",
                    avatar = authResponse.user.avatar,
                    ecoPoints = authResponse.user.ecoPoints
                )
                onSuccess()
            } else if (response.code() == 403) {
                // Email non verificata - prendi userId dalla risposta
                val responseBody = response.errorBody()?.string() ?: ""

                // Esempio di risposta: {"error":"Email not verified","user_id":123}
                // Estrai l'ID dell'utente per la verifica
                val regex = "\"user_id\":(\\d+)".toRegex()
                val matchResult = regex.find(responseBody)
                val userId = matchResult?.groupValues?.get(1)?.toIntOrNull()

                if (userId != null) {
                    // Reindirizza alla verifica OTP
                    onNeedVerification(userId)
                } else {
                    loginError.value = "Email non verificata. Controlla la tua email o richiedi un nuovo codice di verifica."
                }
            } else {
                loginError.value = "Credenziali non valide. Riprova."
            }
        } catch (e: Exception) {
            loginError.value = "Errore di connessione: ${e.message}"
        } finally {
            isLoggingIn.value = false
        }
    }

    /**
     * Effettua la registrazione
     */
    suspend fun register(onNeedVerification: (Int) -> Unit) {
        // Reset degli errori
        registerError.value = null

        // Validazione
        if (registerUsername.value.isBlank() ||
            registerEmail.value.isBlank() ||
            registerPassword.value.isBlank()) {
            registerError.value = "Tutti i campi contrassegnati sono obbligatori"
            return
        }

        if (registerPassword.value != registerConfirmPassword.value) {
            registerError.value = "Le password non corrispondono"
            return
        }

        if (registerPassword.value.length < 8) {
            registerError.value = "La password deve essere di almeno 8 caratteri"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(registerEmail.value).matches()) {
            registerError.value = "Email non valida"
            return
        }

        isRegistering.value = true

        try {
            val response = apiService.register(
                RegisterRequest(
                    username = registerUsername.value,
                    email = registerEmail.value,
                    password = registerPassword.value,
                    firstName = registerFirstName.value,
                    lastName = registerLastName.value
                )
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                val userId = responseBody?.userId

                if (userId != null) {
                    // Mostra dialogo di successo
                    registrationSuccess.value = true

                    // Redirigi alla schermata di verifica OTP
                    onNeedVerification(userId)
                } else {
                    // Successo ma senza userId, mostra solo il dialogo di successo
                    registrationSuccess.value = true
                }
            } else {
                // Gestione errori HTTP
                when (response.code()) {
                    400 -> {
                        val errorBody = response.errorBody()?.string() ?: ""
                        registerError.value = when {
                            errorBody.contains("Username already exists") -> "Nome utente già registrato"
                            errorBody.contains("Email already exists") -> "Email già registrata"
                            else -> "Errore nella registrazione"
                        }
                    }
                    else -> registerError.value = "Errore nella registrazione: ${response.code()}"
                }
            }
        } catch (e: Exception) {
            registerError.value = "Errore di connessione: ${e.message}"
        } finally {
            isRegistering.value = false
        }
    }

    /**
     * Invia nuovamente il codice di verifica OTP
     */
    suspend fun resendVerificationCode(email: String, onSuccess: () -> Unit) {
        try {
            val response = apiService.resendVerification(mapOf("email" to email))
            if (response.isSuccessful) {
                verificationSent.value = true
                onSuccess()
            }
        } catch (e: Exception) {
            // Gestione errori
        }
    }

    /**
     * Pulisce gli stati di registrazione
     */
    fun resetRegistration() {
        registerUsername.value = ""
        registerEmail.value = ""
        registerPassword.value = ""
        registerConfirmPassword.value = ""
        registerFirstName.value = ""
        registerLastName.value = ""
        registerError.value = null
        registrationSuccess.value = false
    }
}
@Composable
fun AuthScreen(
    onAuthComplete: () -> Unit,
    onNeedVerification: (Int) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Login", "Registrati")
    val coroutineScope = rememberCoroutineScope()

    // Stati dal ViewModel
    val loginUsername by viewModel.loginUsername
    val loginPassword by viewModel.loginPassword
    val loginError by viewModel.loginError
    val isLoggingIn by viewModel.isLoggingIn

    val registerUsername by viewModel.registerUsername
    val registerEmail by viewModel.registerEmail
    val registerPassword by viewModel.registerPassword
    val registerConfirmPassword by viewModel.registerConfirmPassword
    val registerFirstName by viewModel.registerFirstName
    val registerLastName by viewModel.registerLastName
    val registerError by viewModel.registerError
    val isRegistering by viewModel.isRegistering
    val registrationSuccess by viewModel.registrationSuccess
    var verificationSent by viewModel.verificationSent

    // Dialogo per reinviare l'email di verifica
    var showResendDialog by remember { mutableStateOf(false) }
    var emailForResend by remember { mutableStateOf("") }

    // Dialogo di successo registrazione
    if (registrationSuccess) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetRegistration()
                selectedTabIndex = 0  // Torna al tab login
            },
            title = { Text("Registrazione completata") },
            text = {
                Text(
                    "Ti abbiamo inviato un codice di verifica via email. " +
                            "Per favore, inseriscilo nella prossima schermata per attivare il tuo account."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetRegistration()
                        selectedTabIndex = 0  // Torna al tab login
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("Ho capito")
                }
            }
        )
    }

    // Dialogo reinvio verifica
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.resendVerificationCode(emailForResend) {
                                showResendDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("Invia")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResendDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Email inviata conferma
    if (verificationSent && !registrationSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.verificationSent.value = false },
            title = { Text("Codice inviato") },
            text = { Text("Se l'email è registrata, ti abbiamo inviato un nuovo codice di verifica.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.verificationSent.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Benvenuto in HappyGreen",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Green600,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Green600
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (selectedTabIndex == 0) {
                LoginContent(
                    username = loginUsername,
                    password = loginPassword,
                    isLoading = isLoggingIn,
                    errorMessage = loginError,
                    onUsernameChange = { viewModel.loginUsername.value = it },
                    onPasswordChange = { viewModel.loginPassword.value = it },
                    onForgotPasswordClick = { showResendDialog = true },
                    onLoginClick = {
                        coroutineScope.launch {
                            viewModel.login(
                                onSuccess = onAuthComplete,
                                onNeedVerification = onNeedVerification
                            )
                        }
                    }
                )
            } else {
                RegisterContent(
                    username = registerUsername,
                    email = registerEmail,
                    password = registerPassword,
                    confirmPassword = registerConfirmPassword,
                    firstName = registerFirstName,
                    lastName = registerLastName,
                    isLoading = isRegistering,
                    errorMessage = registerError,
                    onUsernameChange = { viewModel.registerUsername.value = it },
                    onEmailChange = { viewModel.registerEmail.value = it },
                    onPasswordChange = { viewModel.registerPassword.value = it },
                    onConfirmPasswordChange = { viewModel.registerConfirmPassword.value = it },
                    onFirstNameChange = { viewModel.registerFirstName.value = it },
                    onLastNameChange = { viewModel.registerLastName.value = it },
                    onRegisterClick = {
                        coroutineScope.launch {
                            viewModel.register(onNeedVerification)
                        }
                    }
                )
            }

            // Aggiungi spazio in fondo per evitare che contenuti siano nascosti
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LoginContent(
    username: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Messaggio di errore
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Email o Username") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPasswordClick) {
                Text("Verifica account / Password dimenticata?")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Green600
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Login",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun RegisterContent(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    firstName: String,
    lastName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Messaggio di errore
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username*") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email*") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = { Text("Nome") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                label = { Text("Cognome") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password*") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Minimo 8 caratteri") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Conferma Password*") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegisterClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Green600
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Registrati",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Nota informativa
        Text(
            text = "* Campi obbligatori",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}