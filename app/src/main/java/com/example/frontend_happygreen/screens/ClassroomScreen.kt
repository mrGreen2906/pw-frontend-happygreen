package com.example.frontend_happygreen.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewModelScope
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.data.Comment
import com.example.frontend_happygreen.data.MemberData
import com.example.frontend_happygreen.data.Post
import com.example.frontend_happygreen.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


/**
 * Data Models per la UI dei post
 */
data class ClassPost(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatarId: Int = R.drawable.happy_green_logo,
    val isTeacher: Boolean = false,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Date = Date(),
    val reactions: Map<String, List<String>> = emptyMap(), // emoji -> list of user names
    val comments: List<PostComment> = emptyList()
)

data class PostComment(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatarId: Int = R.drawable.happy_green_logo,
    val content: String,
    val timestamp: Date = Date()
)

/**
 * ViewModel aggiornato per la schermata di un gruppo/classe
 */
class ClassroomViewModel : ViewModel() {
    // API service
    private val apiService = RetrofitClient.create(ApiService::class.java)

    // Post del gruppo
    private val _posts = mutableStateOf<List<ClassPost>>(emptyList())
    val posts: State<List<ClassPost>> = _posts

    // Membri del gruppo
    private val _members = MutableStateFlow<List<MemberData>>(emptyList())
    val members: StateFlow<List<MemberData>> = _members

    // Stati UI
    var newPostContent = mutableStateOf("")
    var selectedImageUri = mutableStateOf<String?>(null)
    var showPostInput = mutableStateOf(false)
    var showImagePreview = mutableStateOf(false)
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    /**
     * Carica i dati di un gruppo e i suoi post
     */
    fun loadGroupData(groupId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val token = UserSession.getAuthHeader() ?: run {
                    errorMessage.value = "Token di autenticazione non disponibile"
                    isLoading.value = false
                    return@launch
                }

                // Carica i dettagli del gruppo
                val groupResponse = apiService.getGroupById(groupId, token)

                if (groupResponse.isSuccessful && groupResponse.body() != null) {
                    val groupData = groupResponse.body()!!

                    // Aggiorna la lista dei membri
                    _members.value = groupData.members

                    // Carica i post del gruppo
                    val postsResponse = apiService.getGroupPosts(groupId, token)

                    if (postsResponse.isSuccessful && postsResponse.body() != null) {
                        val posts = postsResponse.body()!!

                        // Converti in ClassPost
                        _posts.value = posts.map { post ->
                            val author = groupData.members.find { it.user.id == post.userId }?.user
                            val userRole = groupData.members.find { it.user.id == post.userId }?.role

                            ClassPost(
                                id = post.id.toString(),
                                authorName = author?.username ?: "Utente",
                                isTeacher = userRole == "teacher" || userRole == "admin",
                                content = post.caption ?: "",
                                imageUrl = post.imageUrl,
                                // Converti la data da stringa a Date
                                timestamp = try {
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                        .parse(post.createdAt ?: "") ?: Date()
                                } catch (e: Exception) {
                                    Date()
                                },
                                // Per ora le reazioni non sono implementate nel backend
                                reactions = emptyMap(),
                                // TODO: Caricare i commenti associati a questo post
                                comments = emptyList()
                            )
                        }
                    } else {
                        errorMessage.value = "Errore nel caricamento dei post: ${postsResponse.code()}"
                        // Mostra dati di esempio in caso di errore
                        _posts.value = getSamplePosts()
                    }
                } else {
                    errorMessage.value = "Errore nel caricamento del gruppo: ${groupResponse.code()}"
                    // Mostra dati di esempio in caso di errore
                    _posts.value = getSamplePosts()
                }
            } catch (e: Exception) {
                errorMessage.value = "Errore di connessione: ${e.message}"
                // Mostra dati di esempio in caso di errore
                _posts.value = getSamplePosts()
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Pubblica un nuovo post nel gruppo
     */
    fun addPost(groupId: Int, content: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val token = UserSession.getAuthHeader() ?: run {
                    errorMessage.value = "Non sei autenticato"
                    isLoading.value = false
                    return@launch
                }

                val userId = UserSession.getUserId() ?: run {
                    errorMessage.value = "ID utente non disponibile"
                    isLoading.value = false
                    return@launch
                }

                // Crea oggetto post
                val post = Post(
                    userId = userId,
                    groupId = groupId,
                    imageUrl = imageUri?.toString() ?: "",
                    caption = content
                )

                // Invia il post al server
                val response = apiService.createPost(post, token)

                if (response.isSuccessful && response.body() != null) {
                    // Aggiorna i post
                    loadGroupData(groupId)
                    // Reset del form
                    newPostContent.value = ""
                    selectedImageUri.value = null
                    showPostInput.value = false
                } else {
                    errorMessage.value = "Errore nella pubblicazione del post: ${response.code()}"

                    // Fallback: aggiungi post localmente
                    val newPost = ClassPost(
                        authorName = UserSession.getUsername() ?: "Tu",
                        content = content,
                        imageUrl = imageUri?.toString()
                    )
                    _posts.value = listOf(newPost) + _posts.value
                }
            } catch (e: Exception) {
                errorMessage.value = "Errore di connessione: ${e.message}"

                // Fallback: aggiungi post localmente
                val newPost = ClassPost(
                    authorName = UserSession.getUsername() ?: "Tu",
                    content = content,
                    imageUrl = imageUri?.toString()
                )
                _posts.value = listOf(newPost) + _posts.value
            } finally {
                isLoading.value = false
                newPostContent.value = ""
                selectedImageUri.value = null
                showPostInput.value = false
            }
        }
    }

    /**
     * Aggiunge un commento a un post
     */
    fun addComment(groupId: Int, postId: String, content: String) {
        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: return@launch
                val userId = UserSession.getUserId() ?: return@launch

                // Converti postId da String a Int
                val postIdInt = postId.toIntOrNull() ?: run {
                    // Se postId non è un numero valido, aggiorna localmente
                    updatePostsWithLocalComment(postId, content)
                    return@launch
                }

                // Crea oggetto commento
                val comment = Comment(
                    postId = postIdInt,
                    userId = userId,
                    content = content
                )

                // Invia il commento al server
                val response = apiService.createComment(comment, token)

                if (response.isSuccessful) {
                    // Ricarica i post aggiornati
                    loadGroupData(groupId)
                } else {
                    // Fallback: aggiorna localmente
                    updatePostsWithLocalComment(postId, content)
                }
            } catch (e: Exception) {
                // Fallback: aggiorna localmente
                updatePostsWithLocalComment(postId, content)
            }
        }
    }

    /**
     * Aggiunge un commento localmente (fallback)
     */
    private fun updatePostsWithLocalComment(postId: String, content: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newComment = PostComment(
                    authorName = UserSession.getUsername() ?: "Tu",
                    content = content
                )
                post.copy(comments = post.comments + newComment)
            } else {
                post
            }
        }
    }

    /**
     * Simula reazioni ai post
     */
    fun toggleReaction(postId: String, emoji: String, userName: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val reactions = post.reactions.toMutableMap()
                val usersWithReaction = reactions[emoji]?.toMutableList() ?: mutableListOf()

                if (userName in usersWithReaction) {
                    usersWithReaction.remove(userName)
                } else {
                    usersWithReaction.add(userName)
                }

                reactions[emoji] = usersWithReaction
                post.copy(reactions = reactions)
            } else {
                post
            }
        }
    }

    /**
     * Dati di esempio per fallback
     */
    private fun getSamplePosts(): List<ClassPost> {
        return listOf(
            ClassPost(
                authorName = "Prof. Smith",
                isTeacher = true,
                content = "Benvenuti alla classe! Questa settimana parleremo di economia circolare e riciclaggio.",
                reactions = mapOf(
                    "👍" to listOf("Maria", "Giovanni"),
                    "❤️" to listOf("Alice")
                )
            ),
            ClassPost(
                authorName = "Maria",
                content = "Ho trovato questo interessante articolo sulla riduzione dei rifiuti di plastica. Cosa ne pensate?",
                imageUrl = "sample_article_image",
                comments = listOf(
                    PostComment(
                        authorName = "Giovanni",
                        content = "Molto interessante! Grazie per la condivisione."
                    )
                )
            ),
            ClassPost(
                authorName = "Alice",
                content = "Ricordate che domani avremo il quiz sul riciclaggio della carta!",
                reactions = mapOf(
                    "📚" to listOf("Giovanni", "Maria", "Marco")
                )
            )
        )
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

    // Carica i dati del gruppo quando la schermata viene visualizzata
    LaunchedEffect(classRoom.id) {
        viewModel.loadGroupData(classRoom.id)
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
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage.value = null },
            title = { Text("Errore") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(
                    onClick = { viewModel.errorMessage.value = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ClassroomTopBar(
                classRoom = classRoom,
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showPostInput.value = true },
                containerColor = Green600
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crea post",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && posts.isEmpty()) {
                // Mostra loader solo se è la prima caricamento
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Green600)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Caricamento posts...")
                    }
                }
            } else if (posts.isEmpty() && !isLoading) {
                // Nessun post
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nessun post in questo gruppo",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sii il primo a condividere qualcosa!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.showPostInput.value = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Green600)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crea Post")
                        }
                    }
                }
            } else {
                // Lista dei post
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        ClassroomPostCard(
                            post = post,
                            onReactionClick = { emoji ->
                                viewModel.toggleReaction(post.id, emoji,
                                    UserSession.getUsername() ?: "Tu")
                            },
                            onCommentAdd = { comment ->
                                viewModel.addComment(classRoom.id, post.id, comment)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // UI per la creazione di un nuovo post
            if (showPostInput) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Crea Nuovo Post",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = newPostContent,
                                    onValueChange = { viewModel.newPostContent.value = it },
                                    label = { Text("Cosa vuoi condividere?") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Pulsante per selezionare immagine
                                TextButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Aggiungi immagine"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedImageUri != null)
                                            "Immagine selezionata"
                                        else
                                            "Aggiungi immagine"
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.showPostInput.value = false
                                            viewModel.newPostContent.value = ""
                                            viewModel.selectedImageUri.value = null
                                        }
                                    ) {
                                        Text("Annulla")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            // Converti selectedImageUri a Uri se presente
                                            val uri = selectedImageUri?.let { Uri.parse(it) }
                                            viewModel.addPost(
                                                groupId = classRoom.id,
                                                content = newPostContent,
                                                imageUri = uri
                                            )
                                        },
                                        enabled = newPostContent.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Green600
                                        )
                                    ) {
                                        Text("Pubblica")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomTopBar(
    classRoom: ClassRoom,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Image(
            painter = painterResource(id = classRoom.backgroundImageId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.4f)
                )
        )

        // Pulsante indietro in alto a sinistra
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 42.dp, start = 16.dp)  // Padding per posizionarlo sotto la status bar
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Indietro",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = classRoom.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Insegnante: ${classRoom.teacherName}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
@Composable
fun ClassroomPostCard(
    post: ClassPost,
    onReactionClick: (String) -> Unit,
    onCommentAdd: (String) -> Unit
) {
    var showCommentInput by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    val timeFormatter = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = post.authorAvatarId),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (post.isTeacher) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Docente", fontSize = 12.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Green100,
                                    labelColor = Green800
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    Text(
                        text = timeFormatter.format(post.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )

            // Image if present
            post.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = if (imageUrl == "sample_article_image") {
                        painterResource(id = R.drawable.happy_green_logo)
                    } else {
                        rememberAsyncImagePainter(imageUrl)
                    },
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reactions
            ReactionRow(
                reactions = post.reactions,
                onReactionClick = onReactionClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ClassActionButton(
                    icon = Icons.Default.ThumbUp,
                    text = "Like",
                    onClick = { onReactionClick("👍") }
                )

                ClassActionButton(
                    icon = Icons.Default.ChatBubbleOutline,
                    text = "Commenta",
                    onClick = { showCommentInput = !showCommentInput }
                )

                ClassActionButton(
                    icon = Icons.Default.Share,
                    text = "Condividi",
                    onClick = { /* Implement sharing */ }
                )
            }

            // Comments section
            if (post.comments.isNotEmpty() || showCommentInput) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                post.comments.forEach { comment ->
                    CommentItem(comment = comment)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Comment input
                if (showCommentInput) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Scrivi un commento...") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Green100,
                                unfocusedContainerColor = Green100
                            )
                        )

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onCommentAdd(commentText)
                                    commentText = ""
                                    showCommentInput = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Invia commento",
                                tint = Green600
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionRow(
    reactions: Map<String, List<String>>,
    onReactionClick: (String) -> Unit
) {
    val availableEmojis = listOf("👍", "❤️", "😊", "😮", "😢", "😡")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableEmojis.forEach { emoji ->
            val count = reactions[emoji]?.size ?: 0
            ReactionChip(
                emoji = emoji,
                count = count,
                onClick = { onReactionClick(emoji) }
            )
        }
    }
}

@Composable
fun ReactionChip(
    emoji: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (count > 0) Green600 else Gray400,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (count > 0) Green100 else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 14.sp
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    color = Green600,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ClassActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Gray600,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Gray600
        )
    }
}

@Composable
fun CommentItem(comment: PostComment) {
    val timeFormatter = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Green100),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = comment.authorAvatarId),
                contentDescription = "Avatar",
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Gray100,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }

            Text(
                text = timeFormatter.format(comment.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = Gray600
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomPostInput(
    content: String,
    onContentChange: (String) -> Unit,
    onImageSelect: () -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    hasImage: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                placeholder = { Text("Condividi qualcosa con la classe...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Green600,
                    unfocusedBorderColor = Gray400
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = onImageSelect) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Aggiungi immagine",
                            tint = if (hasImage) Green600 else Gray600
                        )
                    }

                    IconButton(onClick = { /* Implement file attachment */ }) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Allega file",
                            tint = Gray600
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onCancel) {
                        Text("Annulla")
                    }

                    Button(
                        onClick = onSend,
                        enabled = content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green600
                        )
                    ) {
                        Text("Pubblica")
                    }
                }
            }
        }
    }
}