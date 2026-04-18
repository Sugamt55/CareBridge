package com.example.carebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.carebridge.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    val backgroundColor = Color(0xFF0D0800)
    val orangeColor = Color(0xFFFF6B00)
    val navBackgroundColor = Color(0xFF1A1A1A)
    val unselectedGrey = Color(0xFF888888)
    val whiteColor = Color(0xFFFFFFFF)
    val darkOverlay = Color(0x80000000)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background: Fullscreen background (Using VideoBackground from HomeScreen.kt)
        VideoBackground(resourceId = R.raw.background_glow)

        // 2. Dark Overlay (#80000000)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkOverlay)
        )

        // 3. Subtle Radial Gradient Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            orangeColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 1800f
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NutriScan AI",
                                color = Color(0xFFFFF176),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                            Icon(Icons.Outlined.Bolt, contentDescription = null, tint = orangeColor)
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.align(Alignment.Center).size(20.dp),
                                    tint = whiteColor
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = navBackgroundColor,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("HOME", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = unselectedGrey,
                            unselectedTextColor = unselectedGrey,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("scan") },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                        label = { Text("SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = unselectedGrey,
                            unselectedTextColor = unselectedGrey,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Already here */ },
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
                        label = { Text("CHAT", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = orangeColor,
                            selectedTextColor = orangeColor,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { /* TODO */ },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = unselectedGrey,
                            unselectedTextColor = unselectedGrey,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        ) { innerPadding ->
            NutritionalLibraryScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
