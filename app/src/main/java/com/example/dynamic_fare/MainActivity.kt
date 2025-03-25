package com.example.dynamic_fare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance().reference

        // Properly handle database initialization with robust error handling
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

        val signUpViewModel = ViewModelProvider(this, SignUpViewModelFactory(auth, db)).get(SignUpViewModel::class.java)

        setContent {
            DynamicMatauFareAppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable(Routes.LoginScreenContent) { LoginScreenContent(navController) }
                    composable(Routes.SignUpScreen) { SignUpScreen(navController, signUpViewModel) }
                    composable(Routes.OperatorHome) { DisplayInfoScreen(navController) }
                    composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController) }
                    composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }
                    composable(Routes.FooterWithIcons) { ProfileScreen() }
                }
            }
        }
    }
}
