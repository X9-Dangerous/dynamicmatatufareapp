package com.example.dynamic_fare



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.dynamic_fare.R

class PriceEstimateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PriceEstimateScreen(onBackPressed = { finish() }) // Pass function to handle back button
        }
    }
}

@Composable
fun PriceEstimateScreen(onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back Button
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .size(48.dp)
                .clickable { }
        )

        Spacer(modifier = Modifier.height(50.dp)) // Add spacing before the map

        // Map Placeholder (Lowered a bit)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.Gray), // Temporary placeholder for the map
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Map Placeholder", color = Color.White, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(40.dp)) // More spacing below the map

        // Price Estimate Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Price", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Ksh 50", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPriceEstimateScreen() {
    PriceEstimateScreen(onBackPressed = {})
}

