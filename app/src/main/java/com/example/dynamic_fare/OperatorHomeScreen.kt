package com.example.dynamic_fare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamic_fare.data.MatatuRepository

@Composable
fun OperatorHomeScreen(navController: NavController, operatorId: String) {
    var matatuList by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(operatorId) {
        MatatuRepository.fetchMatatusForOperator(operatorId) { fetchedMatatus ->
            matatuList = fetchedMatatus
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Operator Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))


                IconButton(onClick = { navController.navigate("registration_screen/$operatorId") }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Matatu",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Display list of registered Matatus
            matatuList.forEach { matatuRegNumber ->
                MatatuItem(navController, operatorId, matatuRegNumber)
            }
        }
    }
}

@Composable
fun MatatuItem(navController: NavController, operatorId: String, regNumber: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate("operatorHome/$operatorId/$regNumber")
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Matatu: $regNumber", fontSize = 18.sp)
        }
    }
}
