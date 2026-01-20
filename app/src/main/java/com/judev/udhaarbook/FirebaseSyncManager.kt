package com.judev.udhaarbook

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.judev.udhaarbook.data.*
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseSyncManager(private val database: com.judev.udhaarbook.data.AccountDatabase) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun uploadAccount(account: com.judev.udhaarbook.data.Account) {
        val userId = getUserId() ?: return
        firestore.collection("users").document(userId)
            .collection("accounts").document(account.id.toString())
            .set(account, SetOptions.merge())
            .await()
    }

    suspend fun uploadPurchase(purchase: com.judev.udhaarbook.data.Purchase) {
        val userId = getUserId() ?: return
        firestore.collection("users").document(userId)
            .collection("purchases").document(purchase.id.toString())
            .set(purchase, SetOptions.merge())
            .await()
    }

    suspend fun uploadPayment(payment: com.judev.udhaarbook.data.Payment) {
        val userId = getUserId() ?: return
        firestore.collection("users").document(userId)
            .collection("payments").document(payment.id.toString())
            .set(payment, SetOptions.merge())
            .await()
    }

    suspend fun deleteAccountFromFirebase(accountId: Int) {
        val userId = getUserId() ?: return
        firestore.collection("users").document(userId)
            .collection("accounts").document(accountId.toString())
            .delete()
            .await()
    }

    suspend fun uploadProfileImage(file: File): String? {
        val userId = getUserId() ?: return null
        val ref = storage.reference.child("profiles/$userId.jpg")
        return try {
            ref.putFile(Uri.fromFile(file)).await()
            val url = ref.downloadUrl.await().toString()
            firestore.collection("users").document(userId)
                .set(mapOf("profileImageUrl" to url), SetOptions.merge())
                .await()
            url
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error uploading image: ${e.message}")
            null
        }
    }

    suspend fun deleteFullUserAccount(): Boolean {
        val user = auth.currentUser ?: return false
        val userId = user.uid
        return try {
            firestore.collection("users").document(userId).delete().await()
            user.delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting user: ${e.message}")
            false
        }
    }

    suspend fun syncFromFirebase() {
        val userId = getUserId() ?: return
        val dao = database.accountDao()

        try {
            // Restore Accounts
            val accountsSnapshot = firestore.collection("users").document(userId)
                .collection("accounts").get().await()
            for (doc in accountsSnapshot.documents) {
                val account = doc.toObject(com.judev.udhaarbook.data.Account::class.java)
                if (account != null) dao.insertAccount(account)
            }

            // Restore Purchases
            val purchasesSnapshot = firestore.collection("users").document(userId)
                .collection("purchases").get().await()
            for (doc in purchasesSnapshot.documents) {
                val purchase = doc.toObject(com.judev.udhaarbook.data.Purchase::class.java)
                if (purchase != null) dao.insertPurchase(purchase)
            }

            // Restore Payments
            val paymentsSnapshot = firestore.collection("users").document(userId)
                .collection("payments").get().await()
            for (doc in paymentsSnapshot.documents) {
                val payment = doc.toObject(com.judev.udhaarbook.data.Payment::class.java)
                if (payment != null) dao.insertPayment(payment)
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error restoring data: ${e.message}")
        }
    }
}
