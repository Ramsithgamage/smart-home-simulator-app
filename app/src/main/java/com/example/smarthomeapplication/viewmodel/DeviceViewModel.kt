package com.example.smarthomeapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomeapplication.data.FirebaseRepository
import com.example.smarthomeapplication.model.Device
import com.example.smarthomeapplication.model.DeviceStatus
import com.example.smarthomeapplication.model.DeviceType
import com.example.smarthomeapplication.model.Floor
import com.example.smarthomeapplication.model.UsageLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _floors = MutableStateFlow<List<Floor>>(emptyList())
    val floors: StateFlow<List<Floor>> = _floors.asStateFlow()

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    private val _usageLogs = MutableStateFlow<List<UsageLog>>(emptyList())
    val usageLogs: StateFlow<List<UsageLog>> = _usageLogs.asStateFlow()

    init {
        fetchFloors()
        fetchUsageLogs()
    }

    private fun fetchFloors() {
        viewModelScope.launch {
            repository.getFloors().collect { floorList ->
                _floors.value = floorList
            }
        }
    }

    fun selectFloor(floorId: String) {
        viewModelScope.launch {
            repository.getDevicesForFloor(floorId).collect { deviceList ->
                _devices.value = deviceList
            }
        }
    }

    private fun fetchUsageLogs() {
        viewModelScope.launch {
            repository.getUsageLogs().collect { logs ->
                _usageLogs.value = logs
            }
        }
    }

    fun toggleDeviceStatus(deviceId: String, newStatus: DeviceStatus) {
        repository.updateDeviceStatus(deviceId, newStatus)
    }

    fun toggleMultiSwitch(deviceId: String, switchId: String, newStatus: DeviceStatus) {
        repository.updateMultiSwitch(deviceId, switchId, newStatus)
    }

    fun addFloor(name: String, rows: Int, cols: Int) {
        val newFloor = Floor(name = name, grid_rows = rows, grid_cols = cols)
        repository.addFloor(newFloor)
    }

    fun addDevice(floorId: String, name: String, type: DeviceType, x: Int, y: Int) {
        val newDevice = Device(
            floor_id = floorId,
            name = name,
            type = type.name,
            status = DeviceStatus.OFF.name,
            grid_x = x,
            grid_y = y
        )
        repository.addDevice(newDevice)
    }
}
