package com.example.dynamic_fare.ui.screens

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamic_fare.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isDarkMode by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("en") }

    // Load user-specific settings from DataStore
    LaunchedEffect(userId) {
        SettingsDataStore.getTheme(context, userId).collect { isDarkMode = it }
        SettingsDataStore.getLanguage(context, userId).collect { selectedLanguage = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // ✅ Theme Toggle (User-Specific)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        isDarkMode = it
                        coroutineScope.launch { SettingsDataStore.saveTheme(context, userId, it) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Language Dropdown (User-Specific)
            var expanded by remember { mutableStateOf(false) }
            val languages = mapOf(
                "en" to "English",
                "sw" to "Kiswahili",
                "fr" to "Français",
                "it" to "Italiano",
                "es" to "Español"
            )

            Box {
                Button(onClick = { expanded = true }) {
                    Text("Language: ${languages[selectedLanguage]}")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                expanded = false
                                selectedLanguage = code
                                coroutineScope.launch { SettingsDataStore.saveLanguage(context, userId, code) }
                                setLocale(context, code)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Function to change the app's language
fun setLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}
