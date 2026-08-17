package com.example.smarthomeapplication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthomeapplication.viewmodel.DeviceViewModel
import com.example.smarthomeapplication.model.DeviceType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageReportingScreen(
    viewModel: DeviceViewModel,
    onBackClick: () -> Unit,
) {
    var selectedFloorId by remember { mutableStateOf<String?>(null) }
    val floors by viewModel.floors.collectAsState()
    val allDevices by viewModel.allDevices.collectAsState()
    val usageReports by viewModel.usageReports.collectAsState()

    val handleBack = {
        if (selectedFloorId != null) {
            selectedFloorId = null
        } else {
            onBackClick()
        }
    }

    // Handle system back button
    BackHandler(onBack = handleBack)

    val selectedFloorName = floors.find { it.id == selectedFloorId }?.name ?: "Usage Reports"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedFloorName) },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (selectedFloorId == null) {
                FloorListView(
                    floors = floors,
                    onFloorClick = { selectedFloorId = it.id }
                )
            } else {
                val filteredReports = remember(selectedFloorId, usageReports, allDevices) {
                    val devicesInFloor = allDevices.filter { it.floor_id == selectedFloorId }
                    val deviceIdsInFloor = devicesInFloor.map { it.id }.toSet()
                    
                    usageReports.filter { report ->
                        report.device_id in deviceIdsInFloor && (
                            report.safety_cutoffs_triggered > 0 || 
                            report.total_data_used_gb > 0.0 ||
                            report.total_usage_time_ms > 0 ||
                            report.last_turn_on_timestamp != null
                        )
                    }.mapNotNull { report ->
                        val device = devicesInFloor.find { it.id == report.device_id }
                        if (device != null) {
                            device to report
                        } else null
                    }
                }

                ReportListView(reports = filteredReports)
            }
        }
    }
}

@Composable
fun FloorListView(
    floors: List<com.example.smarthomeapplication.model.Floor>,
    onFloorClick: (com.example.smarthomeapplication.model.Floor) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(floors) { floor ->
            ListItem(
                headlineContent = { Text(floor.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFloorClick(floor) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun ReportListView(
    reports: List<Pair<com.example.smarthomeapplication.model.Device, com.example.smarthomeapplication.model.DeviceUsageReport>>
) {
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reports for this floor")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(reports) { (device, report) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = device.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    when (device.getDeviceTypeEnum()) {
                        DeviceType.SAFETY_APPLIANCE -> {
                            Text(
                                text = "Safety cutoffs triggered: ${report.safety_cutoffs_triggered} times",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        DeviceType.CAMERA -> {
                            Text(
                                text = "Total data used: ${String.format("%.2f", report.total_data_used_gb)} GB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        DeviceType.OUTLET -> {
                            OutletUsageText(report)
                        }
                        else -> {
                            // Should not happen based on filtering logic
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun OutletUsageText(report: com.example.smarthomeapplication.model.DeviceUsageReport) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    // Update time every 10 seconds for real-time reporting
    LaunchedEffect(report.last_turn_on_timestamp) {
        if (report.last_turn_on_timestamp != null) {
            while (true) {
                delay(10000)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val totalMs = if (report.last_turn_on_timestamp != null) {
        val currentSession = currentTime - report.last_turn_on_timestamp
        report.total_usage_time_ms + currentSession
    } else {
        report.total_usage_time_ms
    }

    // Log the values to debug
    android.util.Log.d("UsageReportingScreen", "Device: ${report.device_id}, totalMs: $totalMs, report.total_usage_time_ms: ${report.total_usage_time_ms}")

    val hours = totalMs / (1000 * 60 * 60)
    val minutes = (totalMs % (1000 * 60 * 60)) / (1000 * 60)

    Text(
        text = "Total used time: ${hours}h ${minutes}m",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.tertiary
    )
}
