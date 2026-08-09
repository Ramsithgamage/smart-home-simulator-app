package com.example.smarthomemonitor // Make sure this matches your package name

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.bumptech.glide.Glide
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    // Track states locally
    private var lightStatus = "OFF"
    private var ironStatus = "OFF"

    // Multi-Switch tracking states
    private var fanStatus = "OFF"
    private var ffLightStatus = "OFF"
    private var tvStatus = "OFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance().reference

        // Cards
        val cardGroundFloorLivingRoom = findViewById<CardView>(R.id.cardGroundFloorLivingRoom)
        val cardLaundryRoom = findViewById<CardView>(R.id.cardLaundryRoom)
        val cardFirstFloorLivingRoom = findViewById<CardView>(R.id.cardFirstFloorLivingRoom)
        val cardFirstFloorCamera = findViewById<CardView>(R.id.cardFirstFloorCamera)
        val cardReportingDashboard = findViewById<CardView>(R.id.cardReportingDashboard)

        // Tab Layout & Header
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutFloors)
        val tvFloorTitle = findViewById<TextView>(R.id.tvFloorTitle)

        // --- TAB LOGIC ---
        // Default View: Ground Floor
        cardGroundFloorLivingRoom.visibility = View.VISIBLE
        cardLaundryRoom.visibility = View.GONE
        cardFirstFloorLivingRoom.visibility = View.GONE
        cardFirstFloorCamera.visibility = View.GONE
        cardReportingDashboard.visibility = View.GONE // Hidden by default

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Ground Floor
                        tvFloorTitle.text = "Ground Floor Devices"
                        cardGroundFloorLivingRoom.visibility = View.VISIBLE
                        cardLaundryRoom.visibility = View.GONE
                        cardFirstFloorLivingRoom.visibility = View.GONE
                        cardFirstFloorCamera.visibility = View.GONE
                        cardReportingDashboard.visibility = View.GONE
                    }
                    1 -> { // First Floor
                        tvFloorTitle.text = "First Floor Devices"
                        cardGroundFloorLivingRoom.visibility = View.GONE
                        cardLaundryRoom.visibility = View.VISIBLE
                        cardFirstFloorLivingRoom.visibility = View.VISIBLE
                        cardFirstFloorCamera.visibility = View.VISIBLE
                        cardReportingDashboard.visibility = View.GONE
                    }
                    2 -> { // Reports Tab
                        tvFloorTitle.text = "Analytics & Usage"
                        cardGroundFloorLivingRoom.visibility = View.GONE
                        cardLaundryRoom.visibility = View.GONE
                        cardFirstFloorLivingRoom.visibility = View.GONE
                        cardFirstFloorCamera.visibility = View.GONE
                        cardReportingDashboard.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // --- REAL-TIME LISTENERS & BUTTON CLICKS ---

        // 1. Ground Floor Light
        val tvLightStatus = findViewById<TextView>(R.id.tvLightStatus)
        val btnToggleLight = findViewById<Button>(R.id.btnToggleLight)
        val lightRef = database.child("house").child("ground_floor").child("living_room").child("light_1").child("status")

        setupDeviceListener(lightRef, tvLightStatus, "Main Light: ") { newStatus -> lightStatus = newStatus }
        btnToggleLight.setOnClickListener { lightRef.setValue(if (lightStatus == "ON") "OFF" else "ON") }

        // 2. Laundry Room Iron
        val tvIronStatus = findViewById<TextView>(R.id.tvIronStatus)
        val btnToggleIron = findViewById<Button>(R.id.btnToggleIron)
        val ironRef = database.child("house").child("first_floor").child("laundry_room").child("clothing_iron").child("status")

        setupDeviceListener(ironRef, tvIronStatus, "Iron: ") { newStatus -> ironStatus = newStatus }
        btnToggleIron.setOnClickListener { ironRef.setValue(if (ironStatus == "ON") "OFF" else "ON") }

        // 3. Multi-Switch Unit: Fan, Light, TV
        val tvFanStatus = findViewById<TextView>(R.id.tvFanStatus)
        val btnToggleFan = findViewById<Button>(R.id.btnToggleFan)
        val tvFfLightStatus = findViewById<TextView>(R.id.tvFfLightStatus)
        val btnToggleFfLight = findViewById<Button>(R.id.btnToggleFfLight)
        val tvTvStatus = findViewById<TextView>(R.id.tvTvStatus)
        val btnToggleTv = findViewById<Button>(R.id.btnToggleTv)

        // --- CAMERA REAL-TIME LISTENER ---
        var cameraStatus = "OFF"
        val tvCameraStatus = findViewById<TextView>(R.id.tvCameraStatus)
        val imgCameraFeed = findViewById<ImageView>(R.id.imgCameraFeed)
        val btnToggleCamera = findViewById<Button>(R.id.btnToggleCamera)

        val cameraRef = database.child("house").child("first_floor").child("living_room").child("security_cam_1")

        cameraRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cameraStatus = snapshot.child("status").getValue(String::class.java) ?: "OFF"
                val streamUrl = snapshot.child("stream_url").getValue(String::class.java) ?: ""

                tvCameraStatus.text = "Camera: $cameraStatus"

                if (cameraStatus == "ON") {
                    tvCameraStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_on))
                    // Use Glide to load the mock URL into the ImageView
                    Glide.with(this@MainActivity)
                        .load(streamUrl)
                        .into(imgCameraFeed)
                } else {
                    tvCameraStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_off))
                    // Clear the image to simulate offline feed (just a grey background)
                    Glide.with(this@MainActivity).clear(imgCameraFeed)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        btnToggleCamera.setOnClickListener {
            cameraRef.child("status").setValue(if (cameraStatus == "ON") "OFF" else "ON")
        }

        val multiSwitchRef = database.child("house").child("first_floor").child("living_room").child("multi_switch_unit_1")

        setupDeviceListener(multiSwitchRef.child("switch_a_fan"), tvFanStatus, "Fan: ") { newStatus -> fanStatus = newStatus }
        btnToggleFan.setOnClickListener { multiSwitchRef.child("switch_a_fan").setValue(if (fanStatus == "ON") "OFF" else "ON") }

        setupDeviceListener(multiSwitchRef.child("switch_b_light"), tvFfLightStatus, "Light: ") { newStatus -> ffLightStatus = newStatus }
        btnToggleFfLight.setOnClickListener { multiSwitchRef.child("switch_b_light").setValue(if (ffLightStatus == "ON") "OFF" else "ON") }

        setupDeviceListener(multiSwitchRef.child("switch_c_tv"), tvTvStatus, "TV: ") { newStatus -> tvStatus = newStatus }
        btnToggleTv.setOnClickListener { multiSwitchRef.child("switch_c_tv").setValue(if (tvStatus == "ON") "OFF" else "ON") }

        // --- REPORTS REAL-TIME LISTENER ---
        val tvReportLight = findViewById<TextView>(R.id.tvReportLight)
        val tvReportIron = findViewById<TextView>(R.id.tvReportIron)
        val tvReportCamera = findViewById<TextView>(R.id.tvReportCamera)

        val reportsRef = database.child("house").child("reports")

        reportsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Fetch Light Data
                val lightToggled = snapshot.child("living_room_light").child("times_toggled").getValue(Int::class.java) ?: 0
                val lightHours = snapshot.child("living_room_light").child("total_hours_on").getValue(Double::class.java) ?: 0.0
                tvReportLight.text = "💡 Main Light:\n- Toggled $lightToggled times\n- $lightHours hours total usage"

                // Fetch Iron Data
                val ironToggled = snapshot.child("clothing_iron").child("times_toggled").getValue(Int::class.java) ?: 0
                val ironCutoffs = snapshot.child("clothing_iron").child("safety_cutoffs_triggered").getValue(Int::class.java) ?: 0
                tvReportIron.text = "🔥 Clothing Iron:\n- Used $ironToggled times\n- ⚠️ Safety cutoffs triggered: $ironCutoffs"

                // Fetch Camera Data
                val camData = snapshot.child("security_camera").child("total_data_used_gb").getValue(Double::class.java) ?: 0.0
                tvReportCamera.text = "📷 Security Camera:\n- Total Data Used: $camData GB"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    // A helper function to reduce repetitive code when creating Firebase listeners
    private fun setupDeviceListener(ref: DatabaseReference, textView: TextView, prefix: String, updateLocalState: (String) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: "OFF"
                updateLocalState(status)
                textView.text = "$prefix$status"
                if (status == "ON") {
                    textView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_on))
                } else {
                    textView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_off))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}