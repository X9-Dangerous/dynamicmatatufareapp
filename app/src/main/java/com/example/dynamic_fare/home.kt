package com.example.dynamic_fare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamicmataufareapp.R



@Composable
fun MatatuEstimateScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding(), // Ensures it is above system buttons
//        horizontalArrangement = Arrangement.SpaceAround,
//        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button and Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Fare Estimate",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Map Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Map Placeholder", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar
        var searchText by remember { mutableStateOf("") }
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text(text = "Where to?", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp)
        )
        FooterWithIcons()
    }

}
@Preview(showBackground = true)
@Composable
fun MatatuEstimateScreenPreview() {
    MatatuEstimateScreen()
}

