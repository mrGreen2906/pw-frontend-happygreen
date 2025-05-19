package com.example.frontend_happygreen.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import android.content.Context

/**
 * Data class representing an EcoCenter
 */
data class EcoCenter(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    val address: String?,
    val amenities: List<String>
)

/**
 * ViewModel for EcoCenters functionality
 */
class EcoCentersViewModel : ViewModel() {
    // States
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _ecoCenters = mutableStateOf<List<EcoCenter>>(emptyList())
    val ecoCenters: State<List<EcoCenter>> = _ecoCenters

    private val _userLocation = mutableStateOf<Pair<Double, Double>?>(null)
    val userLocation: State<Pair<Double, Double>?> = _userLocation

    private val _selectedEcoCenter = mutableStateOf<EcoCenter?>(null)
    val selectedEcoCenter: State<EcoCenter?> = _selectedEcoCenter

    private val _searchRadius = mutableStateOf(5000) // 5km default
    val searchRadius: State<Int> = _searchRadius

    /**
     * Set the user's current location
     */
    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
        // Load eco centers based on new location
        loadEcoCenters()
    }

    /**
     * Update search radius
     */
    fun updateSearchRadius(radius: Int) {
        _searchRadius.value = radius
        loadEcoCenters()
    }

    /**
     * Select an eco center
     */
    fun selectEcoCenter(ecoCenter: EcoCenter) {
        _selectedEcoCenter.value = ecoCenter
    }

    /**
     * Clear selection
     */
    fun clearSelection() {
        _selectedEcoCenter.value = null
    }

    /**
     * Load eco centers from Overpass API
     */
    fun loadEcoCenters() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val location = _userLocation.value ?: return@launch
                val (latitude, longitude) = location
                val radius = _searchRadius.value

                // Call Overpass API
                val ecoCenters = fetchEcoCentersFromOverpass(latitude, longitude, radius)
                _ecoCenters.value = ecoCenters
            } catch (e: Exception) {
                _errorMessage.value = "Errore nel caricamento degli ecocentri: ${e.message}"
                Log.e("EcoCentersVM", "Error loading eco centers", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch eco centers from Overpass API
     */
    private suspend fun fetchEcoCentersFromOverpass(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): List<EcoCenter> = withContext(Dispatchers.IO) {
        // Build Overpass query for recycling facilities
        val overpassQuery = """
            [out:json];
            (
              node["amenity"="recycling"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="recycling"](around:$radiusMeters,$latitude,$longitude);
              relation["amenity"="recycling"](around:$radiusMeters,$latitude,$longitude);
              
              node["recycling_type"="centre"](around:$radiusMeters,$latitude,$longitude);
              way["recycling_type"="centre"](around:$radiusMeters,$latitude,$longitude);
              relation["recycling_type"="centre"](around:$radiusMeters,$latitude,$longitude);
              
              node["waste"="disposal"](around:$radiusMeters,$latitude,$longitude);
              way["waste"="disposal"](around:$radiusMeters,$latitude,$longitude);
              relation["waste"="disposal"](around:$radiusMeters,$latitude,$longitude);
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()

        val url = URL("https://overpass-api.de/api/interpreter")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        // Write the query
        connection.outputStream.use { output ->
            output.write(overpassQuery.toByteArray())
        }

        // Read the response
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonResponse = JSONObject(response)
        val elements = jsonResponse.getJSONArray("elements")

        // Parse the response to get eco centers
        val ecoCenters = mutableListOf<EcoCenter>()
        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            if (element.getString("type") == "node") {
                // Only process nodes for now
                val id = element.getString("id")
                val nodeLat = element.getDouble("lat")
                val nodeLon = element.getDouble("lon")
                val tags = if (element.has("tags")) element.getJSONObject("tags") else null

                // Get eco center name
                val name = tags?.optString("name") ?: "Ecocentro"

                // Get address data
                val street = tags?.optString("addr:street", "")
                val houseNumber = tags?.optString("addr:housenumber", "")
                val city = tags?.optString("addr:city", "")
                val address = if (street?.isNotEmpty() == true) {
                    "$street ${if (houseNumber?.isNotEmpty() == true) houseNumber else ""}, ${if (city?.isNotEmpty() == true) city else ""}"
                } else {
                    null
                }

                // Get amenities
                val amenities = mutableListOf<String>()
                if (tags != null) {
                    // Look for recycling materials
                    val keys = tags.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key.startsWith("recycling:") && tags.getString(key) == "yes") {
                            val material = key.substringAfter("recycling:")
                            amenities.add(material.replaceFirstChar { it.uppercase() })
                        }
                    }

                    // Check for specific recycling type
                    if (tags.has("recycling_type")) {
                        amenities.add("Tipo: ${tags.getString("recycling_type")}")
                    }
                }

                // Calculate distance from user
                val distance = calculateDistance(latitude, longitude, nodeLat, nodeLon)

                ecoCenters.add(
                    EcoCenter(
                        id = id,
                        name = name,
                        latitude = nodeLat,
                        longitude = nodeLon,
                        distance = distance,
                        address = address,
                        amenities = amenities
                    )
                )
            }
        }

        // Sort by distance
        return@withContext ecoCenters.sortedBy { it.distance }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371e3 // Earth radius in meters
        val φ1 = lat1 * Math.PI / 180
        val φ2 = lat2 * Math.PI / 180
        val Δφ = (lat2 - lat1) * Math.PI / 180
        val Δλ = (lon2 - lon1) * Math.PI / 180

        val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return r * c // Distance in meters
    }
}

/**
 * Content for the Explore tab showing eco centers
 */
@Composable
fun ExploreContent(
    onEcoCenterMapClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "Esplora la tua città",
                style = MaterialTheme.typography.titleLarge,
                color = Green800,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Trova ecocentri e luoghi eco-friendly vicino a te",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        item {
            // Eco Centers Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEcoCenterMapClick),
                colors = CardDefaults.cardColors(containerColor = Green100),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Green600),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ecocentri Vicini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Green800
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Trova i punti di raccolta differenziata più vicini a te",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Green600
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.NavigateNext,
                            contentDescription = null,
                            tint = Green600
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Mappa") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Green600,
                                labelColor = Color.White,
                                leadingIconContentColor = Color.White
                            )
                        )

                        AssistChip(
                            onClick = { },
                            label = { Text("Lista") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White,
                                labelColor = Green600,
                                leadingIconContentColor = Green600
                            )
                        )
                    }
                }
            }
        }

        item {
            // Additional explore features
            Text(
                text = "Altre funzionalità",
                style = MaterialTheme.typography.titleMedium,
                color = Green800,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            // Coming soon cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComingSoonCard(
                    title = "Negozi Eco",
                    icon = Icons.Default.Store,
                    modifier = Modifier.weight(1f)
                )
                ComingSoonCard(
                    title = "Eventi Green",
                    icon = Icons.Default.Event,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComingSoonCard(
                    title = "Bike Sharing",
                    icon = Icons.Default.DirectionsBike,
                    modifier = Modifier.weight(1f)
                )
                ComingSoonCard(
                    title = "Mercati Locali",
                    icon = Icons.Default.LocalGroceryStore,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ComingSoonCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Prossimamente",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Main screen for finding eco centers near the user
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoCentersMapScreen(
    onBack: () -> Unit,
    viewModel: EcoCentersViewModel = viewModel()
) {
    val context = LocalContext.current
    val ecoCenters by viewModel.ecoCenters
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val userLocation by viewModel.userLocation
    val selectedEcoCenter by viewModel.selectedEcoCenter
    val searchRadius by viewModel.searchRadius

    var mapInitialized by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Request location permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            // Get user location after permission is granted
            getUserLocation(context) { latitude, longitude ->
                viewModel.setUserLocation(latitude, longitude)
            }
        }
    }

    // Get user location if permission is granted
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted && userLocation == null) {
            getUserLocation(context) { latitude, longitude ->
                viewModel.setUserLocation(latitude, longitude)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ecocentri Vicini", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        userLocation?.let { (lat, lon) ->
                            viewModel.setUserLocation(lat, lon) // Refresh
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Aggiorna",
                            tint = Color.White
                        )
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
            if (!locationPermissionGranted) {
                // Show permission request screen
                PermissionRequestScreen(
                    onRequestPermission = {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                )
            } else if (userLocation == null) {
                // Loading user location
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green600)
                    Text(
                        text = "Recupero posizione...",
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            } else {
                // Main content with map and list
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Map container - takes 60% of the screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                    ) {
                        // OSM Map
                        val mapView = remember { MapView(context) }
                        val (latitude, longitude) = userLocation!!

                        AndroidView(
                            factory = { mapView },
                            update = { map ->
                                // Setup map
                                if (!mapInitialized) {
                                    map.setTileSource(TileSourceFactory.MAPNIK)
                                    map.setMultiTouchControls(true)

                                    // Add user location overlay
                                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                                    locationOverlay.enableMyLocation()
                                    map.overlays.add(locationOverlay)

                                    mapInitialized = true
                                }

                                // Set map center to user location
                                val mapController = map.controller
                                mapController.setZoom(14.0)
                                val startPoint = GeoPoint(latitude, longitude)
                                mapController.setCenter(startPoint)

                                // Clear existing markers
                                map.overlays.removeIf { it is Marker }

                                // Add user location marker
                                val userMarker = Marker(map)
                                userMarker.position = startPoint
                                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                userMarker.title = "La tua posizione"
                                userMarker.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
                                map.overlays.add(userMarker)

                                // Add eco centers markers
                                ecoCenters.forEach { ecoCenter ->
                                    val marker = Marker(map)
                                    marker.position = GeoPoint(ecoCenter.latitude, ecoCenter.longitude)
                                    marker.title = ecoCenter.name
                                    marker.snippet = "Distanza: ${formatDistance(ecoCenter.distance)}"
                                    marker.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass)
                                    marker.setOnMarkerClickListener { _, _ ->
                                        viewModel.selectEcoCenter(ecoCenter)
                                        true
                                    }
                                    map.overlays.add(marker)
                                }

                                // Highlight selected eco center
                                selectedEcoCenter?.let { ecoCenter ->
                                    val selectedMarker = Marker(map)
                                    selectedMarker.position = GeoPoint(ecoCenter.latitude, ecoCenter.longitude)
                                    selectedMarker.title = ecoCenter.name
                                    selectedMarker.snippet = "Distanza: ${formatDistance(ecoCenter.distance)}"
                                    selectedMarker.icon = ContextCompat.getDrawable(context, android.R.drawable.star_on)
                                    map.overlays.add(selectedMarker)

                                    // Center map on selected eco center
                                    mapController.animateTo(GeoPoint(ecoCenter.latitude, ecoCenter.longitude))
                                }

                                map.invalidate()
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Search radius selector
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Raggio: ${searchRadius / 1000} km",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Decrease radius
                                        IconButton(
                                            onClick = {
                                                if (searchRadius > 1000) {
                                                    viewModel.updateSearchRadius(searchRadius - 1000)
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Riduci raggio"
                                            )
                                        }

                                        // Increase radius
                                        IconButton(
                                            onClick = {
                                                viewModel.updateSearchRadius(searchRadius + 1000)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Aumenta raggio"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Loading indicator
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }

                    // List of eco centers - takes 40% of the screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)
                            .background(Color.White)
                    ) {
                        when {
                            isLoading && ecoCenters.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Green600)
                                }
                            }
                            errorMessage != null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = errorMessage!!,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            ecoCenters.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nessun ecocentro trovato nelle vicinanze.\nProva ad aumentare il raggio di ricerca.",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        Text(
                                            text = "Ecocentri trovati: ${ecoCenters.size}",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    items(ecoCenters) { ecoCenter ->
                                        EcoCenterItem(
                                            ecoCenter = ecoCenter,
                                            isSelected = selectedEcoCenter?.id == ecoCenter.id,
                                            onClick = { viewModel.selectEcoCenter(ecoCenter) }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected eco center details
                AnimatedVisibility(
                    visible = selectedEcoCenter != null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    selectedEcoCenter?.let { ecoCenter ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Header with close button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ecoCenter.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = { viewModel.clearSelection() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Green100)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Chiudi",
                                            tint = Green600
                                        )
                                    }
                                }

                                // Details
                                Spacer(modifier = Modifier.height(8.dp))

                                // Distance
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Green600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Distanza: ${formatDistance(ecoCenter.distance)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Address
                                ecoCenter.address?.let { address ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = Green600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = address,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Amenities
                                if (ecoCenter.amenities.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Materiali accettati:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ecoCenter.amenities.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                // Actions
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            try {
                                                // Opzione primaria: Aprire in Google Maps
                                                val uri = Uri.parse("geo:${ecoCenter.latitude},${ecoCenter.longitude}?q=${ecoCenter.latitude},${ecoCenter.longitude}(${ecoCenter.name})")
                                                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                                mapIntent.setPackage("com.google.android.apps.maps")

                                                // Verifica se Google Maps è installato
                                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(mapIntent)
                                                } else {
                                                    // Fallback: Usa il browser web se Google Maps non è installato
                                                    val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${ecoCenter.latitude},${ecoCenter.longitude}")
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                                                    context.startActivity(browserIntent)

                                                    // Opzionalmente mostra un Toast
                                                    Toast.makeText(context, "Google Maps non trovata, apertura nel browser", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                // Log e gestione degli errori
                                                Log.e("EcoCentersMap", "Errore nell'apertura di Maps: ${e.message}", e)
                                                Toast.makeText(context, "Impossibile aprire Maps: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Green600
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Navigation,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Indicazioni")
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

/**
 * Eco center item for the list
 */
@Composable
fun EcoCenterItem(
    ecoCenter: EcoCenter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Green100 else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Green600),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ecoCenter.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Address if available
                ecoCenter.address?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Materials if available (show only first 3)
                if (ecoCenter.amenities.isNotEmpty()) {
                    val shownAmenities = ecoCenter.amenities.take(3)
                    val remaining = ecoCenter.amenities.size - shownAmenities.size

                    Text(
                        text = shownAmenities.joinToString(", ") +
                                if (remaining > 0) " (+$remaining)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Distance
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatDistance(ecoCenter.distance),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Green600,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = null,
                    tint = if (isSelected) Green600 else Color.Gray
                )
            }
        }
    }
}

/**
 * Permission request screen
 */
@Composable
fun PermissionRequestScreen(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Green600,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Autorizzazione alla posizione",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Per mostrare gli ecocentri vicini a te, l'app ha bisogno di accedere alla tua posizione.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "La tua posizione sarà utilizzata solo all'interno dell'app per trovare gli ecocentri più vicini.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = Green600
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Consenti accesso alla posizione",
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Format distance to display as meters or kilometers
 */
fun formatDistance(distanceInMeters: Double): String {
    return when {
        distanceInMeters < 1000 -> "${distanceInMeters.toInt()} m"
        else -> {
            val km = distanceInMeters / 1000
            "%.1f km".format(km)
        }
    }
}

/**
 * Get user location
 */
private fun getUserLocation(
    context: Context,
    onLocationReceived: (latitude: Double, longitude: Double) -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Get last location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationReceived(location.latitude, location.longitude)
                    } else {
                        // If last location is null, request location updates
                        requestLocationUpdates(context, fusedLocationClient, onLocationReceived)
                    }
                }
                .addOnFailureListener {
                    // If getting last location fails, request location updates
                    requestLocationUpdates(context, fusedLocationClient, onLocationReceived)
                }
        }
    } catch (e: Exception) {
        Log.e("LocationUtil", "Error getting location", e)
    }
}

/**
 * Request location updates
 */
private fun requestLocationUpdates(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationReceived: (latitude: Double, longitude: Double) -> Unit
) {
    try {
        // Create location request
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        // Location callback
        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                }

                // Remove updates after getting location
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        // Request location updates
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        }
    } catch (e: Exception) {
        Log.e("LocationUtil", "Error requesting location updates", e)
    }
}