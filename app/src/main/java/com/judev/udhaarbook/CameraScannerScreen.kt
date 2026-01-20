package com.judev.udhaarbook

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.judev.udhaarbook.data.Purchase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerScreen(
    accountId: Int,
    accountName: String,
    accountProfile: String?,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val database = LocalDatabase.current
    val syncManager = LocalSyncManager.current
    
    // SECURE: API key is now loaded from local.properties via BuildConfig
    val apiKey = BuildConfig.GEMINI_API_KEY
    val assistant = remember { GeminiChatAssistant(apiKey, database) }

    var identifiedItems by remember { mutableStateOf(listOf<String>()) }
    var isProcessing by remember { mutableStateOf(false) }
    
    var showAmountPrompt by remember { mutableStateOf(false) }
    var currentScannedItem by remember { mutableStateOf("") }
    var inputAmount by remember { mutableStateOf("") }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    if (showAmountPrompt) {
        AlertDialog(
            onDismissRequest = { showAmountPrompt = false },
            title = { Text("Enter Price for $currentScannedItem", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        val purchase = Purchase(
                            id = Random.nextInt(100000, 999999),
                            accountId = accountId,
                            accountName = accountName,
                            accountProfile = accountProfile,
                            itemName = currentScannedItem,
                            amount = amount,
                            date = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())
                        )
                        database.accountDao().insertPurchaseAndUpdateAccount(purchase)
                        syncManager.uploadPurchase(purchase)
                        
                        identifiedItems = identifiedItems + "$currentScannedItem: $amount₹"
                        showAmountPrompt = false
                        inputAmount = ""
                    }
                }) { Text("Save Item") }
            },
            dismissButton = {
                TextButton(onClick = { showAmountPrompt = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (hasCameraPermission) {
        LaunchedEffect(hasCameraPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                } catch (e: Exception) {
                    Log.e("CameraScanner", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

            Column(modifier = Modifier.align(Alignment.TopStart).padding(16.dp).width(250.dp)) {
                Text("Saved in this Session:", color = Color.White, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                LazyColumn {
                    items(identifiedItems) { item ->
                        Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                            Text(item, color = Color(0xFF81C784), fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
            }

            if (isProcessing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 64.dp), 
                horizontalArrangement = Arrangement.SpaceEvenly, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel, 
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                }

                Box(
                    modifier = Modifier.size(84.dp).clip(CircleShape).background(if (isProcessing) Color.Gray else Color.White).clickable(enabled = !isProcessing) {
                        imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = image.toBitmap()
                                image.close()
                                scope.launch {
                                    isProcessing = true
                                    val name = assistant.identifyProduct(bitmap)
                                    if (name != null && !name.contains("Error", ignoreCase = true)) {
                                        currentScannedItem = name
                                        showAmountPrompt = true
                                    }
                                    isProcessing = false
                                }
                            }
                            override fun onError(exception: ImageCaptureException) { Log.e("CameraScanner", "Capture failed") }
                        })
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(72.dp).border(3.dp, Color.Black, CircleShape))
                    Icon(Icons.Default.Camera, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(40.dp))
                }

                IconButton(
                    onClick = onDone, 
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF81C784))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White)
                }
            }
        }
    }
}

fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
