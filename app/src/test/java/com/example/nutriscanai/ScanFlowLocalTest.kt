package com.example.nutriscanai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.nutriscanai.data.FoodPredictionResponse
import com.example.nutriscanai.data.model.FoodDatabaseItem
import com.example.nutriscanai.data.model.Macronutrients
import com.example.nutriscanai.data.model.Micronutrients
import com.example.nutriscanai.ui.screens.ScanScreen
import com.example.nutriscanai.ui.theme.nutriscanaiTheme
import com.example.nutriscanai.ui.viewmodel.MainViewModel
import com.example.nutriscanai.ui.viewmodel.ScanUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class ScanFlowLocalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var uiStateFlow: MutableStateFlow<ScanUiState>
    private lateinit var mockViewModel: MainViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<MainViewModel>(relaxed = true)
        uiStateFlow = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
        every { mockViewModel.uiState } returns uiStateFlow
    }

    @Test
    fun testEndToEndScanFlowLocal() {
        composeTestRule.setContent {
            nutriscanaiTheme {
                ScanScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel,
                    isTestMode = true,
                    onTestCapture = {
                        uiStateFlow.value = ScanUiState.Loading
                    }
                )
            }
        }

        // --- STEP 1: INITIAL STATE ---
        composeTestRule.onNodeWithText("AI Report Scanner").assertIsDisplayed()
        val captureButton = composeTestRule.onNodeWithContentDescription("Capture", useUnmergedTree = true)
        captureButton.assertIsDisplayed()

        // --- STEP 2: TRIGGER CAPTURE & VERIFY LOADING ---
        captureButton.performClick()
        composeTestRule.onNodeWithText("Analyzing Food...").assertIsDisplayed()

        // --- STEP 3: SIMULATE SUCCESS (RESULT DISPLAY) ---
        val dummyResponse = FoodPredictionResponse(
            foodName = "Apple",
            servingSize = "100g",
            calories = 52,
            protein = "0.3g",
            carbs = "14g",
            fat = "0.2g",
            phLevel = 3.5,
            isAlkaline = false
        )
        
        val dummyDetailedData = FoodDatabaseItem(
            foodName = "Apple",
            servingSize = "1 medium (182g)",
            calories = 95,
            macronutrients = Macronutrients(0.5, 25.0, 0.3, 4.4, 19.0),
            micronutrients = Micronutrients(0.1, 8.4, 1.0, 0.1, 195.0),
            phClassification = "Acidic",
            phReason = "High in malic acid."
        )

        uiStateFlow.value = ScanUiState.Success(dummyResponse, dummyDetailedData)

        // Verify Bottom Sheet elements appear
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        
        // Scroll to and verify integrated data
        composeTestRule.onNodeWithText("Acidic").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Potassium: 195.0mg").performScrollTo().assertIsDisplayed()

        // --- STEP 4: DISMISSAL ---
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        
        uiStateFlow.value = ScanUiState.Idle
        
        // Verify we are back to the main scanner view
        composeTestRule.onNodeWithText("AI Report Scanner").assertIsDisplayed()
    }
}
