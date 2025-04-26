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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.googleapp.presentation.theme.GoogleappTheme
import com.google.android.gms.wearable.*

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
            TimeText()

            if (authToken.isNotEmpty()) {
                // Show greeting if logged in
                Greeting(greetingName = username)
            } else {
                // Show orange square if not logged in
                Canvas(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                ) {
                    drawRect(Color(0xFFFF8800)) // Orange color
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
