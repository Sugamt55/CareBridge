package com.example.carebridge.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.carebridge.R
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    var showSheet by remember { mutableStateOf(false) }
    var scannedFoodData by remember { mutableStateOf<FoodPredictionResponse?>(null) }

    // Style Constants matching HomeScreen
    val orangeColor = Color(0xFFFF6B00)
    val navBackgroundColor = Color(0xFF1A1A1A)
    val unselectedGrey = Color(0xFF888888)
    val whiteColor = Color(0xFFFFFFFF)
    val darkOverlay = Color(0x80000000)

    // Permission Handling
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Success Logic to show the BottomSheet
    LaunchedEffect(uiState) {
        if (uiState is ScanUiState.Success) {
            scannedFoodData = (uiState as ScanUiState.Success).response
            showSheet = true
        }
    }

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
                    navigationIcon = {
                        IconButton(onClick = { 
                            viewModel.resetState()
                            navController.navigateUp() 
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = whiteColor
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
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                // Bottom Navigation Bar: Dark/black (#1A1A1A), stylized like HomeScreen
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
                        onClick = {
                            viewModel.resetState()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("HOME", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = unselectedGrey,
                            unselectedTextColor = unselectedGrey,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Already here */ },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                        label = { Text("SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = orangeColor,
                            selectedTextColor = orangeColor,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            viewModel.resetState()
                            navController.navigate("chat") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
                        label = { Text("CHAT", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = unselectedGrey,
                            unselectedTextColor = unselectedGrey,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                // Title: "AI Report Scanner" - white, bold, large (matching homepage)
                Text(
                    text = "AI Report Scanner",
                    color = whiteColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Subtitle: white
                Text(
                    text = "Align your food within the frame to start the analysis.",
                    textAlign = TextAlign.Center,
                    color = whiteColor,
                    lineHeight = 24.sp,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Scanner Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    if (hasCameraPermission) {
                        CameraPreview(
                            lifecycleOwner = lifecycleOwner,
                            imageCapture = imageCapture,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Camera permission required", color = whiteColor)
                        }
                    }

                    // Corner frame brackets (Orange)
                    ScannerCorners(modifier = Modifier.fillMaxSize(), color = orangeColor)

                    // Camera capture button: white circular button with a subtle dark border
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 60.dp)
                            .size(72.dp)
                            .border(4.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                            .background(whiteColor, CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                enabled = uiState !is ScanUiState.Loading && hasCameraPermission,
                                onClick = {
                                    takePicture(context, imageCapture, executor) { file ->
                                        Log.d("ScanScreen", "Triggering analysis for: ${file.name}")
                                        viewModel.analyzeFood(file)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(32.dp))
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
                                CircularProgressIndicator(color = orangeColor)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Analyzing Food...", color = whiteColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Display
                if (uiState is ScanUiState.Error) {
                    Text(
                        text = (uiState as ScanUiState.Error).message,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    // Result Bottom Sheet
    if (showSheet && scannedFoodData != null) {
        FoodResultBottomSheet(
            foodData = scannedFoodData!!,
            detailedData = (uiState as? ScanUiState.Success)?.detailedData,
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
fun ScannerCorners(modifier: Modifier, color: Color) {
    Box(modifier = modifier.padding(24.dp)) {
        val cornerSize = 40.dp
        val strokeWidth = 4.dp

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
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).align(Alignment.TopEnd).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).background(color))
        }
    }
}
