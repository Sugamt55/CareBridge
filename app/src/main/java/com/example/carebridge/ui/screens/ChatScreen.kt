package com.example.carebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
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
fun ChatScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0056B3))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "AI Assistant",
                                modifier = Modifier.align(Alignment.Center).size(20.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "AI Assistant",
                                color = Color(0xFF0056B3),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.DarkGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Column {
                // Input Field Area
                ChatInputArea()
                
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("scan") },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                        label = { Text("Scan") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Already here */ },
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                MessageBubble(
                    message = "Hello. I have analyzed your recent blood work and symptoms. Based on the data, I've prepared a preliminary diagnostic summary for your review.",
                    isFromMe = false,
                    time = "09:41 AM"
                )
            }

            item {
                DiagnosticSummaryCard()
            }

            item {
                Text(
                    "Observations suggest Microcytic Hypochromic Anemia. Clinical correlation with your reported fatigue and dizziness is highly consistent.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                MessageBubble(
                    message = "Thank you. I've been feeling quite tired lately. What are the next steps for treatment?",
                    isFromMe = true,
                    time = "09:43 AM"
                )
            }

            item {
                MessageBubble(
                    message = "Typically, this involves iron supplementation and a dietary review. However, we should rule out underlying causes first. I recommend scheduling a consultation with a hematologist.",
                    isFromMe = false,
                    time = "09:45 AM"
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(label = "What does this diagnosis mean?")
                    SuggestionChip(label = "Should I see a specialist?")
                }
            }

            item {
                Text(
                    "Disclaimer: Sanctuary AI provides information based on data patterns. This is not a formal medical diagnosis. Always consult with a licensed physician for medical advice.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun MessageBubble(message: String, isFromMe: Boolean, time: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isFromMe) Color(0xFF1B4985) else Color.White,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isFromMe) 12.dp else 0.dp,
                bottomEnd = if (isFromMe) 0.dp else 12.dp
            ),
            shadowElevation = 1.dp
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                color = if (isFromMe) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
        Text(
            text = time,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun DiagnosticSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = Color(0xFF6366F1),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "Select",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Color(0xFF1B4985))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Diagnostic Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryStatCard(
                    label = "PRIMARY FINDING",
                    value = "Iron Deficiency Anemia",
                    badgeText = "URGENT FOLLOW-UP",
                    badgeColor = Color(0xFFB45309),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    label = "SERUM FERRITIN",
                    value = "12 ng/mL",
                    badgeText = "Below Normal (30-150)",
                    badgeColor = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryStatCard(
    label: String,
    value: String,
    badgeText: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            Surface(
                color = badgeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    badgeText,
                    color = badgeColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            color = Color(0xFF1B4985),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChatInputArea() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                modifier = Modifier.weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Text(
                    "Ask Sanctuary AI...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B4985)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}
