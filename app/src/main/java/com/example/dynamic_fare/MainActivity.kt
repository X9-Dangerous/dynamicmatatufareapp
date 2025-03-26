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
import com.example.dynamic_fare.Routes.FleetRegistrationScreen
import com.example.dynamic_fare.Routes.MatatuInfoScreen
import com.example.dynamic_fare.Routes.MatatuRegistrationScreen
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.example.dynamic_fare.ui.screens.*
import com.example.dynamic_fare.ui.ChooseFleetDialog
import com.example.dynamic_fare.ui.FleetRegistrationScreen
import com.example.dynamic_fare.ui.MatatuInfoScreen
import com.example.dynamic_fare.ui.MatatuRegistrationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance().reference

        try {
            Log.d("MainActivity", "Initializing database")
            val database = AppDatabase.getDatabase(this)
            Log.d("MainActivity", "Starting GTFS import")
            GTFSimporter.importGTFS(this, database)
            Log.d("MainActivity", "GTFS import initiated")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing database: ${e.message}", e)
        }

        val signUpViewModel = ViewModelProvider(this, SignUpViewModelFactory(auth, db)).get(SignUpViewModel::class.java)

        setContent {
            DynamicMatauFareAppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Routes.LoginScreenContent) {
                    composable(Routes.LoginScreenContent) { LoginScreenContent(navController) }
                    composable(Routes.SignUpScreen) { SignUpScreen(navController, signUpViewModel) }
                    composable(Routes.OperatorHome) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        OperatorHomeScreen(navController, operatorId)
                    }
                    composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController) }
                    composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }
                    composable(Routes.FooterWithIcons) { ProfileScreen() }
                    composable(Routes.RegistrationScreen) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        RegistrationScreen(navController, operatorId)
                    }
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
                    composable(Routes.FleetRegistrationScreen) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        FleetRegistrationScreen(navController, operatorId)
                    }
                    composable(Routes.MatatuRegistrationScreen) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
                        MatatuRegistrationScreen(navController, operatorId)
                    }
                    composable(Routes.MatatuInfoScreen) { backStackEntry ->
                        val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
                        MatatuInfoScreen(navController, matatuId)
                    }
                }
            }
        }
    }
}