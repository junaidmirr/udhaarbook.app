package com.judev.udhaarbook

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.judev.udhaarbook.data.Report
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val database = LocalDatabase.current
    val scope = rememberCoroutineScope()
    val reports by database.accountDao().getAllReports().collectAsState(initial = emptyList())
    val context = LocalContext.current
    
    var selectedReport by remember { mutableStateOf<Report?>(null) }
    var showReportOptions by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && selectedReport != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Report") },
            text = { Text("Are you sure? This can't be undone and the file will be deleted from storage.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val file = File(selectedReport!!.filePath)
                        if (file.exists()) file.delete()
                        database.accountDao().deleteReport(selectedReport!!)
                        showDeleteConfirm = false
                        showReportOptions = false
                    }
                }) { Text("Yes", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("No") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showReportOptions && selectedReport != null) {
        ModalBottomSheet(
            onDismissRequest = { showReportOptions = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ReportOptionsContent(
                report = selectedReport!!,
                onView = {
                    viewExcelFile(context, selectedReport!!.filePath)
                    showReportOptions = false
                },
                onDownload = {
                    downloadExcelFile(context, selectedReport!!)
                    showReportOptions = false
                },
                onShare = {
                    shareExcelFile(context, selectedReport!!.filePath)
                    showReportOptions = false
                },
                onDelete = {
                    showDeleteConfirm = true
                }
            )
        }
    }

    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No reports yet", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(reports) { report ->
                ReportItem(report) {
                    selectedReport = report
                    showReportOptions = true
                }
            }
        }
    }
}

@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(report.accountName, color = MaterialTheme.colorScheme.onSurface, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                Text(report.fileName, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Text(report.dateRange, color = Color.Gray, fontSize = 12.sp)
                Text("Generated: ${report.generatedDate}", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun ReportOptionsContent(
    report: Report, 
    onView: () -> Unit, 
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        Text("Report: ${report.fileName}", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        ListItem(
            headlineContent = { Text("View Report", color = MaterialTheme.colorScheme.onSurface) },
            leadingContent = { Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { onView() }
        )
        ListItem(
            headlineContent = { Text("Share Report", color = MaterialTheme.colorScheme.onSurface) },
            leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { onShare() }
        )
        ListItem(
            headlineContent = { Text("Download to Downloads", color = MaterialTheme.colorScheme.onSurface) },
            leadingContent = { Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { onDownload() }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f))
        ListItem(
            headlineContent = { Text("Delete Report", color = Color.Red) },
            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
            modifier = Modifier.clickable { onDelete() }
        )
    }
}

fun viewExcelFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (file.exists()) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open Report"))
    }
}

fun shareExcelFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (file.exists()) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}

fun downloadExcelFile(context: Context, report: Report) {
    val sourceFile = File(report.filePath)
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val destFile = File(downloadsFolder, report.fileName)
    
    try {
        sourceFile.copyTo(destFile, overwrite = true)
        Toast.makeText(context, "Downloaded to ${destFile.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
