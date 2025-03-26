package com.example.dynamic_fare

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dynamic_fare.ui.*
import com.example.dynamic_fare.ui.screens.OperatorHomeScreen

object Routes {
    const val LoginScreenContent = "login"
    const val MatatuEstimateScreen = "clientHome"
    const val PaymentPage = "PaymentPage"
    const val RouteSelectionScreen = "RouteSelectionScreen"
    const val SignUpScreen = "signup"
    const val FooterWithIcons = "FooterWithIcons"
    // Operator Home now expects an operatorId argument.
    const val OperatorHome = "operatorHome/{operatorId}"
    const val PasswordRecoveryScreen = "passwordRecovery"
    // Registration screen now takes both operatorId and fleetId.
    const val RegistrationScreen = "registration_screen/{operatorId}/{fleetId}"
    // Extra routes for choosing registration type and separate fleet/matatu registration:
    const val ChooseFleetDialog = "chooseFleet"
    const val FleetRegistrationScreen = "fleetRegistration/{operatorId}"
    const val MatatuRegistrationScreen = "matatuRegistration/{operatorId}"
    const val MatatuInfoScreen = "matatuInfo/{matatuId}"
}

@Composable
fun AppNavigation(navController: NavHostController, signUpViewModel: SignUpViewModel) {
    NavHost(navController = navController, startDestination = Routes.LoginScreenContent) {
        composable(Routes.LoginScreenContent) { LoginScreenContent(navController) }
        composable(Routes.SignUpScreen) { SignUpScreen(navController, signUpViewModel) }
        composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController) }
        composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }
        composable(Routes.FooterWithIcons) { ProfileScreen() }

        // Operator Home Screen – passes operatorId from the nav arguments
        composable(Routes.OperatorHome) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
            OperatorHomeScreen(navController, operatorId)
        }

        // Registration Screen for single matatu OR fleet choice – passes both operatorId and fleetId.
        // fleetId can be an empty string if not used.
        composable(Routes.RegistrationScreen) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
            val fleetId = backStackEntry.arguments?.getString("fleetId") ?: ""
            // Here we call the MatatuRegistrationScreen which you can update to handle both cases.
            MatatuRegistrationScreen(navController, operatorId, if (fleetId.isEmpty()) null else fleetId)
        }

        // Choose Fleet Dialog – a pop-up that asks whether the user wants to register a fleet or a single matatu.
        composable(Routes.ChooseFleetDialog) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
            ChooseFleetDialog(
                onDismiss = { navController.popBackStack() },
                onSelection = { isFleet ->
                    val route = if (isFleet) Routes.FleetRegistrationScreen else Routes.MatatuRegistrationScreen
                    // We append the operatorId as argument.
                    navController.navigate("$route/$operatorId")
                }
            )
        }

        // Fleet Registration Screen
        composable(Routes.FleetRegistrationScreen) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
            FleetRegistrationScreen(navController, operatorId)
        }

        // Matatu Registration Screen (for when not choosing fleet mode)
        composable(Routes.MatatuRegistrationScreen) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
            // In this case, fleetId is not passed so default to null.
            MatatuRegistrationScreen(navController, operatorId, null)
        }

        // Matatu Info Screen
        composable(Routes.MatatuInfoScreen) { backStackEntry ->
            val matatuId = backStackEntry.arguments?.getString("matatuId") ?: ""
            MatatuInfoScreen(navController, matatuId)
        }
    }
}
