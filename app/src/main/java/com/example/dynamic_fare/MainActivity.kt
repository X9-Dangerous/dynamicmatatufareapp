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
import com.example.dynamic_fare.ui.screens.*
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.example.dynamic_fare.ui.ChooseFleetDialog
import com.example.dynamic_fare.ui.FleetRegistrationScreen
import com.example.dynamic_fare.data.SettingsDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance().reference
        val operatorId = auth.currentUser?.uid ?: ""
        val fareManager = FareManager(FirebaseDatabase.getInstance())
        val timeManager = TimeManager()
        val weatherManager = WeatherManager()

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

        val signUpViewModel = ViewModelProvider(this, SignUpViewModelFactory(applicationContext)).get(SignUpViewModel::class.java)

        setContent {
            DynamicMatauFareAppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Routes.LoginScreenContent) {
                    composable(Routes.LoginScreenContent) { LoginScreenContent(navController) }
                    composable(Routes.SignUpScreen) { SignUpScreen(navController, signUpViewModel) }

                    composable(
                        route = Routes.OperatorHome,
                        arguments = listOf(navArgument("operatorId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId")
                        OperatorHomeScreen(navController, operatorId = operatorId ?: "")
                    }
                    
                    composable(
                        route = Routes.HomeScreen,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        MatatuEstimateScreen(navController)
                    }

                    composable(
                        route = Routes.QRScannerScreen,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        QRScannerScreen(navController = navController, userId = userId)
                    }

                    composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController = navController) }
                    composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }

                    composable(
                        route = Routes.MatatuDetailsScreen,
                        arguments = listOf(navArgument("matatuId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
                        MatatuDetailsScreen(navController = navController, matatuId = matatuId)
                    }

                    composable(Routes.FareDetailsScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) { backStackEntry ->
                        FareDetailsScreen(navController, backStackEntry.arguments?.getString("matatuId") ?: "")
                    }

                    composable(Routes.SetFaresScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) { backStackEntry ->
                        val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
                        SetFaresScreen(matatuId)
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
                        route = Routes.PaymentPage,
                        arguments = listOf(
                            navArgument("scannedQRCode") { type = NavType.StringType },
                            navArgument("userId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val scannedQRCode = backStackEntry.arguments?.getString("scannedQRCode") ?: ""
                        val userId = backStackEntry.arguments?.getString("userId") ?: operatorId
                        Log.d("MainActivity", "Payment page userId: $userId")
                        PaymentPage(
                            navController = navController,
                            scannedQRCode = scannedQRCode,
                            fareManager = fareManager,
                            timeManager = timeManager,
                            weatherManager = weatherManager,
                            getMatatuIdFromRegistration = { regNo, callback ->
                                fareManager.getMatatuIdFromRegistration(regNo, callback)
                            },
                            userId = userId
                        )
                    }

                    composable(
                        route = Routes.NotificationsScreen,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        PaymentHistoryScreen(navController = navController, userId = userId)
                    }

                    composable(
                        route = Routes.FleetDetailsScreen,
                        arguments = listOf(navArgument("fleetId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val fleetId = backStackEntry.arguments?.getString("fleetId") ?: ""
                        FleetDetailsScreen(navController = navController, fleetId = fleetId)
                    }

                    composable(
                        route = Routes.AccessibilitySettingsScreen,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        AccessibilitySettingsScreen(navController = navController, userId = userId)
                    }

                    composable(
                        route = Routes.ProfileScreen,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        ProfileScreen(navController = navController, userId = userId)
                    }

                    composable(
                        route = "detailMatatu/{operatorId}/{matatuId}",
                        arguments = listOf(
                            navArgument("operatorId") { type = NavType.StringType },
                            navArgument("matatuId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId")
                        val matatuId = backStackEntry.arguments?.getString("matatuId")
                        MatatuDetailsScreen(navController, matatuId = matatuId ?: "")
                    }

                    composable(
                        route = "registration/{operatorId}",
                        arguments = listOf(
                            navArgument("operatorId") { type = NavType.StringType },
                            navArgument("fleetId") { 
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId")
                        val fleetId = backStackEntry.arguments?.getString("fleetId")
                        RegistrationScreen(navController, operatorId = operatorId ?: "")
                    }

                    composable(
                        route = Routes.FleetRegistrationScreen,
                        arguments = listOf(navArgument("operatorId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val operatorId = backStackEntry.arguments?.getString("operatorId")
                        FleetRegistrationScreen(navController, operatorId = operatorId ?: "")
                    }

                    composable(
                        route = Routes.ClientProfileScreen,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        ClientProfileScreen(navController = navController, userId = userId)
                    }
                }
            }
        }
    }

    private fun getMatatuIdFromRegistration(registration: String) {
        // Implementation here
    }
}
