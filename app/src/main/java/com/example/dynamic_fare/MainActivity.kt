package com.example.dynamic_fare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dynamic_fare.Routes.MatatuInfoScreen
import com.example.dynamic_fare.ui.MatatuRegistrationScreen
import com.example.dynamic_fare.ui.ChooseFleetDialog
import com.example.dynamic_fare.ui.FleetRegistrationScreen
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.dynamic_fare.ui.MatatuInfoScreen
import com.example.dynamic_fare.ui.screens.OperatorHomeScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Auth & Database
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance().reference

       /* // Properly handle database initialization with robust error handling
        try {
            Log.d("MainActivity", "Initializing database")
            val database = AppDatabase.getDatabase(this)

            // Import GTFS data in a background thread
            Log.d("MainActivity", "Starting GTFS import")
            GTFSimporter.importGTFS(this, database)
            Log.d("MainActivity", "GTFS import initiated")
        } catch (e: Exception) {
            // Log error but don't crash the app
            Log.e("MainActivity", "Error initializing database: ${e.message}", e)
        }
*/
        // Initialize ViewModel for Sign Up
        val signUpViewModel = ViewModelProvider(this, SignUpViewModelFactory(auth, db))
            .get(SignUpViewModel::class.java)

        setContent {
            DynamicMatauFareAppTheme {
                val navController = rememberNavController()

                // Navigation Setup
                NavHost(navController = navController, startDestination = Routes.LoginScreenContent) {
                    composable(Routes.LoginScreenContent) { LoginScreenContent(navController) }
                    composable(Routes.SignUpScreen) { SignUpScreen(navController, signUpViewModel) }
                    composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController) }
                    composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }
                    composable(Routes.FooterWithIcons) { ProfileScreen() }

                    // 🟢 Operator Home Screen - Takes `operatorId` as argument
                    composable(Routes.OperatorHome) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        OperatorHomeScreen(navController, operatorId)
                    }

                    // 🟢 Registration Screen - Takes `operatorId` as argument
                    composable(Routes.RegistrationScreen) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        RegistrationScreen(navController, operatorId)
                    }

                    // 🟢 Choose Fleet or Single Matatu (Popup when `+` is clicked)
                    composable(Routes.ChooseFleetDialog) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        ChooseFleetDialog(
                            onDismiss = { navController.popBackStack() },
                            onSelection = { isFleet ->
                                val route = if (isFleet) {
                                    "fleetRegistration/$operatorId"
                                } else {
                                    "matatuRegistration/$operatorId"
                                }
                                navController.navigate(route)
                            }
                        )
                    }

                    // 🟢 Fleet Registration
                    composable(Routes.FleetRegistrationScreen) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        FleetRegistrationScreen(navController, operatorId)
                    }

                    // 🟢 Matatu Registration
                    composable(Routes.MatatuRegistrationScreen) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        MatatuRegistrationScreen(navController, operatorId)
                    }

                    // 🟢 Matatu Info Page (if exists)
                    composable(Routes.MatatuInfoScreen) { backStackEntry ->
                        val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
                        MatatuInfoScreen(navController, matatuId)
                    }
                }
            }
        }
    }
}
