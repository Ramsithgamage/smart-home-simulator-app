package com.example.smarthomeapplication.model

import androidx.annotation.DrawableRes
import com.google.firebase.database.IgnoreExtraProperties

enum class DeviceStatus {
    ON, OFF, ERROR, DISCONNECTED
}

enum class DeviceType {
    OUTLET, MULTI_SWITCH, SAFETY_APPLIANCE, SCHEDULED_APPLIANCE, CAMERA
}

sealed class SmartDevice {
    data class Outlet(val state: Boolean) : SmartDevice()
    data class MultiSwitch(val states: List<Boolean>) : SmartDevice()
    data class SafetyAppliance(val maxOnDuration: Int) : SmartDevice()
    data class ScheduledAppliance(val startTime: String, val endTime: String) : SmartDevice()
    data class Camera(@DrawableRes val resourceId: Int) : SmartDevice()
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
    val type: String = DeviceType.OUTLET.name,
    val name: String = "",
    val status: String = DeviceStatus.OFF.name,
    val metrics: Map<String, Any>? = null,
    // Multi-Switch specific
    val switches: Map<String, SwitchState>? = null,
    // Safety Appliance specific
    val max_on_duration_mins: Int? = null,
    val last_turned_on: Long? = null,
    // Scheduled Appliance specific
    val start_time: String? = null,
    val end_time: String? = null,
    // Camera specific
    val resource_id: Int? = null
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
            DeviceType.OUTLET
        }
    }
}

@IgnoreExtraProperties
data class DeviceUsageReport(
    val device_id: String = "",
    val safety_cutoffs_triggered: Int = 0,
    val total_data_used_gb: Double = 0.0
)
