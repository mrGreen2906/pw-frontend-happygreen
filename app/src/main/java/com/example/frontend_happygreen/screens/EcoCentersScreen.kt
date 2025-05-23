package com.example.frontend_happygreen.screens
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.ui.components.SectionHeader
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.net.HttpURLConnection
import java.net.URL

// Data classes e helper classes rimangono invariati
enum class WastePointType(val displayName: String, val icon: ImageVector) {
    ECOCENTRO("Ecocentro", Icons.Filled.Business),
    CONTENITORE("Contenitore", Icons.Filled.Delete),
    CENTRO_RACCOLTA("Centro Raccolta", Icons.Filled.Store),
    ISOLA_ECOLOGICA("Isola Ecologica", Icons.Filled.Place),
    GENERICO("Punto Raccolta", Icons.Filled.FindReplace)
}

data class WasteCollectionPoint(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    val address: String?,
    val acceptedMaterials: Set<String>,
    val type: WastePointType,
    val openingHours: String?,
    val phone: String?,
    val website: String?
)

// ViewModel rimane invariato
class EcoCentersViewModel : ViewModel() {
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _allWastePoints = mutableStateOf<List<WasteCollectionPoint>>(emptyList())
    private val _filteredWastePoints = mutableStateOf<List<WasteCollectionPoint>>(emptyList())
    val filteredWastePoints: State<List<WasteCollectionPoint>> = _filteredWastePoints

    private val _userLocation = mutableStateOf<Pair<Double, Double>?>(null)
    val userLocation: State<Pair<Double, Double>?> = _userLocation

    private val _selectedPoint = mutableStateOf<WasteCollectionPoint?>(null)
    val selectedPoint: State<WasteCollectionPoint?> = _selectedPoint

    private val _searchRadius = mutableStateOf(5000) // 5km default
    val searchRadius: State<Int> = _searchRadius

    private val _materialFilter = mutableStateOf("")
    val materialFilter: State<String> = _materialFilter

    private val _selectedTypeFilter = mutableStateOf<WastePointType?>(null)
    val selectedTypeFilter: State<WastePointType?> = _selectedTypeFilter

    // Mappa dei materiali comuni con i loro sinonimi
    private val materialMap = mapOf(
        "plastica" to setOf("plastic", "plastic_bottles", "plastic_packaging", "pvc", "pet"),
        "carta" to setOf("paper", "cardboard", "newspaper", "magazines"),
        "cartone" to setOf("cardboard", "paper"),
        "vetro" to setOf("glass", "glass_bottles"),
        "metallo" to setOf("metal", "aluminium", "steel", "cans", "scrap_metal"),
        "alluminio" to setOf("aluminium", "metal", "cans"),
        "organico" to setOf("organic", "green_waste", "food_waste"),
        "umido" to setOf("organic", "green_waste", "food_waste"),
        "elettronici" to setOf("electrical_appliances", "electronics", "computers", "mobile_phones"),
        "pile" to setOf("batteries", "battery"),
        "batterie" to setOf("batteries", "battery"),
        "farmaci" to setOf("drugs", "medicine"),
        "olio" to setOf("oil", "cooking_oil", "engine_oil"),
        "vestiti" to setOf("clothes", "textiles", "shoes"),
        "legno" to setOf("wood", "furniture"),
        "pneumatici" to setOf("tyres", "tires"),
        "rifiuti pericolosi" to setOf("hazardous_waste", "paint", "chemicals"),
        "ingombranti" to setOf("bulky_waste", "furniture")
    )

    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
        loadWastePoints()
    }

    fun updateSearchRadius(radius: Int) {
        _searchRadius.value = radius
        loadWastePoints()
    }

    fun selectPoint(point: WasteCollectionPoint) {
        _selectedPoint.value = point
    }

    fun clearSelection() {
        _selectedPoint.value = null
    }

    fun filterByMaterial(material: String) {
        _materialFilter.value = material
        applyFilters()
    }

    fun filterByType(type: WastePointType?) {
        _selectedTypeFilter.value = type
        applyFilters()
    }

    private fun applyFilters() {
        val material = _materialFilter.value.trim().lowercase()
        val typeFilter = _selectedTypeFilter.value

        var filtered = _allWastePoints.value

        // Filtra per tipo se selezionato
        if (typeFilter != null) {
            filtered = filtered.filter { it.type == typeFilter }
        }

        // Filtra per materiale se specificato
        if (material.isNotEmpty()) {
            // Trova le parole chiave per il materiale cercato
            val searchKeys = materialMap.entries
                .filter { (key, _) -> key.contains(material) }
                .flatMap { it.value }
                .toSet()

            filtered = filtered.filter { point ->
                // Verifica se il punto accetta il materiale
                val pointMaterials = point.acceptedMaterials.map { it.lowercase() }

                // Cerca match diretto
                val directMatch = pointMaterials.any { it.contains(material) }

                // Cerca match con sinonimi
                val synonymMatch = searchKeys.any { searchKey ->
                    pointMaterials.any { it.contains(searchKey) }
                }

                directMatch || synonymMatch
            }
        }

        _filteredWastePoints.value = filtered
    }

    fun loadWastePoints() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val location = _userLocation.value ?: return@launch
                val (latitude, longitude) = location
                val radius = _searchRadius.value

                val points = fetchWastePointsFromOverpass(latitude, longitude, radius)
                _allWastePoints.value = points
                applyFilters()
            } catch (e: Exception) {
                _errorMessage.value = "Errore nel caricamento: ${e.message}"
                Log.e("EcoCentersVM", "Error loading waste points", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Il resto delle funzioni di fetching e processing rimangono uguali
    private suspend fun fetchWastePointsFromOverpass(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): List<WasteCollectionPoint> = withContext(Dispatchers.IO) {
        // Query Overpass come nell'originale
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

        // Scrivi la query
        connection.outputStream.use { output ->
            output.write(overpassQuery.toByteArray())
        }

        // Leggi la risposta
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonResponse = JSONObject(response)
        val elements = jsonResponse.getJSONArray("elements")

        // Parse della risposta (semplificato, senza deduplicazione aggressiva)
        val wastePoints = mutableListOf<WasteCollectionPoint>()
        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            val elementType = element.getString("type")

            when (elementType) {
                "node" -> {
                    val id = element.getString("id")
                    val nodeLat = element.getDouble("lat")
                    val nodeLon = element.getDouble("lon")
                    val tags = if (element.has("tags")) element.getJSONObject("tags") else null

                    processElement(id, nodeLat, nodeLon, tags, latitude, longitude, wastePoints)
                }
                "way", "relation" -> {
                    // Per way e relation, usa il centro se disponibile
                    val id = element.getString("id")
                    val tags = if (element.has("tags")) element.getJSONObject("tags") else null

                    // Calcola coordinate del centro
                    val center = if (element.has("center")) {
                        val centerObj = element.getJSONObject("center")
                        Pair(centerObj.getDouble("lat"), centerObj.getDouble("lon"))
                    } else if (element.has("bounds")) {
                        val bounds = element.getJSONObject("bounds")
                        val minLat = bounds.getDouble("minlat")
                        val maxLat = bounds.getDouble("maxlat")
                        val minLon = bounds.getDouble("minlon")
                        val maxLon = bounds.getDouble("maxlon")
                        Pair((minLat + maxLat) / 2, (minLon + maxLon) / 2)
                    } else {
                        null
                    }

                    center?.let { (lat, lon) ->
                        processElement(id, lat, lon, tags, latitude, longitude, wastePoints)
                    }
                }
            }
        }

        return@withContext wastePoints.sortedBy { it.distance }
    }

    private fun processElement(
        id: String,
        lat: Double,
        lon: Double,
        tags: JSONObject?,
        userLat: Double,
        userLon: Double,
        results: MutableList<WasteCollectionPoint>
    ) {
        val pointType = determinePointType(tags)
        val name = generatePointName(tags, pointType)
        val address = buildAddress(tags)
        val materials = extractAcceptedMaterials(tags)
        val openingHours = tags?.optString("opening_hours")
        val phone = tags?.optString("phone")
        val website = tags?.optString("website")

        val distance = calculateDistance(userLat, userLon, lat, lon)

        results.add(
            WasteCollectionPoint(
                id = id,
                name = name,
                latitude = lat,
                longitude = lon,
                distance = distance,
                address = address,
                acceptedMaterials = materials,
                type = pointType,
                openingHours = openingHours,
                phone = phone,
                website = website
            )
        )
    }

    private fun determinePointType(tags: JSONObject?): WastePointType {
        if (tags == null) return WastePointType.GENERICO

        // Controlla recycling_type
        when (tags.optString("recycling_type")) {
            "centre" -> return WastePointType.ECOCENTRO
            "container" -> return WastePointType.CONTENITORE
        }

        // Controlla amenity
        when (tags.optString("amenity")) {
            "waste_transfer_station" -> return WastePointType.ECOCENTRO
            "waste_disposal" -> return WastePointType.CENTRO_RACCOLTA
            "recycling" -> {
                // Se ha molti materiali, è un'isola ecologica
                val materialCount = tags.keys().asSequence()
                    .count { it.startsWith("recycling:") && tags.optString(it) == "yes" }

                return when {
                    materialCount > 3 -> WastePointType.ISOLA_ECOLOGICA
                    materialCount > 0 -> WastePointType.CONTENITORE
                    else -> WastePointType.GENERICO
                }
            }
        }

        // Controlla waste
        if (tags.optString("waste") == "disposal") {
            return WastePointType.CENTRO_RACCOLTA
        }

        // Se ha tag di riciclaggio, è almeno un contenitore
        val hasRecyclingTags = tags.keys().asSequence().any {
            it.startsWith("recycling:") && tags.optString(it) == "yes"
        }

        return if (hasRecyclingTags) WastePointType.CONTENITORE else WastePointType.GENERICO
    }

    private fun generatePointName(tags: JSONObject?, type: WastePointType): String {
        if (tags == null) return type.displayName

        val name = tags.optString("name")
        if (name.isNotEmpty()) return name

        val operator = tags.optString("operator")
        if (operator.isNotEmpty()) return "$operator - ${type.displayName}"

        return type.displayName
    }

    private fun buildAddress(tags: JSONObject?): String? {
        if (tags == null) return null

        val street = tags.optString("addr:street", "")
        val houseNumber = tags.optString("addr:housenumber", "")
        val city = tags.optString("addr:city", "")
        val postcode = tags.optString("addr:postcode", "")

        val parts = mutableListOf<String>()

        if (street.isNotEmpty()) {
            val streetPart = if (houseNumber.isNotEmpty()) "$street $houseNumber" else street
            parts.add(streetPart)
        }

        if (postcode.isNotEmpty() && city.isNotEmpty()) {
            parts.add("$postcode $city")
        } else if (city.isNotEmpty()) {
            parts.add(city)
        }

        return if (parts.isNotEmpty()) parts.joinToString(", ") else null
    }

    private fun extractAcceptedMaterials(tags: JSONObject?): Set<String> {
        if (tags == null) return emptySet()

        val materials = mutableSetOf<String>()

        // Scansiona tutti i tag recycling:*
        val keys = tags.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key.startsWith("recycling:") && tags.getString(key) == "yes") {
                val material = key.substringAfter("recycling:")
                    .replace("_", " ")
                    .replaceFirstChar { it.uppercase() }
                materials.add(material)
            }
        }

        // Aggiungi altri tag rilevanti
        if (tags.optString("waste") == "disposal") {
            materials.add("Rifiuti generici")
        }

        return materials
    }

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

// IMPLEMENTAZIONE SCHERMATA MAPPA CON NUOVA UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoCentersMapScreen(
    onBack: () -> Unit,
    viewModel: EcoCentersViewModel = viewModel()
) {
    val context = LocalContext.current
    val filteredPoints by viewModel.filteredWastePoints
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val userLocation by viewModel.userLocation
    val selectedPoint by viewModel.selectedPoint
    val searchRadius by viewModel.searchRadius
    val materialFilter by viewModel.materialFilter
    val selectedTypeFilter by viewModel.selectedTypeFilter

    var locationError by remember { mutableStateOf<String?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    // Stati locali per l'UI
    var mapInitialized by remember { mutableStateOf(false) }
    var toolbarExpanded by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var controlsTab by remember { mutableStateOf(ControlsTab.SEARCH) }

    // Stato per controllare la navigazione tra i diversi livelli dell'interfaccia
    var uiLevel by remember { mutableStateOf(UILevel.MAP) }

    // Animazione per altezza della toolbar
    val toolbarHeight by animateDpAsState(
        targetValue = when {
            toolbarExpanded -> 325.dp
            else -> 150.dp
        },
        animationSpec = tween(300)
    )

    // Permessi di localizzazione
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            locationError = null
            isLoadingLocation = true
            getUserLocationWithTimeout(context) { latitude, longitude ->
                isLoadingLocation = false
                locationError = null
                viewModel.setUserLocation(latitude, longitude)
            }
        } else {
            isLoadingLocation = false
            locationError = "Permesso di geolocalizzazione negato"
        }
    }


    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted && userLocation == null && locationError == null) {
            isLoadingLocation = true
            locationError = null
            getUserLocationWithTimeout(context) { latitude, longitude ->
                isLoadingLocation = false
                locationError = null
                viewModel.setUserLocation(latitude, longitude)
            }
        }
    }


    // Gestione degli effetti del punto selezionato
    LaunchedEffect(selectedPoint) {
        if (selectedPoint != null) {
            showDetails = true
            if (uiLevel == UILevel.LIST) {
                uiLevel = UILevel.MAP_WITH_DETAILS
            }
        } else {
            showDetails = false
        }
    }

    // Contenuto principale
    when {
        !locationPermissionGranted -> {
            PermissionRequestScreen2 {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        locationError != null -> {
            LocationErrorScreen(
                error = locationError!!,
                onRetry = {
                    locationError = null
                    isLoadingLocation = true
                    getUserLocationWithTimeout(context) { latitude, longitude ->
                        isLoadingLocation = false
                        viewModel.setUserLocation(latitude, longitude)
                    }
                },
                onBack = onBack
            )
        }

        isLoadingLocation || (userLocation == null && locationError == null) -> {
            LoadingLocationScreen(onBack = onBack)
        }

        else -> {
            // UI principale con mappa
            Box(modifier = Modifier.fillMaxSize()) {
                // Mappa come sfondo sempre presente
                MapView(
                    context = context,
                    userLocation = userLocation!!,
                    wastePoints = filteredPoints,
                    selectedPoint = selectedPoint,
                    onPointSelected = viewModel::selectPoint,
                    isLoading = isLoading,
                    mapInitialized = mapInitialized,
                    onMapInitialized = { mapInitialized = true },
                    // Aggiungiamo un effetto blur quando siamo in modalità lista
                    blurEffect = uiLevel == UILevel.LIST,
                    modifier = Modifier.fillMaxSize()
                )

                // Livello overlay per interazioni
                when (uiLevel) {
                    UILevel.MAP -> {
                        // Modalità mappa: toolbar in alto + FAB e chip informative
                        MapControls(
                            toolbarHeight = toolbarHeight,
                            toolbarExpanded = toolbarExpanded,
                            onToolbarToggle = { toolbarExpanded = !toolbarExpanded },
                            controlsTab = controlsTab,
                            onTabChange = {
                                controlsTab = it
                                // Assicuriamo che il pannello rimanga aperto quando cambiamo tab
                                if (!toolbarExpanded) {
                                    toolbarExpanded = true
                                }
                            },
                            materialFilter = materialFilter,
                            onMaterialFilterChange = viewModel::filterByMaterial,
                            selectedTypeFilter = selectedTypeFilter,
                            onTypeFilterChange = viewModel::filterByType,
                            searchRadius = searchRadius,
                            onRadiusChange = viewModel::updateSearchRadius,
                            onBack = onBack
                        )

                        // FAB per passare alla lista
                        FloatingActionButton(
                            onClick = { uiLevel = UILevel.LIST },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = Green600
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.List,
                                contentDescription = "Visualizza lista",
                                tint = Color.White
                            )
                        }

                        // Chip con numero di punti trovati
                        if (filteredPoints.isNotEmpty()) {
                            Surface(
                                onClick = { uiLevel = UILevel.LIST },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Green600.copy(alpha = 0.9f),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "${filteredPoints.size} punti trovati",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    UILevel.LIST -> {
                        // Modalità lista: mappa blurrata con lista a schermo intero
                        PointsListPanel(
                            points = filteredPoints,
                            selectedPoint = selectedPoint,
                            onPointSelected = viewModel::selectPoint,
                            onBackToMap = { uiLevel = UILevel.MAP },
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onClearFilters = {
                                viewModel.filterByMaterial("")
                                viewModel.filterByType(null)
                            },
                            onBack = onBack
                        )
                    }

                    UILevel.MAP_WITH_DETAILS -> {
                        // Mappa con dettagli del punto selezionato
                        MapControls(
                            toolbarHeight = toolbarHeight,
                            toolbarExpanded = toolbarExpanded,
                            onToolbarToggle = { toolbarExpanded = !toolbarExpanded },
                            controlsTab = controlsTab,
                            onTabChange = {
                                controlsTab = it
                                // Assicuriamo che il pannello rimanga aperto quando cambiamo tab
                                if (!toolbarExpanded) {
                                    toolbarExpanded = true
                                }
                            },
                            materialFilter = materialFilter,
                            onMaterialFilterChange = viewModel::filterByMaterial,
                            selectedTypeFilter = selectedTypeFilter,
                            onTypeFilterChange = viewModel::filterByType,
                            searchRadius = searchRadius,
                            onRadiusChange = viewModel::updateSearchRadius,
                            onBack = onBack
                        )
                    }
                }

                // Dettagli punto (visibili in modalità MAP e MAP_WITH_DETAILS)
                AnimatedVisibility(
                    visible = selectedPoint != null && showDetails && uiLevel != UILevel.LIST,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    selectedPoint?.let {
                        PointDetailsCard(
                            point = it,
                            onClose = {
                                showDetails = false
                                viewModel.clearSelection()
                                if (uiLevel == UILevel.MAP_WITH_DETAILS) {
                                    uiLevel = UILevel.MAP
                                }
                            },
                            onViewOnMap = {
                                // Centra la mappa sul punto
                                uiLevel = UILevel.MAP
                            },
                            onGetDirections = { openInMaps(context, it) },
                            onCall = {
                                it.phone?.let { phone ->
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phone")
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Indicatore di caricamento
                if (isLoading) {
                    LoadingOverlay()
                }
            }
        }
    }
}

/**
 * Livelli dell'interfaccia utente
 */
enum class UILevel {
    MAP,            // Mappa a schermo intero con controlli minimi
    LIST,           // Lista punti a schermo intero
    MAP_WITH_DETAILS // Mappa con dettagli punto selezionato
}

/**
 * Tab per i controlli
 */
enum class ControlsTab(val icon: ImageVector, val label: String) {
    SEARCH(Icons.Outlined.Search, "Cerca"),
    FILTER(Icons.Outlined.FilterList, "Filtri"),
    INFO(Icons.Outlined.Info, "Info")
}

/**
 * Vista mappa con supporto per effetti
 */
@Composable
fun MapView(
    context: Context,
    userLocation: Pair<Double, Double>,
    wastePoints: List<WasteCollectionPoint>,
    selectedPoint: WasteCollectionPoint?,
    onPointSelected: (WasteCollectionPoint) -> Unit,
    isLoading: Boolean,
    mapInitialized: Boolean,
    onMapInitialized: () -> Unit,
    blurEffect: Boolean = false,
    modifier: Modifier = Modifier
) {
    val mapView = remember { MapView(context) }
    val (latitude, longitude) = userLocation

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            update = { map ->
                // Setup mappa
                if (!mapInitialized) {
                    map.setTileSource(TileSourceFactory.MAPNIK)
                    map.setMultiTouchControls(true)

                    // Aggiungi overlay posizione utente
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                    locationOverlay.enableMyLocation()
                    map.overlays.add(locationOverlay)

                    onMapInitialized()
                }

                // Imposta centro mappa sulla posizione utente o punto selezionato
                val mapController = map.controller
                mapController.setZoom(14.0)
                val centerPoint = selectedPoint?.let {
                    GeoPoint(it.latitude, it.longitude)
                } ?: GeoPoint(latitude, longitude)
                mapController.setCenter(centerPoint)

                // Rimuovi marker esistenti
                map.overlays.removeIf { it is Marker }

                // Aggiungi marker posizione utente
                val userMarker = Marker(map)
                userMarker.position = GeoPoint(latitude, longitude)
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                userMarker.title = "La tua posizione"
                userMarker.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
                map.overlays.add(userMarker)

                // Aggiungi marker per punti di raccolta
                wastePoints.forEach { point ->
                    val marker = Marker(map)
                    marker.position = GeoPoint(point.latitude, point.longitude)
                    marker.title = point.name
                    marker.snippet = "${point.type.displayName} - ${formatDistance(point.distance)}"

                    // Icona basata sul tipo
                    marker.icon = when (point.type) {
                        WastePointType.ECOCENTRO -> ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_map)
                        WastePointType.CONTENITORE -> ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass)
                        WastePointType.CENTRO_RACCOLTA -> ContextCompat.getDrawable(context, android.R.drawable.ic_menu_gallery)
                        WastePointType.ISOLA_ECOLOGICA -> ContextCompat.getDrawable(context, android.R.drawable.ic_menu_info_details)
                        WastePointType.GENERICO -> ContextCompat.getDrawable(context, android.R.drawable.ic_menu_info_details)
                    }

                    marker.setOnMarkerClickListener { _, _ ->
                        onPointSelected(point)
                        true
                    }
                    map.overlays.add(marker)
                }

                // Evidenzia punto selezionato
                selectedPoint?.let { point ->
                    val selectedMarker = Marker(map)
                    selectedMarker.position = GeoPoint(point.latitude, point.longitude)
                    selectedMarker.title = point.name
                    selectedMarker.snippet = "${point.type.displayName} - ${formatDistance(point.distance)}"
                    selectedMarker.icon = ContextCompat.getDrawable(context, android.R.drawable.star_on)
                    map.overlays.add(selectedMarker)

                    // Centra mappa sul punto selezionato
                    mapController.animateTo(GeoPoint(point.latitude, point.longitude))
                }

                map.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Effetto blur quando necessario
        if (blurEffect) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(6.dp)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }
    }
}

/**
 * Controlli per la visualizzazione mappa
 */
@Composable
fun MapControls(
    toolbarHeight: androidx.compose.ui.unit.Dp,
    toolbarExpanded: Boolean,
    onToolbarToggle: () -> Unit,
    controlsTab: ControlsTab,
    onTabChange: (ControlsTab) -> Unit,
    materialFilter: String,
    onMaterialFilterChange: (String) -> Unit,
    selectedTypeFilter: WastePointType?,
    onTypeFilterChange: (WastePointType?) -> Unit,
    searchRadius: Int,
    onRadiusChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(toolbarHeight)
            .padding(top = 44.dp) // Added top padding to prevent overlapping with status bar
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        // Toolbar principale
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsante indietro
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Indietro",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Titolo - migliorato centraggio e stile
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Punti di Raccolta",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tab di navigazione
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlsTab.values().forEach { tab ->
                    val selected = tab == controlsTab
                    IconButton(
                        onClick = { onTabChange(tab) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(onClick = onToolbarToggle) {
                    Icon(
                        imageVector = if (toolbarExpanded)
                            Icons.Default.ExpandLess
                        else
                            Icons.Default.ExpandMore,
                        contentDescription = if (toolbarExpanded) "Comprimi" else "Espandi",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Contenuto espandibile in base al tab selezionato
        AnimatedVisibility(visible = toolbarExpanded) {
            when (controlsTab) {
                ControlsTab.SEARCH -> SearchControls(
                    materialFilter = materialFilter,
                    onMaterialFilterChange = onMaterialFilterChange
                )
                ControlsTab.FILTER -> FilterControls(
                    selectedTypeFilter = selectedTypeFilter,
                    onTypeFilterChange = onTypeFilterChange,
                    searchRadius = searchRadius,
                    onRadiusChange = onRadiusChange
                )
                ControlsTab.INFO -> InfoControls()
            }
        }
    }
}

/**
 * Controlli di ricerca
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchControls(
    materialFilter: String,
    onMaterialFilterChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Campo di ricerca con icona
        OutlinedTextField(
            value = materialFilter,
            onValueChange = onMaterialFilterChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cerca materiale (es. plastica, vetro, pile...)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Green600
                )
            },
            trailingIcon = {
                if (materialFilter.isNotEmpty()) {
                    IconButton(onClick = { onMaterialFilterChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Cancella",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green600,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Suggerimenti rapidi
        Text(
            text = "Suggerimenti:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            val commonMaterials = listOf("Plastica", "Carta", "Vetro", "Pile", "Organico", "Elettronici")
            items(commonMaterials) { material ->
                SuggestionChip(
                    onClick = { onMaterialFilterChange(material) },
                    label = { Text(material) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Green100,
                        labelColor = Green800
                    )
                )
            }
        }
    }
}

/**
 * Controlli dei filtri
 */
@Composable
fun FilterControls(
    selectedTypeFilter: WastePointType?,
    onTypeFilterChange: (WastePointType?) -> Unit,
    searchRadius: Int,
    onRadiusChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Filtro per tipo
        Text(
            text = "Tipo di punto",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Chips orizzontali per i tipi
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { onTypeFilterChange(null) },
                    label = { Text("Tutti") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AllInclusive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            items(WastePointType.values()) { type ->
                FilterChip(
                    selected = selectedTypeFilter == type,
                    onClick = { onTypeFilterChange(type) },
                    label = { Text(type.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Raggio di ricerca
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Raggio: ${searchRadius / 1000} km",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(100.dp)
            )

            Slider(
                value = searchRadius.toFloat(),
                onValueChange = { onRadiusChange(it.toInt()) },
                valueRange = 1000f..20000f,
                steps = 19,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Green600,
                    activeTrackColor = Green600
                )
            )
        }
    }
}

/**
 * Controlli informativi
 */
@Composable
fun InfoControls() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Legenda punti",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Legenda tipi di punto
        WastePointType.values().forEach { type ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Green600.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = null,
                            tint = Green600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Pannello per la lista dei punti
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsListPanel(
    points: List<WasteCollectionPoint>,
    selectedPoint: WasteCollectionPoint?,
    onPointSelected: (WasteCollectionPoint) -> Unit,
    onBackToMap: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onClearFilters: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // App bar con pulsanti di navigazione
            TopAppBar(
                title = { Text("Punti di Raccolta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBackToMap) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Visualizza mappa"
                        )
                    }

                    if (points.isNotEmpty()) {
                        IconButton(onClick = onClearFilters) {
                            Icon(
                                imageVector = Icons.Default.FilterListOff,
                                contentDescription = "Rimuovi filtri"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )

            // Contenuto principale
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading && points.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Green600)
                        }
                    }
                    errorMessage != null -> {
                        ErrorView(errorMessage = errorMessage)
                    }
                    points.isEmpty() -> {
                        EmptyResultsView(onClearFilters = onClearFilters)
                    }
                    else -> {
                        // Lista di punti
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header con conteggio
                            item {
                                Text(
                                    text = "${points.size} punti trovati",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            // Raggruppamento per tipo se ci sono più di 5 punti
                            if (points.size > 5) {
                                val groupedPoints = points.groupBy { it.type }

                                groupedPoints.forEach { (type, typePoints) ->
                                    item {
                                        CategoryHeader(
                                            title = type.displayName,
                                            count = typePoints.size,
                                            icon = type.icon
                                        )
                                    }

                                    items(typePoints) { point ->
                                        PointListItem(
                                            point = point,
                                            isSelected = selectedPoint?.id == point.id,
                                            onClick = { onPointSelected(point) }
                                        )
                                    }

                                    item {
                                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                }
                            } else {
                                // Lista semplice se ci sono pochi punti
                                items(points) { point ->
                                    PointListItem(
                                        point = point,
                                        isSelected = selectedPoint?.id == point.id,
                                        onClick = { onPointSelected(point) }
                                    )
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
 * Header di categoria per la lista
 */
@Composable
fun CategoryHeader(
    title: String,
    count: Int,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Green600,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Green800,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = Green600,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Green100, CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/**
 * Item punto nella lista
 */
@Composable
fun PointListItem(
    point: WasteCollectionPoint,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Green100 else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        border = if (isSelected)
            BorderStroke(2.dp, Green600)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona del tipo
            Surface(
                shape = CircleShape,
                color = if (isSelected) Green600 else Green100,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = point.type.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Green600,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informazioni punto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = point.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                point.address?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Etichette materiali (solo le prime 2 + contatore)
                if (point.acceptedMaterials.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayedMaterials = point.acceptedMaterials.take(2)
                        val remaining = point.acceptedMaterials.size - displayedMaterials.size

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            displayedMaterials.forEach { material ->
                                Text(
                                    text = material,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Green600,
                                    modifier = Modifier
                                        .background(
                                            color = Green100,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (remaining > 0) {
                                Text(
                                    text = "+$remaining",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Distanza
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatDistance(point.distance),
                    style = MaterialTheme.typography.titleSmall,
                    color = Green600,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected)
                            Icons.Filled.Info
                        else
                            Icons.Outlined.NavigateNext,
                        contentDescription = "Dettagli",
                        tint = if (isSelected) Green600 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Card dettagli punto
 */
@Composable
fun PointDetailsCard(
    point: WasteCollectionPoint,
    onClose: () -> Unit,
    onViewOnMap: () -> Unit,
    onGetDirections: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Maniglia superiore
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header con nome e tipo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Green100,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = point.type.icon,
                                    contentDescription = null,
                                    tint = Green600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = point.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = point.type.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = Green600
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Chiudi",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Schede informative
            DetailCard(
                title = "Indirizzo",
                value = point.address ?: "Non disponibile",
                icon = Icons.Default.Place,
                trailing = formatDistance(point.distance)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Orari se disponibili
            point.openingHours?.let {
                DetailCard(
                    title = "Orari",
                    value = it,
                    icon = Icons.Default.Schedule
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Removed phone/website section since they're always empty

            // Materiali accettati
            if (point.acceptedMaterials.isNotEmpty()) {
                Text(
                    text = "Materiali accettati",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    items(point.acceptedMaterials.toList()) { material ->
                        MaterialChip(material = material)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Azione principale - solo indicazioni stradali
            Button(
                onClick = onGetDirections,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green600
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Indicazioni Stradali")
            }
        }
    }
}
/**
 * Card informativa per dettagli
 */
@Composable
fun DetailCard(
    title: String,
    value: String,
    icon: ImageVector,
    trailing: String? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Green600,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    trailing?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = Green600,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Chip per materiale
 */
@Composable
fun MaterialChip(material: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Green100,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Green600,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = material,
                style = MaterialTheme.typography.bodySmall,
                color = Green800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Vista errore
 */
@Composable
fun ErrorView(errorMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Si è verificato un errore",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Retry logic */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Riprova")
        }
    }
}

/**
 * Vista nessun risultato
 */
@Composable
fun EmptyResultsView(onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nessun punto trovato",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Prova a modificare i filtri di ricerca o ad aumentare il raggio",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClearFilters,
            colors = ButtonDefaults.buttonColors(
                containerColor = Green600
            )
        ) {
            Icon(
                imageVector = Icons.Default.FilterListOff,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rimuovi filtri")
        }
    }
}

/**
 * Overlay di caricamento
 */
@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Green600)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Caricamento...",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Schermata richiesta permessi (versione alternativa)
 */
@Composable
fun PermissionRequestScreen2(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Green600.copy(alpha = 0.7f),
                        Color.White
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icona
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Green100, Green600.copy(alpha = 0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Titolo
                Text(
                    text = "Posizione necessaria",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Descrizione
                Text(
                    text = "Per trovare i punti di raccolta vicini a te, l'app ha bisogno di accedere alla tua posizione.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "La tua posizione sarà utilizzata solo all'interno dell'app e non sarà condivisa con terze parti.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Pulsante
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONSENTI ACCESSO",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { /* Info privacy */ }) {
                    Text("Informativa sulla privacy")
                }
            }
        }
    }
}

/**
 * Formatta la distanza
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
 * Ottieni posizione utente
 */
private fun getUserLocation(
    context: Context,
    onLocationReceived: (latitude: Double, longitude: Double) -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationReceived(location.latitude, location.longitude)
                    } else {
                        requestLocationUpdates(context, fusedLocationClient, onLocationReceived)
                    }
                }
                .addOnFailureListener {
                    requestLocationUpdates(context, fusedLocationClient, onLocationReceived)
                }
        }
    } catch (e: Exception) {
        Log.e("LocationUtil", "Error getting location", e)
    }
}

/**
 * Richiedi aggiornamenti posizione
 */
private fun requestLocationUpdates(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationReceived: (latitude: Double, longitude: Double) -> Unit
) {
    try {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

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

/**
 * Apri in mappe
 */
private fun openInMaps(context: Context, point: WasteCollectionPoint) {
    try {
        val uri = Uri.parse("geo:${point.latitude},${point.longitude}?q=${point.latitude},${point.longitude}(${point.name})")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${point.latitude},${point.longitude}")
            val browserIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
            context.startActivity(browserIntent)
            Toast.makeText(context, "Google Maps non trovata, apertura nel browser", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("EcoCentersMap", "Errore nell'apertura di Maps: ${e.message}", e)
        Toast.makeText(context, "Impossibile aprire Maps: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

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
            Text(
                text = "Esplora",
                style = MaterialTheme.typography.titleLarge,
                color = Green800,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scopri luoghi eco-friendly e iniziative sostenibili nella tua zona",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Punti di raccolta - Eco Centers
        item {
            SectionHeader(title = "Punti di raccolta")

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable(onClick = onEcoCenterMapClick),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Background con gradiente
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Green600, Green800)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Contenuto superiore
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = "Eco-Centers",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Trova i punti di raccolta differenziata più vicini a te",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Pulsante
                        Button(
                            onClick = onEcoCenterMapClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Green800
                            ),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Visualizza Mappa")
                        }
                    }
                }
            }
        }

        // Sfide ecologiche
        item {
            SectionHeader(title = "Sfide ecologiche")

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Green100
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sfida settimanale",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green800,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Riduci il consumo di plastica monouso per 7 giorni",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Green800
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { /* Partecipa */ },
                            label = { Text("Partecipa") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Green600,
                                labelColor = Color.White
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        )

                        Text(
                            text = "50 Green Points",
                            style = MaterialTheme.typography.labelLarge,
                            color = Green600,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Eventi locali
        item {
            SectionHeader(title = "Eventi locali")

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(3) { index ->
                    EventCard(
                        title = when (index) {
                            0 -> "Pulizia spiaggia"
                            1 -> "Workshop riciclo"
                            else -> "Mercatino eco"
                        },
                        date = when (index) {
                            0 -> "22 Maggio"
                            1 -> "27 Maggio"
                            else -> "1 Giugno"
                        },
                        location = when (index) {
                            0 -> "Lido di Venezia"
                            1 -> "Centro civico"
                            else -> "Piazza Ferretto"
                        }
                    )
                }
            }
        }

        // Consigli eco-friendly
        item {
            SectionHeader(title = "Consigli eco-friendly")

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EcoTip(
                        tip = "Usa borse riutilizzabili per la spesa",
                        icon = Icons.Default.LocalGroceryStore
                    )

                    Divider()

                    EcoTip(
                        tip = "Spegni le luci quando non servono",
                        icon = Icons.Default.Check
                    )

                    Divider()

                    EcoTip(
                        tip = "Preferisci la bicicletta per brevi tragitti",
                        icon = Icons.Default.Check
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun EventCard(
    title: String,
    date: String,
    location: String
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelMedium,
                        color = Green600
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { /* Dettagli evento */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green600
                )
            ) {
                Text("Partecipa")
            }
        }
    }
}

@Composable
fun EcoTip(
    tip: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Green100,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Green600
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = tip,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Placeholder per la schermata di caricamento
 */
@Composable
fun LoadingScreenMap() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Green600)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rilevamento posizione...",
                style = MaterialTheme.typography.titleMedium,
                color = Green600
            )
        }
    }
}

@Composable
fun LocationErrorScreen(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Red.copy(alpha = 0.1f),
                        Color.White
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icona di errore
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Problema con la geolocalizzazione",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Suggerimenti
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Suggerimenti:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val suggestions = listOf(
                            "Assicurati che il GPS sia attivo",
                            "Controlla che l'app abbia i permessi di localizzazione",
                            "Prova a spostarti all'aperto per un segnale migliore",
                            "Riavvia l'app se il problema persiste"
                        )

                        suggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text(
                                    suggestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Pulsanti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Indietro")
                    }

                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Green600)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RIPROVA")
                    }
                }
            }
        }
    }
}

/**
 * AGGIUNTO: Schermata di caricamento con timeout per la posizione
 */
@Composable
fun LoadingLocationScreen(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        // Pulsante indietro in alto a sinistra
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
                .background(Color.White, CircleShape)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Indietro"
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Cerchio di caricamento
                CircularProgressIndicator(
                    color = Green600,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(80.dp)
                )

                // Icona GPS rotante
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Green600,
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(rotation.value)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Rilevamento posizione...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Green800
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sto cercando la tua posizione per trovare i punti di raccolta vicini",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Indicatore di progress
            LinearProgressIndicator(
                color = Green600,
                trackColor = Green100,
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

/**
 * MODIFICATO: Funzione per ottenere la posizione con timeout
 */
private fun getUserLocationWithTimeout(
    context: Context,
    onLocationReceived: (latitude: Double, longitude: Double) -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Prima prova con l'ultima posizione nota
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationReceived(location.latitude, location.longitude)
                    } else {
                        // Se non c'è una posizione salvata, richiedi aggiornamenti
                        requestLocationUpdatesWithTimeout(context, fusedLocationClient, onLocationReceived)
                    }
                }
                .addOnFailureListener {
                    // Se fallisce, prova con gli aggiornamenti
                    requestLocationUpdatesWithTimeout(context, fusedLocationClient, onLocationReceived)
                }
        }
    } catch (e: Exception) {
        Log.e("LocationUtil", "Error getting location", e)
        // In caso di errore, prova a ottenere comunque la posizione
        requestLocationUpdatesWithTimeout(context, LocationServices.getFusedLocationProviderClient(context), onLocationReceived)
    }
}

/**
 * MODIFICATO: Richiesta aggiornamenti posizione con timeout
 */
private fun requestLocationUpdatesWithTimeout(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (latitude: Double, longitude: Double) -> Unit,
    onTimeout: (() -> Unit)? = null // opzionale: callback in caso di timeout
) {
    try {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Priorità
            0L // Intervallo (millisecondi)
        ).apply {
            setMaxUpdates(1)
            setGranularity(Granularity.GRANULARITY_FINE)
        }.build()



        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

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

            // Timeout manuale dopo 15 secondi
            Handler(context.mainLooper).postDelayed({
                fusedLocationClient.removeLocationUpdates(locationCallback)
                onTimeout?.invoke() // opzionale: notifica del timeout
            }, 15000)
        } else {
            Log.e("LocationUtil", "Permesso di localizzazione non concesso")
        }
    } catch (e: Exception) {
        Log.e("LocationUtil", "Errore durante la richiesta della posizione", e)
    }
}
