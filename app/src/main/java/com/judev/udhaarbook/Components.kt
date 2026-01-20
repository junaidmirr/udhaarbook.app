package com.judev.udhaarbook

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.judev.udhaarbook.data.Screen
import com.judev.udhaarbook.data.Transaction

@Composable
fun SummaryItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

@Composable
fun TransactionItem(transaction: Transaction, amountColor: Color) {
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
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                Text(transaction.date, color = Color.Gray, fontSize = 10.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(transaction.type, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Text(transaction.amount, color = amountColor, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

class NotchedPillShape(private val notchRadius: Dp) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val notchRadiusPx = with(density) { notchRadius.toPx() }
        val cornerRadiusPx = size.height / 2
        
        val path = Path().apply {
            moveTo(cornerRadiusPx, 0f)
            
            val notchCenter = size.width / 2
            val notchClearance = notchRadiusPx * 1.2f
            
            lineTo(notchCenter - notchClearance, 0f)
            
            cubicTo(
                x1 = notchCenter - notchRadiusPx, y1 = 0f,
                x2 = notchCenter - notchRadiusPx, y2 = notchRadiusPx,
                x3 = notchCenter, y3 = notchRadiusPx
            )
            cubicTo(
                x1 = notchCenter + notchRadiusPx, y1 = notchRadiusPx,
                x2 = notchCenter + notchRadiusPx, y2 = 0f,
                x3 = notchCenter + notchClearance, y3 = 0f
            )
            
            lineTo(size.width - cornerRadiusPx, 0f)
            
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(size.width - size.height, 0f, size.width, size.height),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            
            lineTo(cornerRadiusPx, size.height)
            
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.height, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun CustomBottomBar(
    selectedScreen: Screen, 
    onScreenSelected: (Screen) -> Unit,
    onPlusClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth()
                .height(60.dp),
            shape = NotchedPillShape(35.dp),
            color = Color(0xFF5E35B1).copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BottomNavItem(Screen.Home, selectedScreen == Screen.Home) { onScreenSelected(Screen.Home) }
                    BottomNavItem(Screen.Accounts, selectedScreen == Screen.Accounts) { onScreenSelected(Screen.Accounts) }
                }

                Spacer(modifier = Modifier.width(80.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BottomNavItem(Screen.Chat, selectedScreen == Screen.Chat) { onScreenSelected(Screen.Chat) }
                    BottomNavItem(Screen.Assignment, selectedScreen == Screen.Assignment) { onScreenSelected(Screen.Assignment) }
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(bottom = 36.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD54F), Color(0xFFFBC02D))
                    )
                )
                .clickable { onPlusClick() }
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun BottomNavItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent,
        label = "pill_bg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            screen.icon,
            contentDescription = screen.title,
            tint = if (isSelected) Color(0xFFFFD54F) else Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
