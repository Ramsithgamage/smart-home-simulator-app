package com.example.smarthomeapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomeapplication.data.FirebaseRepository
import com.example.smarthomeapplication.model.Device
import com.example.smarthomeapplication.model.DeviceStatus
import com.example.smarthomeapplication.model.DeviceType
import com.example.smarthomeapplication.model.Floor
import com.example.smarthomeapplication.model.DeviceUsageReport
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

    private val _allDevices = MutableStateFlow<List<Device>>(emptyList())
    val allDevices: StateFlow<List<Device>> = _allDevices.asStateFlow()

    private val _usageReports = MutableStateFlow<List<DeviceUsageReport>>(emptyList())
    val usageReports: StateFlow<List<DeviceUsageReport>> = _usageReports.asStateFlow()

    init {
        fetchFloors()
        fetchUsageReports()
        fetchAllDevices()
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

    private fun fetchUsageReports() {
        viewModelScope.launch {
            repository.getDeviceUsageReports().collect { reports ->
                _usageReports.value = reports
            }
        }
    }

    private fun fetchAllDevices() {
        viewModelScope.launch {
            repository.getAllDevices().collect { deviceList ->
                _allDevices.value = deviceList
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

    fun addDevice(device: Device) {
        repository.addDevice(device)
    }
}
