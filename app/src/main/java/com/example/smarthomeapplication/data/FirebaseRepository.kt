package com.example.smarthomeapplication.data

import com.example.smarthomeapplication.model.Device
import com.example.smarthomeapplication.model.DeviceStatus
import com.example.smarthomeapplication.model.Floor
import com.example.smarthomeapplication.model.UsageLog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance().reference

    fun getFloors(): Flow<List<Floor>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val floors = snapshot.children.mapNotNull { it.getValue(Floor::class.java) }
                trySend(floors)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.child("floors")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getDevicesForFloor(floorId: String): Flow<List<Device>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = snapshot.children.mapNotNull { it.getValue(Device::class.java) }
                trySend(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val query = database.child("devices").orderByChild("floor_id").equalTo(floorId)
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }
    
    fun getAllDevices(): Flow<List<Device>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = snapshot.children.mapNotNull { it.getValue(Device::class.java) }
                trySend(devices)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.child("devices")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getUsageLogs(): Flow<List<UsageLog>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logs = mutableListOf<UsageLog>()
                for (deviceSnapshot in snapshot.children) {
                    val deviceLogs = deviceSnapshot.children.mapNotNull { it.getValue(UsageLog::class.java) }
                    logs.addAll(deviceLogs)
                }
                logs.sortByDescending { it.timestamp }
                trySend(logs)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.child("usage_logs")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun updateDeviceStatus(deviceId: String, newStatus: DeviceStatus) {
        database.child("devices").child(deviceId).child("status").setValue(newStatus.name)
    }

    fun updateMultiSwitch(deviceId: String, switchId: String, newStatus: DeviceStatus) {
        database.child("devices").child(deviceId).child("switches").child(switchId).child("status").setValue(newStatus.name)
    }
}
