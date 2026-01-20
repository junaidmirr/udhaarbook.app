package com.judev.udhaarbook

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.judev.udhaarbook.data.AccountDatabase
import kotlinx.coroutines.flow.first

class GeminiChatAssistant(
    private val apiKey: String,
    private val database: AccountDatabase
) {
    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    suspend fun generateResponse(userPrompt: String): String {
        val accountDao = database.accountDao()
        
        return try {
            val accounts = accountDao.getAllAccounts().first()
            val purchases = accountDao.getAllPurchases().first()
            val payments = accountDao.getAllPayments().first()

            val contextPrompt = """
                You are "UdhaarBook Assistant", a helpful financial AI. 
                Answer the user's questions based ONLY on this data.
                If asked about something not here, say you don't have that record.
                Be concise and professional.

                --- DATABASE ---
                ACCOUNTS: ${accounts.joinToString { "Name: ${it.name}, Bal: ${it.remainingBalance}₹" }}
                PURCHASES: ${purchases.joinToString { "${it.accountName} bought ${it.itemName} (${it.amount}₹)" }}
                PAYMENTS: ${payments.joinToString { "${it.accountName} paid ${it.amount}₹" }}
                -----------------

                User: $userPrompt
            """.trimIndent()

            val response = model.generateContent(contextPrompt)
            response.text ?: "I couldn't generate a response."
        } catch (e: Exception) {
            if (e.message?.contains("404") == true) {
                "Error: AI model not found. Please ensure your API key is valid and has access to Gemini 2.0 Flash."
            } else {
                "Error: ${e.localizedMessage}"
            }
        }
    }

    suspend fun identifyProduct(bitmap: Bitmap): String? {
        return try {
            val inputContent = content {
                image(bitmap)
                text("Identify this product name. Return ONLY the product name, nothing else. If it's a grocery item, be specific (e.g., 'Milk Packet', 'Bread').")
            }
            val response = model.generateContent(inputContent)
            response.text?.trim()
        } catch (e: Exception) {
            null
        }
    }
}
