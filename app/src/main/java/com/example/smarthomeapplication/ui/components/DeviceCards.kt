package com.example.smarthomeapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smarthomeapplication.model.Device
import com.example.smarthomeapplication.model.DeviceStatus
import com.example.smarthomeapplication.model.DeviceType

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
                DeviceType.ELECTRICAL_OUTLET -> OutletControls(device, onStatusToggle)
                DeviceType.MULTI_SWITCH -> MultiSwitchControls(device, onMultiSwitchToggle)
                DeviceType.SAFETY_DEVICE -> SafetyDeviceControls(device, onStatusToggle)
                DeviceType.SECURITY_CAMERA -> CameraControls(device)
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
    device.switches?.forEach { (switchId, state) ->
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(state.name)
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

@Composable
fun SafetyDeviceControls(device: Device, onToggle: (DeviceStatus) -> Unit) {
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
fun CameraControls(device: Device) {
    Column {
        device.stream_url?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Camera Stream",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        } ?: Text("No stream URL available", color = Color.Gray)
    }
}
