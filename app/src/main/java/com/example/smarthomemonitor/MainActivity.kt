package com.example.smarthomemonitor

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    // Track current states locally to toggle them correctly
    private var lightStatus = "OFF"
    private var ironStatus = "OFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Database
        database = FirebaseDatabase.getInstance().reference

        // Find UI elements
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutFloors)
        val tvFloorTitle = findViewById<TextView>(R.id.tvFloorTitle)
        val cardLaundryRoom = findViewById<CardView>(R.id.cardLaundryRoom)

        val tvLightStatus = findViewById<TextView>(R.id.tvLightStatus)
        val tvIronStatus = findViewById<TextView>(R.id.tvIronStatus)
        val btnToggleLight = findViewById<Button>(R.id.btnToggleLight)
        val btnToggleIron = findViewById<Button>(R.id.btnToggleIron)

        // --- MULTI-FLOOR VIEW LOGIC ---
        tvFloorTitle.text = "Ground Floor Devices"
        cardLaundryRoom.visibility = View.GONE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        tvFloorTitle.text = "Ground Floor Devices"
                        cardLaundryRoom.visibility = View.GONE
                    }
                    1 -> {
                        tvFloorTitle.text = "First Floor Devices"
                        cardLaundryRoom.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        // --- REAL-TIME LISTENERS (INBOUND) ---

        // 1. Listen to Living Room Light Status
        val lightRef = database.child("house").child("ground_floor").child("living_room").child("light_1").child("status")
        lightRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the value from the cloud, default to "OFF" if empty
                val status = snapshot.getValue(String::class.java) ?: "OFF"
                lightStatus = status

                // Update UI text and color reactively
                tvLightStatus.text = "Status: $status"
                if (status == "ON") {
                    tvLightStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_on))
                } else {
                    tvLightStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_off))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // 2. Listen to Laundry Iron Status
        val ironRef = database.child("house").child("first_floor").child("laundry_room").child("clothing_iron").child("status")
        ironRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: "OFF"
                ironStatus = status

                tvIronStatus.text = "Iron: $status"
                if (status == "ON") {
                    tvIronStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_on))
                } else {
                    tvIronStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_off))
                }
            }

            override fun onCancelled(error: DatabaseError) { {} }
        })


        // --- USER INTERACTIONS (OUTBOUND) ---

        // When clicked, check what the current state is and flip it to the opposite value
        btnToggleLight.setOnClickListener {
            val nextState = if (lightStatus == "ON") "OFF" else "ON"
            lightRef.setValue(nextState)
        }

        btnToggleIron.setOnClickListener {
            val nextState = if (ironStatus == "ON") "OFF" else "ON"
            ironRef.setValue(nextState)
        }
    }
}