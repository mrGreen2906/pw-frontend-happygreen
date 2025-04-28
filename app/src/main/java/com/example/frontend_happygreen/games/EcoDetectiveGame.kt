// EcoDetectiveGame.kt
package com.example.frontend_happygreen.games

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.ui.theme.FrontendhappygreenTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Definizione dei tipi di rifiuti e bidoni
enum class WasteType {
    PLASTIC,
    PAPER,
    GLASS,
    ORGANIC,
    MIXED
}

// Definizione di un item di rifiuto
data class WasteItem(
    val id: Int,
    val name: String,
    val imageUrl: String, // URL for the image from API
    val type: WasteType,
    var position: MutableState<Offset> = mutableStateOf(Offset.Zero),
    var isDragging: MutableState<Boolean> = mutableStateOf(false),
    var isCollected: MutableState<Boolean> = mutableStateOf(false)
)

// Definizione di un bidone
data class TrashBin(
    val id: Int,
    val name: String,
    val iconRes: Int,
    val color: Color,
    val type: WasteType,
    val position: Offset = Offset.Zero
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoDetectiveGameScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var gameActive by remember { mutableStateOf(true) }
    var gameOver by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var currentItemIndex by remember { mutableStateOf(0) } // Track current item index
    var isLoading by remember { mutableStateOf(true) }
    val totalItems = 10 // Total number of items in the game

    // Google Custom Search API configuration
    val apiKey = "AIzaSyBgTWP_2E3NEHcGtNDAf89269Ic8AbJI-8"
    val searchEngineId = "54671b1c3db084e4a" // Replace with your actual Search Engine ID
    val numResults = 1

    // Search terms for different waste types
    val searchQueries = mapOf(
        WasteType.PLASTIC to listOf("plastic bottle waste", "plastic bag trash", "plastic container recycling"),
        WasteType.PAPER to listOf("paper waste", "cardboard recycling", "newspaper waste"),
        WasteType.GLASS to listOf("glass bottle waste", "glass jar recycling", "glass waste"),
        WasteType.ORGANIC to listOf("food waste", "organic waste", "compost material"),
        WasteType.MIXED to listOf("mixed waste", "general waste", "non-recyclable waste")
    )

    // Define trash bins
    val trashBins = remember {
        listOf(
            TrashBin(
                id = 1,
                name = "Plastica",
                iconRes = R.drawable.happy_green_logo, // Replace with proper icon
                color = Color(0xFFFFEB3B),
                type = WasteType.PLASTIC
            ),
            TrashBin(
                id = 2,
                name = "Carta",
                iconRes = R.drawable.happy_green_logo, // Replace with proper icon
                color = Color(0xFF2196F3),
                type = WasteType.PAPER
            ),
            TrashBin(
                id = 3,
                name = "Vetro",
                iconRes = R.drawable.happy_green_logo, // Replace with proper icon
                color = Color(0xFF4CAF50),
                type = WasteType.GLASS
            ),
            TrashBin(
                id = 4,
                name = "Organico",
                iconRes = R.drawable.happy_green_logo, // Replace with proper icon
                color = Color(0xFF795548),
                type = WasteType.ORGANIC
            ),
            TrashBin(
                id = 5,
                name = "Indifferenziato",
                iconRes = R.drawable.happy_green_logo, // Replace with proper icon
                color = Color(0xFF9E9E9E),
                type = WasteType.MIXED
            )
        )
    }

    // Master list of all waste items in the game
    val allWasteItems = remember { mutableStateListOf<WasteItem>() }

    // Currently active waste item (only one shown at a time)
    var currentWasteItem by remember { mutableStateOf<WasteItem?>(null) }

    // Function to search for images using Google Custom Search API
    suspend fun searchForImage(query: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://www.googleapis.com/customsearch/v1?key=$apiKey&cx=$searchEngineId&q=$query&searchType=image&num=$numResults"
                val response = URL(url).readText()
                val jsonObject = JSONObject(response)
                val items = jsonObject.getJSONArray("items")

                if (items.length() > 0) {
                    val item = items.getJSONObject(0)
                    item.getString("link")
                } else {
                    // Fallback to a placeholder if no image is found
                    "https://via.placeholder.com/150?text=Waste"
                }
            } catch (e: Exception) {
                Log.e("EcoDetectiveGame", "Error fetching image: ${e.message}")
                // Return a placeholder image URL in case of error
                "https://via.placeholder.com/150?text=Error"
            }
        }
    }

    // Load images and generate waste items
    LaunchedEffect(Unit) {
        isLoading = true
        allWasteItems.clear()

        val wasteNamesByType = mapOf(
            WasteType.PLASTIC to listOf("Bottiglia plastica", "Sacchetto plastica", "Contenitore yogurt"),
            WasteType.PAPER to listOf("Giornale", "Scatola cartone", "Foglio carta"),
            WasteType.GLASS to listOf("Bottiglia vetro", "Barattolo vetro", "Bicchiere vetro"),
            WasteType.ORGANIC to listOf("Buccia banana", "Avanzi cibo", "Foglie"),
            WasteType.MIXED to listOf("Pannolino", "Cicca sigaretta", "Oggetti misti")
        )

        // Create 2 items of each type to have 10 total
        val wasteTypes = WasteType.values()
        val itemsToCreate = mutableListOf<Pair<WasteType, Int>>()

        wasteTypes.forEach { type ->
            repeat(2) {
                itemsToCreate.add(Pair(type, it))
            }
        }

        // Shuffle the list for randomness
        itemsToCreate.shuffle()

        // Create and fetch images for each waste item
        itemsToCreate.forEachIndexed { index, (type, variationIndex) ->
            val wasteNames = wasteNamesByType[type] ?: listOf("Oggetto generico")
            val wasteName = wasteNames[variationIndex % wasteNames.size]

            // Get search query for this type
            val queries = searchQueries[type] ?: listOf("waste")
            val query = queries[variationIndex % queries.size]

            // Fetch image URL from API
            val imageUrl = searchForImage(query)

            // Generate position (centered in play area)
            val xPos = 160f
            val yPos = 300f

            allWasteItems.add(
                WasteItem(
                    id = index,
                    name = wasteName,
                    imageUrl = imageUrl,
                    type = type,
                    position = mutableStateOf(Offset(xPos, yPos))
                )
            )
        }

        // Set the first waste item as active
        if (allWasteItems.isNotEmpty()) {
            currentWasteItem = allWasteItems[0]
        }

        isLoading = false
    }

    // Check collision between waste item and trash bin
    fun checkCollision(wasteItem: WasteItem, trashBin: TrashBin, binPosition: Offset, binSize: Float): Boolean {
        val wasteItemPosition = wasteItem.position.value
        val distance = kotlin.math.sqrt(
            (wasteItemPosition.x - binPosition.x).pow(2) +
                    (wasteItemPosition.y - binPosition.y).pow(2)
        )
        return distance < binSize / 2
    }

    // Check if waste was disposed in the correct bin
    fun checkWasteDisposal(wasteItem: WasteItem, trashBin: TrashBin) {
        if (wasteItem.type == trashBin.type) {
            // Correct!
            score += 10
            wasteItem.isCollected.value = true
            showSuccessDialog = true
            coroutineScope.launch {
                delay(800)
                showSuccessDialog = false

                // Move to the next item
                if (currentItemIndex < totalItems - 1) {
                    currentItemIndex++
                    currentWasteItem = allWasteItems[currentItemIndex]
                } else {
                    // Game complete
                    gameOver = true
                }
            }
        } else {
            // Wrong!
            lives--
            showErrorDialog = true
            coroutineScope.launch {
                delay(800)
                showErrorDialog = false

                // Game over if no lives left
                if (lives <= 0) {
                    gameOver = true
                    gameActive = false
                } else {
                    // Reset position of current item for retry
                    wasteItem.position.value = Offset(160f, 300f)
                }
            }
        }
    }

    // Pre-calculate bin positions
    val binPositions = remember {
        listOf(
            Offset(80f, 700f),
            Offset(240f, 700f),
            Offset(400f, 700f),
            Offset(560f, 700f),
            Offset(720f, 700f)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "EcoDetective Game",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Show score
                        Text(
                            text = "Score: $score",
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Show lives
                        Row {
                            for (i in 1..3) {
                                Icon(
                                    painter = painterResource(id = R.drawable.happy_green_logo), // Replace with heart icon
                                    contentDescription = "Vita",
                                    tint = if (i <= lives) Color.Red else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE8F5E9))
        ) {
            if (isLoading) {
                // Show loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4CAF50)
                    )

                    Text(
                        text = "Caricamento immagini...",
                        modifier = Modifier.padding(top = 60.dp),
                        color = Color(0xFF4CAF50)
                    )
                }
            } else if (gameActive && !gameOver) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Current progress
                    Text(
                        text = "Oggetto ${currentItemIndex + 1} di $totalItems",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Game area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Only draw the current waste item
                        currentWasteItem?.let { wasteItem ->
                            if (!wasteItem.isCollected.value) {
                                val scale by animateFloatAsState(targetValue = if (wasteItem.isDragging.value) 1.2f else 1f)

                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = wasteItem.position.value.x.dp,
                                            y = wasteItem.position.value.y.dp
                                        )
                                        .size(80.dp)
                                        .scale(scale)
                                        .zIndex(10f) // Make sure waste items are above bins
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(
                                            width = if (wasteItem.isDragging.value) 2.dp else 1.dp,
                                            color = if (wasteItem.isDragging.value) Color(0xFF4CAF50) else Color.LightGray,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .pointerInput(wasteItem.id) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    wasteItem.isDragging.value = true
                                                },
                                                onDragEnd = {
                                                    wasteItem.isDragging.value = false

                                                    // Check if released on a bin
                                                    trashBins.forEachIndexed { index, bin ->
                                                        if (checkCollision(wasteItem, bin, binPositions[index], 80f)) {
                                                            checkWasteDisposal(wasteItem, bin)
                                                        }
                                                    }
                                                },
                                                onDragCancel = {
                                                    wasteItem.isDragging.value = false
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    wasteItem.position.value = Offset(
                                                        x = wasteItem.position.value.x + dragAmount.x,
                                                        y = wasteItem.position.value.y + dragAmount.y
                                                    )
                                                }
                                            )
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Load image from URL using Coil
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(wasteItem.imageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = wasteItem.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )

                                        Text(
                                            text = wasteItem.name,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bins area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color(0xFFDCEDC8))
                            .padding(horizontal = 8.dp)
                            .zIndex(1f), // Make sure bins are below waste items
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        trashBins.forEach { bin ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bin.color)
                                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = bin.iconRes),
                                        contentDescription = bin.name,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }

                                Text(
                                    text = bin.name,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Success dialog
                if (showSuccessDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x88000000))
                            .zIndex(100f),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Corretto!",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Error dialog
                if (showErrorDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x88000000))
                            .zIndex(100f),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF44336)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sbagliato!",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else if (gameOver) {
                // Game Over screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (lives > 0) "Hai completato il gioco!" else "Game Over",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (lives > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Punteggio finale: $score",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Oggetti raccolti correttamente: ${score / 10}/$totalItems",
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            // Restart the game
                            score = 0
                            lives = 3
                            currentItemIndex = 0
                            gameOver = false
                            gameActive = true

                            // Reset all waste items
                            allWasteItems.forEach {
                                it.isCollected.value = false
                                it.position.value = Offset(160f, 300f)
                            }

                            // Set first item as active
                            if (allWasteItems.isNotEmpty()) {
                                currentWasteItem = allWasteItems[0]
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Gioca di nuovo")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9E9E9E)
                        )
                    ) {
                        Text("Torna indietro")
                    }
                }
            }
        }
    }
}

// Extension to calculate the power of a number
fun Float.pow(exponent: Int): Float {
    var result = 1f
    repeat(exponent) {
        result *= this
    }
    return result
}

@Preview(showBackground = true)
@Composable
fun EcoDetectiveGameScreenPreview() {
    FrontendhappygreenTheme {
        EcoDetectiveGameScreen()
    }
}