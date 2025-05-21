package com.example.frontend_happygreen.screens

import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.frontend_happygreen.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data models for chat functionality
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date = Date(),
    val imageUri: Uri? = null
)

/**
 * ViewModel for EcoAI Chat screen
 */
class EcoAIChatViewModel : ViewModel() {
    // Chat messages
    private val _messages = mutableStateOf<List<ChatMessage>>(emptyList())
    val messages: State<List<ChatMessage>> = _messages

    // User input
    var userInput = mutableStateOf("")

    // Loading state
    var isLoading = mutableStateOf(false)

    init {
        // Add initial welcome message
        addBotMessage("Ciao! Sono EcoAI, la tua assistente per la gestione dei rifiuti. Inviami una foto di un oggetto e ti darò informazioni su come smaltirlo correttamente o riciclarlo!")
    }

    /**
     * Add a message from the user
     */
    fun addUserMessage(content: String, imageUri: Uri? = null) {
        val message = ChatMessage(
            content = content,
            isFromUser = true,
            imageUri = imageUri
        )
        _messages.value = _messages.value + message
    }

    /**
     * Add a message from the bot
     */
    fun addBotMessage(content: String) {
        val message = ChatMessage(
            content = content,
            isFromUser = false
        )
        _messages.value = _messages.value + message
    }

    /**
     * Process a message from the user
     */
    suspend fun processUserMessage(message: String, imageUri: Uri? = null) {
        isLoading.value = true

        addUserMessage(message, imageUri)

        delay(1500)

        val response = if (imageUri != null) {
            generateImageResponse(imageUri)
        } else {
            generateTextResponse(message)
        }
        addBotMessage(response)

        isLoading.value = false
    }

    /**
     * Generate a text response based on user input
     */
    private fun generateTextResponse(message: String): String {

        return when {
            message.contains("plastica", ignoreCase = true) ->
                "La plastica deve essere separata in base al tipo. Le bottiglie in PET vanno nella raccolta plastica, mentre alcuni imballaggi compositi potrebbero richiedere uno smaltimento speciale."

            message.contains("carta", ignoreCase = true) ->
                "La carta pulita può essere riciclata. Ricorda di rimuovere eventuali parti in plastica o metallo prima di gettarla nel contenitore della carta."

            message.contains("vetro", ignoreCase = true) ->
                "Il vetro è riciclabile al 100% e può essere rifuso infinite volte senza perdere qualità. Assicurati di rimuovere i tappi e di sciacquare i contenitori."

            message.contains("batterie", ignoreCase = true) || message.contains("pile", ignoreCase = true) ->
                "Le batterie e le pile contengono sostanze tossiche e devono essere portate ai punti di raccolta speciali o alle isole ecologiche. Non gettarle mai nei rifiuti indifferenziati!"

            message.contains("ciao", ignoreCase = true) || message.contains("salve", ignoreCase = true) ->
                "Ciao! Come posso aiutarti oggi con la gestione dei rifiuti?"

            message.contains("grazie", ignoreCase = true) ->
                "Prego! Sono qui per aiutarti a gestire i rifiuti in modo sostenibile."

            else ->
                "Per darti informazioni precise, inviami una foto dell'oggetto che vuoi smaltire o riciclare, oppure descrivi il tipo di materiale in modo più specifico."
        }
    }

    /**
     * Generate a response based on an image
     */
    private fun generateImageResponse(imageUri: Uri): String {
        // In a real app, this would use image recognition and AI
        // For now, return a generic response
        return "Ho analizzato l'immagine. Sembra essere un oggetto composto principalmente da plastica. " +
                "Questo tipo di plastica va conferito nel contenitore della raccolta differenziata della plastica. " +
                "Ricorda di pulirlo prima di gettarlo. Se contiene parti in metallo, dovresti separarle e depositarle " +
                "nel contenitore dei metalli."
    }

    /**
     * Clear the chat history
     */
    fun clearChat() {
        _messages.value = emptyList()
        addBotMessage("Ciao! Sono EcoAI, la tua assistente per la gestione dei rifiuti. Inviami una foto di un oggetto e ti darò informazioni su come smaltirlo correttamente o riciclarlo!")
    }
}

/**
 * Main EcoAI Chat Screen
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoAIChatScreen(
    onBack: () -> Unit,
    viewModel: EcoAIChatViewModel = viewModel()
) {
    val messages by viewModel.messages
    val userInput by viewModel.userInput
    val isLoading by viewModel.isLoading
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // State for camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera permission request launcher


    // Create a temporary file for camera photo with error handling
    val tempImageFile = remember {
        try {
            File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
                deleteOnExit()
            }
        } catch (e: Exception) {
            Log.e("EcoAIChatScreen", "Error creating temp file: ${e.message}")
            // Fallback if createTempFile fails
            File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg").apply {
                deleteOnExit()
            }
        }
    }

    // Create URI with error handling
    val tempImageUri = remember {
        try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempImageFile
            )
        } catch (e: Exception) {
            Log.e("EcoAIChatScreen", "Error creating file URI: ${e.message}")
            Uri.EMPTY
        }
    }

    // Image picker launcher with error handling
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                coroutineScope.launch {
                    viewModel.processUserMessage("Ho inviato un'immagine", it)
                    // Scroll to bottom
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size)
                    }
                }
            } catch (e: Exception) {
                Log.e("EcoAIChatScreen", "Error processing picked image: ${e.message}")
                coroutineScope.launch {
                    viewModel.addBotMessage("Si è verificato un problema con l'elaborazione dell'immagine.")
                }
            }
        }
    }

    // Camera launcher with error handling
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            try {
                coroutineScope.launch {
                    viewModel.processUserMessage("Ho scattato una foto", tempImageUri)
                    // Scroll to bottom
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size)
                    }
                }
            } catch (e: Exception) {
                Log.e("EcoAIChatScreen", "Error processing camera image: ${e.message}")
                coroutineScope.launch {
                    viewModel.addBotMessage("Si è verificato un problema con l'elaborazione della foto.")
                }
            }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            try {
                // Permission granted, now we can launch camera
                if (tempImageUri != Uri.EMPTY) {
                    cameraLauncher.launch(tempImageUri)
                } else {
                    coroutineScope.launch {
                        viewModel.addBotMessage("Si è verificato un problema con la preparazione della fotocamera.")
                    }
                }
            } catch (e: Exception) {
                Log.e("EcoAIChatScreen", "Error launching camera after permission: ${e.message}")
                coroutineScope.launch {
                    viewModel.addBotMessage("Si è verificato un errore nell'avvio della fotocamera. Riprova più tardi.")
                }
            }
        } else {
            // Permission denied, show a message
            coroutineScope.launch {
                viewModel.addBotMessage("Per utilizzare la fotocamera, devi concedere l'autorizzazione nelle impostazioni dell'app.")
            }
        }
    }
    val handleCameraClick: () -> Unit = {
        try {
            if (hasCameraPermission) {
                if (tempImageUri != Uri.EMPTY) {
                    cameraLauncher.launch(tempImageUri)
                } else {
                    coroutineScope.launch {
                        viewModel.addBotMessage("Si è verificato un problema con la preparazione della fotocamera.")
                    }
                }
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        } catch (e: Exception) {
            Log.e("EcoAIChatScreen", "Error in camera click handler: ${e.message}")
            coroutineScope.launch {
                viewModel.addBotMessage("Si è verificato un errore nell'accesso alla fotocamera. Assicurati che l'app abbia i permessi necessari.")
            }
        }
    }


    // Function to safely handle gallery click
    val handleGalleryClick: () -> Unit = {
        try {
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e("EcoAIChatScreen", "Error launching image picker: ${e.message}")
            coroutineScope.launch {
                viewModel.addBotMessage("Si è verificato un problema con l'accesso alla galleria.")
            }
        }
    }

    val sendClickHandler: () -> Unit = {
        if (userInput.isNotBlank()) {
            coroutineScope.launch {
                viewModel.processUserMessage(userInput)
                viewModel.userInput.value = ""
            }
        }
    }
    // Effect to scroll to bottom when a new message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = "EcoAI",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EcoAI - Esperta in Rifiuti",
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Pulisci chat",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                value = userInput,
                onValueChange = { viewModel.userInput.value = it },
                onSendClick = {
                    if (userInput.isNotBlank()) {
                        coroutineScope.launch {
                            viewModel.processUserMessage(userInput)
                            viewModel.userInput.value = ""
                        }
                    }
                },
                onCameraClick = handleCameraClick,
                onGalleryClick = handleGalleryClick,
                isLoading = isLoading
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    ChatMessageItem(message = message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Green600
                )
            }
        }
    }
}

/**
 * Chat input bar with text field, send button and attachment options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    isLoading: Boolean
) {
    var showAttachmentOptions by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Column {
            // Attachment options
            AnimatedVisibility(visible = showAttachmentOptions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentOption(
                        icon = Icons.Default.Camera,
                        label = "Camera",
                        onClick = {
                            onCameraClick()
                            showAttachmentOptions = false
                        }
                    )

                    AttachmentOption(
                        icon = Icons.Default.Photo,
                        label = "Galleria",
                        onClick = {
                            onGalleryClick()
                            showAttachmentOptions = false
                        }
                    )
                }
            }

            // Input field and buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment button
                IconButton(
                    onClick = { showAttachmentOptions = !showAttachmentOptions }
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Allega",
                        tint = Green600
                    )
                }

                // Input field
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("Scrivi un messaggio...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Green300,
                        unfocusedBorderColor = Green100
                    ),
                    maxLines = 4
                )

                // Send button
                IconButton(
                    onClick = onSendClick,
                    enabled = value.isNotBlank() && !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Invia",
                        tint = if (value.isNotBlank() && !isLoading) Green600 else Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Attachment option button
 */
@Composable
fun AttachmentOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Green100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Green600,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

/**
 * Individual chat message item
 */
@Composable
fun ChatMessageItem(message: ChatMessage) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isFromUser) Green200 else Color.White
    val textColor = if (message.isFromUser) Color.Black else Color.Black
    val borderColor = if (message.isFromUser) Green300 else Color.LightGray
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Message content
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (message.isFromUser) 8.dp else 0.dp,
                        topEnd = if (message.isFromUser) 0.dp else 8.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    )
                )
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(
                        topStart = if (message.isFromUser) 8.dp else 0.dp,
                        topEnd = if (message.isFromUser) 0.dp else 8.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                // Avatar for bot messages
                if (!message.isFromUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Green600),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assistant,
                                contentDescription = "EcoAI",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "EcoAI",
                            fontWeight = FontWeight.Bold,
                            color = Green800
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = Color.LightGray
                    )
                }

                // Image if present
                message.imageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Immagine allegata",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Message text
                Text(
                    text = message.content,
                    color = textColor
                )

                // Timestamp
                Text(
                    text = timeFormatter.format(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}