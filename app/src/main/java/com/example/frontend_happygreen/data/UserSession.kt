package com.example.frontend_happygreen.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
object UserSession {
    // Chiavi per le SharedPreferences
    private const val PREFS_NAME = "happy_green_user_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"
    private const val KEY_AVATAR = "avatar"
    private const val KEY_ECO_POINTS = "eco_points"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    // StateFlow per reattività dell'UI
    private val _isLoggedInFlow = MutableStateFlow(false)
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow.asStateFlow()

    private val _userDataFlow = MutableStateFlow<UserData?>(null)
    val userDataFlow: StateFlow<UserData?> = _userDataFlow.asStateFlow()

    // Variabili di istanza private per i dati della sessione
    private var token: String? = null
    private var userId: Int? = null
    private var username: String? = null
    private var email: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var avatar: String? = null
    private var ecoPoints: Int? = null

    // SharedPreferences
    private lateinit var preferences: SharedPreferences

    /**
     * Verifica se l'utente ha un'autenticazione valida
     */
    fun hasValidAuthentication(): Boolean {
        return token != null && userId != null && _isLoggedInFlow.value
    }

    /**
     * Inizializza la sessione utente dal contesto
     * Da chiamare all'avvio dell'applicazione (in Application o Activity principale)
     */
    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Verifica se l'utente era precedentemente loggato
        val storedLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false)

        // Se non era loggato, non c'è niente da caricare
        if (!storedLoggedIn) {
            _isLoggedInFlow.value = false
            _userDataFlow.value = null
            return
        }

        // Carica i dati solo se l'utente era precedentemente loggato
        token = preferences.getString(KEY_TOKEN, null)
        userId = preferences.getInt(KEY_USER_ID, -1).let { if (it == -1) null else it }
        username = preferences.getString(KEY_USERNAME, null)
        email = preferences.getString(KEY_EMAIL, null)
        firstName = preferences.getString(KEY_FIRST_NAME, null)
        lastName = preferences.getString(KEY_LAST_NAME, null)
        avatar = preferences.getString(KEY_AVATAR, null)
        ecoPoints = preferences.getInt(KEY_ECO_POINTS, -1).let { if (it == -1) null else it }

        // Verifica se i dati essenziali sono presenti
        val isValidSession = token != null && userId != null

        if (isValidSession) {
            // Se abbiamo una sessione valida, aggiorna gli StateFlow
            _isLoggedInFlow.value = true
            _userDataFlow.value = UserData(
                id = userId!!,
                username = username ?: "",
                email = email ?: "",
                firstName = firstName,
                lastName = lastName,
                avatar = avatar,
                ecoPoints = ecoPoints ?: 0
            )
        } else {
            // Se i dati sono corrotti o mancanti ma il flag era true,
            // reimposta tutto per evitare uno stato incoerente
            clear()
        }
    }

    /**
     * Salva la sessione dell'utente dopo il login
     */
    fun saveUserSession(
        token: String,
        userId: Int,
        username: String,
        email: String,
        firstName: String = "",
        lastName: String = "",
        avatar: String? = null,
        ecoPoints: Int = 0
    ) {
        // Imposta i valori in memoria
        this.token = token
        this.userId = userId
        this.username = username
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.avatar = avatar
        this.ecoPoints = ecoPoints

        // Salva nelle SharedPreferences
        if (::preferences.isInitialized) {
            preferences.edit(commit = true) {
                putString(KEY_TOKEN, token)
                putInt(KEY_USER_ID, userId)
                putString(KEY_USERNAME, username)
                putString(KEY_EMAIL, email)
                putString(KEY_FIRST_NAME, firstName)
                putString(KEY_LAST_NAME, lastName)
                putString(KEY_AVATAR, avatar)
                putInt(KEY_ECO_POINTS, ecoPoints)
                putBoolean(KEY_IS_LOGGED_IN, true)
            }
        }

        // Aggiorna i StateFlow
        _isLoggedInFlow.value = true
        _userDataFlow.value = UserData(
            id = userId,
            username = username,
            email = email,
            firstName = firstName,
            lastName = lastName,
            avatar = avatar,
            ecoPoints = ecoPoints
        )
    }

    /**
     * Aggiorna i dati dell'utente in sessione
     */
    fun updateUserData(userData: UserData) {
        userId = userData.id
        username = userData.username
        email = userData.email
        firstName = userData.firstName
        lastName = userData.lastName
        avatar = userData.avatar
        ecoPoints = userData.ecoPoints

        // Aggiorna nelle SharedPreferences
        if (::preferences.isInitialized) {
            preferences.edit(commit = true) {
                putInt(KEY_USER_ID, userData.id)
                putString(KEY_USERNAME, userData.username)
                putString(KEY_EMAIL, userData.email)
                putString(KEY_FIRST_NAME, userData.firstName ?: "")
                putString(KEY_LAST_NAME, userData.lastName ?: "")
                putString(KEY_AVATAR, userData.avatar)
                putInt(KEY_ECO_POINTS, userData.ecoPoints)
            }
        }

        // Aggiorna lo userDataFlow
        _userDataFlow.value = userData
    }

    /**
     * Verifica se l'utente è autenticato
     */
    fun isLoggedIn(): Boolean = _isLoggedInFlow.value

    /**
     * Restituisce l'header di autorizzazione per le richieste API
     */
    fun getAuthHeader(): String? = token?.let { "Token $it" }

    // Getters per i dati dell'utente
    fun getToken(): String? = token
    fun getUserId(): Int? = userId
    fun getUsername(): String? = username
    fun getEmail(): String? = email
    fun getFullName(): String = "${firstName ?: ""} ${lastName ?: ""}".trim()
    fun getFirstName(): String? = firstName
    fun getLastName(): String? = lastName
    fun getAvatar(): String? = avatar
    fun getEcoPoints(): Int? = ecoPoints

    /**
     * Cancella la sessione utente (logout)
     */
    fun clear() {
        // Reset di tutti i valori in memoria
        token = null
        userId = null
        username = null
        email = null
        firstName = null
        lastName = null
        avatar = null
        ecoPoints = null

        // Aggiornamento immediato degli stati di flusso
        _isLoggedInFlow.value = false
        _userDataFlow.value = null

        // Pulizia completa delle SharedPreferences
        if (::preferences.isInitialized) {
            preferences.edit {
                // Rimuovi esplicitamente ogni chiave
                remove(KEY_TOKEN)
                remove(KEY_USER_ID)
                remove(KEY_USERNAME)
                remove(KEY_EMAIL)
                remove(KEY_FIRST_NAME)
                remove(KEY_LAST_NAME)
                remove(KEY_AVATAR)
                remove(KEY_ECO_POINTS)
                putBoolean(KEY_IS_LOGGED_IN, false) // Importante: imposta esplicitamente a false

                // Assicurati che le modifiche vengano applicate immediatamente
                commit()
            }
        }
    }

    fun setEcoPoints(points: Int) {
        // Aggiorna il valore in memoria
        this.ecoPoints = points

        // Aggiorna nelle SharedPreferences
        if (::preferences.isInitialized) {
            preferences.edit(commit = true) {
                putInt(KEY_ECO_POINTS, points)
            }
        }

        // Aggiorna lo userDataFlow
        _userDataFlow.value = _userDataFlow.value?.copy(ecoPoints = points)
    }
}