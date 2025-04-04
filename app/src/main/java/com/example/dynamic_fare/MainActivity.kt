package com.example.dynamic_fare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dynamic_fare.ui.screens.RegistrationScreen
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.example.dynamic_fare.ui.screens.*
import com.example.dynamic_fare.ui.ChooseFleetDialog
import com.example.dynamic_fare.ui.FleetRegistrationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.dynamic_fare.data.SettingsDataStore
import com.example.dynamic_fare.ui.screens.setLocale
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance().reference
        val operatorId = auth.currentUser?.uid ?: ""
        val database = AppDatabase.getDatabase(this)
        val fareManager = FareManager(FirebaseDatabase.getInstance())
        val timeManager = TimeManager()
        val apiKey = "d77ed3bf47a3594d4053bb96e601958f"
        val weatherManager = WeatherManager(apiKey)
        val getMatatuIdFromRegistration = fareManager::getMatatuIdFromRegistration



        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                SettingsDataStore.getLanguage(applicationContext, userId = operatorId).collect { langCode ->
                    setLocale(this@MainActivity, langCode)
                }
            }
        }

        try {
            Log.d("MainActivity", "Initializing database")
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

                    composable(Routes.OperatorHome, arguments = listOf(
                        navArgument("operatorId") { type = NavType.StringType }
                    )) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("operatorId") ?: ""
                        OperatorHomeScreen(navController, id)
                    }
                    composable(Routes.MatatuDetailsScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) {
                            backStackEntry -> MatatuDetailsScreen(navController, backStackEntry.arguments?.getString("matatuId") ?: "")
                    }
                    composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController) }
                    composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }
                    composable(Routes.SetFaresScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) { backStackEntry ->
                        val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
                        SetFaresScreen(matatuId)
                    }
                    composable(Routes.ProfileScreen, arguments = listOf(
                        navArgument("userId") { type = NavType.StringType }
                    )) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("userId") ?: operatorId
                        ProfileScreen(navController, id)
                    }
                    composable(Routes.FareTabbedScreen) { backStackEntry ->
                        val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
                        FareTabbedScreen(navController, matatuId)
                    }
                    composable(Routes.SettingsScreen, arguments = listOf(
                        navArgument("userId") { type = NavType.StringType }
                    )) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("userId") ?: operatorId
                        SettingsScreen(navController, id)
                    }

                    composable(Routes.RegistrationScreen, arguments = listOf(
                        navArgument("operatorId") { type = NavType.StringType },
                        navArgument("fleetId") { type = NavType.StringType; defaultValue = "" }
                    )) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("operatorId") ?: operatorId
                        val fleetId = backStackEntry.arguments?.getString("fleetId") ?: ""
                        RegistrationScreen(navController, id)
                    }

                    composable(Routes.ChooseFleetDialog) {
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

                    composable(
                        Routes.PaymentPage,
                        arguments = listOf(navArgument("scannedQRCode") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val scannedQRCode = backStackEntry.arguments?.getString("scannedQRCode") ?: ""
                        PaymentPage(
                            navController = navController,
                            scannedQRCode = scannedQRCode,
                            fareManager = fareManager,
                            getMatatuIdFromRegistration = getMatatuIdFromRegistration,
                            timeManager = timeManager,
                            weatherManager = weatherManager,
                        )
                    }

                    composable(Routes.QRScannerScreen) {
                        QRScannerScreen(navController) { scannedData ->
                            navController.navigate(Routes.PaymentScreen.replace("{scannedQRCode}", scannedData))
                        }
                    }
                    composable(
                        Routes.PaymentPage,
                        arguments = listOf(navArgument("scannedQRCode") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val scannedQRCode = backStackEntry.arguments?.getString("scannedQRCode") ?: ""
                        PaymentPage(
                            navController = navController,
                            scannedQRCode = scannedQRCode,
                            fareManager = fareManager,
                            weatherManager = weatherManager,
                            timeManager = timeManager,
                            getMatatuIdFromRegistration = { registration, callback ->
                                val matatuId = getMatatuIdFromRegistration(registration)
                                callback(matatuId.toString())
                            }
                        )
                    }

                }
            }
        }
    }

    private fun getMatatuIdFromRegistration(registration: String) {

    }
}
