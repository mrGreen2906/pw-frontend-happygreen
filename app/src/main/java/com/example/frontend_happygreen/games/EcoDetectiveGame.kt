package com.example.frontend_happygreen.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.frontend_happygreen.R
import androidx.compose.ui.unit.IntOffset
enum class WasteType { PLASTIC, PAPER, ORGANIC, GLASS }

data class WasteItem(val name: String, val imageRes: Int, val type: WasteType)

data class BinBounds(val type: WasteType, val bounds: Rect)

@Composable
fun EcoDetectiveGameScreen(onBack: () -> Unit) {
    val allItems = listOf(
        WasteItem("Bottiglia di vetro", R.drawable.placeholder, WasteType.GLASS),
        WasteItem("Bottiglia di plastica", R.drawable.placeholder, WasteType.PLASTIC),
        WasteItem("Giornale", R.drawable.placeholder, WasteType.PAPER),
        WasteItem("Mela", R.drawable.placeholder, WasteType.ORGANIC)
    )

    var currentItem by remember { mutableStateOf(allItems.random()) }
    var itemOffset by remember { mutableStateOf(Offset.Zero) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var timeLeft by remember { mutableStateOf(60) }
    var isDragging by remember { mutableStateOf(false) }

    // Store the bounds of each bin for collision detection
    var binsBounds by remember { mutableStateOf<List<BinBounds>>(emptyList()) }
    // Store the current item bounds
    var itemBounds by remember { mutableStateOf<Rect?>(null) }
    var itemInitialCenter by remember { mutableStateOf(Offset.Zero) }

    // For visual debugging of drop zones (can be removed in production)
    var lastDropResult by remember { mutableStateOf<Pair<Boolean, WasteType?>>(Pair(false, null)) }

    val scope = rememberCoroutineScope()

    // Timer countdown
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0 && lives > 0) {
            delay(1000)
            timeLeft--
        }
    }

    // Reset game on fail
    fun resetPosition() {
        scope.launch {
            animatedOffset.animateTo(Offset.Zero, tween(300))
            itemOffset = Offset.Zero
        }
    }

    fun checkDrop(draggedItemBounds: Rect?): Pair<Boolean, WasteType?> {
        if (draggedItemBounds == null) return Pair(false, null)

        // Calculate the center of the dragged item
        val itemCenter = Offset(
            draggedItemBounds.left + draggedItemBounds.width / 2 + itemOffset.x,
            draggedItemBounds.top + draggedItemBounds.height / 2 + itemOffset.y
        )

        // Check if the center of the item is inside any bin
        for (binBounds in binsBounds) {
            if (itemCenter.x >= binBounds.bounds.left &&
                itemCenter.x <= binBounds.bounds.right &&
                itemCenter.y >= binBounds.bounds.top &&
                itemCenter.y <= binBounds.bounds.bottom) {
                // Item center is inside this bin
                return Pair(true, binBounds.type)
            }
        }

        return Pair(false, null)
    }

    // Ho rimosso la dichiarazione duplicata di nextItem qui

    fun nextItem() {
        currentItem = allItems.random()
        resetPosition()
    }
    fun handleDrop() {
        val (isInBin, binType) = checkDrop(itemBounds)

        if (isInBin && binType != null) {
            if (binType == currentItem.type) {
                // Correct bin
                score += 10
                // Visual feedback for correct drop
                scope.launch {
                    // Briefly scale the animation for feedback
                    delay(300)
                    nextItem()
                }
            } else {
                // Wrong bin
                lives--
                resetPosition()
            }
        } else {
            // Not dropped in any bin
            resetPosition()
        }
    }


    if (lives <= 0 || timeLeft <= 0) {
        GameOverScreen(score = score, onRestart = {
            score = 0
            lives = 3
            timeLeft = 60
            nextItem()
        }, onBack = onBack)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Punteggio: $score", fontSize = 20.sp)
                Text("Vite: $lives", fontSize = 20.sp)
                Text("Tempo: $timeLeft", fontSize = 20.sp)
            }

            // Waste item (moved above bins for better drag experience)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = currentItem.imageRes),
                    contentDescription = currentItem.name,
                    modifier = Modifier
                        .offset { animatedOffset.value.toIntOffset() }
                        .size(120.dp)
                        .onGloballyPositioned { coordinates ->
                            // Store the initial bounds of the item
                            itemBounds = coordinates.boundsInWindow()
                            val bounds = coordinates.boundsInWindow()
                            // Always update the center position when positioned
                            itemInitialCenter = Offset(
                                bounds.left + bounds.width / 2,
                                bounds.top + bounds.height / 2
                            )
                            println("Initial center set to: $itemInitialCenter")
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    // Reset offset to ensure we're calculating from the right position
                                    if (itemOffset != Offset.Zero) {
                                        itemOffset = Offset.Zero
                                        scope.launch {
                                            animatedOffset.snapTo(Offset.Zero)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    isDragging = false
                                    println("Drag ended with offset: $itemOffset")
                                    handleDrop()
                                },
                                onDragCancel = {
                                    isDragging = false
                                    resetPosition()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newOffset = itemOffset + Offset(dragAmount.x, dragAmount.y)
                                    itemOffset = newOffset
                                    scope.launch {
                                        animatedOffset.snapTo(newOffset)
                                    }
                                }
                            )
                        },
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = currentItem.name,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(top = 130.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Trash bins
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrashBin(
                    type = WasteType.PLASTIC,
                    onPositioned = { bounds ->
                        // Update bins bounds list with this bin's position
                        val existingList = binsBounds.filter { it.type != WasteType.PLASTIC }
                        binsBounds = existingList + BinBounds(WasteType.PLASTIC, bounds)
                    }
                )

                TrashBin(
                    type = WasteType.PAPER,
                    onPositioned = { bounds ->
                        val existingList = binsBounds.filter { it.type != WasteType.PAPER }
                        binsBounds = existingList + BinBounds(WasteType.PAPER, bounds)
                    }
                )

                TrashBin(
                    type = WasteType.ORGANIC,
                    onPositioned = { bounds ->
                        val existingList = binsBounds.filter { it.type != WasteType.ORGANIC }
                        binsBounds = existingList + BinBounds(WasteType.ORGANIC, bounds)
                    }
                )
            }

            // Game instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Trascina il rifiuto nel cestino corretto!",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp
                )
            }

            // Back button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text("Torna indietro")
            }
        }
    }
}

@Composable
fun TrashBin(type: WasteType, onPositioned: (Rect) -> Unit) {
    val binColor = when (type) {
        WasteType.PLASTIC -> Color(0xFF03A9F4) // Blue
        WasteType.PAPER -> Color(0xFFFFEB3B)   // Yellow
        WasteType.ORGANIC -> Color(0xFF4CAF50) // Green
        WasteType.GLASS -> TODO()
    }

    val binIcon = when (type) {
        WasteType.PLASTIC -> R.drawable.placeholder // Replace with actual icons
        WasteType.PAPER -> R.drawable.placeholder
        WasteType.ORGANIC -> R.drawable.placeholder
        WasteType.GLASS -> TODO()
    }

    Box(
        modifier = Modifier
            .size(110.dp)
            .background(binColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .onGloballyPositioned { coordinates ->
                // Pass the bin's bounds back for collision detection
                onPositioned(coordinates.boundsInWindow())
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = binIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = when (type) {
                    WasteType.PLASTIC -> "Plastica"
                    WasteType.PAPER -> "Carta"
                    WasteType.ORGANIC -> "Organico"
                    WasteType.GLASS -> "Vetro"
                },
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun GameOverScreen(score: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Game Over", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Punteggio: $score", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart) {
            Text("Riprova")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Torna alla Home")
        }
    }
}

// Estensione per convertire Offset a IntOffset

fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())