package com.judev.udhaarbook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.judev.udhaarbook.data.Account
import com.judev.udhaarbook.data.Payment
import com.judev.udhaarbook.data.Purchase
import com.judev.udhaarbook.data.Report
import com.judev.udhaarbook.data.AccountDatabase
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(onNavigateToScanner: (Int) -> Unit) {
    val database = LocalDatabase.current
    val syncManager = LocalSyncManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val accounts by database.accountDao().getAllAccounts().collectAsState(initial = emptyList())
    
    var selectedAccountId by remember { mutableIntStateOf(-1) }
    var showDetailModal by remember { mutableStateOf(false) }
    
    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.find { it.id == selectedAccountId }
    }
    
    var showAddPurchaseOptions by remember { mutableStateOf(false) }
    var showAddPurchaseForm by remember { mutableStateOf(false) }
    var showPayCreditForm by remember { mutableStateOf(false) }
    var showViewPurchasesModal by remember { mutableStateOf(false) }
    var showGenerateReportModal by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    
    // AI Scanner Result Handling
    val navBackStackEntry = (context as? MainActivity)?.let { 
        // In a real navigation scenario, we check the backstack for returned results
    }
    
    // We can use a simpler approach for now: handle results in MainActivity and trigger this
    // For this prompt, I will implement the Review Form directly in AccountsScreen

    // Delete Account Dialog
    if (showDeleteAccountConfirm && selectedAccount != null) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure? This will permanently delete this account and all its transaction data.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        database.accountDao().deleteAccountAndData(selectedAccount)
                        syncManager.deleteAccountFromFirebase(selectedAccount.id)
                        showDeleteAccountConfirm = false
                        showDetailModal = false
                        selectedAccountId = -1
                    }
                }) { Text("Yes, Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) { Text("No") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    // Detail Modal
    if (showDetailModal && selectedAccount != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showDetailModal = false 
                selectedAccountId = -1
            },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            AccountDetailContent(
                account = selectedAccount,
                onAddPurchase = { 
                    showDetailModal = false
                    showAddPurchaseOptions = true 
                },
                onPayCredit = { 
                    showDetailModal = false
                    showPayCreditForm = true 
                },
                onViewPurchases = {
                    showDetailModal = false
                    showViewPurchasesModal = true
                },
                onGenerateReport = {
                    showDetailModal = false
                    showGenerateReportModal = true
                },
                onDeleteAccount = {
                    showDeleteAccountConfirm = true
                }
            )
        }
    }

    // Add Purchase Options (AI vs Manual)
    if (showAddPurchaseOptions && selectedAccount != null) {
        ModalBottomSheet(
            onDismissRequest = { showAddPurchaseOptions = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
                Text("Select Input Mode", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        showAddPurchaseOptions = false
                        onNavigateToScanner(selectedAccount.id)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("AI Product Scan", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { 
                        showAddPurchaseOptions = false
                        showAddPurchaseForm = true 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Manual Entry")
                }
            }
        }
    }

    // View Purchases History Modal
    if (showViewPurchasesModal && selectedAccount != null) {
        var selectedDate by remember { mutableStateOf(SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())) }
        val datePickerState = rememberDatePickerState()
        var showDatePicker by remember { mutableStateOf(false) }
        
        val purchases by database.accountDao().getPurchasesByDate(selectedAccount.id, selectedDate).collectAsState(initial = emptyList())

        ModalBottomSheet(
            onDismissRequest = { showViewPurchasesModal = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Purchases on $selectedDate", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    selectedDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(it))
                                }
                                showDatePicker = false
                            }) { Text("OK") }
                        }
                    ) { DatePicker(state = datePickerState) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (purchases.isEmpty()) {
                    Text("No purchases on this date", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(purchases) { purchase ->
                            ListItem(
                                headlineContent = { Text(purchase.itemName, color = MaterialTheme.colorScheme.onSurface) },
                                trailingContent = { Text("${purchase.amount}₹", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) },
                                supportingContent = { Text(purchase.date, fontSize = 12.sp, color = Color.Gray) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }

    // Generate Report Modal
    if (showGenerateReportModal && selectedAccount != null) {
        val dateRangePickerState = rememberDateRangePickerState()
        
        ModalBottomSheet(
            onDismissRequest = { showGenerateReportModal = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Select Date Range", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.weight(1f),
                    title = null,
                    headline = null,
                    showModeToggle = false
                )

                Button(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            scope.launch {
                                val adjustedEnd = end + 86399999L 
                                val purchases = database.accountDao().getPurchasesInRange(selectedAccount.id, start, adjustedEnd)
                                generateExcelReport(context, database, selectedAccount, purchases, start, end)
                                showGenerateReportModal = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null
                ) {
                    Text("Generate Report")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Add Purchase Modal
    if (showAddPurchaseForm && selectedAccount != null) {
        ModalBottomSheet(
            onDismissRequest = { showAddPurchaseForm = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddPurchaseForm(
                onCancel = { showAddPurchaseForm = false },
                onDone = { itemName, amount, date ->
                    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                    val parsedTimestamp = try { sdf.parse(date)?.time } catch(e: Exception) { null } ?: System.currentTimeMillis()
                    
                    val purchase = com.judev.udhaarbook.data.Purchase(
                        id = Random.nextInt(100000, 999999),
                        accountId = selectedAccount.id,
                        accountName = selectedAccount.name,
                        accountProfile = selectedAccount.profile,
                        itemName = itemName,
                        amount = amount,
                        date = date,
                        timestamp = parsedTimestamp
                    )
                    scope.launch {
                        database.accountDao().insertPurchaseAndUpdateAccount(purchase)
                        syncManager.uploadPurchase(purchase)
                        val updatedAccount = database.accountDao().getAccountById(selectedAccount.id)
                        if (updatedAccount != null) syncManager.uploadAccount(updatedAccount)
                        showAddPurchaseForm = false
                    }
                }
            )
        }
    }

    // Pay Credit Modal
    if (showPayCreditForm && selectedAccount != null) {
        ModalBottomSheet(
            onDismissRequest = { showPayCreditForm = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PayCreditForm(
                remainingBal = selectedAccount.remainingBalance,
                onDiscard = { showPayCreditForm = false },
                onDone = { amount ->
                    scope.launch {
                        val payment = com.judev.udhaarbook.data.Payment(
                            id = Random.nextInt(100000, 999999),
                            accountId = selectedAccount.id,
                            accountName = selectedAccount.name,
                            accountProfile = selectedAccount.profile,
                            amount = amount,
                            date = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date()),
                            month = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
                        )
                        database.accountDao().insertPaymentAndUpdateAccount(payment)
                        syncManager.uploadPayment(payment)
                        
                        if (selectedAccount.remainingBalance <= 0.0) {
                            val currentDeposit = selectedAccount.depositval?.toDoubleOrNull() ?: 0.0
                            database.accountDao().updateAccountDeposit(selectedAccount.id, (currentDeposit + amount).toString())
                        }
                        
                        val updatedAccount = database.accountDao().getAccountById(selectedAccount.id)
                        if (updatedAccount != null) syncManager.uploadAccount(updatedAccount)
                        
                        showPayCreditForm = false
                    }
                }
            )
        }
    }

    if (accounts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No accounts yet", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(items = accounts, key = { it.id }) { account ->
                Card(
                    onClick = {
                        selectedAccountId = account.id
                        showDetailModal = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    AccountItemContent(account)
                }
            }
        }
    }
}

@Composable
fun AccountItemContent(account: Account) {
    Row(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            if (account.profile != null) {
                AsyncImage(model = File(account.profile), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(account.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("ID: ${account.id}", color = Color.Gray, fontSize = 10.sp)
        }
        if (!account.depositval.isNullOrEmpty()) {
            Column(horizontalAlignment = Alignment.End) {
                Text("Deposit", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("${account.depositval}₹", color = Color(0xFF81C784), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AccountDetailContent(
    account: Account, 
    onAddPurchase: () -> Unit, 
    onPayCredit: () -> Unit,
    onViewPurchases: () -> Unit,
    onGenerateReport: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
    
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                if (account.profile != null) {
                    AsyncImage(model = File(account.profile), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (!account.acmail.isNullOrEmpty()) Text(account.acmail, color = Color.Gray, fontSize = 12.sp)
                Text("ID: ${account.id} • Created: ${account.date}", color = Color.Gray, fontSize = 10.sp)
            }
            IconButton(onClick = onDeleteAccount) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Account", tint = Color.Red)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onViewPurchases,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Purchases", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = onGenerateReport,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate Report", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailSummaryItem("Purchases", "${account.purchaseCount}")
            DetailSummaryItem("Total", "${account.totalPurchases}₹")
            DetailSummaryItem("Paid ($currentMonth)", "${account.totalPaid}₹")
            DetailSummaryItem("Remaining", "${account.remainingBalance}₹", Color(0xFFE57373))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAddPurchase,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AddShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Purchase")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onPayCredit,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Payments, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pay Credit", color = Color.Black)
        }
    }
}

@Composable
fun DetailSummaryItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AddPurchaseForm(onCancel: () -> Unit, onDone: (String, Double, String) -> Unit) {
    var itemName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())) }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        Text("Add Purchase", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
        
        OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
            Button(onClick = { onDone(itemName, amount.toDoubleOrNull() ?: 0.0, date) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Done", color = Color.Black, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun PayCreditForm(remainingBal: Double, onDiscard: () -> Unit, onDone: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        Text("Remaining Balance: ${remainingBal}₹", color = Color(0xFFE57373), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
        
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Payment Amount", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onDiscard, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) { Text("Discard", color = MaterialTheme.colorScheme.onSurface) }
            Button(onClick = { onDone(amount.toDoubleOrNull() ?: 0.0) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Done", color = Color.Black, fontWeight = FontWeight.Bold) }
        }
    }
}

suspend fun generateExcelReport(
    context: android.content.Context,
    database: AccountDatabase,
    account: Account,
    purchases: List<Purchase>,
    startMillis: Long,
    endMillis: Long
) {
    val workbook = XSSFWorkbook()
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val rangeStr = "${sdf.format(Date(startMillis))} - ${sdf.format(Date(endMillis))}"
    
    val reportSheet = workbook.createSheet("Report")
    val headerRow = reportSheet.createRow(0)
    headerRow.createCell(0).setCellValue("Date")
    headerRow.createCell(1).setCellValue("Item")
    headerRow.createCell(2).setCellValue("Amount")

    var rowNum = 1
    for (purchase in purchases) {
        val row = reportSheet.createRow(rowNum++)
        row.createCell(0).setCellValue(purchase.date)
        row.createCell(1).setCellValue(purchase.itemName)
        row.createCell(2).setCellValue(purchase.amount)
    }

    val fileName = "Report_${account.name}_${System.currentTimeMillis()}.xlsx"
    val file = File(context.filesDir, fileName)
    
    try {
        val out = FileOutputStream(file)
        workbook.write(out)
        out.close()
        workbook.close()

        val report = Report(
            accountName = account.name,
            fileName = fileName,
            filePath = file.absolutePath,
            dateRange = rangeStr,
            generatedDate = sdf.format(Date())
        )
        database.accountDao().insertReport(report)
        
        android.widget.Toast.makeText(context, "Report generated! Check in reports section", android.widget.Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
