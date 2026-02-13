@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.androidkiosk.ui.menu.components

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.androidkiosk.R
import com.example.androidkiosk.model.WeatherUiState
import com.example.androidkiosk.model.getWeatherDisplayLabel
import java.util.Locale

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@OptIn(UnstableApi::class)
@Composable
fun WeatherSection(weatherState: WeatherUiState) {
    val context = LocalContext.current

    val (titleText, subtitleText, videoResId) = when (weatherState) {
        is WeatherUiState.Loading -> Triple(
            "Cebu City • Updating...",
            "Fetching current conditions",
            R.raw.sunny_weather
        )
        is WeatherUiState.Error -> Triple(
            "Cebu City • Weather unavailable",
            "Showing default video",
            R.raw.sunny_weather
        )
        is WeatherUiState.Available -> {
            val temp = String.format(Locale.getDefault(), "%.0f", weatherState.temperatureC)
            val displayLabel = getWeatherDisplayLabel(weatherState.condition, weatherState.timeOfDay)
            Triple(
                "Cebu City • ${temp}°C • $displayLabel",
                "Visayas, Philippines",
                weatherState.timeOfDay.videoResId
            )
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
        }
    }

    LaunchedEffect(videoResId) {
        val videoUri = "android.resource://${context.packageName}/$videoResId".toUri()
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
