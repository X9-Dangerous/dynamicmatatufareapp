package com.example.dynamic_fare.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun MatatuInfoScreen(navController: NavController, matatuId: String) {
    // This is a placeholder. You would fetch detailed matatu info using matatuId.
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Matatu Detailed Information", fontSize = 24.sp, color = Color.Black)

    }
}
