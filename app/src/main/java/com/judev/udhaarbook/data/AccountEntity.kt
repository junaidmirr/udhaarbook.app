package com.judev.udhaarbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: Int = 0,
    val name: String = "",
    val profile: String? = null,
    val acmail: String? = null,
    val depositval: String? = null,
    val date: String = "",
    val purchaseCount: Int = 0,
    val totalPurchases: Double = 0.0,
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey val id: Int = 0,
    val accountId: Int = 0,
    val accountName: String = "",
    val accountProfile: String? = null,
    val itemName: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey val id: Int = 0,
    val accountId: Int = 0,
    val accountName: String = "",
    val accountProfile: String? = null,
    val amount: Double = 0.0,
    val date: String = "",
    val month: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountName: String = "",
    val fileName: String = "",
    val filePath: String = "",
    val dateRange: String = "",
    val generatedDate: String = ""
)

@Entity(tableName = "chat_history")
data class ChatHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
