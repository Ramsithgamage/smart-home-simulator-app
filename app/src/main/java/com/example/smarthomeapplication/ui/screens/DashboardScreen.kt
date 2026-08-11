package com.example.smarthomeapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Smart Home Dashboard") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onViewReportsClick) {
                Text("Reports")
            }
        }
    ) { padding ->
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
fun FloorCard(floor: Floor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = floor.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "ID: ${floor.id}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
