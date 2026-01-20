package com.judev.udhaarbook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", color = MaterialTheme.colorScheme.onBackground) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "UdhaarBook Privacy Policy",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Welcome to UdhaarBook. Your privacy is critically important to us.\n\n" +
                "1. Data Collection: We collect account names, email addresses, and transaction history to provide you with a seamless debt-tracking experience.\n\n" +
                "2. Data Storage: Your data is stored locally on your device and synchronized securely with Google Firebase servers to ensure you can recover your data if you switch devices.\n\n" +
                "3. AI Assistant: We use Google Gemini AI to help you analyze your financial data. Your data is sent to Google's servers only for processing your specific queries and is not used for training models.\n\n" +
                "4. Security: We implement industry-standard encryption to protect your account data and profile pictures.\n\n" +
                "5. Account Deletion: You can delete your account and all associated data at any time through the Settings menu. This action is permanent and cannot be undone.\n\n" +
                "6. Contact Us: If you have any questions about this Privacy Policy, please contact our support team.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}
