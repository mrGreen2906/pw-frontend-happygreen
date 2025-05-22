package com.example.frontend_happygreen.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.ui.theme.Green100
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EcoAIChatScreen(
    onBack: () -> Unit,
    jotformAgentUrl: String = "https://eu.jotform.com/agent/0196f2287b19770a882e00f231a07d2c754c"
) {
    var isLoading by remember { mutableStateOf(true) }
    var webViewError by remember { mutableStateOf<String?>(null) }

    // Welcome dialog
    var showWelcomeDialog by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = "EcoAI",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EcoAI - Esperta Ambientale",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
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
                .background(Color(0xFFF5F5F5))
        ) {
            // Gestisce eventuali errori di caricamento
            webViewError?.let { error ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Si è verificato un errore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { webViewError = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green600
                            )
                        ) {
                            Text("Riprova")
                        }
                    }
                }
                return@Box
            }

            // WebView per l'integrazione dell'agente Jotform
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Configurazione WebView
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            setSupportMultipleWindows(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            setSupportZoom(false)
                            textZoom = 100
                        }

                        // WebViewClient per gestire eventi di caricamento
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false

                                // Applica CSS per adattare l'agente alla nostra app e ai colori HappyGreen
                                val cssScript = """
                                    javascript:(function() {
                                        var style = document.createElement('style');
                                        style.type = 'text/css';
                                        style.innerHTML = `
                                            body { 
                                                background-color: #F5F5F5; 
                                                font-family: 'Roboto', sans-serif;
                                            }
                                            .jfCard-wrapper { 
                                                max-width: 100% !important; 
                                                margin: 0 !important;
                                                box-shadow: none !important;
                                            }
                                            .jfCardForm { 
                                                border-radius: 10px !important; 
                                                box-shadow: 0 2px 10px rgba(0,0,0,0.08) !important;
                                            }
                                            
                                            /* Personalizzazione colori EcoAI */
                                            .jfa-button { 
                                                background-color: #4CAF50 !important; 
                                                border-color: #4CAF50 !important;
                                            }
                                            .jfa-assistant-bubble { 
                                                background-color: #4CAF50 !important; 
                                            }
                                            .jfa-assistant-name { 
                                                color: #2E7D32 !important; 
                                                font-weight: bold !important;
                                            }
                                            .jfa-user-bubble { 
                                                background-color: #E8F5E9 !important; 
                                                border-color: #A5D6A7 !important;
                                            }
                                            .jfa-send-button { 
                                                color: #2E7D32 !important; 
                                            }
                                            .jfa-input-field { 
                                                border-color: #A5D6A7 !important; 
                                            }
                                            .jfa-input-field:focus { 
                                                border-color: #4CAF50 !important; 
                                                box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2) !important;
                                            }
                                        `;
                                        document.head.appendChild(style);
                                    })()
                                """.trimIndent()

                                view?.loadUrl(cssScript)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                webViewError = error?.description?.toString() ?: "Errore di connessione"
                                isLoading = false
                            }
                        }

                        // Carica l'URL dell'agente Jotform
                        loadUrl(jotformAgentUrl)
                    }
                },
                update = { /* Nessun aggiornamento necessario */ }
            )

            // Indicatore di caricamento personalizzato in stile HappyGreen
            AnimatedVisibility(visible = isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5).copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    EcoLoadingIndicator()
                }
            }
        }
    }

    // Dialog di benvenuto
    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = { showWelcomeDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assistant,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(40.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Benvenuto/a in EcoAI!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Sono la tua assistente virtuale esperta in temi ambientali. Puoi chiedermi informazioni su:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "Raccolta differenziata e riciclo",
                        "Gestione dei rifiuti",
                        "Inquinamento e sostenibilità",
                        "Consigli per uno stile di vita eco-friendly"
                    ).forEach { topic ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Green600)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(topic)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWelcomeDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600
                    )
                ) {
                    Text("Inizia a chattare")
                }
            }
        )
    }
}

/**
 * Indicatore di caricamento personalizzato con il logo HappyGreen
 */
@Composable
fun EcoLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Cerchio verde di sfondo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Green100)
            )

            // Logo rotante
            Image(
                painter = painterResource(id = R.drawable.happy_green_logo),
                contentDescription = "Loading",
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotation.value)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Caricamento EcoAI...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Green800
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sto preparando le mie conoscenze ambientali",
            style = MaterialTheme.typography.bodyMedium,
            color = Green600
        )
    }
}