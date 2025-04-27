package com.example.googleapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.googleapp.presentation.theme.GoogleappTheme
import com.google.android.gms.wearable.*
import kotlin.random.Random

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private var authToken by mutableStateOf("")
    private var username by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // Initial content
        setContent {
            WearApp(authToken, username)
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
        // Request latest data on resume
        fetchStoredAuthData()
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)

                if (event.dataItem.uri.path == "/auth_status") {
                    authToken = dataMapItem.dataMap.getString("auth_token") ?: ""
                    username = dataMapItem.dataMap.getString("username") ?: ""

                    // Update UI with new data
                    setContent {
                        WearApp(authToken, username)
                    }
                }
            }
        }
    }

    private fun fetchStoredAuthData() {
        Wearable.getDataClient(this).dataItems
            .addOnSuccessListener { dataItems ->
                dataItems.forEach { dataItem ->
                    if (dataItem.uri.path == "/auth_status") {
                        val dataMapItem = DataMapItem.fromDataItem(dataItem)
                        authToken = dataMapItem.dataMap.getString("auth_token") ?: ""
                        username = dataMapItem.dataMap.getString("username") ?: ""

                        // Update UI with stored data
                        setContent {
                            WearApp(authToken, username)
                        }
                    }
                }

                dataItems.release()
            }
    }
}

@Composable
fun WearApp(authToken: String, username: String) {
    GoogleappTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            if (authToken.isNotEmpty()) {
                // Show greeting if logged in
                Greeting(greetingName = username)
            } else {
                // Show orange square if not logged in
                Canvas(
                    modifier = Modifier
                        .fillMaxSize() // Fill the entire screen
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Define pint glass dimensions
                    val glassWidth = canvasWidth * 0.3f // 30% of screen width
                    val glassHeight = glassWidth * 1.6f // Proportional height
                    val glassThickness = glassWidth * 0.05f

                    // Center coordinates
                    val centerX = canvasWidth / 2
                    val centerY = canvasHeight / 2

                    // Glass top-left position (centered)
                    val glassLeft = centerX - (glassWidth / 2)
                    val glassTop = centerY - (glassHeight / 2)

                    // Draw glass - slightly transparent
                    drawRoundRect(
                        color = Color(0x80D0D0D0), // Semi-transparent glass
                        topLeft = Offset(glassLeft, glassTop),
                        size = Size(glassWidth, glassHeight),
                        cornerRadius = CornerRadius(glassWidth * 0.1f, glassWidth * 0.1f)
                    )

                    // Draw inner glass area
                    drawRoundRect(
                        color = Color(0x30FFFFFF), // Very light transparency
                        topLeft = Offset(glassLeft + glassThickness, glassTop + glassThickness),
                        size = Size(glassWidth - (glassThickness * 2), glassHeight - (glassThickness * 2)),
                        cornerRadius = CornerRadius(glassWidth * 0.08f, glassWidth * 0.08f)
                    )

                    // Beer liquid - fills 85% of glass
                    val beerHeight = (glassHeight - (glassThickness * 2)) * 0.85f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE8A517), // Lighter top
                                Color(0xFFD78500)  // Darker bottom
                            )
                        ),
                        topLeft = Offset(glassLeft + glassThickness, glassTop + glassThickness),
                        size = Size(glassWidth - (glassThickness * 2), beerHeight),
                        cornerRadius = CornerRadius(glassWidth * 0.08f, glassWidth * 0.08f)
                    )

                    // Foam layer
                    val foamHeight = (glassHeight - (glassThickness * 2)) * 0.15f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFAF0), // Top cream color
                                Color(0xFFF5E7C6)  // Bottom cream color
                            )
                        ),
                        topLeft = Offset(glassLeft + glassThickness, glassTop + glassThickness),
                        size = Size(glassWidth - (glassThickness * 2), foamHeight),
                        cornerRadius = CornerRadius(glassWidth * 0.08f, 0f)
                    )

                    // Highlight on glass (reflection)
                    val highlightWidth = glassWidth * 0.1f
                    drawRoundRect(
                        color = Color(0x40FFFFFF),
                        topLeft = Offset(glassLeft + glassWidth * 0.15f, glassTop + glassThickness),
                        size = Size(highlightWidth, glassHeight * 0.7f),
                        cornerRadius = CornerRadius(highlightWidth / 2, highlightWidth / 2)
                    )

                    // Add bubbles in beer
                    val random = Random(42) // Fixed seed for consistent bubbles
                    repeat(20) {
                        val bubbleSize = (3..8).random(random).toFloat()
                        val bubbleX = glassLeft + glassThickness + random.nextFloat() * (glassWidth - glassThickness * 2 - bubbleSize)
                        val bubbleY = glassTop + glassThickness + random.nextFloat() * (beerHeight - bubbleSize)

                        drawCircle(
                            color = Color(0x50FFFFFF),
                            radius = bubbleSize,
                            center = Offset(bubbleX, bubbleY)
                        )
                    }

                    // Add foam details
                    repeat(15) {
                        val foamBubbleSize = (4..12).random(random).toFloat()
                        val foamX = glassLeft + glassThickness + random.nextFloat() * (glassWidth - glassThickness * 2 - foamBubbleSize)
                        val foamY = glassTop + glassThickness + random.nextFloat() * foamHeight

                        drawCircle(
                            color = Color(0xB0FFFBF0),
                            radius = foamBubbleSize,
                            center = Offset(foamX, foamY)
                        )
                    }

                    // Glass base
                    val baseWidth = glassWidth * 0.8f
                    val baseHeight = glassHeight * 0.1f
                    drawRoundRect(
                        color = Color(0xC0D0D0D0),
                        topLeft = Offset(centerX - (baseWidth / 2), glassTop + glassHeight),
                        size = Size(baseWidth, baseHeight),
                        cornerRadius = CornerRadius(baseWidth * 0.1f, baseWidth * 0.1f)
                    )

                    // Glass rim highlight
                    drawRect(
                        color = Color(0x30FFFFFF),
                        topLeft = Offset(glassLeft + glassThickness, glassTop + glassThickness),
                        size = Size(glassWidth - (glassThickness * 2), glassThickness)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = "Hello, $greetingName!"
    )
}