package com.example.smarthomeapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthomeapplication.model.Device
import com.example.smarthomeapplication.model.DeviceType
import com.example.smarthomeapplication.ui.components.DeviceCard
import com.example.smarthomeapplication.ui.components.FloorPlanView
import com.example.smarthomeapplication.viewmodel.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveFloorScreen(
    floorId: String,
    viewModel: DeviceViewModel,
    onBackClick: () -> Unit
) {
    val floors by viewModel.floors.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val floor = floors.find { it.id == floorId }

    // State for bottom sheet
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(floorId) {
        viewModel.selectFloor(floorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(floor?.name ?: "Floor Plan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDeviceDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Device")
            }
        }
    ) { padding ->
        if (showAddDeviceDialog) {
            AddDeviceDialog(
                onDismiss = { showAddDeviceDialog = false },
                onConfirm = { name, type, x, y ->
                    viewModel.addDevice(floorId, name, type, x, y)
                    showAddDeviceDialog = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (floor != null) {
                FloorPlanView(
                    floor = floor,
                    devices = devices,
                    onDeviceClick = { device ->
                        selectedDevice = device
                    },
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text("Loading floor plan...", modifier = Modifier.padding(16.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Devices on this floor",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(devices.size) { index ->
                    val device = devices[index]
                    DeviceCard(
                        device = device,
                        onStatusToggle = { newStatus ->
                            viewModel.toggleDeviceStatus(device.id, newStatus)
                        },
                        onMultiSwitchToggle = { switchId, newStatus ->
                            viewModel.toggleMultiSwitch(device.id, switchId, newStatus)
                        }
                    )
                }
            }
        }

        // Bottom Sheet for selected device
        selectedDevice?.let { device ->
            // Make sure to find the updated device from the list so the bottom sheet updates reactively
            val updatedDevice = devices.find { it.id == device.id } ?: device
            
            ModalBottomSheet(
                onDismissRequest = { selectedDevice = null },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text("Control ${updatedDevice.name}", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    DeviceCard(
                        device = updatedDevice,
                        onStatusToggle = { newStatus ->
                            viewModel.toggleDeviceStatus(updatedDevice.id, newStatus)
                        },
                        onMultiSwitchToggle = { switchId, newStatus ->
                            viewModel.toggleMultiSwitch(updatedDevice.id, switchId, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceDialog(onDismiss: () -> Unit, onConfirm: (String, DeviceType, Int, Int) -> Unit) {
    var deviceName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DeviceType.ELECTRICAL_OUTLET) }
    var xCoord by remember { mutableStateOf("0") }
    var yCoord by remember { mutableStateOf("0") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Device") },
        text = {
            Column {
                TextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("Device Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        readOnly = true,
                        value = selectedType.name,
                        onValueChange = { },
                        label = { Text("Device Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DeviceType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = xCoord,
                        onValueChange = { xCoord = it },
                        label = { Text("Grid X") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = yCoord,
                        onValueChange = { yCoord = it },
                        label = { Text("Grid Y") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val x = xCoord.toIntOrNull() ?: 0
                val y = yCoord.toIntOrNull() ?: 0
                if (deviceName.isNotBlank()) onConfirm(deviceName, selectedType, x, y)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
