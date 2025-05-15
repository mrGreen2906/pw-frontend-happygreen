package com.example.frontend_happygreen.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend_happygreen.ui.components.CenteredLoader
import com.example.frontend_happygreen.ui.theme.Green600
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSearchScreen(
    onBack: () -> Unit,
    viewModel: MainScreenViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<ClassRoom>>(emptyList()) }
    var searchError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Stato per il dialogo di conferma
    var showJoinDialog by remember { mutableStateOf<ClassRoom?>(null) }

    // Stato per feedback operazione
    var operationFeedback by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    // Dialogo di conferma per unirsi al gruppo
    showJoinDialog?.let { group ->
        AlertDialog(
            onDismissRequest = { showJoinDialog = null },
            title = { Text("Unisciti al gruppo") },
            text = {
                Text("Vuoi davvero unirti al gruppo \"${group.name}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Esegui l'operazione di unirsi al gruppo
                        coroutineScope.launch {
                            viewModel.joinGroup(
                                groupId = group.id,
                                onSuccess = {
                                    operationFeedback = "Ti sei unito al gruppo con successo!" to true
                                    showJoinDialog = null
                                },
                                onError = { error ->
                                    operationFeedback = error to false
                                    showJoinDialog = null
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = null }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Feedback operazione
    operationFeedback?.let { (message, isSuccess) ->
        AlertDialog(
            onDismissRequest = { operationFeedback = null },
            title = { Text(if (isSuccess) "Operazione riuscita" else "Errore") },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = { operationFeedback = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) Green600 else Color.Red
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cerca Gruppi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Campo di ricerca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cerca gruppi") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Cancella"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pulsante di ricerca
            Button(
                onClick = {
                    // Esegui la ricerca solo se la query non è vuota
                    if (searchQuery.isNotEmpty()) {
                        isSearching = true
                        searchError = null

                        coroutineScope.launch {
                            viewModel.searchGroups(
                                query = searchQuery,
                                onSuccess = { results ->
                                    searchResults = results
                                    isSearching = false
                                },
                                onError = { error ->
                                    searchError = error
                                    isSearching = false
                                }
                            )
                        }
                    }
                },
                enabled = searchQuery.isNotEmpty() && !isSearching,
                colors = ButtonDefaults.buttonColors(containerColor = Green600),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerca")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Risultati della ricerca
            when {
                isSearching -> {
                    CenteredLoader(message = "Ricerca in corso...")
                }

                searchError != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Errore nella ricerca",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = searchError!!)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // Riprova la ricerca
                                    isSearching = true
                                    searchError = null

                                    coroutineScope.launch {
                                        viewModel.searchGroups(
                                            query = searchQuery,
                                            onSuccess = { results ->
                                                searchResults = results
                                                isSearching = false
                                            },
                                            onError = { error ->
                                                searchError = error
                                                isSearching = false
                                            }
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Green600)
                            ) {
                                Text("Riprova")
                            }
                        }
                    }
                }

                searchResults.isEmpty() && searchQuery.isNotEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun risultato trovato per \"$searchQuery\"",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                searchResults.isNotEmpty() -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults) { group ->
                            GroupSearchItem(
                                group = group,
                                onJoinClick = {
                                    showJoinDialog = group
                                }
                            )
                        }
                    }
                }

                else -> {
                    // Nessuna ricerca ancora effettuata
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inserisci il nome di un gruppo per iniziare la ricerca",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupSearchItem(
    group: ClassRoom,
    onJoinClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Immagine del gruppo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = group.backgroundImageId),
                    contentDescription = group.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informazioni del gruppo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (group.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.teacherName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Pulsante per unirsi
            Button(
                onClick = onJoinClick,
                colors = ButtonDefaults.buttonColors(containerColor = Green600)
            ) {
                Text("Unisciti")
            }
        }
    }
}

