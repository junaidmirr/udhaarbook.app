package com.judev.udhaarbook.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val icon: ImageVector, val title: String) {
    Home(Icons.Default.Home, "Home"),
    Accounts(Icons.Default.Groups, "Accounts"),
    Chat(Icons.AutoMirrored.Filled.Chat, "Chats"),
    Assignment(Icons.AutoMirrored.Filled.Assignment, "Reports")
}

data class Transaction(
    val id: Int = 0,
    val name: String = "",
    val date: String = "",
    val amount: String = "",
    val type: String = "Credit"
)
