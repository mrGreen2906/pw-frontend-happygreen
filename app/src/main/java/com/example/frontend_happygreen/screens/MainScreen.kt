package com.example.frontend_happygreen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.games.EcoDetectiveGameScreen
import com.example.frontend_happygreen.ui.theme.FrontendhappygreenTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Class data structure
data class ClassRoom(
    val name: String,
    val backgroundImageId: Int,
    val teacherName: String = "John Doe"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    volumeLevel: Float = 0.5f,
    onVolumeChange: (Float) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    val tabItems = listOf("Home", "Profile", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Person, Icons.Default.Settings)

    // State for profile dialog
    var showProfileDialog by remember { mutableStateOf(false) }

    // State for showing create class dialog
    var showCreateClassDialog by remember { mutableStateOf(false) }

    // State for showing game screen
    var showEcoDetectiveGame by remember { mutableStateOf(false) }

    // State for showing/hiding games banner
    var showGamesBanner by remember { mutableStateOf(true) }

    // State for loading data
    var isLoading by remember { mutableStateOf(true) }

    // Performance optimization: Memoize these lists to prevent recomposition
    // List of available games
    val gamesList = remember {
        listOf(
            Triple("EcoDetective", "Sort waste into the correct bins", R.drawable.happy_green_logo),
            Triple("Green Quiz", "Test your environmental knowledge", R.drawable.happy_green_logo),
            Triple("Tree Planter", "Virtual tree planting simulator", R.drawable.happy_green_logo)
        )
    }

    // Sample class list (would be loaded from a database in a real app)
    var classList by remember { mutableStateOf(listOf<ClassRoom>()) }

    // Simulate loading data from database in background thread
    LaunchedEffect(key1 = Unit) {
        withContext(Dispatchers.IO) {
            // Simulating data loading delay
            kotlinx.coroutines.delay(100)
            val loadedClasses = listOf(
                ClassRoom("Eco Science 101", R.drawable.happy_green_logo),
                ClassRoom("Environmental Studies", R.drawable.happy_green_logo),
                ClassRoom("Green Technologies", R.drawable.happy_green_logo)
            )

            // Update UI on main thread
            withContext(Dispatchers.Main) {
                classList = loadedClasses
                isLoading = false
            }
        }
    }

    if (showEcoDetectiveGame) {
        // Show the EcoDetective game screen
        EcoDetectiveGameScreen(
            onBack = { showEcoDetectiveGame = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // App Logo
                            Image(
                                painter = painterResource(id = R.drawable.happy_green_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // App Name
                            Text(
                                text = "HappyGreen",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Profile Picture
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                                    .border(1.dp, Color.White, CircleShape)
                                    .clickable { showProfileDialog = true }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.happy_green_logo),
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    tabItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { showCreateClassDialog = true },
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Class",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White),
            ) {
                when (selectedTab) {
                    0 -> HomeContent(
                        classList = classList,
                        onClassSelected = { /* Handle class selection */ }
                    )
                    1 -> ProfileContent()
                    2 -> SettingsContent()
                }

                // Games Banner at the bottom of the screen
                if (showGamesBanner) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Games",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.padding(start = 8.dp)
                                )

                                IconButton(
                                    onClick = {
                                        // Use coroutine to prevent UI blocking
                                        coroutineScope.launch {
                                            showGamesBanner = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Games Banner",
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            }

                            // Horizontal scrollable games list with optimized rendering
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            ) {
                                items(
                                    items = gamesList,
                                    key = { (name, _, _) -> name } // Use unique key for better performance
                                ) { (name, description, iconId) ->
                                    GameCardCompact(
                                        name = name,
                                        description = description,
                                        iconId = iconId,
                                        onClick = {
                                            // Handle game launching in a coroutine to prevent UI blocking
                                            coroutineScope.launch {
                                                when (name) {
                                                    "EcoDetective" -> showEcoDetectiveGame = true
                                                    // Add other game cases when implemented
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Profile Dialog
            if (showProfileDialog) {
                Dialog(onDismissRequest = { showProfileDialog = false }) {
                    ProfileDialog(
                        volumeLevel = volumeLevel,
                        onVolumeChange = onVolumeChange,
                        onDismiss = { showProfileDialog = false },
                        onLogout = {
                            showProfileDialog = false
                            onLogout()
                        }
                    )
                }
            }

            // Create Class Dialog
            if (showCreateClassDialog) {
                CreateClassDialog(
                    onDismiss = { showCreateClassDialog = false },
                    onClassCreated = { newClass ->
                        classList = classList + newClass
                        showCreateClassDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    classList: List<ClassRoom>,
    onClassSelected: (ClassRoom) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome header
        Text(
            text = "Welcome to HappyGreen!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join eco-friendly classes and earn Green Points!",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Classes section
        Text(
            text = "Your Classes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Classes list with key for better recomposition
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = classList,
                key = { it.name } // Use unique key for better performance
            ) { classRoom ->
                ClassCard(
                    classRoom = classRoom,
                    onClick = { onClassSelected(classRoom) }
                )
            }
        }
    }
}

@Composable
fun ClassCard(
    classRoom: ClassRoom,
    onClick: () -> Unit
) {
    // Use remember to prevent unnecessary recompositions
    val cardShape = remember { RoundedCornerShape(12.dp) }
    val overlayColor = remember { Color.Black.copy(alpha = 0.4f) }
    val textSecondaryColor = remember { Color.White.copy(alpha = 0.8f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = cardShape
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image - optimize rendering
            Image(
                painter = painterResource(id = classRoom.backgroundImageId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Semi-transparent overlay for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor)
            )

            // Class info - optimize text rendering
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = classRoom.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Teacher: ${classRoom.teacherName}",
                    fontSize = 14.sp,
                    color = textSecondaryColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun GameCardCompact(
    name: String,
    description: String,
    iconId: Int,
    onClick: () -> Unit
) {
    // Use remember to prevent unnecessary recompositions
    val cardShape = remember { RoundedCornerShape(12.dp) }
    val boxShape = remember { RoundedCornerShape(8.dp) }
    val iconSize = remember { 50.dp }
    val boxSize = remember { 70.dp }

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = cardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Game icon - simplified layout
            Box(
                modifier = Modifier
                    .size(boxSize)
                    .clip(boxShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                // Optimize image loading
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = name,
                    modifier = Modifier.size(iconSize)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game info with optimized text rendering
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                softWrap = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassDialog(
    onDismiss: () -> Unit,
    onClassCreated: (ClassRoom) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var className by remember { mutableStateOf("") }
    var selectedBackgroundIndex by remember { mutableStateOf(0) }
    var showBackgroundOptions by remember { mutableStateOf(false) }

    // Pre-compute and remember values to reduce object creation during recomposition
    // Background options (in a real app, these would be actual different images)
    val backgroundOptions = remember {
        listOf(
            R.drawable.happy_green_logo,
            R.drawable.happy_green_logo,
            R.drawable.happy_green_logo,
            R.drawable.happy_green_logo
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create New Class",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Class name input
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Background selection
                Text(
                    text = "Choose Background",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preview of selected background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showBackgroundOptions = true }
                ) {
                    Image(
                        painter = painterResource(id = backgroundOptions[selectedBackgroundIndex]),
                        contentDescription = "Selected Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                    )

                    // Text indicating this is clickable
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap to change background",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Background options dropdown
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenu(
                        expanded = showBackgroundOptions,
                        onDismissRequest = { showBackgroundOptions = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        backgroundOptions.forEachIndexed { index, backgroundId ->
                            DropdownMenuItem(
                                text = { Text("Background ${index + 1}") },
                                leadingIcon = {
                                    Image(
                                        painter = painterResource(id = backgroundId),
                                        contentDescription = "Background ${index + 1}",
                                        modifier = Modifier.size(40.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                onClick = {
                                    selectedBackgroundIndex = index
                                    showBackgroundOptions = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Create button
                Button(
                    onClick = {
                        if (className.isNotBlank()) {
                            // Use coroutine to prevent UI blocking
                            coroutineScope.launch {
                                val newClass = ClassRoom(
                                    name = className,
                                    backgroundImageId = backgroundOptions[selectedBackgroundIndex]
                                )
                                onClassCreated(newClass)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = className.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Create"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Create Class",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Content",
            fontSize = 20.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SettingsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings Content",
            fontSize = 20.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileDialog(
    volumeLevel: Float,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Profile Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(2.dp, Color(0xFF4CAF50), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.happy_green_logo),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = "John Doe",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User Email
            Text(
                text = "john.doe@example.com",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Green Points
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Green Points",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "1250",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )

                    Text(
                        text = "Level: Eco Warrior",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Music Volume Control
            Text(
                text = "Background Music",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Volume Down",
                    tint = Color(0xFF4CAF50)
                )

                Slider(
                    value = volumeLevel,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4CAF50),
                        activeTrackColor = Color(0xFFAED581),
                        inactiveTrackColor = Color(0xFFE8F5E9)
                    )
                )

                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Volume Up",
                    tint = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Logout",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FrontendhappygreenTheme {
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileDialogPreview() {
    FrontendhappygreenTheme {
        Surface {
            ProfileDialog(
                volumeLevel = 0.5f,
                onVolumeChange = {},
                onDismiss = {},
                onLogout = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    FrontendhappygreenTheme {
        Surface {
            HomeContent(
                classList = listOf(
                    ClassRoom("Eco Science 101", R.drawable.happy_green_logo),
                    ClassRoom("Environmental Studies", R.drawable.happy_green_logo)
                ),
                onClassSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateClassDialogPreview() {
    FrontendhappygreenTheme {
        Surface {
            CreateClassDialog(
                onDismiss = {},
                onClassCreated = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameBannerPreview() {
    FrontendhappygreenTheme {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9))
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Games",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Games Banner",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                    ) {
                        items(3) { index ->
                            GameCardCompact(
                                name = "Game $index",
                                description = "Game description",
                                iconId = R.drawable.happy_green_logo,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}