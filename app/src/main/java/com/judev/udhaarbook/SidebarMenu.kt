package com.judev.udhaarbook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarContent(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onClose: () -> Unit,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = LocalSession.current
    val userName = sessionManager.getUserName()
    val userImage = sessionManager.getUserImage()

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 32.dp, top = 16.dp)
                    .clickable { 
                        onProfileClick()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (userImage != null) {
                        AsyncImage(
                            model = File(userImage),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(userName.ifEmpty { "User" }, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("view profile", color = Color.Gray, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 16.dp))

            // Menu Items
            DrawerMenuItem(Icons.Default.Share, "Share App") { onClose() }
            DrawerMenuItem(Icons.Default.Star, "Rate App") { onClose() }
            DrawerMenuItem(Icons.Default.PrivacyTip, "Privacy Policy") { onPrivacyClick() }
            DrawerMenuItem(Icons.Default.Settings, "Settings") { onSettingsClick() }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color.White)
            }
        }
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(24.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
    }
}
