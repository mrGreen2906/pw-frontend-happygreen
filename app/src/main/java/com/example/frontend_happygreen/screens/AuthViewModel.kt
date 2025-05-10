package com.example.frontend_happygreen.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.data.AuthRequest
import com.example.frontend_happygreen.data.RegisterRequest
import com.example.frontend_happygreen.data.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel per la gestione dell'autenticazione con persistenza dello stato migliorata
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

    // Controllo dello stato di login
    var isLoggedIn = mutableStateOf(false)

    init {
        // Verifica subito se l'utente è già loggato
        viewModelScope.launch {
            isLoggedIn.value = UserSession.isLoggedInFlow.first()

            // Se l'utente è già loggato ma non abbiamo i suoi dati aggiornati
            if (isLoggedIn.value) {
                refreshUserData()
            }
        }
    }

    /**
     * Aggiorna i dati dell'utente dal server
     */
    private suspend fun refreshUserData() {
        try {
            val token = UserSession.getAuthHeader() ?: return
            val response = apiService.getCurrentUser(token)

            if (response.isSuccessful && response.body() != null) {
                val userData = response.body()!!
                UserSession.updateUserData(userData)
            } else {
                // Se non riusciamo a ottenere i dati dell'utente, il token potrebbe essere scaduto
                // Forziamo il logout per sicurezza
                if (response.code() == 401) {
                    UserSession.clear()
                    isLoggedIn.value = false
                }
            }
        } catch (e: Exception) {
            // Errore di rete, non facciamo nulla e manteniamo i dati in cache
        }
    }

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

                isLoggedIn.value = true
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

    /**
     * Effettua il logout
     */
    fun logout() {
        UserSession.clear()
        isLoggedIn.value = false
    }
}