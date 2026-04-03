package com.example.carebridge.ui.screens

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.carebridge.data.FoodPredictionResponse
import com.example.carebridge.ui.viewmodel.MainViewModel
import com.example.carebridge.ui.viewmodel.ScanUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    var autoCaptureEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    var showSheet by remember { mutableStateOf(false) }
    var scannedFoodData by remember { mutableStateOf<FoodPredictionResponse?>(null) }

    // Success Logic to show the BottomSheet
    LaunchedEffect(uiState) {
        if (uiState is ScanUiState.Success) {
            scannedFoodData = (uiState as ScanUiState.Success).response
            showSheet = true
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.DarkGray
                        )
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already here */ },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                    label = { Text("Scan") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("chat") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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
                    .background(Color(0xFFDEE2E6))
            ) {
                CameraPreview(
                    lifecycleOwner = lifecycleOwner,
                    imageCapture = imageCapture,
                    modifier = Modifier.fillMaxSize()
                )

                // Corner markers
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

                // Capture Trigger (Overlay Button)
                IconButton(
                    onClick = {
                        takePicture(context, imageCapture, executor) { file ->
                            viewModel.analyzeFood(file)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape),
                    enabled = uiState !is ScanUiState.Loading
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.White)
                }

                // Loading Indicator
                if (uiState is ScanUiState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Analyzing Food...", color = Color.White)
                        }
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
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF0056B3)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enhanced AI\nMode", fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Result Bottom Sheet
    if (showSheet && scannedFoodData != null) {
        FoodResultBottomSheet(
            foodData = scannedFoodData!!,
            onDismiss = {
                showSheet = false
                viewModel.resetState()
            }
        )
    }
}

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preview = remember { Preview.Builder().build() }
    val previewView = remember { PreviewView(context) }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    useCaseGroup
                )
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (File) -> Unit
) {
    val outputDirectory = File(context.cacheDir, "images").apply {
        if (!exists()) mkdirs()
    }
    
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("ScanScreen", "Capture failed", exception)
            }
        }
    )
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
