package com.example.dynamic_fare

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dynamic_fare.Routes.PaymentScreen
import com.example.dynamic_fare.ui.*
import com.example.dynamic_fare.ui.screens.*
import com.example.dynamic_fare.ui.screens.RegistrationScreen
import com.example.dynamic_fare.FareManager
import com.example.dynamic_fare.TimeManager
import com.example.dynamic_fare.WeatherManager

object Routes {
    const val LoginScreenContent = "login"
    const val SignUpScreen = "signup"
    const val MatatuEstimateScreen = "clientHome"
    const val PasswordRecoveryScreen = "passwordRecovery"
    const val ClientProfile = "clientProfile"
    const val OperatorHome = "operatorHome/{operatorId}"
    const val RegistrationScreen = "registration_screen/{operatorId}?fleetId={fleetId}"
    const val ChooseFleetDialog = "chooseFleet/{operatorId}"
    const val FleetRegistrationScreen = "fleetRegistration/{operatorId}"
    const val FleetDetailsScreen = "fleetDetails/{fleetId}"
    const val ProfileScreen = "profile/{userId}"
    const val SettingsScreen = "settings/{userId}"
    const val MatatuInfoScreen = "matatuInfoScreen/{operatorId}/{matatuId}"
    const val MatatuDetailsScreen = "matatuDetails/{matatuId}"
    const val FareDetailsScreen = "fareDetails/{matatuId}"
    const val QRScannerScreen = "qrScanner"
    const val PaymentScreen = "paymentScreen/{scannedQRCode}"
    const val FareTabbedScreen = "fareTabbedScreen/{matatuId}"
    const val FleetAndFareTabs = "fleetAndFare/{fleetId}"
    const val SetFaresScreen = "setFares/{matatuId}"
    const val FareDisplayScreen = "fareDisplay/{matatuId}"
    const val PaymentPage = "paymentPage/{scannedQRCode}"
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
        composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController) }
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

        composable(Routes.OperatorHome, arguments = listOf(navArgument("operatorId") { type = NavType.StringType })) {
                backStackEntry -> OperatorHomeScreen(navController, backStackEntry.arguments?.getString("operatorId") ?: "")
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

        composable(Routes.FareDetailsScreen, arguments = listOf(navArgument("matatuId") { type = NavType.StringType })) {
                backStackEntry -> FareDetailsScreen(navController, backStackEntry.arguments?.getString("matatuId") ?: "")
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
                    callback(matatuId)
                }
            )
        }

    }
}
