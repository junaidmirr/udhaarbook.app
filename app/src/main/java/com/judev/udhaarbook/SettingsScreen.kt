package com.judev.udhaarbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeChanged: (String) -> Unit,
    onDeleteAccount: () -> Unit
) {
    val sessionManager = LocalSession.current
    val syncManager = LocalSyncManager.current
    val scope = rememberCoroutineScope()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val currentTheme = sessionManager.getTheme()

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Full Account") },
            text = { Text("Are you sure? This will permanently delete your profile, transactions, and ALL data from our servers. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val success = syncManager.deleteFullUserAccount()
                        if (success) {
                            showDeleteConfirm = false
                            onDeleteAccount()
                        }
                    }
                }) { Text("Delete Permanently", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("App Theme") },
            text = {
                Column {
                    ThemeOptionItem("System Default", currentTheme == "system") {
                        sessionManager.saveTheme("system")
                        onThemeChanged("system")
                        showThemeDialog = false
                    }
                    ThemeOptionItem("Light", currentTheme == "light") {
                        sessionManager.saveTheme("light")
                        onThemeChanged("light")
                        showThemeDialog = false
                    }
                    ThemeOptionItem("Dark", currentTheme == "dark") {
                        sessionManager.saveTheme("dark")
                        onThemeChanged("dark")
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SettingItem(Icons.Default.Palette, "App Theme", "Current: ${currentTheme.replaceFirstChar { it.uppercase() }}") {
                showThemeDialog = true
            }
            
            SettingItem(Icons.Default.Language, "App Language", "English") {
                // Placeholder for language
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete My Account", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ThemeOptionItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
