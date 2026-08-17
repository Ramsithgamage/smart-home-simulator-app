package com.example.smarthomeapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smarthomeapplication.R
import com.example.smarthomeapplication.model.Device
import com.example.smarthomeapplication.model.DeviceStatus
import com.example.smarthomeapplication.model.DeviceType
import kotlinx.coroutines.delay

@Composable
fun DeviceCard(
    device: Device,
    onStatusToggle: (DeviceStatus) -> Unit,
    onMultiSwitchToggle: (String, DeviceStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = device.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            
            val statusColor = when (device.getDeviceStatus()) {
                DeviceStatus.ON -> Color(0xFF4CAF50)
                DeviceStatus.OFF -> Color.Gray
                DeviceStatus.ERROR -> Color.Red
                DeviceStatus.DISCONNECTED -> Color.DarkGray
            }
            Text(
                text = "Status: ${device.status}",
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (device.getDeviceTypeEnum()) {
                DeviceType.OUTLET -> OutletControls(device, onStatusToggle)
                DeviceType.MULTI_SWITCH -> MultiSwitchControls(device, onMultiSwitchToggle)
                DeviceType.SAFETY_APPLIANCE -> SafetyApplianceControls(device, onStatusToggle)
                DeviceType.SCHEDULED_APPLIANCE -> ScheduledApplianceControls(device)
                DeviceType.CAMERA -> CameraControls(device, onStatusToggle)
            }
        }
    }
}

@Composable
fun OutletControls(device: Device, onToggle: (DeviceStatus) -> Unit) {
    val isOn = device.getDeviceStatus() == DeviceStatus.ON
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Power")
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isOn,
            onCheckedChange = { isChecked ->
                onToggle(if (isChecked) DeviceStatus.ON else DeviceStatus.OFF)
            }
        )
    }
}

@Composable
fun MultiSwitchControls(
    device: Device,
    onMultiSwitchToggle: (String, DeviceStatus) -> Unit
) {
    val switches = device.switches?.toList()?.sortedBy { it.first } ?: emptyList()
    
    Column {
        switches.forEach { (switchId, state) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(state.name, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = state.status == DeviceStatus.ON.name,
                    onCheckedChange = { isChecked ->
                        onMultiSwitchToggle(switchId, if (isChecked) DeviceStatus.ON else DeviceStatus.OFF)
                    }
                )
            }
        }
    }
}

@Composable
fun SafetyApplianceControls(device: Device, onToggle: (DeviceStatus) -> Unit) {
    val isOn = device.getDeviceStatus() == DeviceStatus.ON
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Power")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isOn,
                onCheckedChange = { isChecked ->
                    onToggle(if (isChecked) DeviceStatus.ON else DeviceStatus.OFF)
                }
            )
        }
        device.max_on_duration_mins?.let { maxMins ->
            Text(
                text = "Max On Time: $maxMins mins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ScheduledApplianceControls(device: Device) {
    Column {
        Text("Schedule", style = MaterialTheme.typography.labelMedium)
        Row {
            Text("Start: ${device.start_time ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(16.dp))
            Text("End: ${device.end_time ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CameraControls(device: Device, onToggle: (DeviceStatus) -> Unit) {
    val isOn = device.getDeviceStatus() == DeviceStatus.ON
    val cameraFrames = listOf(
        R.drawable.camera_frame_1,
        R.drawable.camera_frame_2,
        R.drawable.camera_frame_3,
        R.drawable.camera_frame_4,
        R.drawable.camera_frame_5,
        R.drawable.camera_frame_6
    )
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isOn) {
        if (isOn) {
            while (true) {
                delay(5000L)
                currentFrameIndex = (currentFrameIndex + 1) % cameraFrames.size
            }
        }
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Power")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isOn,
                onCheckedChange = { isChecked ->
                    onToggle(if (isChecked) DeviceStatus.ON else DeviceStatus.OFF)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        if (isOn) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = cameraFrames[currentFrameIndex]),
                contentDescription = "Simulated Camera Feed",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                tint = Color.Unspecified
            )
            Text(
                text = "Live Simulation - Frame ${currentFrameIndex + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera is OFF",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
