package com.example.smarthomeapplication.model

import com.google.firebase.database.IgnoreExtraProperties

enum class DeviceStatus {
    ON, OFF, ERROR, DISCONNECTED
}

enum class DeviceType {
    ELECTRICAL_OUTLET, MULTI_SWITCH, SAFETY_DEVICE, SECURITY_CAMERA
}

@IgnoreExtraProperties
data class Floor(
    val id: String = "",
    val name: String = "",
    val image_asset: String = "",
    val grid_rows: Int = 4,
    val grid_cols: Int = 4
)

@IgnoreExtraProperties
data class SwitchState(
    val id: String = "",
    val name: String = "",
    val status: String = DeviceStatus.OFF.name
)

@IgnoreExtraProperties
data class Device(
    val id: String = "",
    val floor_id: String = "",
    val grid_x: Int = 0,
    val grid_y: Int = 0,
    val type: String = DeviceType.ELECTRICAL_OUTLET.name,
    val name: String = "",
    val status: String = DeviceStatus.OFF.name,
    val metrics: Map<String, Any>? = null,
    // Multi-Switch specific
    val switches: Map<String, SwitchState>? = null,
    // Safety Device specific
    val max_on_duration_mins: Int? = null,
    val last_turned_on: Long? = null,
    // Security Camera specific
    val stream_url: String? = null
) {
    fun getDeviceStatus(): DeviceStatus {
        return try {
            DeviceStatus.valueOf(status)
        } catch (e: Exception) {
            DeviceStatus.ERROR
        }
    }
    
    fun getDeviceTypeEnum(): DeviceType {
        return try {
            DeviceType.valueOf(type)
        } catch (e: Exception) {
            DeviceType.ELECTRICAL_OUTLET
        }
    }
}

@IgnoreExtraProperties
data class UsageLog(
    val id: String = "",
    val device_id: String = "",
    val timestamp: Long = 0,
    val action: String = "",
    val duration_mins: Int = 0
)
