package com.example.nutriscanai.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nutriscanai.data.FoodPredictionResponse
import com.example.nutriscanai.ui.theme.LightGreen
import com.example.nutriscanai.ui.theme.clinicalBackground
import com.example.nutriscanai.ui.viewmodel.MainViewModel
import com.example.nutriscanai.ui.viewmodel.ScanUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    navController: NavController, 
    viewModel: MainViewModel = viewModel(),
    isTestMode: Boolean = false,
    onTestCapture: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    val imageCapture = remember { if (!isTestMode) ImageCapture.Builder().build() else null }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    var showSheet by remember { mutableStateOf(false) }
    var scannedFoodData by remember { mutableStateOf<FoodPredictionResponse?>(null) }
    
    var isFlashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    // Style Constants
    val orangeColor = MaterialTheme.colorScheme.primary
    val whiteColor = MaterialTheme.colorScheme.onSurface
    val unselectedGrey = Color(0xFF888888)

    // Permission Handling
    var hasCameraPermission by remember {
        mutableStateOf(
            isTestMode || ContextCompat.checkSelfPermission(
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
        if (!hasCameraPermission && !isTestMode) {
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

    Scaffold(
        modifier = Modifier.clinicalBackground(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (cameraControl != null) {
                                isFlashEnabled = !isFlashEnabled
                                cameraControl?.enableTorch(isFlashEnabled)
                            }
                        }) {
                            Icon(
                                imageVector = if (isFlashEnabled) Icons.Filled.Bolt else Icons.Outlined.Bolt, 
                                contentDescription = "Toggle Flash", 
                                tint = if (isFlashEnabled) Color.Yellow else orangeColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LightGreen,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
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
                        navController.navigate("context") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Context") },
                    label = { Text("CONTEXT", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = unselectedGrey,
                        unselectedTextColor = unselectedGrey,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(orangeColor.copy(alpha = 0.05f), Color.Transparent),
                            radius = 1800f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "AI Food Scanner",
                    color = whiteColor,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Align your food within the inner square frame to start the analysis.",
                    textAlign = TextAlign.Center,
                    color = whiteColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                ) {
                    if (hasCameraPermission) {
                        if (!isTestMode && imageCapture != null) {
                            CameraPreview(
                                lifecycleOwner = lifecycleOwner,
                                imageCapture = imageCapture,
                                modifier = Modifier.fillMaxSize(),
                                onCameraReady = { camera ->
                                    cameraControl = camera.cameraControl
                                }
                            )
                        } else {
                            // Placeholder for tests
                            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray).testTag("CameraPlaceholder"))
                        }
                        ScannerOverlay(color = orangeColor)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Camera permission required", color = Color.Gray)
                        }
                    }

                    ScannerCorners(modifier = Modifier.fillMaxSize(), color = orangeColor)

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                            .size(72.dp)
                            .border(4.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                            .background(Color.White, CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                enabled = uiState !is ScanUiState.Loading && hasCameraPermission,
                                onClick = {
                                    if (!isTestMode && imageCapture != null) {
                                        takePicture(context, imageCapture, executor) { file ->
                                            viewModel.analyzeFood(file)
                                        }
                                    } else {
                                        onTestCapture()
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(32.dp))
                    }

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
                                Text("Analyzing Food...", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Place food inside the frame.\nKeep hands out of view.",
                    color = whiteColor.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                if (uiState is ScanUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
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
fun ScannerOverlay(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val rectSize = width * 0.6f
        val left = (width - rectSize) / 2
        val top = (height - rectSize) / 2

        drawRect(color = Color.Black.copy(alpha = 0.2f), size = size)

        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(rectSize, rectSize),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(width = 3.dp.toPx())
        )
        
        drawRoundRect(
            color = color.copy(alpha = 0.1f),
            topLeft = Offset(left, top),
            size = Size(rectSize, rectSize),
            cornerRadius = CornerRadius(24f, 24f)
        )
    }
}

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier,
    onCameraReady: (Camera) -> Unit = {}
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
                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
                onCameraReady(camera)
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (File) -> Unit
) {
    val outputDirectory = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    val photoFile = File(outputDirectory, SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date()) + ".jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions, executor,
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
        Box(modifier = Modifier.size(cornerSize).align(Alignment.TopStart)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).background(color))
        }
        Box(modifier = Modifier.size(cornerSize).align(Alignment.TopEnd)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).align(Alignment.TopEnd).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).background(color))
        }
        Box(modifier = Modifier.size(cornerSize).align(Alignment.BottomStart)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).align(Alignment.BottomStart).background(color))
        }
        Box(modifier = Modifier.size(cornerSize).align(Alignment.BottomEnd)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).align(Alignment.TopEnd).background(color))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).background(color))
        }
    }
}
