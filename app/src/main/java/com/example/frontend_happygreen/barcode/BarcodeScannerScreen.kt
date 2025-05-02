// File: app/src/main/java/com/example/frontend_happygreen/barcode/BarcodeScannerScreen.kt
package com.example.frontend_happygreen.barcode

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green600
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * ViewModel for Barcode Scanner functionality
 */
class BarcodeScannerViewModel : ViewModel() {
    // Scanned barcode result
    var barcodeValue = mutableStateOf<String?>(null)

    // Product information (would be fetched from a database in a real app)
    var productInfo = mutableStateOf<ProductInfo?>(null)

    // Flash state
    var isFlashEnabled = mutableStateOf(false)

    // Check if we're in scanning mode or result display mode
    var isShowingResult = mutableStateOf(false)

    // Toggle flash
    fun toggleFlash() {
        isFlashEnabled.value = !isFlashEnabled.value
    }

    // Reset scanner to scan again
    fun resetScanner() {
        barcodeValue.value = null
        productInfo.value = null
        isShowingResult.value = false
    }

    // Process barcode value
    fun processBarcode(value: String) {
        barcodeValue.value = value
        isShowingResult.value = true

        // In a real app, this would fetch product info from an API or database
        // For demo purposes, we're just creating dummy data
        productInfo.value = when {
            value.startsWith("978") -> ProductInfo(
                name = "Eco-friendly Book",
                material = "Recycled Paper",
                recyclingInfo = "Recyclable in paper bin",
                sustainabilityScore = 9
            )
            value.startsWith("50") -> ProductInfo(
                name = "Glass Bottle",
                material = "Glass",
                recyclingInfo = "Recyclable in glass bin",
                sustainabilityScore = 8
            )
            value.startsWith("40") -> ProductInfo(
                name = "Plastic Container",
                material = "Type 2 Plastic (HDPE)",
                recyclingInfo = "Recyclable in plastic bin",
                sustainabilityScore = 6
            )
            else -> ProductInfo(
                name = "Unknown Product",
                material = "Unknown Material",
                recyclingInfo = "Check product label for recycling information",
                sustainabilityScore = 5
            )
        }
    }
}

/**
 * Data class representing product sustainability information
 */
data class ProductInfo(
    val name: String,
    val material: String,
    val recyclingInfo: String,
    val sustainabilityScore: Int
)

/**
 * Main Barcode Scanner Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onBack: () -> Unit,
    viewModel: BarcodeScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val coroutineScope = rememberCoroutineScope()
    val barcodeValue by viewModel.barcodeValue
    val isShowingResult by viewModel.isShowingResult
    val isFlashEnabled by viewModel.isFlashEnabled

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Check and request camera permission
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Product Barcode",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (hasCameraPermission && !isShowingResult) {
                        IconButton(onClick = { viewModel.toggleFlash() }) {
                            Icon(
                                imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Flash",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isShowingResult) {
                // Show product info when a barcode is scanned
                ProductInfoScreen(
                    barcode = barcodeValue ?: "",
                    productInfo = viewModel.productInfo.value,
                    onScanAgain = { viewModel.resetScanner() }
                )
            } else {
                // Camera preview for scanning
                if (hasCameraPermission) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CameraPreview(
                            onBarcodeDetected = { barcode ->
                                if (!isShowingResult) {
                                    coroutineScope.launch {
                                        viewModel.processBarcode(barcode)
                                    }
                                }
                            },
                            isFlashEnabled = isFlashEnabled
                        )

                        // Overlay with scanning instructions
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Point camera at a product barcode",
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                } else {
                    // No camera permission message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Camera Permission Required",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please grant camera permission to scan barcodes",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = Green600)
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Camera Preview Composable
 */
@Composable
fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    isFlashEnabled: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up the preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Configure barcode scanner
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93
                )
                .build()

            val barcodeScanner = BarcodeScanning.getClient(options)

            // Set up the image analysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        BarcodeAnalyzer(barcodeScanner, onBarcodeDetected)
                    )
                }

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                // Enable or disable flash
                if (camera.cameraInfo.hasFlashUnit()) {
                    camera.cameraControl.enableTorch(isFlashEnabled)
                }

            } catch (exc: Exception) {
                Log.e("BarcodeScannerScreen", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

/**
 * Barcode Analyzer for processing images
 */
private class BarcodeAnalyzer(
    private val barcodeScanner: BarcodeScanner,
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private var isScanning = true
    private var lastAnalyzedTimestamp = 0L

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // Skip analysis if not actively scanning
        if (!isScanning) {
            imageProxy.close()
            return
        }

        // Throttle analysis to every 1 second
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp < 1000) {
            imageProxy.close()
            return
        }

        imageProxy.image?.let { mediaImage ->
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes[0]  // Get the first barcode found

                        barcode.rawValue?.let { value ->
                            if (value.isNotEmpty()) {
                                isScanning = false  // Stop scanning once a barcode is found
                                onBarcodeDetected(value)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("BarcodeAnalyzer", "Barcode scanning failed", exception)
                }
                .addOnCompleteListener {
                    // Important: Close the imageProxy
                    imageProxy.close()
                    lastAnalyzedTimestamp = currentTimestamp
                }
        } ?: imageProxy.close()
    }
}

/**
 * Product Information Screen displayed after scanning
 */
@Composable
fun ProductInfoScreen(
    barcode: String,
    productInfo: ProductInfo?,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Barcode info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Green100
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Barcode",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = barcode,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Product details
        productInfo?.let { info ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Product Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Product name
                    Column {
                        Text(
                            text = "Product Name",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = info.name,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Material
                    Column {
                        Text(
                            text = "Material",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = info.material,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recycling info
                    Column {
                        Text(
                            text = "Recycling Information",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = info.recyclingInfo,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sustainability score
                    Column {
                        Text(
                            text = "Sustainability Score",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        // Score visualization
                        val scoreColor = when {
                            info.sustainabilityScore >= 8 -> Color(0xFF388E3C) // Green
                            info.sustainabilityScore >= 5 -> Color(0xFFFFA000) // Amber
                            else -> Color(0xFFD32F2F) // Red
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${info.sustainabilityScore}/10",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            LinearProgressIndicator(
                                progress = info.sustainabilityScore / 10f,
                                modifier = Modifier
                                    .height(8.dp)
                                    .weight(1f),
                                color = scoreColor,
                                trackColor = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Scan again button
        Button(
            onClick = onScanAgain,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green600
            )
        ) {
            Text("Scan Another Product")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// File: app/src/main/java/com/example/frontend_happygreen/screens/MainScreen.kt

// Add the following modifications to the GamesBanner function in MainScreen.kt
// This adds a barcode scanner icon to the banner

/*
@Composable
fun GamesBanner(
    games: List<Game>,
    onGameSelected: (String) -> Unit,
    onScanBarcode: () -> Unit, // New parameter for barcode scanning
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Green100)
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Giochi & Strumenti",  // Changed from just "Giochi" to include tools
                    style = MaterialTheme.typography.titleMedium,
                    color = Green600,
                    modifier = Modifier.padding(start = 8.dp)
                )
                
                // Barcode Scanner Button - Add this
                IconButton(onClick = onScanBarcode) {
                    Icon(
                        imageVector = Icons.Default.CropFree,  // Barcode icon
                        contentDescription = "Scansiona Codice a Barre",
                        tint = Green600
                    )
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Chiudi Banner",
                        tint = Green600
                    )
                }
            }
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(
                    items = games,
                    key = { it.id }
                ) { game ->
                    GameCardCompact(
                        name = game.name,
                        description = game.description,
                        iconId = game.iconId,
                        onClick = { onGameSelected(game.id) }
                    )
                }
            }
        }
    }
}
*/

// Then modify MainAppScaffold to add barcode scanner state and call handler

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    currentTab: Int,
    tabItems: List<String>,
    icons: List<ImageVector>,
    hasNotifications: Boolean,
    notificationCount: Int,
    isLoading: Boolean,
    classList: List<ClassRoom>,
    showGamesBanner: Boolean,
    gamesList: List<Game>,
    onTabSelected: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onCreateClassClick: () -> Unit,
    onClassSelected: (ClassRoom) -> Unit,
    onGameSelected: (String) -> Unit,
    onHideGamesBanner: () -> Unit
) {
    var showEcoAIChat by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }  // Add this state

    if (showEcoAIChat) {
        EcoAIChatScreen(onBack = { showEcoAIChat = false })
    } else if (showBarcodeScanner) {  // Add this condition
        BarcodeScannerScreen(onBack = { showBarcodeScanner = false })
    } else {
        Scaffold(
            // ... existing code ...
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White),
            ) {
                // ... existing code ...

                // Games banner with barcode scanner
                if (showGamesBanner) {
                    GamesBanner(
                        games = gamesList,
                        onGameSelected = onGameSelected,
                        onScanBarcode = { showBarcodeScanner = true },  // Add this parameter
                        onDismiss = onHideGamesBanner,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
*/