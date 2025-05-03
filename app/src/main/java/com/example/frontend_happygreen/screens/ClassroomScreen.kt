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

// Data Models
data class ClassPost(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatarId: Int = R.drawable.happy_green_logo,
    val isTeacher: Boolean = false,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Date = Date(),
    val reactions: Map<String, List<String>> = emptyMap(), // emoji -> list of user names
    val comments: List<Comment> = emptyList()
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatarId: Int = R.drawable.happy_green_logo,
    val content: String,
    val timestamp: Date = Date()
)

// ViewModel
class ClassroomViewModel : ViewModel() {
    // Sample data
    private val _posts = mutableStateOf<List<ClassPost>>(getSamplePosts())
    val posts: State<List<ClassPost>> = _posts

    var newPostContent = mutableStateOf("")
    var selectedImageUri = mutableStateOf<String?>(null)
    var showPostInput = mutableStateOf(false)
    var showImagePreview = mutableStateOf(false)

    fun addPost(content: String, imageUri: String? = null) {
        val newPost = ClassPost(
            authorName = "John Doe", // Current user
            content = content,
            imageUrl = imageUri
        )
        _posts.value = listOf(newPost) + _posts.value
        newPostContent.value = ""
        selectedImageUri.value = null
        showPostInput.value = false
    }

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

    fun addComment(postId: String, content: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newComment = Comment(
                    authorName = "John Doe", // Current user
                    content = content
                )
                post.copy(comments = post.comments + newComment)
            } else {
                post
            }
        }
    }

    private fun getSamplePosts(): List<ClassPost> {
        return listOf(
            ClassPost(
                authorName = "Prof. Smith",
                isTeacher = true,
                content = "Benvenuti alla classe di Environmental Science! Questa settimana parleremo di economia circolare e riciclaggio.",
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
                    Comment(
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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.selectedImageUri.value = it.toString()
            viewModel.showImagePreview.value = true
        }
    }

    Scaffold(
        topBar = {
            ClassroomTopBar(
                classRoom = classRoom,
                onBack = onBack
            )
        },
        // resto del codice rimane uguale...
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            state = listState,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                ClassroomPostCard(
                    post = post,
                    onReactionClick = { emoji ->
                        viewModel.toggleReaction(post.id, emoji, "John Doe")
                    },
                    onCommentAdd = { comment ->
                        viewModel.addComment(post.id, comment)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                ActionButton(
                    icon = Icons.Default.ThumbUp,
                    text = "Like",
                    onClick = { onReactionClick("👍") }
                )

                ActionButton(
                    icon = Icons.Default.ChatBubbleOutline,
                    text = "Commenta",
                    onClick = { showCommentInput = !showCommentInput }
                )

                ActionButton(
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
fun ActionButton(
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
fun CommentItem(comment: Comment) {
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