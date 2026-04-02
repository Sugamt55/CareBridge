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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(navController: NavController) {
    var autoCaptureEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF343A40),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Stitch - Design with AI",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Bolt, contentDescription = null, tint = Color.DarkGray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0056B3))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.align(Alignment.Center).size(20.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFE9ECEF),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                Surface(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color(0xFFDEE2E6)
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Already here */ },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                        label = { Text("Scan") }
                    )
                }
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO */ },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "AI Report Scanner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Align your medical document within the\nframe to start the analysis.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Scanner Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFDEE2E6)) // Placeholder for camera
            ) {
                // Corner markers (simplified)
                ScannerCorners(modifier = Modifier.fillMaxSize())
                
                // AI READY Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0056B3))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI READY", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD0E1FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF0056B3))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Gallery", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("AUTO-", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("CAPTURE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Switch(
                                checked = autoCaptureEnabled,
                                onCheckedChange = { autoCaptureEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0056B3)
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, size = 16.dp, tint = Color(0xFF0056B3))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enhanced AI\nMode", fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerCorners(modifier: Modifier) {
    Box(modifier = modifier.padding(24.dp)) {
        val cornerSize = 40.dp
        val strokeWidth = 4.dp
        val color = Color(0xFF0056B3)

        // Top Left
        Box(modifier = Modifier
            .size(cornerSize)
            .align(Alignment.TopStart)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).background(color))
        }

        // Top Right
        Box(modifier = Modifier
            .size(cornerSize)
            .align(Alignment.TopEnd)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).align(Alignment.TopEnd).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).background(color))
        }

        // Bottom Left
        Box(modifier = Modifier
            .size(cornerSize)
            .align(Alignment.BottomStart)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).align(Alignment.BottomStart).background(color))
        }

        // Bottom Right
        Box(modifier = Modifier
            .size(cornerSize)
            .align(Alignment.BottomEnd)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).align(Alignment.BottomEnd).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).align(Alignment.BottomEnd).background(color))
        }
    }
}

private fun Modifier.size(size: Int): Modifier = this.size(size.dp)
