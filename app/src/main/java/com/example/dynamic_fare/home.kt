package com.example.dynamic_fare

import androidx.compose.ui.tooling.preview.Preview
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavOptions
import com.example.dynamic_fare.R
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MatatuEstimateScreen(navController: NavController = rememberNavController()) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding(),
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
            AndroidView(factory = { context: Context ->
                MapView(context).apply {
                    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                    controller.setZoom(15.0)
                    val startPoint = GeoPoint(-1.286389, 36.817223) // Example coordinates for Nairobi, Kenya
                    controller.setCenter(startPoint)
                    val marker = Marker(this)
                    marker.position = startPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Matatu Stop"
                    overlays.add(marker)
                }
            }, modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Where to" button with proper navigation options
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    try {
                        // Using the navigation pattern consistent with the app
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(Routes.MatatuEstimateScreen, false)
                            .setLaunchSingleTop(true)
                            .build()
                        
                        navController.navigate(Routes.RouteSelectionScreen, navOptions)
                    } catch (e: Exception) {
                        println("Navigation error: ${e.message}")
                    }
                },
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search_button),
                    contentDescription = "Search",
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Where to?",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        FooterWithIcons(navController)
    }
}

@Preview(showBackground = true)
@Composable
fun MatatuEstimateScreenPreview() {
    MatatuEstimateScreen()
}
