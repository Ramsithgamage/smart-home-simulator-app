package com.example.smarthomeapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.smarthomeapplication.viewmodel.DeviceViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageReportingScreen(
    viewModel: DeviceViewModel,
    onBackClick: () -> Unit,
) {
    val usageLogs by viewModel.usageLogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usage Reports") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Device Usage Duration (Minutes)", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Aggregate data: Sum of duration per event (or per device if we had device info in logs, for simplicity we plot duration)
            // Just for demonstration of the canvas chart, we'll plot the top 5 durations
            val chartData = usageLogs.asSequence().take(5).map { it.duration_mins.toFloat() }.toList()

            if (chartData.isNotEmpty()) {
                BarChart(data = chartData, modifier = Modifier.fillMaxWidth().height(250.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                    Text("No usage logs available")
                }
            }
        }
    }
}

@Composable
fun BarChart(data: List<Float>, modifier: Modifier = Modifier) {
    val maxValue = max(data.maxOrNull() ?: 0f, 10f) // Prevent divide by zero

    Canvas(modifier = modifier.padding(16.dp).background(Color.White)) {
        val barWidth = size.width / (data.size * 2)
        data.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height
            val xOffset = index * (barWidth * 2) + barWidth / 2
            val yOffset = size.height - barHeight

            drawRect(
                color = Color(0xFF2196F3),
                topLeft = Offset(xOffset, yOffset),
                size = Size(barWidth, barHeight)
            )
            
            // Draw value text
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(value.toInt().toString(), xOffset + barWidth / 2, yOffset - 10f, paint)
            }
        }
        
        // Draw X axis
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f
        )
    }
}
