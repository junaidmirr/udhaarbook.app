package com.judev.udhaarbook

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
    val id: Int,
    val name: String,
    val date: String,
    val amount: String,
    val type: String = "Credit"
)

val sampleTransactions = listOf(
    Transaction(1, "Alex jones", "12-Jun-26 11:52 pm", "100₹"),
    Transaction(2, "Deen jac", "12-Jun-26 11:52 pm", "100₹"),
    Transaction(3, "Kate est", "12-Jun-26 11:52 pm", "100₹"),
    Transaction(4, "Jack rest", "12-Jun-26 11:52 pm", "100₹"),
    Transaction(5, "Ilif navel", "12-Jun-26 11:52 pm", "100₹"),
    Transaction(6, "Pastin lee", "12-Jun-26 11:52 pm", "100₹"),
    Transaction(7, "Mast mee", "12-Jun-26 11:52 pm", "100₹")
)
