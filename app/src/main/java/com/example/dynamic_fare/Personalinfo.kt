package com.example.dynamic_fare

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.dynamic_fare.R  // Make sure you import your R file

@Composable
fun PersonalInfoScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val userName = user?.displayName ?: "Unknown Name"
    val userPhone = user?.phoneNumber ?: "Not Available"
    val userEmail = user?.email ?: "Not Available"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar with Custom Back Button and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Back Button (Drawable)
            val backIcon: Painter = painterResource(id = R.drawable.ic_arrow_back)  // Replace with your drawable file

            Image(
                painter = backIcon,
                contentDescription = "Back",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() } // Makes it clickable
            )

            // Centered Title
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Personal Info",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(40.dp))

        // User Information
        UserInfoItem(label = "Name", value = userName)
        UserInfoItem(label = "Phone", value = userPhone)
        UserInfoItem(label = "Email", value = userEmail)
    }
}

@Composable
fun UserInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = value, fontSize = 14.sp)
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
}
@Preview(showBackground = true)
@Composable
fun PersonalInfoScreenPreview() {
    PersonalInfoScreen(navController = rememberNavController())
}

