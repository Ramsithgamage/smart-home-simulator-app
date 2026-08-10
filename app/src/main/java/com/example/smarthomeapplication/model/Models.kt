package com.example.smarthomeapplication.model

enum class DeviceStatus {
    ON, OFF, ERROR, DISCONNECTED
}

enum class DeviceType {
    ELECTRICAL_OUTLET, MULTI_SWITCH, SAFETY_DEVICE, SECURITY_CAMERA
}

data class Floor(
    val id: String = "",
    val name: String = "",
    val image_asset: String = ""
)

data class SwitchState(
    val name: String = "",
    val status: String = DeviceStatus.OFF.name
)

data class Device(
    val id: String = "",
    val floor_id: String = "",
    val grid_x: Int = 0,
    val grid_y: Int = 0,
    val type: String = DeviceType.ELECTRICAL_OUTLET.name,
    val name: String = "",
    val status: String = DeviceStatus.OFF.name,
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

data class UsageLog(
    val timestamp: Long = 0,
    val event: String = "",
    val duration_mins: Int = 0
)
