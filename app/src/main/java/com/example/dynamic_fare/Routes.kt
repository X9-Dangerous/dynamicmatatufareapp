package com.example.dynamic_fare

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dynamic_fare.ui.*

object Routes {
    const val LoginScreenContent = "login"
    const val MatatuEstimateScreen = "clientHome"
    const val PaymentPage = "PaymentPage"
    const val RouteSelectionScreen = "RouteSelectionScreen"
    const val SignUpScreen = "signup"
    const val FooterWithIcons = "FooterWithIcons"
    const val OperatorHome = "operatorHome/{operatorId}"
    const val PasswordRecoveryScreen = "passwordRecovery"
    const val RegistrationScreen = "registration_screen/{operatorId}"

    // 🟢 Keep the extra routes you added
    const val ChooseFleetDialog = "chooseFleet"
    const val FleetRegistrationScreen = "fleetRegistration/{operatorId}"
    const val MatatuRegistrationScreen = "matatuRegistration/{operatorId}"
}

@Composable
fun AppNavigation(navController: NavHostController, signUpViewModel: SignUpViewModel) {
    NavHost(navController = navController, startDestination = Routes.LoginScreenContent) {
        composable(Routes.LoginScreenContent) { LoginScreenContent(navController) }
        composable(Routes.SignUpScreen) { SignUpScreen(navController, signUpViewModel) }
        composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController) }
        composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }

        // Ensure Operator Home stays
        composable(Routes.OperatorHome) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
            OperatorHomeScreen(navController, operatorId)
        }

        // Registration Screen (Fleet OR Single Matatu Choice)
        composable(Routes.RegistrationScreen) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId") ?: ""
            RegistrationScreen(navController, operatorId)
        }

        // Choose Fleet or Single Matatu (Popup)
        composable(Routes.ChooseFleetDialog) {
            ChooseFleetDialog(
                onDismiss = { navController.popBackStack() },
                onSelection = { isFleet ->
                    val route = if (isFleet) Routes.FleetRegistrationScreen else Routes.MatatuRegistrationScreen
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
    }
}
