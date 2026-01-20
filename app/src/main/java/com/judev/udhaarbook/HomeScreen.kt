package com.judev.udhaarbook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.judev.udhaarbook.data.Payment
import com.judev.udhaarbook.data.Purchase
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeHeader(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    totalCredit: Double,
    totalPaid: Double,
    accountsCount: Int,
    grandTotal: Double
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedDate, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    calendar.time = try { sdf.parse(selectedDate) ?: Date() } catch(e: Exception) { Date() }
                    calendar.add(Calendar.DATE, -1)
                    onDateChange(sdf.format(calendar.time))
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = {
                    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    calendar.time = try { sdf.parse(selectedDate) ?: Date() } catch(e: Exception) { Date() }
                    calendar.add(Calendar.DATE, 1)
                    onDateChange(sdf.format(calendar.time))
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            Row {
                IconButton(onClick = onFilterClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onSortClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("Credit", "${totalCredit}₹", Color(0xFFE57373))
            SummaryItem("Paid", "${totalPaid}₹", Color(0xFF81C784))
            SummaryItem("Accounts", accountsCount.toString(), Color(0xFF4FC3F7))
            SummaryItem("Total", "${grandTotal}₹", MaterialTheme.colorScheme.onBackground)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val database = LocalDatabase.current
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())) }
    var filterBy by remember { mutableStateOf("all") } // "all", "paid", "credit"
    var sortBy by remember { mutableStateOf("date") } // "name", "date", "amount"
    
    val purchases by database.accountDao().getAllPurchases().collectAsState(initial = emptyList())
    val payments by database.accountDao().getAllPayments().collectAsState(initial = emptyList())
    val accounts by database.accountDao().getAllAccounts().collectAsState(initial = emptyList())

    val totalCredit = purchases.sumOf { it.amount }
    val totalPaid = payments.sumOf { it.amount }
    val grandTotal = totalCredit - totalPaid

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredItems = remember(purchases, payments, selectedDate, filterBy, sortBy) {
        val all = mutableListOf<HomeFeedItem>()
        if (filterBy == "all" || filterBy == "credit") {
            all.addAll(purchases.filter { it.date == selectedDate }.map { HomeFeedItem.PurchaseItem(it) })
        }
        if (filterBy == "all" || filterBy == "paid") {
            all.addAll(payments.filter { it.date == selectedDate }.map { HomeFeedItem.PaymentItem(it) })
        }

        when (sortBy) {
            "name" -> all.sortedBy { 
                when(it) {
                    is HomeFeedItem.PurchaseItem -> it.purchase.accountName
                    is HomeFeedItem.PaymentItem -> it.payment.accountName
                }
            }
            "amount" -> all.sortedByDescending { 
                when(it) {
                    is HomeFeedItem.PurchaseItem -> it.purchase.amount
                    is HomeFeedItem.PaymentItem -> it.payment.amount
                }
            }
            else -> all.sortedByDescending { 
                when(it) {
                    is HomeFeedItem.PurchaseItem -> it.purchase.timestamp
                    is HomeFeedItem.PaymentItem -> it.payment.timestamp
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box {
            HomeHeader(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                onFilterClick = { showFilterMenu = true },
                onSortClick = { showSortMenu = true },
                totalCredit = totalCredit,
                totalPaid = totalPaid,
                accountsCount = accounts.size,
                grandTotal = grandTotal
            )
            
            DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                DropdownMenuItem(text = { Text("All") }, onClick = { filterBy = "all"; showFilterMenu = false })
                DropdownMenuItem(text = { Text("Credit Only") }, onClick = { filterBy = "credit"; showFilterMenu = false })
                DropdownMenuItem(text = { Text("Paid Only") }, onClick = { filterBy = "paid"; showFilterMenu = false })
            }
            
            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                DropdownMenuItem(text = { Text("Sort by Date") }, onClick = { sortBy = "date"; showSortMenu = false })
                DropdownMenuItem(text = { Text("Sort by Name") }, onClick = { sortBy = "name"; showSortMenu = false })
                DropdownMenuItem(text = { Text("Sort by Amount") }, onClick = { sortBy = "amount"; showSortMenu = false })
            }
        }
        
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No transactions on $selectedDate", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = filteredItems,
                    key = { item ->
                        when(item) {
                            is HomeFeedItem.PurchaseItem -> "pur_${item.purchase.id}"
                            is HomeFeedItem.PaymentItem -> "pay_${item.payment.id}"
                        }
                    }
                ) { item ->
                    when(item) {
                        is HomeFeedItem.PurchaseItem -> PurchaseItem(item.purchase)
                        is HomeFeedItem.PaymentItem -> PaymentItem(item.payment)
                    }
                }
            }
        }
    }
}

sealed class HomeFeedItem {
    data class PurchaseItem(val purchase: Purchase) : HomeFeedItem()
    data class PaymentItem(val payment: Payment) : HomeFeedItem()
}

@Composable
fun PurchaseItem(purchase: Purchase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD54F)),
                contentAlignment = Alignment.Center
            ) {
                if (purchase.accountProfile != null) {
                    AsyncImage(
                        model = File(purchase.accountProfile),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(purchase.accountName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(purchase.itemName, color = Color.Gray, fontSize = 12.sp)
                Text(purchase.date, color = Color.Gray, fontSize = 10.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("Credit", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${purchase.amount}₹", color = Color(0xFFE57373), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("ID: ${purchase.id}", color = Color.Gray, fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun PaymentItem(payment: Payment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF81C784)),
                contentAlignment = Alignment.Center
            ) {
                if (payment.accountProfile != null) {
                    AsyncImage(
                        model = File(payment.accountProfile),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(payment.accountName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Payment Received", color = Color.Gray, fontSize = 12.sp)
                Text(payment.date, color = Color.Gray, fontSize = 10.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("Paid", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${payment.amount}₹", color = Color(0xFF81C784), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("ID: ${payment.id}", color = Color.Gray, fontSize = 8.sp)
            }
        }
    }
}
