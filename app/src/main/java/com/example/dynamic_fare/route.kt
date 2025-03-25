package com.example.dynamic_fare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun RouteSelectionScreen(navController: NavController = rememberNavController()) {
    val context = LocalContext.current
    
    // State for the input fields
    var startingPoint by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Bar with Back Arrow and Title
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { 
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            println("Navigation error: ${e.message}")
                        }
                    }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Your route",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Starting Point Input - now interactive
        RouteInputField(
            label = "Starting point",
            value = startingPoint,
            onValueChange = { startingPoint = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Destination Input - now interactive
        RouteInputField(
            label = "Destination",
            value = destination,
            onValueChange = { destination = it }
        )
    }
}

@Composable
fun RouteInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_button),
                contentDescription = "Search",
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFF2196F3),
            unfocusedIndicatorColor = Color.Gray
        )
    )
}

@Preview(showBackground = true)
@Composable
fun RouteSelectionScreenPreview() {
    RouteSelectionScreen()
}
