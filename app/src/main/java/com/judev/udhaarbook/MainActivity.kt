package com.judev.udhaarbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.judev.udhaarbook.data.AccountDatabase
import com.judev.udhaarbook.data.SessionManager
import com.judev.udhaarbook.ui.theme.UdhaarbookTheme

// UNIFIED PACKAGE STRUCTURE: All screens and managers now use the 'data' package models.
val LocalSession = staticCompositionLocalOf<SessionManager> { error("No SessionManager provided") }
val LocalDatabase = staticCompositionLocalOf<AccountDatabase> { error("No Database provided") }
val LocalSyncManager = staticCompositionLocalOf<FirebaseSyncManager> { error("No SyncManager provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Single instances for the entire app lifecycle
        val sessionManager = SessionManager(this)
        val database = AccountDatabase.getDatabase(this)
        val syncManager = FirebaseSyncManager(database)

        setContent {
            var currentTheme by remember { mutableStateOf(sessionManager.getTheme()) }

            CompositionLocalProvider(
                LocalSession provides sessionManager,
                LocalDatabase provides database,
                LocalSyncManager provides syncManager
            ) {
                UdhaarbookTheme(appTheme = currentTheme) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen {
                                val destination = if (sessionManager.isLoggedIn()) "main" else "auth"
                                navController.navigate(destination) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                        composable("auth") {
                            AuthScreen {
                                navController.navigate("main") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        }
                        composable("main") {
                            DailyKhataScreen(
                                onLogout = {
                                    sessionManager.logout()
                                    navController.navigate("auth") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                },
                                onNavigateToProfile = {
                                    if (navController.currentDestination?.route != "profile") {
                                        navController.navigate("profile") { launchSingleTop = true }
                                    }
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToPrivacy = {
                                    navController.navigate("privacy")
                                },
                                onNavigateToScanner = { accountId ->
                                    navController.navigate("scanner/$accountId")
                                },
                                onThemeChanged = { newTheme ->
                                    currentTheme = newTheme
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(onBack = { navController.popBackStack() })
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onThemeChanged = { currentTheme = it },
                                onDeleteAccount = {
                                    sessionManager.logout()
                                    navController.navigate("auth") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("privacy") {
                            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                        }
                        composable("scanner/{accountId}") { backStackEntry ->
                            val accountId = backStackEntry.arguments?.getString("accountId")?.toIntOrNull() ?: -1
                            
                            val accounts by database.accountDao().getAllAccounts().collectAsState(initial = emptyList())
                            val account = remember(accounts) { accounts.find { it.id == accountId } }
                            
                            if (account != null) {
                                CameraScannerScreen(
                                    accountId = account.id,
                                    accountName = account.name,
                                    accountProfile = account.profile,
                                    onDone = { navController.popBackStack() },
                                    onCancel = { navController.popBackStack() }
                                )
                            } else if (accounts.isNotEmpty()) {
                                LaunchedEffect(Unit) { navController.popBackStack() }
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
