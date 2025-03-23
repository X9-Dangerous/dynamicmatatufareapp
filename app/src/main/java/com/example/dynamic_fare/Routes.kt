package com.example.dynamic_fare
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object Routes {
    val MatatuEstimateScreen= "MatatuEstimateScreen"
    val LoginScreenContent= "LoginScreenContent"
    val PaymentPage= "PaymentPage"
    val RouteSelectionScreen= "RouteSelectionScreen"
    val SignUpScreen= "SignUpScreen"
    val FooterWithIcons = "FooterWithIcons"
}


@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreenContent(navController) }
        composable("operatorHome") { OperatorHomeScreen() }
        composable("clientHome") { ClientHomeScreen() }
        // Add more routes as needed
    }
}