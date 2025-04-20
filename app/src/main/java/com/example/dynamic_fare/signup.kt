package com.example.dynamic_fare

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController

@Composable
fun SignUpScreen(navController: NavController, viewModel: SignUpViewModel) {
    val uiState by remember { mutableStateOf(SignUpUiState()) }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { uiState.name = it },
                    label = { Text("First Name", color=Color.Black) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.surname,
                    onValueChange = { uiState.surname = it },
                    label = { Text("Surname",color=Color.Black) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { uiState.phone = it },
                    label = { Text("Phone Number",color=Color.Black) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { uiState.email = it },
                    label = { Text("Email Address",color=Color.Black) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { uiState.password = it },
                    label = { Text("Password", color=Color.Black) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { uiState.confirmPassword = it },
                    label = { Text("Confirm Password", color=Color.Black) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("Select Role", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = uiState.selectedRole == "Matatu Operator",
                        onClick = { uiState.selectedRole = "Matatu Operator" }
                    )
                    Text("Matatu Operator", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(
                        selected = uiState.selectedRole == "Matatu Client",
                        onClick = { uiState.selectedRole = "Matatu Client" }
                    )
                    Text("Matatu Client")
                }
                if (uiState.selectedRole.isBlank()) {
                    Text("Please select a role to continue.", color = Color.Red, fontSize = 12.sp)
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = uiState.termsAccepted,
                        onCheckedChange = { uiState.termsAccepted = it }
                    )
                    Text("I agree to Terms & Conditions")
                }
            }

            item {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            item {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            viewModel.signUpUser(
                                uiState.name,
                                uiState.surname,
                                uiState.phone,
                                uiState.email,
                                uiState.password,
                                uiState.confirmPassword,
                                uiState.selectedRole,
                                uiState.termsAccepted
                            ) {
                                navController.navigate("login")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)) // Purple button
                    ) {
                        Text("CONTINUE")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Have an account? Log in",
                    color = Color.Blue,
                    modifier = Modifier.clickable { navController.navigate("login") }
                )
            }
        }
    }}