package com.example.smarthomeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthomeapplication.ui.screens.DashboardScreen
import com.example.smarthomeapplication.ui.screens.InteractiveFloorScreen
import com.example.smarthomeapplication.ui.screens.UsageReportingScreen
import com.example.smarthomeapplication.ui.theme.SmartHomeApplicationTheme
import com.example.smarthomeapplication.viewmodel.DeviceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeApplicationTheme {
                val navController = rememberNavController()
                val deviceViewModel: DeviceViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = deviceViewModel,
                                onFloorSelected = { floorId ->
                                    navController.navigate("floor/$floorId")
                                },
                                onViewReportsClick = {
                                    navController.navigate("reports")
                                }
                            )
                        }
                        composable("floor/{floorId}") { backStackEntry ->
                            val floorId = backStackEntry.arguments?.getString("floorId") ?: return@composable
                            InteractiveFloorScreen(
                                floorId = floorId,
                                viewModel = deviceViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("reports") {
                            UsageReportingScreen(
                                viewModel = deviceViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}