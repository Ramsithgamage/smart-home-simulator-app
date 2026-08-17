package com.example.smarthomeapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smarthomeapplication.model.Floor
import com.example.smarthomeapplication.viewmodel.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DeviceViewModel,
    onFloorSelected: (String) -> Unit,
    onViewReportsClick: () -> Unit
) {
    val floors by viewModel.floors.collectAsState()
    var showAddFloorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Home Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = onViewReportsClick,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text("Reports")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = { showAddFloorDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Floor")
                }
            }
        }
    ) { padding ->
        if (showAddFloorDialog) {
            AddFloorDialog(
                onDismiss = { showAddFloorDialog = false },
                onConfirm = { name, rows, cols ->
                    viewModel.addFloor(name, rows, cols)
                    showAddFloorDialog = false
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Select a Floor",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(floors) { floor ->
                FloorCard(floor = floor, onClick = { onFloorSelected(floor.id) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AddFloorDialog(onDismiss: () -> Unit, onConfirm: (String, Int, Int) -> Unit) {
    var floorName by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf("4") }
    var cols by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Floor") },
        text = {
            Column {
                TextField(
                    value = floorName,
                    onValueChange = { floorName = it },
                    label = { Text("Floor Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = rows,
                        onValueChange = { rows = it },
                        label = { Text("Rows") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = cols,
                        onValueChange = { cols = it },
                        label = { Text("Columns") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val r = rows.toIntOrNull() ?: 4
                val c = cols.toIntOrNull() ?: 4
                if (floorName.isNotBlank()) onConfirm(floorName, r, c)
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

@Composable
fun FloorCard(floor: Floor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = floor.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
