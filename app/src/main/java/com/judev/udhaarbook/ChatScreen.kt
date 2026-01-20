package com.judev.udhaarbook

import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.content.Intent
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.judev.udhaarbook.data.ChatHistory
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val database = LocalDatabase.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // SECURE: Use API key from BuildConfig (loaded from local.properties)
    val apiKey = BuildConfig.GEMINI_API_KEY
    val assistant = remember { GeminiChatAssistant(apiKey, database) }
    
    val chatHistory by database.accountDao().getAllChatMessages().collectAsState(initial = emptyList())
    
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.getDefault()
            }
        }
        tts.value = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            inputText = data?.get(0) ?: ""
        }
    }

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Assistant", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Row {
                IconButton(onClick = { 
                    scope.launch { database.accountDao().clearChatHistory() }
                }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = Color.Red)
                }
                IconButton(onClick = { 
                    scope.launch { database.accountDao().clearChatHistory() }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = chatHistory,
                key = { it.id }
            ) { message ->
                ChatBubble(message, onReadAloud = { text ->
                    tts.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                })
            }
            if (isTyping) {
                item {
                    Text("Assistant is thinking...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp).imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    }
                    voiceLauncher.launch(intent)
                }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Type", tint = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about your udhaar...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isTyping) {
                            val userText = inputText
                            inputText = ""
                            scope.launch {
                                database.accountDao().insertChatMessage(ChatHistory(text = userText, isUser = true))
                                isTyping = true
                                val response = assistant.generateResponse(userText)
                                database.accountDao().insertChatMessage(ChatHistory(text = response, isUser = false))
                                isTyping = false
                            }
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatHistory, onReadAloud: (String) -> Unit) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = if (message.isUser) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
            tonalElevation = 2.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = message.text, color = textColor, modifier = Modifier.weight(1f, fill = false), fontSize = 15.sp)
                if (!message.isUser) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Read Aloud",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp).clickable { onReadAloud(message.text) }
                    )
                }
            }
        }
    }
}
