package com.example.dynamic_fare

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dynamic_fare.data.SettingsDataStore
import com.example.dynamic_fare.datastore.UserSessionDataStore
import com.example.dynamic_fare.ui.screens.*
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.example.dynamic_fare.ui.ChooseFleetDialog
import com.example.dynamic_fare.ui.FleetRegistrationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance().reference
        val operatorId = auth.currentUser?.uid ?: ""
        val fareManager = FareManager(applicationContext)
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
                var showExitDialog by remember { mutableStateOf(false) }
                var backPressCount by remember { mutableStateOf(0) }
                var lastBackPressTime by remember { mutableStateOf(0L) }

                // On app launch, check if a user is already logged in
                LaunchedEffect(Unit) {
                    val email = UserSessionDataStore.getUserEmail(applicationContext).first()
                    val role = UserSessionDataStore.getUserRole(applicationContext).first()
                    if (!email.isNullOrBlank() && !role.isNullOrBlank()) {
                        if (role == "Matatu Operator") {
                            navController.navigate(Routes.operatorHomeRoute(email)) {
                                popUpTo(0)
                            }
                        } else {
                            navController.navigate(Routes.matatuEstimateScreenRoute(email)) {
                                popUpTo(0)
                            }
                        }
                    }
                }

                BackHandler {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime > 2000) {
                        // Reset if too much time has passed
                        backPressCount = 1
                        lastBackPressTime = currentTime
                    } else {
                        backPressCount += 1
                        lastBackPressTime = currentTime
                    }
                    if (backPressCount == 2) {
                        Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                    } else if (backPressCount == 3) {
                        showExitDialog = true
                        backPressCount = 0
                    }
                }

                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = { Text("Exit App") },
                        text = { Text("Are you sure you want to exit?") },
                        confirmButton = {
                            TextButton(onClick = { finishAffinity() }) {
                                Text("Exit")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExitDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }


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
                        MatatuEstimateScreen(navController = navController, userId = userId)
                    }

                    composable(
                        route = Routes.QRScannerScreen,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        QRScannerScreen(navController = navController, userId = userId)
                    }

                    composable(
                        route = Routes.MatatuEstimateScreen + "/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        MatatuEstimateScreen(navController = navController, userId = userId)
                    }

                    composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }

                    composable(
                        route = Routes.MatatuDetailsScreen,
                        arguments = listOf(navArgument("matatuId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
                        MatatuDetailsScreen(navController = navController, matatuId = matatuId ?: "")
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
                                fareManager.getMatatuIdFromRegistration(regNo) { strId ->
                                    callback(strId)
                                }
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
                        FleetDetailsScreen(

                            navController = navController,fleetId = fleetId,
                        )
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

                    // Support for clientHome/{userId} navigation
                    composable(
                        route = "clientHome/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        MatatuEstimateScreen(navController = navController, userId = userId)
                    }
                }
            }
        }
    }

    private fun getMatatuIdFromRegistration(registration: String) {
        // Implementation here
    }
}
