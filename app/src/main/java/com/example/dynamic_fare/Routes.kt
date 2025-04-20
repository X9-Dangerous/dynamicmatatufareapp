package com.example.dynamic_fare

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dynamic_fare.ui.*
import com.example.dynamic_fare.ui.screens.*
import com.example.dynamic_fare.ui.screens.RegistrationScreen
import com.example.dynamic_fare.ui.screens.AccessibilitySettingsScreen
import com.example.dynamic_fare.ui.screens.SettingsScreen

object Routes {
    const val LoginScreenContent = "login"
    const val SignUpScreen = "signup"
    const val MatatuEstimateScreen = "clientHome"
    const val PasswordRecoveryScreen = "passwordRecovery"
    const val OperatorHome = "operatorHomeScreen/{operatorId}"
    const val HomeScreen = "home/{userId}"
    const val QRScannerScreen = "qrScanner/{userId}"
    const val RegistrationScreen = "registration/{operatorId}"
    const val ChooseFleetDialog = "chooseFleet/{operatorId}"
    const val FleetRegistrationScreen = "fleetRegistration/{operatorId}"
    const val FleetDetailsScreen = "fleetDetails/{fleetId}"
    const val ProfileScreen = "profile/{userId}"
    const val MatatuInfoScreen = "matatuInfoScreen/{operatorId}/{matatuId}"
    const val MatatuDetailsScreen = "matatuDetails/{matatuId}"
    const val FareDetailsScreen = "fareDetails/{matatuId}"
    const val FareTabbedScreen = "fare_tabbed_screen/{matatuId}"
    const val FleetAndFareTabs = "fleetAndFare/{fleetId}"
    const val SetFaresScreen = "setFares/{matatuId}"
    const val FareDisplayScreen = "fareDisplay/{matatuId}"
    const val PaymentPage = "paymentPage/{scannedQRCode}/{userId}"
    const val NotificationsScreen = "notifications/{userId}"
    const val SettingsScreen = "settings/{userId}"
    const val AccessibilitySettingsScreen = "accessibility/{userId}"
    const val ClientProfileScreen = "clientProfile/{userId}"
    const val SetNewPasswordScreen = "setNewPassword/{email}"

    fun matatuEstimateRoute(): String = "clientHome"
    fun operatorHomeRoute(operatorId: String): String = "operatorHomeScreen/$operatorId"
    fun homeRoute(userId: String): String = "home/$userId"
    fun qrScannerRoute(userId: String): String = "qrScanner/$userId"
    fun registrationRoute(operatorId: String, fleetId: String = ""): String = "registration/$operatorId?fleetId=$fleetId"
    fun fleetRegistrationRoute(operatorId: String): String = "fleetRegistration/$operatorId"
    fun fleetDetailsRoute(fleetId: String): String = "fleetDetails/$fleetId"
    fun fareTabbedRoute(matatuId: String): String = "fare_tabbed_screen/$matatuId"
    fun profileRoute(userId: String): String = "profile/$userId"
    fun paymentPageWithQRCode(scannedQRCode: String, userId: String): String = "paymentPage/$scannedQRCode/$userId"
    fun notificationsRoute(userId: String): String = "notifications/$userId"
    fun clientProfileRoute(userId: String): String = "clientProfile/$userId"
    fun settingsRoute(userId: String): String = "settings/$userId"
    fun accessibilitySettingsRoute(userId: String): String = "accessibility/$userId"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    signUpViewModel: SignUpViewModel,
    fareManager: FareManager,
    getMatatuIdFromRegistration: (String) -> String,
    timeManager: TimeManager,
    weatherManager: WeatherManager
) {
    NavHost(navController = navController, startDestination = Routes.LoginScreenContent) {
        composable(Routes.LoginScreenContent) { LoginScreenContent(navController) }
        composable(Routes.SignUpScreen) { SignUpScreen(navController, signUpViewModel) }
        composable(
            route = Routes.MatatuEstimateScreen,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MatatuEstimateScreen(navController)
        }
        composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }
        composable(Routes.FareTabbedScreen) { backStackEntry ->
            val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
            FareTabbedScreen(navController, matatuId)
        }
        composable(Routes.FareDisplayScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) { backStackEntry ->
            val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
            FareDisplayScreen(matatuId)
        }

        composable(Routes.ProfileScreen, arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                backStackEntry -> ProfileScreen(navController, backStackEntry.arguments?.getString("userId") ?: "")
        }

        composable(Routes.SettingsScreen, arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                backStackEntry -> SettingsScreen(navController, backStackEntry.arguments?.getString("userId") ?: "")
        }

        composable(
            route = "operatorHomeScreen/{operatorId}",
            arguments = listOf(navArgument("operatorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId")
            OperatorHomeScreen(navController, operatorId = operatorId ?: "")
        }

        composable(Routes.FleetRegistrationScreen, arguments = listOf(navArgument("operatorId") { type = NavType.StringType })) {
                backStackEntry -> FleetRegistrationScreen(navController, backStackEntry.arguments?.getString("operatorId") ?: "")
        }

        composable(Routes.FleetDetailsScreen, arguments = listOf(navArgument("fleetId") { type = NavType.StringType })) {
                backStackEntry -> FleetDetailsScreen(navController, backStackEntry.arguments?.getString("fleetId") ?: "")
        }

        composable(Routes.RegistrationScreen, arguments = listOf(
            navArgument("operatorId") { type = NavType.StringType },
            navArgument("fleetId") { type = NavType.StringType; defaultValue = "" }
        )) {
                backStackEntry -> RegistrationScreen(navController, backStackEntry.arguments?.getString("operatorId") ?: "")
        }

        composable(Routes.SetFaresScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) { backStackEntry ->
            val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
            SetFaresScreen(matatuId)
        }

        composable(Routes.MatatuDetailsScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) {
                backStackEntry -> MatatuDetailsScreen(navController, backStackEntry.arguments?.getString("matatuId") ?: "")
        }
        composable(Routes.FleetAndFareTabs, arguments = listOf(navArgument("fleetId") { type = NavType.StringType })) {
                backStackEntry ->
            val fleetId = backStackEntry.arguments?.getString("fleetId") ?: ""
            FleetAndFareTabs(navController, fleetId)
        }
        composable(
            Routes.PaymentPage,
            arguments = listOf(
                navArgument("scannedQRCode") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val scannedQRCode = backStackEntry.arguments?.getString("scannedQRCode") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            PaymentPage(
                navController = navController,
                scannedQRCode = scannedQRCode,
                fareManager = fareManager,
                weatherManager = weatherManager,
                timeManager = timeManager,
                getMatatuIdFromRegistration = { regNo, callback ->
                    fareManager.getMatatuIdFromRegistration(regNo, callback)
                },
                onPaymentSuccess = { /* handle payment success */ },
                userId = userId
            )
        }
        composable(
            route = Routes.QRScannerScreen,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            QRScannerScreen(
                navController = navController,
                userId = userId
            )
        }
        composable(Routes.FareDetailsScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) {
                backStackEntry -> FareDetailsScreen(navController, backStackEntry.arguments?.getString("matatuId") ?: "")
        }

        composable(
            route = "detailMatatu/{matatuId}",
            arguments = listOf(navArgument("matatuId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
            MatatuDetailsScreen(navController = navController, matatuId = matatuId)
        }

        composable(
            route = Routes.NotificationsScreen,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            PaymentHistoryScreen(navController, userId)
        }

        composable(
            route = Routes.SettingsScreen,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SettingsScreen(navController, userId)
        }

        composable(
            route = Routes.AccessibilitySettingsScreen,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AccessibilitySettingsScreen(navController, userId)
        }

        composable(
            route = Routes.ProfileScreen,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(navController, userId)
        }
        composable(
            route = Routes.SetNewPasswordScreen,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            SetNewPasswordScreen(navController, email)
        }
    }
}
