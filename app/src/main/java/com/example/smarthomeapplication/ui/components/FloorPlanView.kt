package com.example.smarthomeapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.smarthomeapplication.model.Device
import com.example.smarthomeapplication.model.DeviceStatus
import com.example.smarthomeapplication.model.DeviceType
import com.example.smarthomeapplication.model.Floor

@Composable
fun FloorPlanView(
    floor: Floor,
    devices: List<Device>,
    onDeviceClick: (Device) -> Unit,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = floor.grid_rows.coerceAtLeast(1)
    val cols = floor.grid_cols.coerceAtLeast(1)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(cols.toFloat() / rows.toFloat())
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
    ) {
        val cellWidth = maxWidth / cols
        val cellHeight = maxHeight / rows

        // Draw Chessboard Pattern and handle cell clicks
        Column {
            for (r in 0 until rows) {
                Row {
                    for (c in 0 until cols) {
                        val isDark = (r + c) % 2 != 0
                        val cellColor = if (isDark) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        } else {
                            Color.White.copy(alpha = 0.7f)
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(cellWidth, cellHeight)
                                .background(cellColor)
                                .clickable { onCellClick(c, r) }
                        )
                    }
                }
            }
        }

        // Place devices on grid
        devices.forEach { device ->
            // clamp to grid
            val gx = device.grid_x.coerceIn(0, cols - 1)
            val gy = device.grid_y.coerceIn(0, rows - 1)

            val iconColor = when (device.getDeviceStatus()) {
                DeviceStatus.ON -> Color(0xFF4CAF50)
                DeviceStatus.OFF -> Color.Gray
                DeviceStatus.ERROR -> Color.Red
                DeviceStatus.DISCONNECTED -> Color.DarkGray
            }

            val iconData = when (device.getDeviceTypeEnum()) {
                DeviceType.OUTLET -> Icons.Default.Power
                DeviceType.MULTI_SWITCH -> Icons.Default.Lightbulb
                DeviceType.SAFETY_APPLIANCE -> Icons.Default.Warning
                DeviceType.SCHEDULED_APPLIANCE -> Icons.Default.Schedule
                DeviceType.CAMERA -> Icons.Default.CameraAlt
            }

            Box(
                modifier = Modifier
                    .offset(x = cellWidth * gx, y = cellHeight * gy)
                    .size(cellWidth, cellHeight)
                    .clickable { onDeviceClick(device) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = iconData,
                        contentDescription = device.name,
                        tint = iconColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
