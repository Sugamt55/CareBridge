package com.example.carebridge.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.carebridge.ui.theme.clinicalBackground

// --- NATURE-CLINICAL DESIGN SYSTEM CONSTANTS ---
private val PrimaryGreen = Color(0xFF006C49)
private val MutedNavGreen = Color(0xFFBCCFBF)
private val SlateGrey = Color(0xFF6B7280)
private val ClinicalLight = Color(0xFFF9FAFB)
private val White = Color(0xFFFFFFFF)

private val SpacingBadgeToHeadline = 32.dp
private val SpacingHeadlineToSubtitle = 20.dp
private val SpacingSubtitleToContent = 48.dp
private val SpacingBetweenComponents = 24.dp

sealed class PhZone {
    object Acidic : PhZone()
    object Neutral : PhZone()
    object Alkaline : PhZone()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    var sliderValue by remember { mutableStateOf(7f) }

    val currentZone = when {
        sliderValue < 6.9f -> PhZone.Acidic
        sliderValue > 7.1f -> PhZone.Alkaline
        else -> PhZone.Neutral
    }

    val unselectedGrey = Color(0xFF888888)

    Scaffold(
        modifier = Modifier.clinicalBackground().fillMaxSize(),
        containerColor = ClinicalLight,
        bottomBar = {
            NavigationBar(
                containerColor = MutedNavGreen,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, MutedNavGreen, RoundedCornerShape(32.dp))
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
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryGreen.copy(alpha = 0.15f), Color.Transparent),
                            radius = 2000f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(SpacingBadgeToHeadline))

                PhKnowledgeHeader()

                Spacer(modifier = Modifier.height(SpacingSubtitleToContent))

                BioMineralSlider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    zone = currentZone
                )

                Spacer(modifier = Modifier.height(SpacingBetweenComponents))

                EducationalPhCard(zone = currentZone)

                Spacer(modifier = Modifier.height(SpacingBetweenComponents))

                WhoIsDrSebiCard()

                Spacer(modifier = Modifier.height(SpacingBetweenComponents))

                MissionCard()

                Spacer(modifier = Modifier.height(SpacingBadgeToHeadline))
            }
        }
    }
}

@Composable
fun PhKnowledgeHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "pH Knowledge",
            color = PrimaryGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(SpacingHeadlineToSubtitle))
        Text(
            text = "Slide to explore what pH means for your body.",
            color = SlateGrey,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BioMineralSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    zone: PhZone
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "pH ${"%.1f".format(value)}",
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            val trackBrush = Brush.horizontalGradient(
                0.0f to Color(0xFFEF4444),
                6.9f / 14f to Color(0xFFEF4444),
                6.9f / 14f to Color(0xFFFBBF24),
                7.1f / 14f to Color(0xFFFBBF24),
                7.1f / 14f to Color(0xFF10B981),
                1.0f to Color(0xFF10B981)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(trackBrush, RoundedCornerShape(4.dp))
                )
                
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = 0f..14f,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryGreen,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val label = when (zone) {
                PhZone.Acidic -> "ACIDIC"
                PhZone.Neutral -> "NEUTRAL"
                PhZone.Alkaline -> "ALKALINE"
            }
            
            Text(
                text = label,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun WhoIsDrSebiCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Who is Dr Sebi?",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Alfredo Bowman, known as \"Dr. Sebi,\" was a Honduran herbalist who sought natural alternatives after Western medicine failed to treat his own chronic illnesses. He developed \"African Bio-Electric Cell Food Therapy,\" asserting that mucus and substances uncomplimentary to one's genetic structure caused all disease, advocating for an alkaline diet. While he attracted a devoted celebrity following who viewed him as a visionary, the scientific community labeled his claims—which denied germ theory—as dangerous pseudoscience. Ultimately, Dr. Sebi remains a polarizing legacy: to supporters, a champion of natural healing; to critics, a controversial purveyor of false hope.",
                    color = SlateGrey,
                    fontSize = 13.sp,
                    lineHeight = 19.5.sp
                )
            }
        }
    }
}

@Composable
fun MissionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Our Mission",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We built this app because we believe everyone deserves to thrive, not just survive. We have seen too many people held back by preventable illnesses, unaware that their daily habits may be slowly draining their vitality. Our mission is to bring clarity to this root cause. Dr. Sebi taught us that disease is the result of ingesting substances uncomplimentary to our genetic structure, which forces the body to produce excessive mucus as a defensive response; this accumulation then clogs our internal pathways and obstructs our natural vitality. By understanding this, we aim to guide you back to your body’s true state of balance—your natural homeostasis.",
                    color = SlateGrey,
                    fontSize = 13.sp,
                    lineHeight = 19.5.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EducationalPhCard(zone: PhZone) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(PrimaryGreen)
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = zone,
                    transitionSpec = {
                        (fadeIn() + slideInVertically { it / 2 })
                            .togetherWith(fadeOut() + slideOutVertically { it / 2 })
                    },
                    label = "ZoneTransition"
                ) { targetZone ->
                    Column {
                        val title = when (targetZone) {
                            PhZone.Acidic -> "Acidic Foods"
                            PhZone.Neutral -> "Neutral Balance"
                            PhZone.Alkaline -> "Alkaline Foods"
                        }
                        
                        val body = when (targetZone) {
                            PhZone.Acidic -> "Foods with a pH below 7 are considered acidic. This is the destructive part of chemical science. Eating \"acid foods\" breaks down the body's protective mucous membrane. Once that membrane is compromised, it liquefies and travels through the bloodstream, coating cells in various organs and depriving them of oxygen. This is Dr Sebi's proposed mechanism for how acidic food causes disease."
                            PhZone.Neutral -> "A neutral pH range of 7 represents biological equilibrium. Water falls in this category."
                            PhZone.Alkaline -> "Alkaline foods have a pH above 7. This is the restorative part of chemical science. Natural alkaline herbs and a vegetarian diet solely made up of what Dr Sebi calls electric foods — mushrooms, greens, and rye breads without starch. This is the alkaline counterpart to his acid-disease theory — the idea that alkaline, plant-based foods support rather than damage the mucous membrane."
                        }

                        Text(
                            text = title,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = body,
                            color = SlateGrey,
                            fontSize = 13.sp,
                            lineHeight = 19.5.sp
                        )
                    }
                }
            }
        }
    }
}
