package com.example.dynamic_fare

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object Routes {
    val MatatuEstimateScreen = "MatatuEstimateScreen"
    val LoginScreenContent = "LoginScreenContent"
    val PaymentPage = "PaymentPage"
    val RouteSelectionScreen = "RouteSelectionScreen"
    val SignUpScreen = "signup"  // route for sign up
    val FooterWithIcons = "FooterWithIcons"
}

@Composable
fun AppNavigation(navController: NavHostController, signUpViewModel: SignUpViewModel) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("operatorHome") { DisplayInfoScreen() }
        composable("clientHome") { MatatuEstimateScreen() }
        composable("signup") { SignUpScreen(navController, signUpViewModel) }
    }
}
