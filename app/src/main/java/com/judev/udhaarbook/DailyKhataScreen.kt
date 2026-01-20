package com.judev.udhaarbook

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import com.judev.udhaarbook.data.Account
import com.judev.udhaarbook.data.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyKhataScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToScanner: (Int) -> Unit,
    onThemeChanged: (String) -> Unit
) {
    val screens = Screen.entries
    val pagerState = rememberPagerState(pageCount = { screens.size })
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // Performance: derivedStateOf prevents unnecessary Scaffold recompositions
    val selectedScreen by remember { derivedStateOf { screens[pagerState.currentPage] } }
    
    val sessionManager = LocalSession.current
    val database = LocalDatabase.current
    
    var userImage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(pagerState.currentPage) {
        userImage = sessionManager.getUserImage()
    }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCreateAccountModal by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showCreateAccountModal) {
        ModalBottomSheet(
            onDismissRequest = { showCreateAccountModal = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            CreateAccountForm(
                onDiscard = { showCreateAccountModal = false },
                onCreate = { account ->
                    scope.launch {
                        database.accountDao().insertAccount(account)
                        showCreateAccountModal = false
                    }
                }
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarContent(
                onProfileClick = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToProfile()
                    }
                },
                onLogoutClick = { 
                    scope.launch { drawerState.close() }
                    showLogoutDialog = true 
                },
                onSettingsClick = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToSettings()
                    }
                },
                onPrivacyClick = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToPrivacy()
                    }
                },
                onClose = { scope.launch { drawerState.close() } },
                onThemeChanged = onThemeChanged
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = selectedScreen.title,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }, label = "title_animation"
                        ) { title ->
                            Text(
                                title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (userImage != null) {
                                AsyncImage(
                                    model = userImage, // Optimized: handles File and URL
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                CustomBottomBar(
                    selectedScreen = selectedScreen,
                    onScreenSelected = { screen ->
                        scope.launch {
                            pagerState.animateScrollToPage(screen.ordinal)
                        }
                    },
                    onPlusClick = {
                        showCreateAccountModal = true
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                userScrollEnabled = true,
                beyondViewportPageCount = 1,
                contentPadding = PaddingValues(0.dp)
            ) { page ->
                Box(Modifier.fillMaxSize()) {
                    when (screens[page]) {
                        Screen.Home -> HomeScreen()
                        Screen.Accounts -> AccountsScreen(onNavigateToScanner = onNavigateToScanner)
                        Screen.Chat -> ChatScreen()
                        Screen.Assignment -> ReportsScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAccountForm(onDiscard: () -> Unit, onCreate: (Account) -> Unit) {
    var name by remember { mutableStateOf("") }
    var isUbUser by remember { mutableStateOf(false) }
    var registedEmail by remember { mutableStateOf("") }
    var accountDeposit by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Create New Account",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isUbUser,
                onCheckedChange = { isUbUser = it },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text("is udhaarbook user", color = MaterialTheme.colorScheme.onSurface)
        }

        if (isUbUser) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = registedEmail,
                onValueChange = { registedEmail = it },
                label = { Text("Registed email of UB user", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = accountDeposit,
            onValueChange = { accountDeposit = it },
            label = { Text("Account deposit if any?", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDiscard,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Discard", color = MaterialTheme.colorScheme.onSurface)
            }
            Button(
                onClick = {
                    val randomId = kotlin.random.Random.nextInt(100000, 999999)
                    val currentDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())
                    val newAccount = Account(
                        id = randomId,
                        name = name,
                        profile = null,
                        acmail = if (isUbUser) registedEmail else null,
                        depositval = accountDeposit,
                        date = currentDate
                    )
                    onCreate(newAccount)
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Create Account", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
