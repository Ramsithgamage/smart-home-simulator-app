package com.example.smarthomeapplication

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class SmartHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable Firebase Realtime Database offline persistence
        FirebaseDatabase.getInstance(
            "https://smart-home-application-29aca-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).setPersistenceEnabled(true)
    }
}
