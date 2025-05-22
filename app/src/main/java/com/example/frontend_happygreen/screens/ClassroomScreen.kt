package com.example.frontend_happygreen.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.ui.theme.Gray100
import com.example.frontend_happygreen.ui.theme.Gray400
import com.example.frontend_happygreen.ui.theme.Gray600
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.CreatePostRequest
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.data.Comment
import com.example.frontend_happygreen.data.MemberData
import com.example.frontend_happygreen.data.UserSession
import com.example.frontend_happygreen.data.ClassRoom
import com.example.frontend_happygreen.ui.theme.Green300
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.TimeZone

/**
 * Data Models per la UI dei post con like e reactions
 */
/**
 * Data Models per la UI dei post con like e reactions - VERSIONE CORRETTA
 */
data class ClassPost(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatarId: Int = R.drawable.happy_green_logo,
    val isTeacher: Boolean = false,
    val content: String,
    val imageUrl: String? = null,
    val reactions: Map<String, Int> = emptyMap(), // emoji -> count
    val comments: List<PostComment> = emptyList(),
    val liked: Boolean = false,
    val likeCount: Int = 0,
    val userReaction: String? = null // La reaction dell'utente corrente
)

data class PostComment(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatarId: Int = R.drawable.happy_green_logo,
    val content: String,
)

/**
 * ViewModel aggiornato con reset automatico e gestione like/reactions
 */
class ClassroomViewModel : ViewModel() {
    private val apiService = RetrofitClient.create(ApiService::class.java)

    // Post del gruppo (SOLO quelli del gruppo specifico)
    private val _posts = mutableStateOf<List<ClassPost>>(emptyList())
    val posts: State<List<ClassPost>> = _posts

    // Membri del gruppo
    private val _members = MutableStateFlow<List<MemberData>>(emptyList())

    // Stati UI
    var newPostContent = mutableStateOf("")
    var selectedImageUri = mutableStateOf<String?>(null)
    var showPostInput = mutableStateOf(false)
    var showImagePreview = mutableStateOf(false)
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    // ID del gruppo corrente per gestire il reset
    private var currentGroupId: Int = -1
// In ClassroomScreen.kt - DateUtils migliorato

    object DateUtils {
        // Formatti per parsing delle date dal backend
        private val API_DATE_FORMATS = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        )
        fun parseApiDate(dateString: String?): Date {
            if (dateString.isNullOrBlank()) {
                Log.w("DateUtils", "Date string is null or blank")
                return Date()
            }

            Log.d("DateUtils", "Parsing date: $dateString")

            for (format in API_DATE_FORMATS) {
                try {
                    val parsedDate = format.parse(dateString)
                    if (parsedDate != null) {
                        Log.d("DateUtils", "Successfully parsed date: $dateString -> $parsedDate")
                        return parsedDate
                    }
                } catch (e: Exception) {
                    // Continua con il prossimo formato
                    continue
                }
            }

            Log.e("DateUtils", "Failed to parse date: $dateString")
            // Come fallback, restituisce la data corrente
            return Date()
        }
    }

    /**
     * Reset immediato quando si cambia gruppo
     */
    fun loadGroupData(groupId: Int) {
        if (groupId <= 0) {
            errorMessage.value = "ID gruppo non valido: $groupId"
            isLoading.value = false
            return
        }

        // IMPORTANTE: Reset immediato se è un gruppo diverso
        if (currentGroupId != groupId) {
            Log.d("ClassroomViewModel", "Switching from group $currentGroupId to group $groupId - resetting data")

            // Reset immediato degli stati
            _posts.value = emptyList()
            _members.value = emptyList()
            newPostContent.value = ""
            selectedImageUri.value = null
            showPostInput.value = false
            showImagePreview.value = false
            errorMessage.value = null

            // Aggiorna l'ID del gruppo corrente
            currentGroupId = groupId
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val token = UserSession.getAuthHeader() ?: run {
                    errorMessage.value = "Token di autenticazione non disponibile"
                    isLoading.value = false
                    return@launch
                }

                Log.d("ClassroomViewModel", "Loading data for group ID: $groupId")

                // Step 1: Carica i dettagli del gruppo
                val groupResponse = apiService.getGroupById(groupId, token)

                // Verifica che stiamo ancora caricando lo stesso gruppo
                if (currentGroupId != groupId) {
                    Log.d("ClassroomViewModel", "Group changed during loading, aborting...")
                    return@launch
                }

                if (groupResponse.isSuccessful && groupResponse.body() != null) {
                    val groupData = groupResponse.body()!!

                    Log.d("ClassroomViewModel", "Group loaded: ${groupData.name}, Members: ${groupData.members.size}")

                    // Verifica ancora che il gruppo non sia cambiato
                    if (currentGroupId != groupId) {
                        Log.d("ClassroomViewModel", "Group changed during loading, aborting...")
                        return@launch
                    }

                    // Aggiorna la lista dei membri
                    _members.value = groupData.members

                    // Step 2: Carica SOLO i post di questo gruppo specifico
                    val postsResponse = apiService.getGroupPosts(groupId, token)

                    // Verifica un'ultima volta che il gruppo non sia cambiato
                    if (currentGroupId != groupId) {
                        Log.d("ClassroomViewModel", "Group changed during loading, aborting...")
                        return@launch
                    }

                    if (postsResponse.isSuccessful && postsResponse.body() != null) {
                        val posts = postsResponse.body()!!

                        Log.d("ClassroomViewModel", "Loaded ${posts.size} posts for group $groupId")

                        // Converte i post del API in ClassPost con dati completi
                        val processedPosts = posts
                            .filter { it.group == groupId }
                            .map { post ->
                                Log.d("ClassroomViewModel", "Processing post ${post.id}, created_at: ${post.createdAt}")

                                val author = groupData.members.find { it.user.id == post.user.id }?.user
                                val userRole = groupData.members.find { it.user.id == post.user.id }?.role

                                // Processa le reactions dal server
                                val reactionsCounts = mutableMapOf<String, Int>()
                                post.reactions?.forEach { reaction ->
                                    val emoji = reaction.reaction
                                    reactionsCounts[emoji] = reactionsCounts.getOrDefault(emoji, 0) + 1
                                }

                                // Processa i commenti
                                val comments = post.comments?.map { comment ->
                                    PostComment(
                                        id = comment.id.toString(),
                                        authorName = comment.user.username,
                                        content = comment.content,
                                    )
                                } ?: emptyList()

                                // Parse della data del post
                                val postDate = DateUtils.parseApiDate(post.createdAt)
                                Log.d("ClassroomViewModel", "Post ${post.id} parsed date: $postDate")

                                ClassPost(
                                    id = post.id.toString(),
                                    authorName = author?.username ?: "Utente",
                                    isTeacher = userRole == "teacher" || userRole == "admin",
                                    content = post.caption ?: "",
                                    imageUrl = when {
                                        post.imageUrl.isNullOrBlank() -> null
                                        post.imageUrl == "https://happygreen.example.com/default-placeholder.jpg" -> null
                                        else -> post.imageUrl
                                    },
                                    reactions = reactionsCounts,
                                    comments = comments,
                                    liked = post.userLiked ?: false,
                                    likeCount = post.likeCount ?: 0,
                                    userReaction = post.userReaction
                                )
                            }


                        if (currentGroupId == groupId) {
                            _posts.value = processedPosts
                            Log.d("ClassroomViewModel", "Successfully updated posts for group $groupId")
                        }

                    } else {
                        val errorBody = postsResponse.errorBody()?.string() ?: "Errore sconosciuto"
                        Log.e("ClassroomViewModel", "Errore caricamento post per gruppo $groupId: ${postsResponse.code()} - $errorBody")

                        if (currentGroupId == groupId) {
                            when (postsResponse.code()) {
                                403 -> errorMessage.value = "Non hai i permessi per visualizzare i post di questo gruppo"
                                404 -> errorMessage.value = "Gruppo non trovato"
                                else -> errorMessage.value = "Errore nel caricamento dei post: ${postsResponse.code()}"
                            }
                        }
                    }
                } else {
                    val errorBody = groupResponse.errorBody()?.string() ?: "Errore sconosciuto"
                    Log.e("ClassroomViewModel", "Errore caricamento gruppo $groupId: ${groupResponse.code()} - $errorBody")

                    if (currentGroupId == groupId) {
                        when (groupResponse.code()) {
                            403 -> errorMessage.value = "Non hai i permessi per accedere a questo gruppo"
                            404 -> errorMessage.value = "Gruppo non trovato"
                            else -> errorMessage.value = "Errore nel caricamento del gruppo: ${groupResponse.code()}"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ClassroomViewModel", "Eccezione nel caricamento gruppo $groupId: ${e.message}", e)

                if (currentGroupId == groupId) {
                    when (e) {
                        is UnknownHostException,
                        is SocketTimeoutException ->
                            errorMessage.value = "Errore di connessione. Controlla la tua connessione internet."
                        else ->
                            errorMessage.value = "Errore: ${e.message}"
                    }
                }
            } finally {
                if (currentGroupId == groupId) {
                    isLoading.value = false
                }
            }
        }
    }

    /**
     * Pubblica un nuovo post NEL GRUPPO SPECIFICO
     */
    /**
     * Pubblica un nuovo post NEL GRUPPO SPECIFICO - VERSIONE CORRETTA
     */
    fun addPost(groupId: Int, content: String, imageUri: Uri? = null) {
        if (groupId <= 0) {
            errorMessage.value = "ID gruppo non valido: $groupId"
            return
        }

        if (groupId != currentGroupId) {
            Log.w("ClassroomViewModel", "Trying to add post to group $groupId but current group is $currentGroupId")
            errorMessage.value = "Errore: gruppo non sincronizzato"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val token = UserSession.getAuthHeader() ?: run {
                    errorMessage.value = "Non sei autenticato"
                    isLoading.value = false
                    return@launch
                }

                // FIX: Gestione corretta dell'URL dell'immagine
                val imageUrl = when {
                    imageUri != null -> {
                        // Qui dovresti implementare l'upload dell'immagine al server
                        // Per ora usiamo l'URI come stringa (NON IDEALE per produzione)
                        imageUri.toString()
                    }
                    else -> "" // URL vuoto invece del placeholder
                }

                val createPostRequest = CreatePostRequest(
                    groupId = groupId,
                    imageUrl = imageUrl,
                    caption = content
                )

                Log.d("ClassroomViewModel", "Creating post in group $groupId: $createPostRequest")

                val response = apiService.createPost(createPostRequest, token)

                if (response.isSuccessful && response.body() != null) {
                    val createdPost = response.body()!!

                    if (createdPost.group != groupId) {
                        Log.e("ClassroomViewModel", "ERROR: Created post belongs to group ${createdPost.group}, expected $groupId")
                        errorMessage.value = "Errore: post creato nel gruppo sbagliato"
                    } else {
                        Log.d("ClassroomViewModel", "Post created successfully in group $groupId with ID ${createdPost.id}")
                    }

                    // FIX: Ricarica solo se siamo ancora nello stesso gruppo
                    if (currentGroupId == groupId) {
                        loadGroupData(groupId)
                    }

                    // Reset del form
                    newPostContent.value = ""
                    selectedImageUri.value = null
                    showPostInput.value = false
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Errore sconosciuto"
                    Log.e("ClassroomViewModel", "Errore creazione post in gruppo $groupId: ${response.code()} - $errorBody")

                    when (response.code()) {
                        403 -> errorMessage.value = "Non hai i permessi per pubblicare in questo gruppo"
                        400 -> errorMessage.value = "Dati del post non validi"
                        else -> errorMessage.value = "Errore nella pubblicazione del post: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e("ClassroomViewModel", "Eccezione creazione post in gruppo $groupId: ${e.message}", e)
                errorMessage.value = "Errore di connessione: ${e.message}"
            } finally {
                if (currentGroupId == groupId) {
                    isLoading.value = false
                    // Non resettare i campi qui in caso di errore
                }
            }
        }
    }

    /**
     * Aggiunge un commento a un post nel gruppo specifico
     */
    fun addComment(groupId: Int, postId: String, content: String) {
        if (groupId != currentGroupId) {
            Log.w("ClassroomViewModel", "Trying to add comment to group $groupId but current group is $currentGroupId")
            return
        }

        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: return@launch
                val userId = UserSession.getUserId() ?: return@launch

                val postIdInt = postId.toIntOrNull() ?: run {
                    updatePostsWithLocalComment(postId, content)
                    return@launch
                }

                val comment = Comment(
                    postId = postIdInt,
                    userId = userId,
                    content = content
                )

                Log.d("ClassroomViewModel", "Creating comment for post $postIdInt in group $groupId")

                val response = apiService.createComment(comment, token)

                if (response.isSuccessful) {
                    Log.d("ClassroomViewModel", "Comment created successfully")
                    if (currentGroupId == groupId) {
                        loadGroupData(groupId)
                    }
                } else {
                    Log.e("ClassroomViewModel", "Error creating comment: ${response.code()}")
                    if (currentGroupId == groupId) {
                        updatePostsWithLocalComment(postId, content)
                    }
                }
            } catch (e: Exception) {
                Log.e("ClassroomViewModel", "Exception creating comment: ${e.message}", e)
                if (currentGroupId == groupId) {
                    updatePostsWithLocalComment(postId, content)
                }
            }
        }
    }

    private fun updatePostsWithLocalComment(postId: String, content: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newComment = PostComment(
                    authorName = UserSession.getUsername() ?: "Tu",
                    content = content,
                )
                post.copy(comments = post.comments + newComment)
            } else {
                post
            }
        }
    }

    /**
     * NUOVO: Toggle like per un post
     */
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: return@launch
                val postIdInt = postId.toIntOrNull() ?: return@launch

                // FIX: Prima aggiorna localmente per feedback immediato
                val currentPost = _posts.value.find { it.id == postId }
                if (currentPost != null) {
                    val newLiked = !currentPost.liked
                    val newCount = if (newLiked) currentPost.likeCount + 1 else (currentPost.likeCount - 1).coerceAtLeast(0)

                    val updatedPost = currentPost.copy(
                        liked = newLiked,
                        likeCount = newCount
                    )

                    _posts.value = _posts.value.map { if (it.id == postId) updatedPost else it }
                }

                // Poi invia la richiesta al server
                val response = apiService.togglePostLike(postIdInt, token)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    // Sincronizza con la risposta del server
                    _posts.value = _posts.value.map { post ->
                        if (post.id == postId) {
                            post.copy(
                                liked = result.liked,
                                likeCount = result.likeCount
                            )
                        } else {
                            post
                        }
                    }
                } else {
                    Log.e("ClassroomViewModel", "Error toggling like: ${response.code()}")
                    // In caso di errore, ricarica i dati del gruppo
                    loadGroupData(currentGroupId)
                }
            } catch (e: Exception) {
                Log.e("ClassroomViewModel", "Error toggling like: ${e.message}", e)
                // In caso di errore, ricarica i dati del gruppo
                loadGroupData(currentGroupId)
            }
        }
    }

    /**
     * NUOVO: Aggiungi o rimuovi reaction per un post
     */
    fun addReaction(postId: String, emoji: String) {
        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: return@launch
                val postIdInt = postId.toIntOrNull() ?: return@launch

                // FIX: Prima aggiorna localmente per feedback immediato
                val currentPost = _posts.value.find { it.id == postId }
                if (currentPost != null) {
                    val updatedReactions = currentPost.reactions.toMutableMap()
                    val currentUserReaction = currentPost.userReaction

                    if (currentUserReaction == emoji) {
                        // Rimuovi la reaction esistente
                        if (updatedReactions[emoji] != null) {
                            updatedReactions[emoji] = (updatedReactions[emoji]!! - 1).coerceAtLeast(0)
                            if (updatedReactions[emoji] == 0) {
                                updatedReactions.remove(emoji)
                            }
                        }
                        val updatedPost = currentPost.copy(
                            reactions = updatedReactions,
                            userReaction = null
                        )

                        _posts.value = _posts.value.map { if (it.id == postId) updatedPost else it }
                    } else {
                        // Rimuovi la vecchia reaction se esiste
                        if (currentUserReaction != null && updatedReactions[currentUserReaction] != null) {
                            updatedReactions[currentUserReaction] = (updatedReactions[currentUserReaction]!! - 1).coerceAtLeast(0)
                            if (updatedReactions[currentUserReaction] == 0) {
                                updatedReactions.remove(currentUserReaction)
                            }
                        }

                        // Aggiungi la nuova reaction
                        updatedReactions[emoji] = (updatedReactions[emoji] ?: 0) + 1

                        val updatedPost = currentPost.copy(
                            reactions = updatedReactions,
                            userReaction = emoji
                        )

                        _posts.value = _posts.value.map { if (it.id == postId) updatedPost else it }
                    }
                }

                // Poi invia la richiesta al server
                val response = apiService.addPostReaction(postIdInt, mapOf("reaction" to emoji), token)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    // Sincronizza con la risposta del server
                    _posts.value = _posts.value.map { post ->
                        if (post.id == postId) {
                            post.copy(
                                userReaction = result.userReaction,
                                reactions = result.reactionsCount
                            )
                        } else post
                    }
                } else {
                    Log.e("ClassroomViewModel", "Error adding reaction: ${response.code()}")
                    // In caso di errore, ricarica i dati del gruppo
                    loadGroupData(currentGroupId)
                }
            } catch (e: Exception) {
                Log.e("ClassroomViewModel", "Exception adding reaction: ${e.message}", e)
                // In caso di errore, ricarica i dati del gruppo
                loadGroupData(currentGroupId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomScreen(
    classRoom: ClassRoom,
    onBack: () -> Unit,
    viewModel: ClassroomViewModel = viewModel()
) {
    val posts by viewModel.posts
    val showPostInput by viewModel.showPostInput
    val newPostContent by viewModel.newPostContent
    val selectedImageUri by viewModel.selectedImageUri
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // IMPORTANTE: Reset e caricamento quando cambia il classRoom
    LaunchedEffect(classRoom.id) {
        Log.d("ClassroomScreen", "ClassRoom changed to: ${classRoom.name} (ID: ${classRoom.id})")

        if (classRoom.id > 0) {
            // Carica i dati del nuovo gruppo
            // Il ViewModel gestirà automaticamente il reset se è un gruppo diverso
            viewModel.loadGroupData(classRoom.id)
        } else {
            Log.e("ClassroomScreen", "Invalid classroom ID: ${classRoom.id}")
            viewModel.errorMessage.value = "ID gruppo non valido: ${classRoom.id}"
        }
    }

    // Reset quando si esce dalla schermata
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ClassroomScreen", "Disposing ClassroomScreen")
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.selectedImageUri.value = it.toString()
            viewModel.showImagePreview.value = true
        }
    }

    // Dialogo di errore
    if (errorMessage != null) {
        ErrorDialog(
            message = errorMessage!!,
            onDismiss = { viewModel.errorMessage.value = null }
        )
    }

    Scaffold(
        topBar = {
            EnhancedClassroomTopBar(
                classRoom = classRoom,
                onBack = onBack
            )
        },
        floatingActionButton = {
            EnhancedFloatingActionButton(
                onClick = { viewModel.showPostInput.value = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading state - mostra sempre durante il caricamento
                isLoading -> {
                    LoadingScreen()
                }
                // Empty state - nessun post
                posts.isEmpty() && !isLoading -> {
                    EmptyPostsScreen(
                        onCreatePost = { viewModel.showPostInput.value = true }
                    )
                }
                // Lista dei post
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8F9FA)),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(posts, key = { it.id }) { post ->
                            EnhancedClassroomPostCard(
                                post = post,
                                onReactionClick = { emoji ->
                                    viewModel.addReaction(post.id, emoji)
                                },
                                onLikeClick = {
                                    viewModel.toggleLike(post.id)
                                },
                                onCommentAdd = { comment ->
                                    viewModel.addComment(classRoom.id, post.id, comment)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Spazio extra in fondo per evitare sovrapposizione con FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            // UI per la creazione di un nuovo post
            if (showPostInput) {
                CreatePostDialog(
                    content = newPostContent,
                    selectedImageUri = selectedImageUri,
                    isLoading = isLoading,
                    onContentChange = { viewModel.newPostContent.value = it },
                    onImageSelect = { imagePickerLauncher.launch("image/*") },
                    onDismiss = {
                        viewModel.showPostInput.value = false
                        viewModel.newPostContent.value = ""
                        viewModel.selectedImageUri.value = null
                    },
                    onPublish = {
                        val uri = selectedImageUri?.let { Uri.parse(it) }
                        viewModel.addPost(
                            groupId = classRoom.id,
                            content = newPostContent,
                            imageUri = uri
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Green600,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Caricamento posts...",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray600
            )
        }
    }
}

@Composable
fun EmptyPostsScreen(
    onCreatePost: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji grande per rendere più amichevole
            Text(
                text = "📝",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Nessun post in questo gruppo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Green800
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sii il primo a condividere qualcosa!\nCrea il primo post e dai vita alla conversazione.",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray600,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onCreatePost,
                colors = ButtonDefaults.buttonColors(containerColor = Green600),
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Crea il primo post",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Oops! Si è verificato un errore",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Gray600
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Green600),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chiudi")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedClassroomTopBar(
    classRoom: ClassRoom,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {
        // Immagine di sfondo
        Image(
            painter = painterResource(id = classRoom.backgroundImageId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay per migliore leggibilità
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Pulsante indietro stilizzato
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
                .background(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Indietro",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Contenuto principale
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Chip per categoria (optional)
            Surface(
                color = Green600.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Gruppo Eco-friendly",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = classRoom.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = classRoom.teacherName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun EnhancedFloatingActionButton(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Green600,
        contentColor = Color.White,
        modifier = Modifier
            .size(56.dp)
            .shadow(8.dp, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Crea post",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun CreatePostDialog(
    content: String,
    selectedImageUri: String?,
    isLoading: Boolean,
    onContentChange: (String) -> Unit,
    onImageSelect: () -> Unit,
    onDismiss: () -> Unit,
    onPublish: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crea un nuovo post",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Green800
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(
                                color = Gray100,
                                shape = CircleShape
                            )
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Area testo
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    placeholder = {
                        Text(
                            "Condividi le tue idee eco-friendly...",
                            color = Gray400
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Green100.copy(alpha = 0.3f),
                        unfocusedContainerColor = Gray100.copy(alpha = 0.5f),
                        focusedIndicatorColor = Green600,
                        unfocusedIndicatorColor = Gray400
                    ),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 6
                )

                // Anteprima immagine se selezionata - SOLO SE C'È IMMAGINE
                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Immagine selezionata",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Pulsante per rimuovere immagine
                            IconButton(
                                onClick = { /* remove image */ },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Rimuovi immagine",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsanti azione
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsante immagine
                    OutlinedButton(
                        onClick = onImageSelect,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Green600
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Green600, Green600))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Aggiungi immagine",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedImageUri != null) "Cambia" else "Foto",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Pulsante pubblica
                    Button(
                        onClick = onPublish,
                        enabled = content.isNotBlank() && !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green600,
                            disabledContainerColor = Gray400
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Pubblica",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedClassroomPostCard(
    post: ClassPost,
    onReactionClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onCommentAdd: (String) -> Unit
) {
    var showCommentInput by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var showReactionSelector by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header del post migliorato
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar migliorato
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (post.isTeacher) Green600 else Green100,
                            shape = CircleShape
                        )
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = post.authorAvatarId),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        if (post.isTeacher) {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        "Insegnante",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Green600,
                                    labelColor = Color.White
                                ),
                                modifier = Modifier.height(24.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { /* show options */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opzioni",
                        tint = Gray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenuto del post
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                lineHeight = 24.sp
            )

            // Immagine se presente - SOLO SE NON È LA DEFAULT
            if (post.imageUrl != null && post.imageUrl != "https://happygreen.example.com/default-placeholder.jpg") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(post.imageUrl),
                        contentDescription = "Immagine del post",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistiche interazioni
            if (post.likeCount > 0 || post.comments.isNotEmpty() || post.reactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like e reactions count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (post.likeCount > 0) {
                            Text(
                                text = "❤️ ${post.likeCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600
                            )
                        }

                        if (post.reactions.isNotEmpty()) {
                            if (post.likeCount > 0) {
                                Text(" • ", style = MaterialTheme.typography.bodySmall, color = Gray600)
                            }
                            post.reactions.entries.take(3).forEach { (emoji, count) ->
                                Text(
                                    text = "$emoji $count",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray600
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }

                    if (post.comments.isNotEmpty()) {
                        Text(
                            text = "${post.comments.size} commenti",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Gray100, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Pulsanti azione migliorati con reactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Pulsante like con long press per reactions
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = onLikeClick,
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showReactionSelector = true
                            }
                        )
                        .background(
                            color = if (post.liked) Green100.copy(alpha = 0.3f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (post.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Mi piace",
                            tint = if (post.liked) Color.Red else Gray600,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Mi piace",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (post.liked) Color.Red else Gray600,
                            fontWeight = if (post.liked) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }

                EnhancedActionButton(
                    icon = Icons.Default.ChatBubbleOutline,
                    text = "Commenta",
                    isActive = showCommentInput,
                    onClick = { showCommentInput = !showCommentInput }
                )
            }

            // Reaction selector popup
            if (showReactionSelector) {
                Spacer(modifier = Modifier.height(8.dp))
                ReactionSelector(
                    onReactionSelected = { emoji ->
                        onReactionClick(emoji)
                        showReactionSelector = false
                    },
                    onDismiss = { showReactionSelector = false },
                    currentReaction = post.userReaction
                )
            }

            // Sezione commenti migliorata
            if (post.comments.isNotEmpty() || showCommentInput) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Gray100, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                post.comments.forEach { comment ->
                    EnhancedCommentItem(comment = comment)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Input commento migliorato
                if (showCommentInput) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Gray100.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = {
                                    Text(
                                        "Aggiungi un commento...",
                                        color = Gray400
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Green600,
                                    unfocusedIndicatorColor = Gray400
                                ),
                                shape = RoundedCornerShape(20.dp),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        onCommentAdd(commentText)
                                        commentText = ""
                                        showCommentInput = false
                                    }
                                },
                                modifier = Modifier
                                    .background(
                                        color = if (commentText.isNotBlank()) Green600 else Gray400,
                                        shape = CircleShape
                                    )
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Invia commento",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionSelector(
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    currentReaction: String?
) {
    val reactions = listOf("👍", "❤️", "😂", "😮", "😢", "😡", "🔥", "👏")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyRow(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reactions) { emoji ->
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onReactionSelected(emoji) }
                        .background(
                            color = if (emoji == currentReaction) Green100 else Color.Transparent,
                            shape = CircleShape
                        ),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedActionButton(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val buttonColor = if (isActive) Green600 else Gray600
    val backgroundColor = if (isActive) Green100.copy(alpha = 0.3f) else Color.Transparent

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = buttonColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = buttonColor,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun EnhancedCommentItem(comment: PostComment) {
    val timeFormatter = remember { SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Avatar del commento
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Green100)
                .border(1.dp, Green300, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = comment.authorAvatarId),
                contentDescription = "Avatar",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            // Contenuto commento
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 4.dp
                ),
                color = Gray100,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Green800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}