package com.example.dynamic_fare

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController // ✅ Added missing import
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

@Composable
fun DisplayInfoScreen(navController: NavController) {
    var regNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val qrBitmap by remember(regNumber) { mutableStateOf(generateQRCodeBitmap(regNumber)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Matatu Info",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            "Registration Number:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        BasicTextField(
            value = regNumber,
            onValueChange = { regNumber = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "QR Code:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        qrBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size(150.dp)
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Payment type:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        BasicTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
    }
}

fun generateQRCodeBitmap(content: String): Bitmap? {
    return if (content.isNotBlank()) {
        try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDisplayInfoScreen() {

    val navController = androidx.navigation.compose.rememberNavController()
    DisplayInfoScreen(navController)
}
