package com.example.dynamic_fare

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object Routes {
    const val LoginScreenContent = "login"
   const val MatatuEstimateScreen = "clientHome"
    const val PaymentPage = "PaymentPage"
    const val RouteSelectionScreen = "RouteSelectionScreen"
    const val SignUpScreen = "signup"
    const val FooterWithIcons = "FooterWithIcons"
    const val OperatorHome = "operatorHome"
   const  val PasswordRecoveryScreen = "passwordRecovery"
}

@Composable
fun AppNavigation(navController: NavHostController, signUpViewModel: SignUpViewModel) {
    NavHost(navController = navController, startDestination = Routes.LoginScreenContent) {
        composable(Routes.LoginScreenContent) { LoginScreenContent(navController) }
        composable(Routes.OperatorHome) { DisplayInfoScreen(navController) }
        composable(Routes.MatatuEstimateScreen) { MatatuEstimateScreen(navController) }
        composable(Routes.SignUpScreen) { SignUpScreen(navController, signUpViewModel) }
        composable(Routes.PasswordRecoveryScreen) { PasswordRecoveryScreen(navController) }
        composable(Routes.RouteSelectionScreen) { RouteSelectionScreen(navController) }
    }
}
