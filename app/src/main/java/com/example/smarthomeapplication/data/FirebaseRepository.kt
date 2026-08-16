package com.example.smarthomeapplication.data

import android.util.Log
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

    companion object {
        private const val TAG = "FirebaseRepository"
        private const val DB_URL = "https://smart-home-application-29aca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    }

    // Explicit URL required since google-services.json does not contain database_url
    private val database = FirebaseDatabase.getInstance(DB_URL).reference

    init {
        monitorConnection()
    }

    private fun monitorConnection() {
        val connectedRef = FirebaseDatabase.getInstance(DB_URL).getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d(TAG, "Firebase connected")
                } else {
                    Log.w(TAG, "Firebase disconnected")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Connection monitor cancelled", error.toException())
            }
        })
    }

    fun getFloors(): Flow<List<Floor>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val floors = snapshot.children.mapNotNull { it.getValue(Floor::class.java) }
                trySend(floors)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching floors", error.toException())
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
                Log.e(TAG, "Error fetching devices for floor $floorId", error.toException())
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
                Log.e(TAG, "Error fetching all devices", error.toException())
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
                Log.e(TAG, "Error fetching usage logs", error.toException())
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

    fun addFloor(floor: Floor) {
        val key = database.child("floors").push().key ?: return
        val newFloor = floor.copy(id = key)
        database.child("floors").child(key).setValue(newFloor)
    }

    fun addDevice(device: Device) {
        val key = database.child("devices").push().key ?: return
        val newDevice = device.copy(id = key)
        database.child("devices").child(key).setValue(newDevice)
    }
}
