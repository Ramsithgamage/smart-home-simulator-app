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
import com.example.smarthomeapplication.model.DeviceStatus
import com.example.smarthomeapplication.model.DeviceType
import com.example.smarthomeapplication.model.SmartDevice
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
    var initialX by remember { mutableStateOf(0) }
    var initialY by remember { mutableStateOf(0) }
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
            FloatingActionButton(onClick = { 
                initialX = 0
                initialY = 0
                showAddDeviceDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Device")
            }
        }
    ) { padding ->
        if (showAddDeviceDialog) {
            AddDeviceDialog(
                initialX = initialX,
                initialY = initialY,
                onDismiss = { showAddDeviceDialog = false },
                onConfirm = { deviceName, smartDevice, x, y ->
                    val type = when (smartDevice) {
                        is SmartDevice.Outlet -> DeviceType.OUTLET
                        is SmartDevice.MultiSwitch -> DeviceType.MULTI_SWITCH
                        is SmartDevice.SafetyAppliance -> DeviceType.SAFETY_APPLIANCE
                        is SmartDevice.ScheduledAppliance -> DeviceType.SCHEDULED_APPLIANCE
                        is SmartDevice.Camera -> DeviceType.CAMERA
                    }
                    
                    val switches = if (smartDevice is SmartDevice.MultiSwitch) {
                        val map = mutableMapOf<String, com.example.smarthomeapplication.model.SwitchState>()
                        for (i in 1..smartDevice.states.size) {
                            val id = "switch_$i"
                            map[id] = com.example.smarthomeapplication.model.SwitchState(
                                id = id,
                                name = "switch-$i",
                                status = DeviceStatus.OFF.name
                            )
                        }
                        map
                    } else null
                    
                    val newDevice = Device(
                        floor_id = floorId,
                        name = deviceName,
                        type = type.name,
                        status = DeviceStatus.OFF.name,
                        grid_x = x,
                        grid_y = y,
                        max_on_duration_mins = (smartDevice as? SmartDevice.SafetyAppliance)?.maxOnDuration,
                        start_time = (smartDevice as? SmartDevice.ScheduledAppliance)?.startTime,
                        end_time = (smartDevice as? SmartDevice.ScheduledAppliance)?.endTime,
                        resource_id = (smartDevice as? SmartDevice.Camera)?.resourceId,
                        switches = switches
                    )
                    
                    viewModel.addDevice(newDevice)
                    showAddDeviceDialog = false
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                if (floor != null) {
                    FloorPlanView(
                        floor = floor,
                        devices = devices,
                        onDeviceClick = { device ->
                            selectedDevice = device
                        },
                        onCellClick = { x, y ->
                            initialX = x
                            initialY = y
                            showAddDeviceDialog = true
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Text("Loading floor plan...", modifier = Modifier.padding(16.dp))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Devices on this floor",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            items(devices.size) { index ->
                val device = devices[index]
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
fun AddDeviceDialog(
    initialX: Int = 0,
    initialY: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (String, SmartDevice, Int, Int) -> Unit
) {
    var deviceName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DeviceType.OUTLET) }
    var xCoord by remember { mutableStateOf(initialX.toString()) }
    var yCoord by remember { mutableStateOf(initialY.toString()) }
    var expanded by remember { mutableStateOf(false) }

    // State for specific fields
    var maxOnDuration by remember { mutableStateOf("60") }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("22:00") }
    var cameraResId by remember { mutableStateOf("0") } // In a real app, this would be a selection from a list
    var multiSwitchCount by remember { mutableStateOf("1") }

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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
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
                
                // Conditional Rendering of specific fields
                when (selectedType) {
                    DeviceType.MULTI_SWITCH -> {
                        TextField(
                            value = multiSwitchCount,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..5)) {
                                    multiSwitchCount = it
                                }
                            },
                            label = { Text("Number of Switches (1-5)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    DeviceType.SAFETY_APPLIANCE -> {
                        TextField(
                            value = maxOnDuration,
                            onValueChange = { maxOnDuration = it },
                            label = { Text("Max On Duration (mins)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    DeviceType.SCHEDULED_APPLIANCE -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                label = { Text("Start Time") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text("End Time") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    DeviceType.CAMERA -> {
                        // Simulation mode: no manual resource ID required
                    }
                    else -> {}
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
                
                // Create SmartDevice based on user selection
                val smartDevice = when (selectedType) {
                    DeviceType.OUTLET -> SmartDevice.Outlet(false)
                    DeviceType.MULTI_SWITCH -> {
                        val count = multiSwitchCount.toIntOrNull()?.coerceIn(1, 5) ?: 1
                        SmartDevice.MultiSwitch(List(count) { false })
                    }
                    DeviceType.SAFETY_APPLIANCE -> SmartDevice.SafetyAppliance(maxOnDuration.toIntOrNull() ?: 60)
                    DeviceType.SCHEDULED_APPLIANCE -> SmartDevice.ScheduledAppliance(startTime, endTime)
                    DeviceType.CAMERA -> SmartDevice.Camera(0)
                }
                
                if (deviceName.isNotBlank()) onConfirm(deviceName, smartDevice, x, y)
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
