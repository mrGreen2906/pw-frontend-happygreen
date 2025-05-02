package com.example.frontend_happygreen.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend_happygreen.R
import com.example.frontend_happygreen.ui.theme.Green300
import com.example.frontend_happygreen.ui.theme.Green600
import com.example.frontend_happygreen.ui.theme.Green800

@Composable
fun WelcomeScreen(onGetStartedClick: () -> Unit) {
    // Animazioni
    val infiniteTransition = rememberInfiniteTransition()

    // Animazione pulsante
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animazione logo
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Sfumatura di sfondo dinamica
    val gradientColors = listOf(Green800, Green600, Green300)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        // Decorazioni di sfondo (cerchi)
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = -50.dp)
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .align(Alignment.BottomStart)
                .offset(x = -50.dp, y = 50.dp)
        )

        // Contenuto principale
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con animazione - senza background bianco
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .rotate(logoRotation)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.happy_green_logo_round),
                    contentDescription = "HappyGreen Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit  // Assicura che l'immagine si adatti senza distorsioni
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Titolo con sfondo
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
                ) {
                    Text(
                        text = "HappyGreen",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green800
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Make our planet greener one step at a time",
                        fontSize = 16.sp,
                        color = Green600,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Pulsante con animazione
            Button(
                onClick = onGetStartedClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .scale(buttonScale)
                    .height(56.dp)
                    .fillMaxWidth(0.8f),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    text = "Get Started",
                    color = Green600,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indicazione animata per attirare l'attenzione
            Text(
                text = "Tap to join the green revolution",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}