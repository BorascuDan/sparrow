package com.example.googleapp.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.ambient.AmbientModeSupport
import com.example.googleapp.R
import com.example.googleapp.api.RetrofitClient
import com.example.googleapp.models.ApiResponse
import com.example.googleapp.models.TemperatureResponse
import com.example.googleapp.presentation.theme.GoogleappTheme
import com.google.android.gms.wearable.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.fragment.app.FragmentActivity
import kotlin.random.Random

class MainActivity : androidx.fragment.app.FragmentActivity(), DataClient.OnDataChangedListener,
    AmbientModeSupport.AmbientCallbackProvider {

    private var authToken by mutableStateOf("")
    private var username by mutableStateOf("")
    private var temperature by mutableStateOf(0.0f)
    private var drinkCount by mutableStateOf(0)
    private var isLoading by mutableStateOf(true)
    private var errorMessage by mutableStateOf("")
    private var isAmbient by mutableStateOf(false)

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize ambient mode
        ambientController = AmbientModeSupport.attach(this)

        // Initial content
        setContent {
            WearApp(
                authToken = authToken,
                username = username,
                temperature = temperature,
                drinkCount = drinkCount,
                isLoading = isLoading,
                errorMessage = errorMessage,
                isAmbient = isAmbient
            )
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
        return object : AmbientModeSupport.AmbientCallback() {
            override fun onEnterAmbient(ambientDetails: Bundle) {
                super.onEnterAmbient(ambientDetails)
                isAmbient = true
                updateUI()
            }

            override fun onExitAmbient() {
                super.onExitAmbient()
                isAmbient = false
                updateUI()
            }

            override fun onUpdateAmbient() {
                super.onUpdateAmbient()
                // Refresh data periodically if needed in ambient mode
                // For example, update every few minutes
            }
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

                    if (authToken.isNotEmpty()) {
                        // Load user data when auth token is received
                        fetchUserData()
                    }

                    // Update UI with new data
                    setContent {
                        WearApp(
                            authToken = authToken,
                            username = username,
                            temperature = temperature,
                            drinkCount = drinkCount,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            isAmbient = isAmbient
                        )
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

                        if (authToken.isNotEmpty()) {
                            // Load user data when auth token is found
                            fetchUserData()
                        }

                        // Update UI with stored data
                        setContent {
                            WearApp(
                                authToken = authToken,
                                username = username,
                                temperature = temperature,
                                drinkCount = drinkCount,
                                isLoading = isLoading,
                                errorMessage = errorMessage,
                                isAmbient = isAmbient
                            )
                        }
                    }
                }

                dataItems.release()
            }
    }

    private fun fetchUserData() {
        isLoading = true
        errorMessage = ""

        // Make API calls to fetch user data
        fetchTemperature()
        fetchDrinkCount()
    }

    private fun fetchTemperature() {
        RetrofitClient.instance.getLastTemp("Bearer $authToken").enqueue(object : Callback<TemperatureResponse> {
            override fun onResponse(call: Call<TemperatureResponse>, response: Response<TemperatureResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val tempResponse = response.body()!!
                    if (tempResponse.success) {
                        temperature = tempResponse.data
                    } else {
                        errorMessage = "Temperature error: ${tempResponse.message}"
                    }
                } else {
                    errorMessage = "Temperature fetch failed: ${response.message()}"
                }
                isLoading = false
                updateUI()
            }

            override fun onFailure(call: Call<TemperatureResponse>, t: Throwable) {
                errorMessage = "Temperature fetch error: ${t.message}"
                isLoading = false
                updateUI()
            }
        })
    }

    private fun fetchDrinkCount() {
        RetrofitClient.instance.getTotalDrinks("Bearer $authToken").enqueue(object : Callback<ApiResponse<Int>> {
            override fun onResponse(call: Call<ApiResponse<Int>>, response: Response<ApiResponse<Int>>) {
                if (response.isSuccessful && response.body() != null) {
                    val drinkResponse = response.body()!!
                    if (drinkResponse.success) {
                        drinkCount = drinkResponse.data ?: 0
                    } else {
                        errorMessage = "Drink count error: ${drinkResponse.message}"
                    }
                } else {
                    errorMessage = "Drink count fetch failed: ${response.message()}"
                }
                isLoading = false
                updateUI()
            }

            override fun onFailure(call: Call<ApiResponse<Int>>, t: Throwable) {
                errorMessage = "Drink count fetch error: ${t.message}"
                isLoading = false
                updateUI()
            }
        })
    }

    private fun updateUI() {
        setContent {
            WearApp(
                authToken = authToken,
                username = username,
                temperature = temperature,
                drinkCount = drinkCount,
                isLoading = isLoading,
                errorMessage = errorMessage,
                isAmbient = isAmbient
            )
        }
    }
}

@Composable
fun WearApp(
    authToken: String,
    username: String,
    temperature: Float,
    drinkCount: Int,
    isLoading: Boolean,
    errorMessage: String,
    isAmbient: Boolean
) {
    GoogleappTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isAmbient) Color.Black else MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            if (authToken.isNotEmpty()) {
                // Logged in view with user info
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isLoading && !isAmbient) {
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            color = if (isAmbient) Color.Gray else Color.White
                        )
                    } else if (errorMessage.isNotEmpty() && !isAmbient) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            color = if (isAmbient) Color.Gray else Color.Red,
                            fontSize = 10.sp
                        )
                    } else {
                        // Greeting with username (simplified in ambient mode)
                        if (!isAmbient) {
                            Text(
                                text = "Hello,",
                                style = MaterialTheme.typography.body1,
                                textAlign = TextAlign.Center,
                                color = if (isAmbient) Color.Gray else Color.White,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = username,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (isAmbient) Color.Gray else Color.White,
                            fontSize = if (isAmbient) 14.sp else 16.sp
                        )
                        Spacer(modifier = Modifier.height(if (isAmbient) 12.dp else 16.dp))

                        // Drink count with beer icon (simplified in ambient mode)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (!isAmbient) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_beer),
                                    contentDescription = "Beer",
                                    tint = if (isAmbient) Color.Gray else Color(0xFFE8A517),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isAmbient) "Drinks: $drinkCount" else "$drinkCount",
                                color = if (isAmbient) Color.Gray else Color.White,
                                fontSize = if (isAmbient) 14.sp else 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(if (isAmbient) 8.dp else 12.dp))

                        // Temperature with thermometer icon (simplified in ambient mode)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (!isAmbient) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_thermometer),
                                    contentDescription = "Temperature",
                                    tint = if (isAmbient) Color.Gray else Color(0xFFFF5252),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isAmbient) "Temp: $temperature°C" else "$temperature°C",
                                color = if (isAmbient) Color.Gray else Color.White,
                                fontSize = if (isAmbient) 14.sp else 16.sp
                            )
                        }
                    }
                }
            } else {
                // Show simplified message in ambient mode if not logged in
                if (isAmbient) {
                    Text(
                        text = "Not logged in",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Show beer glass if not logged in (regular mode)
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // (Keep your existing Canvas code here)
                        // ...
                    }

                    // Text overlay for not logged in state
                    Text(
                        text = "Please log in",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    )
                }
            }
        }
    }
}