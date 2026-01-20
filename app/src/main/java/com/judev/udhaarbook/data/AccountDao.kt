package com.judev.udhaarbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>

    @Insert
    suspend fun insertAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: Int): Account?

    @Insert
    suspend fun insertPurchase(purchase: Purchase)

    @Query("SELECT * FROM purchases WHERE accountId = :accountId")
    fun getPurchasesForAccount(accountId: Int): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE accountId = :accountId AND date = :date")
    fun getPurchasesByDate(accountId: Int, date: String): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE accountId = :accountId AND timestamp BETWEEN :start AND :end")
    suspend fun getPurchasesInRange(accountId: Int, start: Long, end: Long): List<Purchase>

    @Query("SELECT * FROM purchases")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Insert
    suspend fun insertPayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE accountId = :accountId")
    fun getPaymentsForAccount(accountId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("UPDATE accounts SET depositval = :newDeposit WHERE id = :accountId")
    suspend fun updateAccountDeposit(accountId: Int, newDeposit: String)

    @Query("UPDATE accounts SET purchaseCount = purchaseCount + 1, totalPurchases = totalPurchases + :amount, remainingBalance = remainingBalance + :amount WHERE id = :accountId")
    suspend fun updateAccountOnPurchase(accountId: Int, amount: Double)

    @Query("UPDATE accounts SET totalPaid = totalPaid + :amount, remainingBalance = remainingBalance - :amount WHERE id = :accountId")
    suspend fun updateAccountOnPayment(accountId: Int, amount: Double)
    
    @Transaction
    suspend fun insertPurchaseAndUpdateAccount(purchase: Purchase) {
        insertPurchase(purchase)
        updateAccountOnPurchase(purchase.accountId, purchase.amount)
    }

    @Transaction
    suspend fun insertPaymentAndUpdateAccount(payment: Payment) {
        insertPayment(payment)
        updateAccountOnPayment(payment.accountId, payment.amount)
    }

    @Insert
    suspend fun insertReport(report: Report)

    @Query("SELECT * FROM reports ORDER BY id DESC")
    fun getAllReports(): Flow<List<Report>>

    @Delete
    suspend fun deleteReport(report: Report)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("DELETE FROM purchases WHERE accountId = :accountId")
    suspend fun deletePurchasesByAccountId(accountId: Int)

    @Query("DELETE FROM payments WHERE accountId = :accountId")
    suspend fun deletePaymentsByAccountId(accountId: Int)

    @Transaction
    suspend fun deleteAccountAndData(account: Account) {
        deletePurchasesByAccountId(account.id)
        deletePaymentsByAccountId(account.id)
        deleteAccount(account)
    }

    @Insert
    suspend fun insertChatMessage(message: ChatHistory)

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatHistory>>

    @Query("DELETE FROM chat_history")
    suspend fun clearChatHistory()
}
